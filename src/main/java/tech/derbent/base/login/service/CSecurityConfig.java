package tech.derbent.base.login.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import tech.derbent.base.login.security.CLdapAwareAuthenticationProvider;
import tech.derbent.base.login.view.CCustomLoginView;
import tech.derbent.base.users.service.CUserService;

/**
 * Spring Security configuration for the application.
 * 
 * Configures database-based authentication with LDAP support using CUser entities.
 * 
 * Security Flow:
 * 1. User accesses protected resource
 * 2. If not authenticated, redirected to CLoginView
 * 3. User enters credentials in login form
 * 4. Form posts to /login endpoint (handled by Spring Security)
 * 5. CLdapAwareAuthenticationProvider checks authentication type:
 *    - LDAP users: Authenticate via LDAP bind
 *    - Password users: Authenticate via BCrypt password verification
 * 6. If successful, user is authenticated and granted access
 * 7. User roles determine what resources they can access
 * 
 * @author Derbent Team
 * @since 2024
 */
@EnableWebSecurity
@Configuration
@ConditionalOnWebApplication
class CSecurityConfig extends VaadinWebSecurity {

	private final CAuthenticationEntryPoint authenticationEntryPoint;
	private final CAuthenticationSuccessHandler authenticationSuccessHandler;
	private final CUserService loginUserService;
	private final CLdapAwareAuthenticationProvider authenticationProvider;

	/**
	 * Constructor injection of dependencies.
	 * 
	 * @param loginUserService user details service
	 * @param authenticationSuccessHandler handles post-login redirection
	 * @param authenticationEntryPoint handles authentication entry point
	 * @param authenticationProvider custom provider supporting LDAP and password authentication
	 */
	public CSecurityConfig(
			final CUserService loginUserService,
			final CAuthenticationSuccessHandler authenticationSuccessHandler,
			final CAuthenticationEntryPoint authenticationEntryPoint,
			final CLdapAwareAuthenticationProvider authenticationProvider) {
		
		this.loginUserService = loginUserService;
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.authenticationProvider = authenticationProvider;
	}

	/**
	 * Configures HTTP security settings.
	 * 
	 * Sets up the login view, custom authentication provider, and delegates
	 * other security configuration to Vaadin.
	 * 
	 * @param http HttpSecurity configuration object
	 * @throws Exception if configuration fails
	 */
	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		// Apply Vaadin's default security configuration
		// This handles CSRF protection, session management, and other Vaadin-specific security
		super.configure(http);
		
		// Set our custom login view
		// When users need to authenticate, they'll be redirected to CLoginView
		setLoginView(http, CCustomLoginView.class);
		
		// Configure custom authentication provider (supports both LDAP and password)
		http.authenticationProvider(authenticationProvider);
		
		// Configure the UserDetailsService for authentication
		http.userDetailsService(loginUserService);
		
		// Configure custom authentication success handler for post-login redirection
		http.formLogin(form -> form.successHandler(authenticationSuccessHandler));
		
		// Configure custom authentication entry point to save requested URLs
		http.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint));
	}

	/**
	 * Provides BCrypt password encoder bean.
	 * 
	 * BCrypt is a secure hashing function designed for password storage.
	 * 
	 * Password Encoding Flow:
	 * 1. When creating users, plain passwords are encoded with BCrypt
	 * 2. Encoded passwords are stored in database
	 * 3. During authentication, submitted passwords are compared using BCrypt
	 * 4. BCrypt handles salt generation and verification automatically
	 * 
	 * Note: LDAP users bypass this encoder - their passwords are verified via LDAP bind.
	 * 
	 * @return BCryptPasswordEncoder instance for password hashing
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
