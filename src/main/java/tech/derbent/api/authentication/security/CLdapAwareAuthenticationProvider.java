package tech.derbent.api.authentication.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tech.derbent.api.authentication.service.CLdapAuthenticator;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.setup.service.ISystemSettingsService;
import tech.derbent.api.users.service.CUserService;

/**
 * CLdapAwareAuthenticationProvider - Custom authentication provider supporting both LDAP and password authentication.
 * 
 * Authentication Flow:
 * 1. User submits credentials
 * 2. loadUserByUsername() retrieves user details
 * 3. Check if user password starts with "{ldap}" marker
 * 4. If LDAP: Authenticate via CLdapAuthenticator
 * 5. If password: Authenticate via BCrypt password encoder
 * 
 * Security:
 * - LDAP authentication only if enabled in system settings
 * - Comprehensive logging for audit trail
 * - Graceful fallback to password authentication
 * - Implements AuthenticationProvider directly (no deprecated methods)
 * 
 * @author Derbent Team
 * @since 2026-02-10
 */
@Component
public class CLdapAwareAuthenticationProvider implements AuthenticationProvider {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CLdapAwareAuthenticationProvider.class);
	
	/** Marker prefix in password field to indicate LDAP authentication. */
	private static final String LDAP_MARKER = "{ldap}";
	
	private final CUserService userService;
	private final PasswordEncoder passwordEncoder;
	private final CLdapAuthenticator ldapAuthenticator;
	private final ISystemSettingsService systemSettingsService;
	
	/**
	 * Constructor with @Lazy injection to break circular dependencies.
	 * 
	 * Circular Dependency Chains (BROKEN by @Lazy):
	 * 
	 * Chain 1: CLdapAwareAuthenticationProvider → CUserService → CSystemSettings_BabService → 
	 *          CSessionService → CSecurityConfig → CLdapAwareAuthenticationProvider
	 *          BROKEN BY: @Lazy on userService
	 * 
	 * Chain 2: CLdapAwareAuthenticationProvider → PasswordEncoder (@Bean in CSecurityConfig) → 
	 *          CSecurityConfig → CLdapAwareAuthenticationProvider
	 *          BROKEN BY: @Lazy on passwordEncoder
	 * 
	 * Chain 3: CLdapAwareAuthenticationProvider → ISystemSettingsService (CSystemSettings_BabService) → 
	 *          CSessionService → CSecurityConfig → CLdapAwareAuthenticationProvider
	 *          BROKEN BY: @Lazy on systemSettingsService
	 * 
	 * @Lazy delays initialization until first use, breaking all three cycles.
	 * 
	 * @param userService lazy-loaded user service (breaks circular dependency chain 1)
	 * @param passwordEncoder lazy-loaded password encoder bean (breaks circular dependency chain 2)
	 * @param ldapAuthenticator LDAP authenticator
	 * @param systemSettingsService lazy-loaded system settings service (breaks circular dependency chain 3)
	 */
	public CLdapAwareAuthenticationProvider(
			@Lazy final CUserService userService,
			@Lazy final PasswordEncoder passwordEncoder,
			final CLdapAuthenticator ldapAuthenticator,
			@Lazy final ISystemSettingsService systemSettingsService) {
		
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.ldapAuthenticator = ldapAuthenticator;
		this.systemSettingsService = systemSettingsService;
	}
	
	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
		final String username = authentication.getName();
		final String password = authentication.getCredentials().toString();
		
		LOGGER.debug("🔐 Authentication attempt for user: {}", username);
		
		// Load user details
		final UserDetails userDetails;
		try {
			userDetails = userService.loadUserByUsername(username);
		} catch (final UsernameNotFoundException e) {
			LOGGER.warn("❌ User not found: {}", username);
			throw new BadCredentialsException("Invalid username or password");
		}
		
		// Check if this is an LDAP user (password starts with {ldap} marker)
		if (userDetails.getPassword().startsWith(LDAP_MARKER)) {
			return authenticateLdap(username, password, userDetails);
		} else {
			return authenticatePassword(username, password, userDetails);
		}
	}
	
	@Override
	public boolean supports(final Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
	
	/**
	 * Authenticate LDAP user via LDAP bind.
	 * 
	 * @param fullUsername username with company ID (login@company_id)
	 * @param password submitted password
	 * @param userDetails user details from database
	 * @return authenticated token
	 */
	private Authentication authenticateLdap(final String fullUsername, final String password, final UserDetails userDetails) {
		// Security check: password must not be null or empty
		if (password == null || password.isBlank()) {
			LOGGER.warn("❌ LDAP authentication failed: password is null or empty");
			throw new BadCredentialsException("Password is required");
		}
		
		// Extract login from marker: "{ldap}login" → "login"
		final String login = userDetails.getPassword().substring(LDAP_MARKER.length());
		
		// Security check: login must not be empty after extraction
		if (login.isBlank()) {
			LOGGER.error("❌ LDAP authentication failed: invalid login format");
			throw new BadCredentialsException("Invalid user configuration");
		}
		
		LOGGER.info("🔐 LDAP authentication for user: {}", login);
		
		// Get system settings
		final CSystemSettings<?> systemSettings = systemSettingsService.getSystemSettings();
		if (systemSettings == null) {
			LOGGER.error("❌ LDAP authentication failed: System settings not found");
			throw new BadCredentialsException("System configuration error");
		}
		
		// Authenticate via LDAP
		final boolean ldapSuccess = ldapAuthenticator.authenticate(login, password, systemSettings);
		
		if (ldapSuccess) {
			LOGGER.info("✅ LDAP authentication SUCCESS for user: {}", login);
			return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
		} else {
			LOGGER.warn("❌ LDAP authentication FAILED for user: {}", login);
			throw new BadCredentialsException("Invalid LDAP credentials");
		}
	}
	
	/**
	 * Authenticate password user via BCrypt comparison.
	 * 
	 * @param username username submitted
	 * @param password password submitted
	 * @param userDetails user details from database
	 * @return authenticated token
	 */
	private Authentication authenticatePassword(final String username, final String password, final UserDetails userDetails) {
		LOGGER.debug("🔐 Password authentication for user: {}", username);
		
		// Verify password using BCrypt encoder
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			LOGGER.warn("❌ Password authentication FAILED for user: {}", username);
			throw new BadCredentialsException("Invalid username or password");
		}
		
		LOGGER.info("✅ Password authentication SUCCESS for user: {}", username);
		return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
	}
}
