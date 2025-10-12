package tech.derbent.users.service;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentUserProjectSettings;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CUserService extends CAbstractNamedEntityService<CUser> implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CUserService.class);
	@Autowired
	private ApplicationContext applicationContext;
	private final PasswordEncoder passwordEncoder;
	private ISessionService sessionService;

	public CUserService(final IUserRepository repository, final Clock clock) {
		super(repository, clock);
		passwordEncoder = new BCryptPasswordEncoder(); // BCrypt for secure password
		// hashing
		@SuppressWarnings ("unused")
		final CharSequence newPlainPassword = "test123";
		// final String encodedPassword = passwordEncoder.encode(newPlainPassword);
	}

	@PreAuthorize ("permitAll()")
	public long countUsersByProjectId(final Long projectId) {
		return ((IUserRepository) repository).countByProjectId(projectId);
	}

	@Transactional // Write operation requires writable transaction
	public CUser createLoginUser(final String username, final String plainPassword, final String name, final String email, CCompany company,
			CUserCompanyRole role) {
		// Check if username already exists
		if (((IUserRepository) repository).findByUsername(company.getId(), username).isPresent()) {
			LOGGER.warn("Username already exists: {}", username);
			throw new IllegalArgumentException("Username already exists: " + username);
		}
		// Encode the password
		final String encodedPassword = passwordEncoder.encode(plainPassword);
		// Create new login user
		final CUser loginUser = new CUser(username, encodedPassword, name, email, company, role);
		loginUser.setEnabled(true);
		// Save to database
		final CUser savedUser = repository.saveAndFlush(loginUser);
		return savedUser;
	}

	public Component createUserProjectSettingsComponent() {
		LOGGER.debug("Creating enhanced user project settings component");
		try {
			CComponentUserProjectSettings component = new CComponentUserProjectSettings(this, sessionService, applicationContext);
			return component;
		} catch (Exception e) {
			LOGGER.error("Failed to create user project settings component: {}", e.getMessage(), e);
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
		// Enforce company requirement
		CCompany currentCompany = getCurrentCompany();
		LOGGER.debug("Finding all users for company: {}", currentCompany.getName());
		return ((IUserRepository) repository).findByCompanyId(currentCompany.getId());
	}

	/** Finds a user by login username.
	 * @param login the login username
	 * @return the CUser if found, null otherwise */
	public CUser findByLogin(final String login, final Long companyId) {
		return ((IUserRepository) repository).findByUsername(companyId, login).orElse(null);
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

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CUser> getAvailableUsersForCompany(final Long companyId) {
		Check.notNull(companyId, "ID must not be null");
		return ((IUserRepository) repository).findNotAssignedToCompany(companyId);
	}

	@Transactional (readOnly = true)
	@PreAuthorize ("permitAll()")
	public List<CUser> getAvailableUsersForProject(final Long companyId, final Long projectId) {
		Check.notNull(projectId, "User ID must not be null");
		Check.notNull(companyId, "Company ID must not be null");
		return ((IUserRepository) repository).findNotAssignedToProject(companyId, projectId);
	}

	/** Gets the current company from session, throwing exception if not available.
	 * @return current company
	 * @throws IllegalStateException if no company context is available */
	private CCompany getCurrentCompany() {
		Check.notNull(sessionService, "Session service must not be null");
		CCompany currentCompany = sessionService.getCurrentCompany();
		Check.notNull(currentCompany, "No active company in session - company context is required");
		return currentCompany;
	}

	@Override
	protected Class<CUser> getEntityClass() { return CUser.class; }

	public CUser getRandomByCompany(CCompany company) {
		List<CUser> users = ((IUserRepository) repository).findByCompanyId(company.getId());
		if (!users.isEmpty()) {
			int randomIndex = (int) (Math.random() * users.size());
			return users.get(randomIndex);
		} else {
			LOGGER.warn("No users found for company: {}", company.getName());
		}
		return null;
	}

	/** Override the default list method to filter users by active company when available. This allows CUserService to work with dynamic pages without
	 * needing to implement special filtering. If no active company is available, returns all users (preserves existing behavior). */
	@Override
	@Transactional (readOnly = true)
	public Page<CUser> list(final Pageable pageable) {
		CCompany currentCompany = getCurrentCompany();
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
			LOGGER.error("Error listing users by project '{}': {}", project.getName(), e.getMessage(), e);
			throw new RuntimeException("Failed to list users by project", e);
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
			List<CUser> allUsers = listByProject(project);
			// Apply pagination
			int start = (int) Math.min(pageable.getOffset(), allUsers.size());
			int end = Math.min(start + pageable.getPageSize(), allUsers.size());
			List<CUser> content = allUsers.subList(start, end);
			return new PageImpl<>(content, pageable, allUsers.size());
		} catch (final Exception e) {
			LOGGER.error("Error listing users by project '{}' with pagination: {}", project.getName(), e.getMessage(), e);
			throw new RuntimeException("Failed to list users by project with pagination", e);
		}
	}

	@Override
	// overloaded for spring security
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		LOGGER.debug("Attempting to load user by username: {}", username);
		// username syntax is username@company_id
		// split login and company id
		String[] parts = username.split("@");
		Check.isTrue(parts.length == 2, "Username must be in the format username@company_id");
		String login = parts[0];
		Long companyId;
		try {
			companyId = Long.parseLong(parts[1]);
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid company ID in username: {}", parts[1]);
			throw new UsernameNotFoundException("Invalid company ID in username: " + parts[1]);
		}
		final CUser loginUser = ((IUserRepository) repository).findByUsername(companyId, login).orElseThrow(() -> {
			LOGGER.warn("User not found with username: {}", username);
			return new UsernameNotFoundException("User not found with username: " + username);
		});
		// Step 2: Convert user roles to Spring Security authorities
		// fix this next line!!!!!
		final Collection<GrantedAuthority> authorities = getAuthorities("ADMIN,USER");
		// Step 3: Create and return Spring Security UserDetails
		// return
		// User.builder().username(loginUser.getUsername()).password(loginUser.getPassword()).authorities(authorities).accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!loginUser.isEnabled()).build();
		return User.builder().username(username).password(loginUser.getPassword()).authorities(authorities).accountExpired(false).accountLocked(false)
				.credentialsExpired(false).disabled(!loginUser.isEnabled()).build();
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
