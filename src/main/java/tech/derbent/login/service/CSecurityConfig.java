package tech.derbent.login.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import tech.derbent.login.view.CCustomLoginView;
import tech.derbent.session.service.CWebSessionService;
import tech.derbent.users.service.CUserService;

/** Spring Security configuration for the application. Configures database-based authentication using CUser entities with company-aware authentication
 * support. Security Flow: 1. User accesses protected resource 2. If not authenticated, redirected to CCustomLoginView 3. User enters credentials
 * (username, password) and selects company in login form 4. Form posts to /login endpoint with company ID 5. CCompanyAwareAuthenticationFilter
 * intercepts and creates company-aware authentication token 6. CCompanyAwareAuthenticationProvider validates credentials 7. Spring Security calls
 * CUserService.loadUserByUsernameAndCompany() with company context 8. Password is verified using BCryptPasswordEncoder 9. If successful, user is
 * authenticated and granted access 10. User roles determine what resources they can access Multi-Tenant Support: Users can have the same username
 * across different companies (unique constraint: login + company_id). The company context is passed through the authentication chain using
 * CCompanyAwareAuthenticationToken to ensure the correct user is authenticated. */
@EnableWebSecurity
@Configuration
@ConditionalOnWebApplication
class CSecurityConfig extends VaadinWebSecurity {

	private final AuthenticationConfiguration authenticationConfiguration;
	private final CAuthenticationSuccessHandler authenticationSuccessHandler;
	private final CCompanyAwareAuthenticationProvider companyAwareAuthenticationProvider;
	private final CUserService userService;
	private CWebSessionService webSessionService;

	/** Constructor injection of dependencies */
	public CSecurityConfig(final CUserService loginUserService, final CAuthenticationSuccessHandler authenticationSuccessHandler,
			final CCompanyAwareAuthenticationProvider companyAwareAuthenticationProvider,
			final AuthenticationConfiguration authenticationConfiguration) {
		userService = loginUserService;
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.companyAwareAuthenticationProvider = companyAwareAuthenticationProvider;
		this.authenticationConfiguration = authenticationConfiguration;
	}

	/** Configures HTTP security settings. Sets up the login view, custom authentication provider, and delegates other security configuration to
	 * Vaadin.
	 * @param http HttpSecurity configuration object
	 * @throws Exception if configuration fails */
	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		// Apply Vaadin's default security configuration
		super.configure(http);
		// Set our custom login view
		setLoginView(http, CCustomLoginView.class);
		// Configure the UserDetailsService for authentication
		http.userDetailsService(userService);
		// Get the authentication manager properly
		AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
		// Create and configure custom authentication filter
		CCompanyAwareAuthenticationFilter authenticationFilter = new CCompanyAwareAuthenticationFilter();
		authenticationFilter.setAuthenticationManager(authenticationManager);
		authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
		// CRITICAL: Only process POST requests to /login
		authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
		// Replace the default authentication filter with our custom one
		http.addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}

	/** Configures the authentication manager to use our custom authentication provider.
	 * @param auth AuthenticationManagerBuilder
	 * @throws Exception if configuration fails */
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(companyAwareAuthenticationProvider);
	}

	/** Provides BCrypt password encoder bean. BCrypt is a secure hashing function designed for password storage. Password Encoding Flow: 1. When
	 * creating users, plain passwords are encoded with BCrypt 2. Encoded passwords are stored in database 3. During authentication, submitted
	 * passwords are compared using BCrypt 4. BCrypt handles salt generation and verification automatically
	 * @return BCryptPasswordEncoder instance for password hashing */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** Provides UserDetailsService bean for authentication. This exposes the CUserService as the UserDetailsService for Spring Security.
	 * @return CUserService instance configured as UserDetailsService */
	@Bean
	public UserDetailsService userDetailsService() {
		return userService;
	}
}
