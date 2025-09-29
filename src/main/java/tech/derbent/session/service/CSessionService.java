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
	public void deleteAllActiveIds() {
		// No-op in reset mode
	}

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
	public List<CProject> getAvailableProjects() { return projectRepository.findAll(); }

	public tech.derbent.companies.domain.CCompany getCurrentCompany() { return getActiveCompany().orElse(null); }

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
	public void setActiveCompany(final tech.derbent.companies.domain.CCompany company) {
		activeCompany = company;
		LOGGER.debug("Active company set to: {}", company != null ? company.getName() : "null");
	}

	@Override
	public void setActiveId(final String entityType, final Long id) {
		// No-op in reset mode
	}

	@Override
	public void setActiveProject(final CProject project) {
		activeProject = project;
		LOGGER.debug("Active project set to: {}", project != null ? project.getName() : "null");
	}

	@Override
	public void setActiveUser(final CUser user) {
		activeUser = user;
		LOGGER.debug("Active user set to: {}", user != null ? user.getLogin() : "null");
	}

	@Override
	public void setLayoutService(final CLayoutService layoutService) {}
}
