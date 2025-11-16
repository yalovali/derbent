package tech.derbent.base.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.ui.component.CComponentUserProjectSettings;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.roles.domain.CUserCompanyRole;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserService extends CEntityOfCompanyService<CUser> implements UserDetailsService, IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserService.class);
	private final PasswordEncoder passwordEncoder;
	private ISessionService sessionService;

	public CUserService(final IEntityOfCompanyRepository<CUser> repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password
		// hashing
		@SuppressWarnings ("unused")
		final CharSequence newPlainPassword = "test123";
		// final String encodedPassword = passwordEncoder.encode(newPlainPassword);
	}

	@Override
	public String checkDeleteAllowed(final CUser entity) {
		try {
			final String superCheck = super.checkDeleteAllowed(entity);
			if (superCheck != null) {
				return superCheck;
			}
			Check.notNull(entity.getCompany(), "User company cannot be null");
			// Rule 1: Check if this is the last user in the company
			final List<CUser> companyUsers = ((IUserRepository) repository).findByCompanyId(entity.getCompany().getId());
			if (companyUsers.size() == 1) {
				return "Cannot delete the last user in the company. At least one user must remain.";
			}
			// Rule 2: Check if user is trying to delete themselves (if session service is available)
			if (sessionService != null) {
				final Optional<CUser> currentUser = sessionService.getActiveUser();
				if (currentUser.isPresent() && currentUser.get().getId().equals(entity.getId())) {
					return "You cannot delete your own user account while logged in.";
				}
			}
			return null; // User can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for user: {}", entity.getLogin(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@PreAuthorize ("permitAll()")
	public long countUsersByProjectId(final Long projectId) {
		return ((IUserRepository) repository).countByProjectId(projectId);
	}

	@Transactional // Write operation requires writable transaction
	public CUser createLoginUser(final String username, final String plainPassword, final String name, final String email, final CCompany company,
			final CUserCompanyRole role) {
		// Check if username already exists
		if (((IUserRepository) repository).findByUsername(company.getId(), username).isPresent()) {
			LOGGER.warn("Username already exists: {}", username);
			throw new IllegalArgumentException("Username already exists: " + username);
		}
		final String encodedPassword = passwordEncoder.encode(plainPassword);
		final CUser loginUser = new CUser(username, encodedPassword, name, email, company, role);
		final CUser savedUser = repository.saveAndFlush(loginUser);
		return savedUser;
	}

	public Component createUserProjectSettingsComponent() {
		try {
			LOGGER.debug("Creating enhanced user project settings component");
			final CComponentUserProjectSettings component = new CComponentUserProjectSettings(this, sessionService);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create user project settings component.");
			// Fallback to simple div with error message
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading user project settings component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	@Override
	@Transactional (readOnly = true)
	public List<CUser> findAll() {
		final CCompany currentCompany = getCurrentCompany();
		// LOGGER.debug("Finding all users for company: {}", currentCompany.getName());
		return ((IUserRepository) repository).findByCompanyId(currentCompany.getId());
	}

	/** Finds a user by login username.
	 * @param login the login username
	 * @return the CUser if found, null otherwise */
	public CUser findByLogin(final String login, final Long company_id) {
		return ((IUserRepository) repository).findByUsername(company_id, login).orElse(null);
	}

	/** Override to generate unique name based on company-specific user count. Pattern: "User##" where ## is zero-padded number within company.
	 * @return unique user name for the current company */
	@Override
	protected String generateUniqueName() {
		try {
			final CCompany currentCompany = sessionService.getCurrentCompany();
			final List<CUser> existingUsers = ((IUserRepository) repository).findByCompanyId(currentCompany.getId());
			final String prefix = "User";
			return getUniqueNameFromList(prefix, existingUsers);
		} catch (final Exception e) {
			LOGGER.warn("Error generating unique user name, falling back to base class: {}", e.getMessage());
			return super.generateUniqueName();
		}
	}

	/** Converts comma-separated role string to Spring Security authorities. Roles are prefixed with "ROLE_" as per Spring Security convention.
	 * @param rolesString comma-separated roles (e.g., "USER,ADMIN")
	 * @return Collection of GrantedAuthority objects */
	private Collection<GrantedAuthority> getAuthorities(final String rolesString) {
		if (rolesString == null || rolesString.trim().isEmpty()) {
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

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CUser> getAvailableUsersForCompany(final Long company_id) {
		Check.notNull(company_id, "ID must not be null");
		return ((IUserRepository) repository).findNotAssignedToCompany(company_id);
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CUser> getAvailableUsersForProject(final Long company_id, final Long projectId) {
		Check.notNull(projectId, "User ID must not be null");
		Check.notNull(company_id, "Company ID must not be null");
		return ((IUserRepository) repository).findNotAssignedToProject(company_id, projectId);
	}

	/** Gets the current company from session, throwing exception if not available.
	 * @return current company
	 * @throws IllegalStateException if no company context is available */
	private CCompany getCurrentCompany() {
		Check.notNull(sessionService, "Session service must not be null");
		final CCompany currentCompany = sessionService.getCurrentCompany();
		Check.notNull(currentCompany, "No active company in session - company context is required");
		return currentCompany;
	}

	@Override
	public Class<CUser> getEntityClass() { return CUser.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CUserInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceUser.class; }

	public CUser getRandomByCompany(final CCompany company) {
		final List<CUser> users = ((IUserRepository) repository).findByCompanyId(company.getId());
		if (!users.isEmpty()) {
			final int randomIndex = (int) (Math.random() * users.size());
			return users.get(randomIndex);
		} else {
			LOGGER.warn("No users found for company: {}", company.getName());
		}
		return null;
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Initializes a new user entity with default values based on current session and available data. Sets: - Company from current session - User
	 * type (first available) - Company role (first available) - Auto-generated unique name and login - Enabled status - Default password prompt
	 * @param user the newly created user to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CUser user) {
		LOGGER.debug("Initializing new user entity");
		try {
			super.initializeNewEntity(user);
			LOGGER.debug("Initializing new user entity with default values");
			Check.notNull(user, "User cannot be null");
			Check.notNull(sessionService, "Session service is required for user initialization");
			final CCompany currentCompany = sessionService.getCurrentCompany();
			Check.notNull(currentCompany, "No active company in session - company context is required to create users");
			user.setCompany(currentCompany, null);
			user.setLogin(user.getName().toLowerCase());
			user.setLastname("");
			user.setEmail("");
			user.setPhone("1234567");
			user.setAttributeDisplaySectionsAsTabs(true);
			user.setPassword(""); // Empty - user must set password
		} catch (final Exception e) {
			LOGGER.error("Error initializing new user: {}", e.getMessage());
			throw e;
		}
	}

	/** Override the default list method to filter users by active company when available. This allows CUserService to work with dynamic pages without
	 * needing to implement special filtering. If no active company is available, returns all users (preserves existing behavior). */
	@Override
	@Transactional (readOnly = true)
	public Page<CUser> list(final Pageable pageable) {
		final CCompany currentCompany = getCurrentCompany();
		LOGGER.debug("Listing users for company: {}", currentCompany.getName());
		return ((IUserRepository) repository).findByCompanyId(currentCompany.getId(), pageable);
	}

	/** Lists users by project using the CUserProjectSettings relationship. This method allows CUserService to work with dynamic pages that expect
	 * project filtering.
	 * @param project the project to filter users by
	 * @return list of users associated with the project */
	@Transactional (readOnly = true)
	public List<CUser> listByProject(final CProject project) {
		Check.notNull(project, "Project cannot be null");
		try {
			// Find users through the project settings relationship
			return ((IUserRepository) repository).findByProject(project.getId());
		} catch (final Exception e) {
			LOGGER.error("Error listing users by project '{}': {}", project.getName(), e.getMessage());
			throw e;
		}
	}

	/** Lists users by project with pagination. This method allows CUserService to work with dynamic pages that expect project filtering.
	 * @param project  the project to filter users by
	 * @param pageable pagination information
	 * @return page of users associated with the project */
	@Transactional (readOnly = true)
	public Page<CUser> listByProject(final CProject project, final Pageable pageable) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(pageable, "Pageable cannot be null");
		try {
			final List<CUser> allUsers = listByProject(project);
			// Apply pagination
			final int start = (int) Math.min(pageable.getOffset(), allUsers.size());
			final int end = Math.min(start + pageable.getPageSize(), allUsers.size());
			final List<CUser> content = allUsers.subList(start, end);
			return new PageImpl<>(content, pageable, allUsers.size());
		} catch (final Exception e) {
			LOGGER.error("Error listing users by project '{}' with pagination: {}", project.getName(), e.getMessage());
			throw e;
		}
	}

	@Override
	// overloaded for spring security
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		try {
			LOGGER.debug("Attempting to load user by username: {}", username);
			// username syntax is username@company_id
			// split login and company id
			final String[] parts = username.split("@");
			Check.isTrue(parts.length == 2, "Username must be in the format username@company_id");
			final String login = parts[0];
			Long company_id;
			try {
				company_id = Long.parseLong(parts[1]);
			} catch (final NumberFormatException e) {
				LOGGER.warn("Invalid company ID in username: {}", parts[1]);
				throw new UsernameNotFoundException("Invalid company ID in username: " + parts[1]);
			}
			final CUser loginUser = ((IUserRepository) repository).findByUsername(company_id, login).orElseThrow(() -> {
				LOGGER.warn("User not found with username: {}", username);
				return new UsernameNotFoundException("User not found with username: " + username);
			});
			// Step 2: Convert user roles to Spring Security authorities
			// fix this next line!!!!!
			final Collection<GrantedAuthority> authorities = getAuthorities("ADMIN,USER");
			// Step 3: Create and return Spring Security UserDetails
			// return
			// User.builder().username(loginUser.getUsername()).password(loginUser.getPassword()).authorities(authorities).accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!loginUser.isEnabled()).build();
			return User.builder().username(username).password(loginUser.getPassword()).authorities(authorities).accountExpired(false)
					.accountLocked(false).credentialsExpired(false).disabled(!loginUser.getActive()).build();
		} catch (final Exception e) {
			LOGGER.error("Error loading user by username '{}': {}", username, e.getMessage());
			throw e;
		}
	}

	@Override
	public boolean onBeforeSaveEvent(final CUser entity) {
		if (super.onBeforeSaveEvent(entity) == false) {
			return false;
		}
		return true;
	}

	/** Sets the session service. This is called after bean creation to avoid circular dependency.
	 * @param sessionService the session service to set */
	@Override
	public void setSessionService(final ISessionService sessionService) { this.sessionService = sessionService; }

	@Override
	protected void validateEntity(final CUser user) {
		super.validateEntity(user);
		Check.notBlank(user.getLogin(), "User login cannot be null or empty");
		Check.notBlank(user.getName(), "User name cannot be null or empty");
	}
}
