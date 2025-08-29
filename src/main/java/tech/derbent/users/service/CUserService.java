package tech.derbent.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
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
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserService extends CAbstractNamedEntityService<CUser> implements UserDetailsService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserService.class);
	private final PasswordEncoder passwordEncoder;

	public CUserService(final CUserRepository repository, final Clock clock) {
		super(repository, clock);
		this.passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password
															// hashing
		@SuppressWarnings ("unused")
		final CharSequence newPlainPassword = "test123";
		// final String encodedPassword = passwordEncoder.encode(newPlainPassword);
	}

	/** Counts the number of users assigned to a specific project.
	 * @param projectId the project ID
	 * @return count of users assigned to the project */
	@PreAuthorize ("permitAll()")
	public long countUsersByProjectId(final Long projectId) {
		return ((CUserRepository) repository).countByProjectId(projectId);
	}

	@Transactional // Write operation requires writable transaction
	public CUser createLoginUser(final String username, final String plainPassword, final String name, final String email, final String roles) {
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

	/** Finds a user by login username.
	 * @param login the login username
	 * @return the CUser if found, null otherwise */
	public CUser findByLogin(final String login) {
		return ((CUserRepository) repository).findByUsername(login).orElse(null);
	}

	/** Converts comma-separated role string to Spring Security authorities. Roles are prefixed with "ROLE_" as per Spring Security convention.
	 * @param rolesString comma-separated roles (e.g., "USER,ADMIN")
	 * @return Collection of GrantedAuthority objects */
	private Collection<GrantedAuthority> getAuthorities(final String rolesString) {
		if ((rolesString == null) || rolesString.trim().isEmpty()) {
			LOGGER.warn("User has no roles assigned, defaulting to ROLE_USER");
			return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
		}
		// Split roles by comma and convert to authorities
		final Collection<GrantedAuthority> authorities = Arrays.stream(rolesString.split(",")).map(String::trim).filter(role -> !role.isEmpty())
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Add
				// ROLE_ prefix if not present
				.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		LOGGER.debug("Converted roles '{}' to authorities: {}", rolesString, authorities);
		return authorities;
	}

	@Override
	protected Class<CUser> getEntityClass() { return CUser.class; }

	/** Gets the password encoder used by this service. Useful for external password operations.
	 * @return the PasswordEncoder instance */
	public PasswordEncoder getPasswordEncoder() { return passwordEncoder; }

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		LOGGER.debug("Attempting to load user by username: {}", username);
		// Step 1: Query database for user by username
		final CUser loginUser = ((CUserRepository) repository).findByUsername(username).orElseThrow(() -> {
			LOGGER.warn("User not found with username: {}", username);
			return new UsernameNotFoundException("User not found with username: " + username);
		});
		// Step 2: Convert user roles to Spring Security authorities
		final Collection<GrantedAuthority> authorities = getAuthorities(loginUser.getRoles());
		// Step 3: Create and return Spring Security UserDetails
		return User.builder().username(loginUser.getUsername()).password(loginUser.getPassword()) // Already encoded password from database
				.authorities(authorities).accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!loginUser.isEnabled()) // Convert
																																				// enabled
				// flag to disabled flag
				.build();
	}

	@Override
	public boolean onBeforeSaveEvent(final CUser entity) {
		if (super.onBeforeSaveEvent(entity) == false) {
			return false;
		}
		return true;
	}

	@Transactional
	public void updatePassword(final String username, final String newPlainPassword) {
		final CUser loginUser = ((CUserRepository) repository).findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		final String encodedPassword = passwordEncoder.encode(newPlainPassword);
		loginUser.setPassword(encodedPassword);
		repository.saveAndFlush(loginUser);
	}

	@Override
	protected void validateEntity(final CUser user) {
		super.validateEntity(user);
		Check.notBlank(user.getLogin(), "User login cannot be null or empty");
		Check.notBlank(user.getName(), "User name cannot be null or empty");
	}
}
