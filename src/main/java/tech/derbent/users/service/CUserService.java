package tech.derbent.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
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

import jakarta.persistence.EntityNotFoundException;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true) // Default to read-only transactions for better
									// performance
public class CUserService extends CAbstractNamedEntityService<CUser>
	implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserService.class);

	private final PasswordEncoder passwordEncoder;

	public CUserService(final CUserRepository repository, final Clock clock) {
		super(repository, clock);
		this.passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password
															// hashing
		final CharSequence newPlainPassword = "test123";
		final String encodedPassword = passwordEncoder.encode(newPlainPassword);
		LOGGER.info("CUserService initialized with password encoder: {}",
			encodedPassword);
	}

	/**
	 * Counts the number of users assigned to a specific project.
	 * @param projectId the project ID
	 * @return count of users assigned to the project
	 */
	@PreAuthorize ("permitAll()")
	public long countUsersByProjectId(final Long projectId) {
		return ((CUserRepository) repository).countUsersByProjectId(projectId);
	}

	/**
	 * Creates a new login user with encoded password. This method handles password
	 * encoding automatically.
	 * @param username      the username for login
	 * @param plainPassword the plain text password (will be encoded)
	 * @param name          the user's first name
	 * @param email         the user's email
	 * @param roles         comma-separated roles (e.g., "USER,ADMIN")
	 * @return the created and saved CUser
	 */
	@Transactional // Write operation requires writable transaction
	public CUser createLoginUser(final String username, final String plainPassword,
		final String name, final String email, final String roles) {

		// Check if username already exists
		if (((CUserRepository) repository).findByUsername(username).isPresent()) {
			LOGGER.warn("Username already exists: {}", username);
			throw new IllegalArgumentException("Username already exists: " + username);
		}
		// Encode the password
		final String encodedPassword = passwordEncoder.encode(plainPassword);
		// Create new login user
		final CUser loginUser = new CUser(username, encodedPassword, name, email);
		loginUser.setRoles(roles != null ? roles : "USER");
		loginUser.setEnabled(true);
		// Save to database
		final CUser savedUser = repository.saveAndFlush(loginUser);
		return savedUser;
	}

	/**
	 * Find all enabled users with eager loading for UI components. Optimized for
	 * dropdowns, comboboxes, and selection components.
	 * @return List of enabled users with eager loaded associations
	 */
	public List<CUser> findAllEnabledWithEagerLoading() {
		return ((CUserRepository) repository).findAllEnabledWithEagerLoading();
	}

	/**
	 * Find user by ID with optimized eager loading. Uses repository method with JOIN
	 * FETCH to prevent N+1 queries.
	 * @param id the user ID
	 * @return the user with eagerly loaded associations, or null if not found
	 */
	public CUser findById(final Long id) {

		if (id == null) {
			return null;
		}
		return ((CUserRepository) repository).findByIdWithEagerLoading(id).orElse(null);
	}

	/**
	 * Finds a user by login username.
	 * @param login the login username
	 * @return the CUser if found, null otherwise
	 */
	public CUser findByLogin(final String login) {
		return ((CUserRepository) repository).findByUsername(login).orElse(null);
	}

	/**
	 * Converts comma-separated role string to Spring Security authorities. Roles are
	 * prefixed with "ROLE_" as per Spring Security convention.
	 * @param rolesString comma-separated roles (e.g., "USER,ADMIN")
	 * @return Collection of GrantedAuthority objects
	 */
	private Collection<GrantedAuthority> getAuthorities(final String rolesString) {

		if ((rolesString == null) || rolesString.trim().isEmpty()) {
			LOGGER.warn("User has no roles assigned, defaulting to ROLE_USER");
			return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
		}
		// Split roles by comma and convert to authorities
		final Collection<GrantedAuthority> authorities =
			Arrays.stream(rolesString.split(",")).map(String::trim)
				.filter(role -> !role.isEmpty())
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Add
				// ROLE_ prefix if not present
				.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		LOGGER.debug("Converted roles '{}' to authorities: {}", rolesString, authorities);
		return authorities;
	}

	/**
	 * Overrides the base getById method to ensure lazy field initialization.
	 * @param id the user ID
	 * @return optional CUser with lazy fields initialized
	 */
	@Override
	@Transactional (readOnly = true)
	public Optional<CUser> getById(final Long id) {
		final Optional<CUser> user = repository.findById(id);
		user.ifPresent(this::initializeLazyFields);
		return user;
	}

	@Override
	protected Class<CUser> getEntityClass() { return CUser.class; }

	/**
	 * Gets the password encoder used by this service. Useful for external password
	 * operations.
	 * @return the PasswordEncoder instance
	 */
	public PasswordEncoder getPasswordEncoder() { return passwordEncoder; }

	/**
	 * Gets a user with lazy-loaded project settings initialized.
	 * @param id the user ID
	 * @return the user with project settings loaded
	 * @throws EntityNotFoundException if user not found
	 */
	@Transactional (readOnly = true)
	public CUser getUserWithProjects(final Long id) {
		LOGGER.debug("Getting user with projects for ID: {}", id);
		// Get user and initialize all lazy fields including project settings
		final CUser user = repository.findById(id).orElseThrow(
			() -> new EntityNotFoundException("User not found with ID: " + id));
		
		// Initialize lazy fields to prevent LazyInitializationException
		initializeLazyFields(user);
		
		return user;
	}

	/**
	 * Initializes lazy fields for a user entity to prevent LazyInitializationException.
	 * Specifically initializes user type, company, and project settings.
	 * @param user the user entity to initialize
	 */
	@Override
	public void initializeLazyFields(final CUser user) {

		if (user == null) {
			return;
		}

		try {
			super.initializeLazyFields(user);
			initializeLazyRelationship(user.getUserType());
			initializeLazyRelationship(user.getCompany());
			initializeLazyRelationship(user.getProjectSettings());
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy fields for user with ID: {}",
				CSpringAuxillaries.safeGetId(user), e);
		}
	}

	/**
	 * Implementation of UserDetailsService.loadUserByUsername(). This is the core method
	 * called by Spring Security during authentication.
	 * @param username the username from the login form
	 * @return UserDetails object containing user authentication info
	 * @throws UsernameNotFoundException if user not found in database
	 */
	@Override
	public UserDetails loadUserByUsername(final String username)
		throws UsernameNotFoundException {
		LOGGER.debug("Attempting to load user by username: {}", username);
		// Step 1: Query database for user by username
		final CUser loginUser =
			((CUserRepository) repository).findByUsername(username).orElseThrow(() -> {
				LOGGER.warn("User not found with username: {}", username);
				return new UsernameNotFoundException(
					"User not found with username: " + username);
			});
		// Step 2: Convert user roles to Spring Security authorities
		final Collection<GrantedAuthority> authorities =
			getAuthorities(loginUser.getRoles());
		// Step 3: Create and return Spring Security UserDetails
		return User.builder().username(loginUser.getUsername())
			.password(loginUser.getPassword()) // Already encoded password from database
			.authorities(authorities).accountExpired(false).accountLocked(false)
			.credentialsExpired(false).disabled(!loginUser.isEnabled()) // Convert enabled
			// flag to disabled flag
			.build();
	}

	/**
	 * Removes a project setting for a user.
	 * @param userId    the user ID
	 * @param projectId the project ID
	 */
	@Transactional
	public void removeUserProjectSetting(final Long userId, final Long projectId) {
		final CUser user = getUserWithProjects(userId);

		if (user.getProjectSettings() != null) {
			user.getProjectSettings()
				.removeIf(setting -> setting.getProject().getId().equals(projectId));
			repository.saveAndFlush(user);
		}
	}

	/**
	 * Adds or updates a project setting for a user.
	 * @param userProjectSetting the project setting to save
	 * @return the saved project setting
	 */
	@Transactional
	public CUserProjectSettings
		saveUserProjectSetting(final CUserProjectSettings userProjectSetting) {
		LOGGER.info("Saving user project setting for user ID: {} and project ID: {}",
			userProjectSetting.getUser().getId(),
			userProjectSetting.getProject().getId());
		// Ensure the user exists and reload with project settings
		final CUser user = getUserWithProjects(userProjectSetting.getUser().getId());

		// Initialize project settings list if null
		if (user.getProjectSettings() == null) {
			user.setProjectSettings(new java.util.ArrayList<>());
		}
		// Check if this setting already exists (update case)
		boolean updated = false;

		for (final CUserProjectSettings existing : user.getProjectSettings()) {

			if (existing.getProject().getId()
				.equals(userProjectSetting.getProject().getId())) {
				existing.setRole(userProjectSetting.getRole());
				existing.setPermission(userProjectSetting.getPermission());
				updated = true;
				break;
			}
		}

		if (!updated) {
			userProjectSetting.setUser(user);
			user.getProjectSettings().add(userProjectSetting);
		}
		repository.saveAndFlush(user);
		// Return the saved project setting
		return userProjectSetting;
	}







	/**
	 * Updates user password with proper encoding.
	 * @param username         the username to update
	 * @param newPlainPassword the new plain text password
	 * @throws UsernameNotFoundException if user not found
	 */
	@Transactional
	public void updatePassword(final String username, final String newPlainPassword) {
		final CUser loginUser =
			((CUserRepository) repository).findByUsername(username).orElseThrow(
				() -> new UsernameNotFoundException("User not found: " + username));
		final String encodedPassword = passwordEncoder.encode(newPlainPassword);
		loginUser.setPassword(encodedPassword);
		repository.saveAndFlush(loginUser);
	}

	@Override
	protected void validateEntity(final CUser user) {
		super.validateEntity(user);

		// Additional validation for user entities
		if ((user.getLogin() == null) || user.getLogin().trim().isEmpty()) {
			throw new IllegalArgumentException("User login cannot be null or empty");
		}

		if ((user.getName() == null) || user.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("User name cannot be null or empty");
		}
	}
}