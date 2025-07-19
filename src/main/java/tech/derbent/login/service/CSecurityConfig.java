package tech.derbent.login.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import tech.derbent.login.view.CLoginView;
import tech.derbent.users.service.CUserService;

/**
 * Spring Security configuration for the application. Configures database-based
 * authentication using CUser entities. Security Flow: 1. User accesses
 * protected resource 2. If not authenticated, redirected to CLoginView 3. User
 * enters credentials in login form 4. Form posts to /login endpoint (handled by
 * Spring Security) 5. Spring Security calls
 * CLoginUserService.loadUserByUsername() 6. Password is verified using
 * BCryptPasswordEncoder 7. If successful, user is authenticated and granted
 * access 8. User roles determine what resources they can access
 */
@EnableWebSecurity
@Configuration
class CSecurityConfig extends VaadinWebSecurity {

	private static final Logger logger = LoggerFactory.getLogger(CSecurityConfig.class);
	private final CUserService loginUserService;

	/**
	 * Constructor injection of CLoginUserService. This service provides
	 * UserDetailsService implementation for database authentication.
	 */
	public CSecurityConfig(final CUserService loginUserService) {
		this.loginUserService = loginUserService;
		logger.info("Security configuration initialized with database authentication");
	}

	/**
	 * Configures HTTP security settings. Sets up the login view and delegates other
	 * security configuration to Vaadin.
	 * @param http HttpSecurity configuration object
	 * @throws Exception if configuration fails
	 */
	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		logger.debug("Configuring HTTP security with database authentication");
		// Apply Vaadin's default security configuration This handles CSRF protection,
		// session management, and other Vaadin-specific security
		super.configure(http);
		// Set our custom login view When users need to authenticate, they'll be
		// redirected to CLoginView
		setLoginView(http, CLoginView.class);
		// Configure the UserDetailsService for authentication
		http.userDetailsService(loginUserService);
		logger.info("HTTP security configured successfully");
	}

	/**
	 * Provides BCrypt password encoder bean. BCrypt is a secure hashing function
	 * designed for password storage. Password Encoding Flow: 1. When creating
	 * users, plain passwords are encoded with BCrypt 2. Encoded passwords are
	 * stored in database 3. During authentication, submitted passwords are compared
	 * using BCrypt 4. BCrypt handles salt generation and verification automatically
	 * @return BCryptPasswordEncoder instance for password hashing
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		logger.debug("Creating BCrypt password encoder bean");
		return new BCryptPasswordEncoder();
	}

	/**
	 * Provides UserDetailsService bean for authentication.
	 * This exposes the CUserService as the UserDetailsService for Spring Security.
	 * @return CUserService instance configured as UserDetailsService
	 */
	@Bean
	public UserDetailsService userDetailsService() {
		logger.debug("Creating UserDetailsService bean");
		return loginUserService;
	}
}