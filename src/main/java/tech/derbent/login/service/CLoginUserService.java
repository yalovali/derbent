package tech.derbent.login.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.login.domain.CLoginUser;

/**
 * Service class for CLoginUser that implements UserDetailsService for Spring Security.
 * This service bridges our custom user entity with Spring Security's authentication system.
 * 
 * Authentication Flow:
 * 1. User submits login form with username/password
 * 2. Spring Security intercepted the login request
 * 3. Spring Security calls loadUserByUsername() method in this service
 * 4. This method queries database to find user by username
 * 5. If user found, creates UserDetails object with user info and authorities
 * 6. Spring Security compares submitted password with stored password
 * 7. If match, user is authenticated and granted access
 * 8. User roles are converted to Spring Security authorities for authorization
 */
@Service
@Transactional(readOnly = true) // Default to read-only transactions for better performance
public class CLoginUserService extends CAbstractService<CLoginUser> implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(CLoginUserService.class);
	
	private final CLoginUserRepository loginUserRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * Constructor injection of dependencies.
	 * 
	 * @param repository the CLoginUserRepository for database operations
	 * @param clock clock instance for timestamp operations
	 */
	public CLoginUserService(final CLoginUserRepository repository, final Clock clock) {
		super(repository, clock);
		this.loginUserRepository = repository;
		this.passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password hashing
		logger.info("CLoginUserService initialized with database authentication");
	}

	/**
	 * Implementation of UserDetailsService.loadUserByUsername().
	 * This is the core method called by Spring Security during authentication.
	 * 
	 * Authentication Flow Step:
	 * 1. Spring Security calls this method with username from login form
	 * 2. Query database to find CLoginUser by username
	 * 3. If not found, throw UsernameNotFoundException
	 * 4. If found, convert CLoginUser to Spring Security UserDetails
	 * 5. Return UserDetails with username, password, and authorities
	 * 6. Spring Security uses returned UserDetails to verify password
	 * 
	 * @param username the username from the login form
	 * @return UserDetails object containing user authentication info
	 * @throws UsernameNotFoundException if user not found in database
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.debug("Attempting to load user by username: {}", username);
		
		// Step 1: Query database for user by username
		CLoginUser loginUser = loginUserRepository.findByUsername(username)
			.orElseThrow(() -> {
				logger.warn("User not found with username: {}", username);
				return new UsernameNotFoundException("User not found with username: " + username);
			});

		logger.debug("User found: {} with roles: {}", username, loginUser.getRoles());

		// Step 2: Convert user roles to Spring Security authorities
		Collection<GrantedAuthority> authorities = getAuthorities(loginUser.getRoles());

		// Step 3: Create and return Spring Security UserDetails
		// The password will be compared by Spring Security using the configured PasswordEncoder
		return User.builder()
			.username(loginUser.getUsername())
			.password(loginUser.getPassword()) // Already encoded password from database
			.authorities(authorities)
			.accountExpired(false)
			.accountLocked(false)
			.credentialsExpired(false)
			.disabled(!loginUser.isEnabled()) // Convert enabled flag to disabled flag
			.build();
	}

	/**
	 * Converts comma-separated role string to Spring Security authorities.
	 * Roles are prefixed with "ROLE_" as per Spring Security convention.
	 * 
	 * @param rolesString comma-separated roles (e.g., "USER,ADMIN")
	 * @return Collection of GrantedAuthority objects
	 */
	private Collection<GrantedAuthority> getAuthorities(String rolesString) {
		if (rolesString == null || rolesString.trim().isEmpty()) {
			logger.warn("User has no roles assigned, defaulting to ROLE_USER");
			return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
		}

		// Split roles by comma and convert to authorities
		Collection<GrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
			.map(String::trim)
			.filter(role -> !role.isEmpty())
			.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Add ROLE_ prefix if not present
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());

		logger.debug("Converted roles '{}' to authorities: {}", rolesString, authorities);
		return authorities;
	}

	/**
	 * Creates a new login user with encoded password.
	 * This method handles password encoding automatically.
	 * 
	 * @param username the username for login
	 * @param plainPassword the plain text password (will be encoded)
	 * @param name the user's first name
	 * @param email the user's email
	 * @param roles comma-separated roles (e.g., "USER,ADMIN")
	 * @return the created and saved CLoginUser
	 */
	@Transactional // Write operation requires writable transaction
	public CLoginUser createLoginUser(String username, String plainPassword, String name, String email, String roles) {
		logger.info("Creating new login user with username: {}", username);
		
		// Check if username already exists
		if (loginUserRepository.findByUsername(username).isPresent()) {
			throw new IllegalArgumentException("Username already exists: " + username);
		}

		// Encode the password
		String encodedPassword = passwordEncoder.encode(plainPassword);
		
		// Create new login user
		CLoginUser loginUser = new CLoginUser(username, encodedPassword, name, email);
		loginUser.setRoles(roles != null ? roles : "USER");
		loginUser.setEnabled(true);

		// Save to database
		CLoginUser savedUser = loginUserRepository.saveAndFlush(loginUser);
		logger.info("Successfully created login user with ID: {} and username: {}", savedUser.getId(), username);
		
		return savedUser;
	}

	/**
	 * Updates user password with proper encoding.
	 * 
	 * @param username the username to update
	 * @param newPlainPassword the new plain text password
	 * @throws UsernameNotFoundException if user not found
	 */
	@Transactional
	public void updatePassword(String username, String newPlainPassword) {
		logger.info("Updating password for user: {}", username);
		
		CLoginUser loginUser = loginUserRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		String encodedPassword = passwordEncoder.encode(newPlainPassword);
		loginUser.setPassword(encodedPassword);
		
		loginUserRepository.saveAndFlush(loginUser);
		logger.info("Password updated successfully for user: {}", username);
	}

	/**
	 * Gets the password encoder used by this service.
	 * Useful for external password operations.
	 * 
	 * @return the PasswordEncoder instance
	 */
	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}
}