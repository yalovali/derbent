package tech.derbent.session.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.interfaces.IProjectListChangeListener;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.events.ProjectListChangeEvent;
import tech.derbent.projects.service.IProjectRepository;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserCompanySettingsService;
import tech.derbent.users.service.IUserRepository;

/** Service to manage user session state including active user and active project. Uses Vaadin session to store session-specific information. */
@Service ("CSessionService")
@ConditionalOnWebApplication
@Profile ("!reset-db")
public class CWebSessionService implements ISessionService {

	private static final String ACTIVE_ID_KEY = "activeId";
	private static final String ACTIVE_PROJECT_KEY = "activeProject";
	private static final String ACTIVE_USER_KEY = "activeUser";
	private static final String ACTIVE_COMPANY_KEY = "activeCompany";
	private static final Logger LOGGER = LoggerFactory.getLogger(CWebSessionService.class);
	private final AuthenticationContext authenticationContext;
	private final Set<String> idAttributes = ConcurrentHashMap.newKeySet();
	private CLayoutService layoutService;
	// Thread-safe set to store project change listeners
	private final Set<IProjectChangeListener> projectChangeListeners = ConcurrentHashMap.newKeySet();
	// Thread-safe set to store project list change listeners
	private final Set<IProjectListChangeListener> projectListChangeListeners = ConcurrentHashMap.newKeySet();
	private final IProjectRepository projectRepository;
	private final IUserRepository userRepository;
	@Autowired
	private ApplicationContext applicationContext;

	public CWebSessionService(final AuthenticationContext authenticationContext, final IUserRepository userRepository,
			final IProjectRepository projectRepository) {
		this.authenticationContext = authenticationContext;
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
	}

	/** Registers a component to receive notifications when the active project changes. Components should call this method when they are attached to
	 * the UI.
	 * @param listener The component that wants to be notified of project changes */
	@Override
	public void addProjectChangeListener(final IProjectChangeListener listener) {
		if (listener != null) {
			projectChangeListeners.add(listener);
		}
	}

	/** Registers a component to receive notifications when the project list changes. Components should call this method when they are attached to the
	 * UI.
	 * @param listener The component that wants to be notified of project list changes */
	@Override
	public void addProjectListChangeListener(final IProjectListChangeListener listener) {
		if (listener != null) {
			projectListChangeListeners.add(listener);
		}
	}

