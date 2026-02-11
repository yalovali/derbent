package tech.derbent.api.authentication.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tech.derbent.api.authentication.service.CLdapAuthenticator;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.setup.service.ISystemSettingsService;
import tech.derbent.api.users.service.CUserService;

/** CLdapAwareAuthenticationProvider - Custom authentication provider supporting both LDAP and password authentication. Authentication Flow: 1. User
 * submits credentials 2. loadUserByUsername() retrieves user details 3. Check if user password starts with "{ldap}" marker 4. If LDAP: Authenticate
 * via CLdapAuthenticator 5. If password: Authenticate via BCrypt password encoder Security: - LDAP authentication only if enabled in system settings
 * - Comprehensive logging for audit trail - Graceful fallback to password authentication - Implements AuthenticationProvider directly (no deprecated
 * methods)
 * @author Derbent Team
 * @since 2026-02-10 */
@Component
public class CLdapAwareAuthenticationProvider implements AuthenticationProvider {

	/** Marker prefix in password field to indicate LDAP authentication. */
	private static final String LDAP_MARKER = "{ldap}";
	private static final Logger LOGGER = LoggerFactory.getLogger(CLdapAwareAuthenticationProvider.class);
	private final CLdapAuthenticator ldapAuthenticator;
	private final PasswordEncoder passwordEncoder;
	private final ISystemSettingsService systemSettingsService;
	private final CUserService userService;

