package tech.derbent.login.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tech.derbent.users.service.CUserService;

/** Custom authentication provider that handles company-aware authentication. This provider supports both standard UsernamePasswordAuthenticationToken
 * and CCompanyAwareAuthenticationToken. When a company context is provided (via CCompanyAwareAuthenticationToken), it passes the company ID to the
 * user service for tenant-isolated authentication. Authentication Flow: 1. Receives authentication token (with or without company context) 2. Loads
 * user details from CUserService (passing company ID if available) 3. Validates password using BCrypt encoder 4. Returns authenticated token with
 * user authorities */
@Component
public class CCompanyAwareAuthenticationProvider implements AuthenticationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyAwareAuthenticationProvider.class);
	private final PasswordEncoder passwordEncoder;
	private final CUserService userService;

	public CCompanyAwareAuthenticationProvider(CUserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		Long companyId = null;
		// Extract company ID if provided via custom token
		if (authentication instanceof CCompanyAwareAuthenticationToken) {
			companyId = ((CCompanyAwareAuthenticationToken) authentication).getCompanyId();
			LOGGER.debug("Authenticating user '{}' for company ID: {}", username, companyId);
		} else {
			LOGGER.debug("Authenticating user '{}' without company context", username);
		}
		try {
			// Load user details with company context
			UserDetails userDetails = userService.loadUserByUsernameAndCompany(username, companyId);
			// Validate password
			if (!passwordEncoder.matches(password, userDetails.getPassword())) {
				LOGGER.warn("Invalid password for user '{}'", username);
				throw new BadCredentialsException("Invalid username or password");
			}
			LOGGER.info("User '{}' authenticated successfully", username);
			// Return authenticated token with company context if available
			if (companyId != null) {
				return new CCompanyAwareAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(), companyId,
						userDetails.getAuthorities());
			} else {
				return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
			}
		} catch (UsernameNotFoundException e) {
			LOGGER.warn("User '{}' not found", username);
			throw new BadCredentialsException("Invalid username or password");
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		// Support both standard and company-aware authentication tokens
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)
				|| CCompanyAwareAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
