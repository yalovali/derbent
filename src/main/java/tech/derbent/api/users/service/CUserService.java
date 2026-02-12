package tech.derbent.api.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.ICompanyRepository;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.IProjectRepository;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.domain.CUserProjectRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.setup.service.ISystemSettingsService;
import tech.derbent.api.ui.component.enhanced.CComponentUserProjectSettings;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.domain.CUserProjectSettings;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserService extends CEntityOfCompanyService<CUser> implements UserDetailsService, IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserService.class);

	/** Converts comma-separated role string to Spring Security authorities. Roles are prefixed with "ROLE_" as per Spring Security convention.
	 * @param rolesString comma-separated roles (e.g., "USER,ADMIN")
	 * @return Collection of GrantedAuthority objects */
	private static Collection<GrantedAuthority> getAuthorities(final String rolesString) {
		if (!(rolesString == null || rolesString.trim().isEmpty())) {
			// Split roles by comma and convert to authorities
			return Arrays.stream(rolesString.split(",")).map(String::trim).filter(role -> !role.isEmpty())
					.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Add
					// ROLE_ prefix if not present
					.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		}
		LOGGER.warn("User has no roles assigned, defaulting to ROLE_USER");
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
	}

	private final PasswordEncoder passwordEncoder;
	// private final CLdapAuthenticator ldapAuthenticator;
	private final ISystemSettingsService systemSettingsService;

	public CUserService(final IEntityOfCompanyRepository<CUser> repository, final Clock clock, @Lazy final ISessionService sessionService,
			final ISystemSettingsService systemSettingsService) {
		super(repository, clock, sessionService);
		passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password
		// this.ldapAuthenticator = ldapAuthenticator;
		this.systemSettingsService = systemSettingsService;
	}

	/** Assign user to default project with default role. Strategy: 1. Find first available project in company 2. Get first available project role for
	 * company 3. Create user-project relationship
	 * @param user    the user to assign
	 * @param company the company context */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void assignUserToDefaultProject(final CUser user, final CCompany company) {
		try {
			// Get project service (use generic CProjectService bean)
			final IProjectRepository projectRepository = CSpringContext.getBean(IProjectRepository.class);
			final List<CProject<?>> projects = projectRepository.findByCompanyId(company.getId());
			if (projects.isEmpty()) {
				LOGGER.warn("⚠️ No projects available for company: {} - user '{}' not assigned to any project", company.getName(), user.getLogin());
				return; // Not critical - user can be assigned to project later
			}
			// Get first available project
			final CProject<?> defaultProject = projects.get(0);
			LOGGER.debug("✅ Found default project: {}", defaultProject.getName());
			// Get default project role (first available role for company)
			final CUserProjectRoleService projectRoleService = CSpringContext.getBean(CUserProjectRoleService.class);
			final List<CUserProjectRole> projectRoles = projectRoleService.listByCompany(company);
			if (projectRoles.isEmpty()) {
				LOGGER.warn("⚠️ No project roles available for company: {} - user '{}' assigned to project without role", company.getName(),
						user.getLogin());
				// Continue without role - not critical
			}
			// Create user-project settings
			final CUserProjectSettingsService projectSettingsService = CSpringContext.getBean(CUserProjectSettingsService.class);
			final CUserProjectSettings projectSettings = new CUserProjectSettings(user, defaultProject);
			// Assign role if available
			if (!projectRoles.isEmpty()) {
				final CUserProjectRole defaultProjectRole = projectRoles.get(0);
				projectSettings.setRole(defaultProjectRole);
				LOGGER.debug("✅ Assigning default project role: {}", defaultProjectRole.getName());
			}
			// Save project settings
			projectSettingsService.save(projectSettings);
			LOGGER.info("✅ Assigned user '{}' to project: {} with role: {}", user.getLogin(), defaultProject.getName(),
					projectSettings.getRole() != null ? projectSettings.getRole().getName() : "None");
		} catch (final Exception e) {
			// Log error but don't fail user creation
			LOGGER.error("❌ Failed to assign user '{}' to default project: {}", user.getLogin(), e.getMessage(), e);
			LOGGER.warn("⚠️ User '{}' created successfully but not assigned to any project - can be assigned manually", user.getLogin());
		}
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

	/** Service-level method to copy CUser-specific fields using direct setters/getters. This method implements the service-based copy pattern for
	 * User entities.
	 * @param source  the source user to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final CUser source, final CEntityDB<?> target, final CCloneOptions options) {
		// Call parent to copy entity of company fields
		super.copyEntityFieldsTo(source, target, options);
		// Only copy if target is a User
		if (!(target instanceof final CUser targetUser)) {
			return;
		}
		// Handle unique fields - make them unique to avoid constraint violations
		if (source.getEmail() != null) {
			targetUser.setEmail(source.getEmail().replace("@", "+copy@"));
		}
		if (source.getLogin() != null) {
			targetUser.setLogin(source.getLogin() + "_copy");
		}
		// Copy non-sensitive user fields - direct setter/getter
		targetUser.setLastname(source.getLastname());
		targetUser.setPhone(source.getPhone());
		targetUser.setColor(source.getColor());
		targetUser.setAttributeDisplaySectionsAsTabs(source.getAttributeDisplaySectionsAsTabs());
		// SECURITY: Don't copy password, profile pictures, or roles
		// These must be set explicitly after copying for security reasons
		LOGGER.debug("Successfully copied user '{}' with unique identifiers", source.getName());
	}

	@PreAuthorize ("permitAll()")
	public long countUsersByProjectId(final Long projectId) {
		return ((IUserRepository) repository).countByProjectId(projectId);
	}

	/** Create new LDAP user in database after successful LDAP authentication. This method is called when: 1. User successfully authenticates against
	 * LDAP server 2. User does not exist in local database yet Auto-initialization process: 1. Fetch company by ID 2. Create user with LDAP marker
	 * (isLDAPUser = true) 3. Assign default company role (first available) 4. Assign to first available project ✅ NEW 5. Assign default project role
	 * (first available) ✅ NEW 6. Set user as active 7. Save to database Future enhancements (TODO): - Fetch user attributes from LDAP (email, full
	 * name, phone) - Configure default project in company settings - Map LDAP groups to application roles
	 * @param login     LDAP login username
	 * @param companyId company ID for the user
	 * @return UserDetails for Spring Security authentication
	 * @throws IllegalStateException if user creation fails */
	@Transactional
	@PreAuthorize ("permitAll()")
	public UserDetails createLdapUser(final String login, final Long companyId) {
		LOGGER.info("🆕 Creating new LDAP user - Login: {}, Company ID: {}", login, companyId);
		// Validate inputs
		Check.notBlank(login, "Login cannot be blank");
		Check.notNull(companyId, "Company ID cannot be null");
		// Get company
		final CCompany company = CSpringContext.getBean(ICompanyRepository.class).findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
		LOGGER.debug("✅ Found company: {}", company.getName());
		// Get default company role (first available role for company)
		final CUserCompanyRoleService roleService = CSpringContext.getBean(CUserCompanyRoleService.class);
		final List<CUserCompanyRole> roles = roleService.listByCompany(company);
		if (roles.isEmpty()) {
			LOGGER.error("❌ No company roles available for company: {}", company.getName());
			throw new IllegalStateException("No company roles configured - cannot create user");
		}
		final CUserCompanyRole defaultRole = roles.get(0);
		LOGGER.debug("✅ Assigning default company role: {}", defaultRole.getName());
		// Create new user entity
		final CUser newUser = new CUser(login, company);
		newUser.setLogin(login);
		newUser.setName(login); // Default name = login (TODO: fetch from LDAP)
		newUser.setEmail(login + "@ldap"); // Placeholder email (TODO: fetch from LDAP)
		newUser.setIsLDAPUser(true); // Mark as LDAP user
		newUser.setActive(true);
		newUser.setCompany(company, defaultRole);
		newUser.setPassword(""); // No password for LDAP users
		// Save user to database FIRST (user needs ID for project settings)
		final CUser savedUser = repository.saveAndFlush(newUser);
		LOGGER.info("✅ Created LDAP user successfully - ID: {}, Login: {}, Company: {}", savedUser.getId(), savedUser.getLogin(),
				savedUser.getCompany().getName());
		// ✅ NEW: Assign user to default project
		assignUserToDefaultProject(savedUser, company);
		// TODO: Future enhancements
		// - Fetch user attributes from LDAP (email, full name, phone)
		// - Configure default project in company settings
		// - Map LDAP groups to application roles
		// Convert to UserDetails for Spring Security
		final Collection<GrantedAuthority> authorities = getAuthorities("ADMIN,USER");
		// Return UserDetails with LDAP marker password
		return User.builder().username(login + "@" + companyId).password("{ldap}" + login) // Special marker for LDAP authentication
				.authorities(authorities).accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!savedUser.getActive())
				.build();
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
		return repository.saveAndFlush(loginUser);
	}

	public Component createUserProjectSettingsComponent() {
		try {
			LOGGER.debug("Creating enhanced user project settings component");
			return new CComponentUserProjectSettings(this, sessionService);
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
	protected String generateUniqueName(final String clazzName) {
		try {
			final CCompany currentCompany = sessionService.getCurrentCompany();
			final List<CUser> existingUsers = ((IUserRepository) repository).findByCompanyId(currentCompany.getId());
			final String prefix = "User";
			return getUniqueNameFromList(prefix, existingUsers);
		} catch (final Exception e) {
			LOGGER.warn("Error generating unique user name, falling back to base class: {}", e.getMessage());
			return super.generateUniqueName(clazzName);
		}
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
	public Class<?> getPageServiceClass() { return CPageServiceUser.class; }

	public CUser getRandomByCompany(final CCompany company) {
		final List<CUser> users = ((IUserRepository) repository).findByCompanyId(company.getId());
		if (!users.isEmpty()) {
			final int randomIndex = (int) (Math.random() * users.size());
			return users.get(randomIndex);
		}
		LOGGER.warn("No users found for company: {}", company.getName());
		return null;
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Handle LDAP authentication for LDAP users.
	 * @param loginUser    the user entity from database
	 * @param login        the username (without company ID)
	 * @param fullUsername the full username (with company ID)
	 * @return UserDetails for Spring Security */
	private UserDetails handleLdapAuthentication(final CUser loginUser, final String login, final String fullUsername) {
		LOGGER.info("🔗 Checking LDAP authentication for user: {} (isLDAPUser: {})", login, loginUser.getIsLDAPUser());
		// Get system settings to check if LDAP is enabled
		final CSystemSettings<?> systemSettings = systemSettingsService.getSystemSettings();
		if (systemSettings == null || !Boolean.TRUE.equals(systemSettings.getEnableLdapAuthentication())) {
			LOGGER.warn("❌ LDAP authentication disabled for user '{}' - LDAP not enabled in system settings", login);
			throw new UsernameNotFoundException("LDAP authentication is not enabled");
		}
		LOGGER.info("✅ LDAP authentication enabled for user: {} - will use LDAP bind", login);
		// Log LDAP configuration (without sensitive data)
		LOGGER.debug("🔧 LDAP Config for user '{}' - Server: {}, SearchBase: {}", login, systemSettings.getLdapServerUrl(),
				systemSettings.getLdapSearchBase());
		// LDAP authentication will be performed by Spring Security's password check
		// We need to return UserDetails with a special marker that tells Spring Security
		// to use LDAP authentication instead of password comparison
		// Note: The actual LDAP bind happens in the authentication provider
		// For now, we return the UserDetails and let Spring Security handle password checking
		// The LDAP password check will happen via the CustomAuthenticationProvider
		// Convert user roles to Spring Security authorities
		final Collection<GrantedAuthority> authorities = getAuthorities("ADMIN,USER");
		// Return UserDetails - password will be checked by LDAP bind
		// We use a special marker password "{ldap}" to indicate LDAP authentication
		LOGGER.debug("🎫 Returning UserDetails for LDAP user: {} with marker password '{ldap}{}'", login, login);
		return User.builder().username(fullUsername).password("{ldap}" + login) // Special marker for LDAP authentication
				.authorities(authorities).accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!loginUser.getActive())
				.build();
	}

	/** Handle password-based authentication for non-LDAP users.
	 * @param loginUser    the user entity from database
	 * @param fullUsername the full username (with company ID)
	 * @return UserDetails for Spring Security */
	private UserDetails handlePasswordAuthentication(final CUser loginUser, final String fullUsername) {
		LOGGER.debug("Using password authentication for user: {}", loginUser.getLogin());
		// Security check: password must exist for non-LDAP users
		if (loginUser.getPassword() == null || loginUser.getPassword().isBlank()) {
			LOGGER.error("Password authentication failed for user '{}': password is null or empty", loginUser.getLogin());
			throw new UsernameNotFoundException("User password not configured");
		}
		// Convert user roles to Spring Security authorities
		final Collection<GrantedAuthority> authorities = getAuthorities("ADMIN,USER");
		// Return UserDetails with BCrypt password hash - Spring Security will verify
		return User.builder().username(fullUsername).password(loginUser.getPassword()).authorities(authorities).accountExpired(false)
				.accountLocked(false).credentialsExpired(false).disabled(!loginUser.getActive()).build();
	}

	/** Initializes a new user entity with default values based on current session and available data. Sets: - Company from current session - User
	 * type (first available) - Company role (first available) - Auto-generated unique name and login - Enabled status - Default password prompt
	 * @param user the newly created user to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final Object user) {
		super.initializeNewEntity(user);
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
	public List<CUser> listByProject(final CProject<?> project) {
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
	public Page<CUser> listByProject(final CProject<?> project, final Pageable pageable) {
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
	public UserDetails loadUserByUsername(final String username) {
		try {
			LOGGER.debug("🔐 Authentication attempt for username: {}", username);
			// username syntax is username@company_id
			// split login and company id
			final String[] parts = username.split("@");
			Check.isTrue(parts.length == 2, "Username must be in the format username@company_id");
			final String login = parts[0];
			final Long company_id;
			try {
				company_id = Long.parseLong(parts[1]);
			} catch (final NumberFormatException e) {
				LOGGER.warn("Invalid company ID in username: {}", parts[1]);
				throw new UsernameNotFoundException("Invalid company ID in username: " + parts[1]);
			}
			// Find user in database
			final CUser loginUser = ((IUserRepository) repository).findByUsername(company_id, login).orElseThrow(() -> {
				LOGGER.warn("User not found with username: {}", username);
				return new UsernameNotFoundException("User not found with username: " + username);
			});
			LOGGER.debug("User found: {} (LDAP: {})", loginUser.getLogin(), loginUser.isLDAPUser());
			// Check if LDAP authentication should be used
			if (loginUser.isLDAPUser()) {
				return handleLdapAuthentication(loginUser, login, username);
			}
			return handlePasswordAuthentication(loginUser, username);
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
		// 1. Required Fields
		Check.notBlank(user.getLogin(), ValidationMessages.FIELD_REQUIRED);
		Check.notBlank(user.getName(), ValidationMessages.FIELD_REQUIRED);
		Check.notNull(user.getCompany(), ValidationMessages.COMPANY_REQUIRED);
		// 1.1 Password requirement - LDAP users exempt
		// LDAP users authenticate via LDAP server, password field not required
		// Non-LDAP users must have password (for BCrypt authentication)
		if (user.getIsLDAPUser() == null || !user.getIsLDAPUser()) {
			final boolean condition = (user.getPassword() == null || user.getPassword().isBlank()) && user.getId() == null;
			// Only for new users (no ID yet)
			// Non-LDAP user: password is required
			if (condition) {
				throw new CValidationException("Password is required for non-LDAP users");
			}
		} else {
			// LDAP user: warn if password is set (it will be ignored)
			if (user.getPassword() != null && !user.getPassword().isBlank()) {
				LOGGER.warn("⚠️ User '{}' is marked as LDAP user but has password set - password will be ignored during authentication",
						user.getLogin());
			}
		}
		// 2. Length Checks - USE STATIC HELPER
		validateStringLength(user.getLogin(), "Login", CEntityConstants.MAX_LENGTH_NAME);
		validateStringLength(user.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		validateStringLength(user.getEmail(), "Email", CEntityConstants.MAX_LENGTH_NAME);
		// 3. Unique Checks (Database Mirror)
		// Check Login Unique in Company
		if (user.getCompany() == null) {
			return;
		}
		final Optional<CUser> existingLogin = ((IUserRepository) repository).findByUsername(user.getCompany().getId(), user.getLogin());
		if (existingLogin.isPresent() && !existingLogin.get().getId().equals(user.getId())) {
			throw new CValidationException(ValidationMessages.DUPLICATE_USERNAME);
		}
	}
}