	/** Constructor with @Lazy injection to break circular dependencies. Circular Dependency Chains (BROKEN by @Lazy): Chain 1:
	 * CLdapAwareAuthenticationProvider → CUserService → CSystemSettings_BabService → CSessionService → CSecurityConfig →
	 * CLdapAwareAuthenticationProvider BROKEN BY: @Lazy on userService Chain 2: CLdapAwareAuthenticationProvider → PasswordEncoder (@Bean in
	 * CSecurityConfig) → CSecurityConfig → CLdapAwareAuthenticationProvider BROKEN BY: @Lazy on passwordEncoder Chain 3:
	 * CLdapAwareAuthenticationProvider → ISystemSettingsService (CSystemSettings_BabService) → CSessionService → CSecurityConfig →
	 * CLdapAwareAuthenticationProvider BROKEN BY: @Lazy on systemSettingsService
	 * @Lazy delays initialization until first use, breaking all three cycles.
	 * @param userService           lazy-loaded user service (breaks circular dependency chain 1)
	 * @param passwordEncoder       lazy-loaded password encoder bean (breaks circular dependency chain 2)
	 * @param ldapAuthenticator     LDAP authenticator
	 * @param systemSettingsService lazy-loaded system settings service (breaks circular dependency chain 3) */
	public CLdapAwareAuthenticationProvider(@Lazy final CUserService userService, @Lazy final PasswordEncoder passwordEncoder,
			final CLdapAuthenticator ldapAuthenticator, @Lazy final ISystemSettingsService systemSettingsService) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.ldapAuthenticator = ldapAuthenticator;
		this.systemSettingsService = systemSettingsService;
	}

	@Override
	public Authentication authenticate(final Authentication authentication) {
		final String username = authentication.getName();
		final String password = authentication.getCredentials().toString();
		LOGGER.info("🔐 Authentication attempt for user: {}", username);
		
		// Extract login and company ID from username (format: login@company_id)
		final String[] parts = username.split("@");
		if (parts.length != 2) {
			LOGGER.warn("❌ Invalid username format: {} (expected login@company_id)", username);
			throw new BadCredentialsException("Invalid username format");
		}
		final String login = parts[0];
		final Long companyId;
		try {
			companyId = Long.parseLong(parts[1]);
		} catch (final NumberFormatException e) {
			LOGGER.warn("❌ Invalid company ID in username: {}", parts[1]);
			throw new BadCredentialsException("Invalid company ID in username");
		}
		
		// Try to load existing user from database
		UserDetails userDetails = null;
		try {
			userDetails = userService.loadUserByUsername(username);
			LOGGER.debug("✅ User '{}' found in database", username);
			
			// PRIORITY 1: Try password authentication first for existing users
			if (!userDetails.getPassword().startsWith(LDAP_MARKER)) {
				LOGGER.debug("🔑 Attempting password authentication for user: {}", username);
				try {
					return authenticatePassword(username, password, userDetails);
				} catch (final BadCredentialsException e) {
					LOGGER.debug("❌ Password authentication failed, trying LDAP fallback");
					// Fall through to LDAP authentication
				}
			}
			
			// If user has LDAP marker OR password authentication failed, try LDAP
			LOGGER.info("🔗 Attempting LDAP authentication for user: {}", username);
			return authenticateLdap(username, password, userDetails);
			
		} catch (final UsernameNotFoundException e) {
			// User not found in database - try LDAP authentication and auto-create user
			LOGGER.info("👤 User '{}' not found in database - attempting LDAP authentication", username);
			return authenticateLdapNewUser(username, login, companyId, password);
		}
	}

	/** Authenticate LDAP user via LDAP bind.
	 * @param fullUsername username with company ID (login@company_id)
	 * @param password     submitted password
	 * @param userDetails  user details from database
	 * @return authenticated token */
	private Authentication authenticateLdap(final String fullUsername, final String password, final UserDetails userDetails) {
		final long startTime = System.currentTimeMillis();
		// Security check: password must not be null or empty
		if (password == null || password.isBlank()) {
			LOGGER.warn("❌ LDAP authentication failed: password is null or empty");
			throw new BadCredentialsException("Password is required");
		}
		// Extract login from marker: "{ldap}login" → "login"
		final String login = userDetails.getPassword().substring(LDAP_MARKER.length());
		// Security check: login must not be empty after extraction
		if (login.isBlank()) {
			LOGGER.error("❌ LDAP authentication failed: invalid login format for user '{}'", fullUsername);
			throw new BadCredentialsException("Invalid user configuration");
		}
		LOGGER.info("🔐 LDAP authentication for user: {} (extracted login: {})", fullUsername, login);
		// Get system settings
		final CSystemSettings<?> systemSettings = systemSettingsService.getSystemSettings();
		if (systemSettings == null) {
			LOGGER.error("❌ LDAP authentication failed: System settings not found");
			throw new BadCredentialsException("System configuration error");
		}
		// Log LDAP configuration status (without sensitive data)
		LOGGER.debug("🔧 LDAP Configuration - Server: {}, SearchBase: {}, UserFilter: {}, Enabled: {}", systemSettings.getLdapServerUrl(),
				systemSettings.getLdapSearchBase(), systemSettings.getLdapUserFilter(), systemSettings.getEnableLdapAuthentication());
		// Authenticate via LDAP
		final boolean ldapSuccess = ldapAuthenticator.authenticate(login, password, systemSettings);
		final long duration = System.currentTimeMillis() - startTime;
		if (ldapSuccess) {
			LOGGER.info("✅ LDAP authentication SUCCESS for user: {} ({}ms)", login, duration);
			return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
		}
		LOGGER.warn("❌ LDAP authentication FAILED for user: {} ({}ms)", login, duration);
		throw new BadCredentialsException("Invalid LDAP credentials");
	}

	/** Authenticate password user via BCrypt comparison.
	 * @param username    username submitted
	 * @param password    password submitted
	 * @param userDetails user details from database
	 * @return authenticated token */
	private Authentication authenticatePassword(final String username, final String password, final UserDetails userDetails) {
		final long startTime = System.currentTimeMillis();
		LOGGER.debug("🔐 Password authentication for user: {}", username);
		// Verify password using BCrypt encoder
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			final long duration = System.currentTimeMillis() - startTime;
			LOGGER.warn("❌ Password authentication FAILED for user: {} ({}ms)", username, duration);
			throw new BadCredentialsException("Invalid username or password");
		}
		final long duration = System.currentTimeMillis() - startTime;
		LOGGER.info("✅ Password authentication SUCCESS for user: {} ({}ms)", username, duration);
		return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
	}

	/**
	 * Authenticate new LDAP user and auto-create in database.
	 * 
	 * Authentication flow for new users:
	 * 1. Validate LDAP is enabled
	 * 2. Authenticate user against LDAP server
	 * 3. If LDAP authentication succeeds, create new user in database
	 * 4. Mark user as LDAP user (isLDAPUser = true)
	 * 5. Return authenticated token
	 * 
	 * @param fullUsername full username with company ID (login@company_id)
	 * @param login        login username without company ID
	 * @param companyId    company ID
	 * @param password     submitted password
	 * @return authenticated token
	 */
	private Authentication authenticateLdapNewUser(final String fullUsername, final String login, 
			final Long companyId, final String password) {
		final long startTime = System.currentTimeMillis();
		
		// Security check: password must not be null or empty
		if (password == null || password.isBlank()) {
			LOGGER.warn("❌ LDAP authentication failed: password is null or empty");
			throw new BadCredentialsException("Password is required");
		}
		
		// Get system settings
		final CSystemSettings<?> systemSettings = systemSettingsService.getSystemSettings();
		if (systemSettings == null) {
			LOGGER.error("❌ LDAP authentication failed: System settings not found");
			throw new BadCredentialsException("System configuration error");
		}
		
		// Check if LDAP is enabled
		if (!Boolean.TRUE.equals(systemSettings.getEnableLdapAuthentication())) {
			LOGGER.warn("❌ LDAP authentication disabled for new user '{}'", login);
			throw new BadCredentialsException("Invalid username or password");
		}
		
		LOGGER.info("🔐 Authenticating new LDAP user '{}' against LDAP server", login);
		
		// Authenticate against LDAP server
		final boolean ldapSuccess = ldapAuthenticator.authenticate(login, password, systemSettings);
		final long duration = System.currentTimeMillis() - startTime;
		
		if (!ldapSuccess) {
			LOGGER.warn("❌ LDAP authentication FAILED for new user '{}' ({}ms)", login, duration);
			throw new BadCredentialsException("Invalid LDAP credentials");
		}
		
		LOGGER.info("✅ LDAP authentication SUCCESS for new user '{}' ({}ms) - creating user in database", login, duration);
		
		// Create new user in database
		try {
			final UserDetails newUserDetails = userService.createLdapUser(login, companyId);
			LOGGER.info("✅ Created new LDAP user '{}' in database", login);
			
			// Return authenticated token
			return new UsernamePasswordAuthenticationToken(newUserDetails, password, newUserDetails.getAuthorities());
			
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to create new LDAP user '{}': {}", login, e.getMessage(), e);
			throw new BadCredentialsException("Failed to create user account: " + e.getMessage());
		}
	}
	
	@Override
	public boolean supports(final Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