	/** Clears session data on logout. */
	@Override
	public void clearSession() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, null);
			session.setAttribute(ACTIVE_USER_KEY, null);
			session.setAttribute(ACTIVE_COMPANY_KEY, null);
			session.setAttribute(ACTIVE_ID_KEY, null);
		}
		projectChangeListeners.clear();
		projectListChangeListeners.clear();
		if (layoutService != null) {
			layoutService.clearLayoutChangeListeners();
		}
	}

	@Override
	public void deleteAllActiveIds() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return;
		}
		// iterate over all attributes and remove those that start with ACTIVE_ID_KEY
		for (final String attributeName : idAttributes) {
			if (attributeName.startsWith(ACTIVE_ID_KEY)) {
				session.setAttribute(attributeName, null);
			}
		}
	}

	@Override
	public Long getActiveId(final String entityType) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			return (Long) session.getAttribute(ACTIVE_ID_KEY + "_" + entityType);
		}
		return null;
	}

	/** Gets the currently active project from the session. If no project is set, returns the first available project. */
	@Override
	public Optional<CProject> getActiveProject() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return Optional.empty();
		}
		CProject activeProject = (CProject) session.getAttribute(ACTIVE_PROJECT_KEY);
		if (activeProject == null) {
			// If no active project is set, try to set the first available project
			final List<CProject> availableProjects = getAvailableProjects();
			if (!availableProjects.isEmpty()) {
				activeProject = availableProjects.get(0);
				setActiveProject(activeProject);
			}
		}
		return Optional.ofNullable(activeProject);
	}

	/** Gets the currently active user from the session. If no user is set, attempts to load the user from the authentication context. optinal means
	 * that it may return an empty value if no user is found. */
	@Override
	public Optional<CUser> getActiveUser() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return Optional.empty();
		}
		CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
		if (activeUser == null) {
			// Try to load user from authentication context
			final Optional<User> authenticatedUser =
					authenticationContext.getAuthenticatedUser(org.springframework.security.core.userdetails.User.class);
			if (authenticatedUser.isPresent()) {
				final String username = authenticatedUser.get().getUsername();
				activeUser = userRepository.findByUsername(username).orElse(null); // <-- service yerine repo
				setActiveUser(activeUser);
			}
		}
		return Optional.ofNullable(activeUser);
	}

	/** Gets all available projects for the current user. Filters by company if available. */
	@Override
	public List<CProject> getAvailableProjects() {
		// Get current company from session
		CCompany currentCompany = getCurrentCompany();
		if (currentCompany != null) {
			LOGGER.debug("Filtering available projects by company: {}", currentCompany.getName());
			return projectRepository.findByCompanyId(currentCompany.getId());
		}
		// Fallback to all projects if no company context
		LOGGER.debug("No company context, returning all projects");
		return projectRepository.findAll();
	}

	/** Event listener for project list changes. This method is called when projects are created, updated, or deleted to notify all registered
	 * listeners.
	 * @param event The project list change event */
	@Override
	@EventListener
	public void handleProjectListChange(final ProjectListChangeEvent event) {
		notifyProjectListChanged();
	}

	/** Notifies all registered project change listeners about a project change. This method safely handles UI access for components that may be in
	 * different UIs.
	 * @param newProject The newly selected project */
	private void notifyProjectChangeListeners(final CProject newProject) {
		// Use UI.access to safely notify listeners that may be in different UI contexts
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				projectChangeListeners.forEach(listener -> {
					try {
						listener.onProjectChanged(newProject);
					} catch (final Exception e) {
						LOGGER.error("Error notifying project change listener: {}", listener.getClass().getSimpleName(), e);
					}
				});
			});
		}
	}

	/** Notifies all registered project list change listeners about changes to the project list. This method safely handles UI access for components
	 * that may be in different UIs. */
	@Override
	public void notifyProjectListChanged() {
		// Use UI.access to safely notify listeners that may be in different UI contexts
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				projectListChangeListeners.forEach(listener -> {
					try {
						listener.onProjectListChanged();
						LOGGER.debug("Notified project list listener: {}", listener.getClass().getSimpleName());
					} catch (final Exception e) {
						LOGGER.error("Error notifying project list change listener: {}", listener.getClass().getSimpleName(), e);
					}
				});
			});
		}
	}

	/** Unregisters a component from receiving project change notifications. Components should call this method when they are detached from the UI.
	 * @param listener The component to unregister */
	@Override
	public void removeProjectChangeListener(final IProjectChangeListener listener) {
		if (listener != null) {
			projectChangeListeners.remove(listener);
			// LOGGER.debug("Project change listener unregistered: {}",
			// listener.getClass().getSimpleName());
		}
	}

	/** Unregisters a component from receiving project list change notifications. Components should call this method when they are detached from the
	 * UI.
	 * @param listener The component to unregister */
	@Override
	public void removeProjectListChangeListener(final IProjectListChangeListener listener) {
		if (listener != null) {
			projectListChangeListeners.remove(listener);
			// LOGGER.debug("Project list change listener unregistered: {}",
			// listener.getClass().getSimpleName());
		}
	}

	@Override
	public void setActiveId(final String entityType, final Long id) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return;
		}
		final String key = ACTIVE_ID_KEY + "_" + entityType;
		session.setAttribute(key, id);
		LOGGER.debug("Active ID set to: {}", id);
		idAttributes.add(key);
	}

	/** Sets the active project in the session and triggers UI refresh. */
	@Override
	public void setActiveProject(final CProject project) {
		// reset active entity ID when changing project
		final VaadinSession session = VaadinSession.getCurrent();
		deleteAllActiveIds();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, project);
			LOGGER.info("Active project set to: {}", project != null ? project.getName() : "null");
			// Notify all registered project change listeners
			notifyProjectChangeListeners(project);
		}
	}

	/** Sets the active user in the session. */
	@Override
	public void setActiveUser(final CUser user) {
		clearSession(); // Clear session data before setting new user
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_USER_KEY, user);
			LOGGER.info("Active user set to: {}", user != null ? user.getLogin() : "null");
			// Set active company when user is set
			if (user != null) {
				// Lazy-load userCompanySettingsService to avoid circular dependency
				CUserCompanySettingsService userCompanySettingsService = applicationContext.getBean(CUserCompanySettingsService.class);
				CCompany company = user.getCompanyInstance(userCompanySettingsService);
				if (company != null) {
					session.setAttribute(ACTIVE_COMPANY_KEY, company);
					LOGGER.info("Active company set to: {}", company.getName());
				}
			}
		}
	}

	/** Gets the currently active company from the session. */
	@Override
	public Optional<CCompany> getActiveCompany() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return Optional.empty();
		}
		CCompany activeCompany = (CCompany) session.getAttribute(ACTIVE_COMPANY_KEY);
		return Optional.ofNullable(activeCompany);
	}

	/** Gets the current company (convenience method). */
	@Override
	public CCompany getCurrentCompany() { return getActiveCompany().orElse(null); }

	/** Sets the layout service. This is called after bean creation to avoid circular dependency. */
	@Override
	public void setLayoutService(final CLayoutService layoutService) { this.layoutService = layoutService; }
}
