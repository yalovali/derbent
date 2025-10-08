package tech.derbent.session.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.interfaces.IProjectListChangeListener;
import tech.derbent.api.utils.Check;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.events.ProjectListChangeEvent;
import tech.derbent.projects.service.IProjectRepository;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.IUserRepository;

/** Simple session service implementation for non-web applications like database reset. This provides basic functionality without Vaadin
 * dependencies. */
@Profile ("reset-db")
@Service
public class CSessionService implements ISessionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSessionService.class);
	private tech.derbent.companies.domain.CCompany activeCompany;
	private CProject activeProject;
	// Simple in-memory storage for reset operations
	private CUser activeUser;
	private final Set<IProjectChangeListener> projectChangeListeners = ConcurrentHashMap.newKeySet();
	private final Set<IProjectListChangeListener> projectListChangeListeners = ConcurrentHashMap.newKeySet();
	private final IProjectRepository projectRepository;
	private final IUserRepository userRepository;

	public CSessionService(final IUserRepository userRepository, final IProjectRepository projectRepository) {
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		LOGGER.info("Using CSessionService (reset-db) for database reset application");
	}

	// No-op implementations for methods that require Vaadin UI
	@Override
	public void addProjectChangeListener(final IProjectChangeListener listener) {
		Check.notNull(listener, "Listener must not be null");
		projectChangeListeners.add(listener);
	}

	@Override
	public void addProjectListChangeListener(final IProjectListChangeListener listener) {
		Check.notNull(listener, "Listener must not be null");
		projectListChangeListeners.add(listener);
	}

	@Override
	public void clearSession() {
		activeUser = null;
		activeProject = null;
	}

	@Override
	public Optional<tech.derbent.companies.domain.CCompany> getActiveCompany() { return Optional.ofNullable(activeCompany); }

	@Override
	public Long getActiveId(final String entityType) {
		// Simple implementation - return null for reset mode
		return null;
	}

	@Override
	public Optional<CProject> getActiveProject() {
		if (activeProject == null) {
			// For reset operations, try to get the first project
			final List<CProject> projects = projectRepository.findAll();
			if (!projects.isEmpty()) {
				activeProject = projects.get(0);
				LOGGER.debug("Auto-selected project for reset: {}", activeProject.getName());
			}
		}
		return Optional.ofNullable(activeProject);
	}

	@Override
	public Optional<CUser> getActiveUser() {
		if (activeUser == null) {
			// For reset operations, try to get any user
			final List<CUser> allUsers = userRepository.findAll();
			activeUser = allUsers.isEmpty() ? null : allUsers.get(0);
			if (activeUser != null) {
				LOGGER.debug("Auto-selected user for reset: {}", activeUser.getLogin());
			}
		}
		return Optional.ofNullable(activeUser);
	}

	@Override
	public List<CProject> getAvailableProjects() {
		// Get current company
		CCompany currentCompany = getCurrentCompany();
		LOGGER.debug("Filtering available projects by company: {}", currentCompany.getName());
		return projectRepository.findAll().stream().filter(p -> p.getCompanyId() != null && p.getCompanyId().equals(currentCompany.getId())).toList();
	}

	@Override
	public CCompany getCurrentCompany() { return getActiveCompany().orElse(null); }

	// @EventListener method placeholder for compatibility
	@Override
	public void handleProjectListChange(final ProjectListChangeEvent event) {
		// No-op in reset mode
	}

	@Override
	public void notifyProjectListChanged() {
		// No-op in reset mode - no UI to notify
	}

	@Override
	public void removeProjectChangeListener(final IProjectChangeListener listener) {
		Check.notNull(listener, "Listener must not be null");
		projectChangeListeners.remove(listener);
	}

	@Override
	public void removeProjectListChangeListener(final IProjectListChangeListener listener) {
		Check.notNull(listener, "Listener must not be null");
		projectListChangeListeners.remove(listener);
	}

	// Company management methods
	@Override
	public void setActiveCompany(final CCompany company) {
		activeCompany = company;
		LOGGER.debug("Active company set to: {}", company != null ? company.getName() : "null");
	}

	@Override
	public void setActiveId(final String entityType, final Long id) {
		// No-op in reset mode
	}

	@Override
	public void setActiveProject(final CProject project) { activeProject = project; }

	@Override
	public void setActiveUser(final CUser user) {
		activeUser = user;
		if (user == null) {
			setActiveCompany(null);
		} else {
			if (user.getCompany() == null) {
				LOGGER.warn("User {} has no company assigned", user.getUsername());
			}
			setActiveCompany(user.getCompany());
		}
	}

	/** Sets both company and user in the session atomically. This ensures company is always set before user and validates that the user is a member
	 * of the company.
	 * @param company the company to set as active
	 * @param user    the user to set as active (must be a member of the company) */
	@Override
	public void setCompanyAndUser(final CCompany company, final CUser user) {
		Check.notNull(company, "Company must not be null");
		Check.notNull(user, "User must not be null");
		// Validate that user is a member of the company
		if (user.getCompany() == null || !user.getCompany().getId().equals(company.getId())) {
			throw new IllegalArgumentException(String.format("User %s is not a member of company %s", user.getUsername(), company.getName()));
		}
		LOGGER.info("Setting company {} and user {} atomically", company.getName(), user.getUsername());
		// Set company first
		setActiveCompany(company);
		// Then set user
		activeUser = user;
	}

	@Override
	public void setLayoutService(final CLayoutService layoutService) {}
}
