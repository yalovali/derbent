package tech.derbent.session.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;

import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * Service to manage user session state including active user and active
 * project. Uses Vaadin session to store session-specific information.
 */
@Service
public class SessionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);
	private static final String ACTIVE_PROJECT_KEY = "activeProject";
	private static final String ACTIVE_USER_KEY = "activeUser";
	// Thread-safe set to store project change listeners
	private final Set<CProjectChangeListener> projectChangeListeners = ConcurrentHashMap.newKeySet();
	private final AuthenticationContext authenticationContext;
	private final CUserService userService;
	private final CProjectService projectService;

	public SessionService(final AuthenticationContext authenticationContext, final CUserService userService, final CProjectService projectService) {
		this.authenticationContext = authenticationContext;
		this.userService = userService;
		this.projectService = projectService;
	}

	/**
	 * Registers a component to receive notifications when the active project
	 * changes. Components should call this method when they are attached to the UI.
	 * @param listener The component that wants to be notified of project changes
	 */
	public void addProjectChangeListener(final CProjectChangeListener listener) {
		if (listener != null) {
			projectChangeListeners.add(listener);
			LOGGER.debug("Project change listener registered: {}", listener.getClass().getSimpleName());
		}
	}

	/**
	 * Clears session data on logout.
	 */
	public void clearSession() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, null);
			session.setAttribute(ACTIVE_USER_KEY, null);
			LOGGER.info("Session data cleared");
		}
		// Clear all project change listeners when session is cleared
		projectChangeListeners.clear();
		LOGGER.debug("Project change listeners cleared");
	}

	/**
	 * Gets the currently active project from the session. If no project is set,
	 * returns the first available project.
	 */
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

	/**
	 * Gets the currently active user from the session. If no user is set, attempts
	 * to load the user from the authentication context. optinal means that it may
	 * return an empty value if no user is found.
	 */
	public Optional<CUser> getActiveUser() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return Optional.empty();
		}
		CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
		if (activeUser == null) {
			// Try to load user from authentication context
			final Optional<org.springframework.security.core.userdetails.User> authenticatedUser = authenticationContext.getAuthenticatedUser(org.springframework.security.core.userdetails.User.class);
			if (authenticatedUser.isPresent()) {
				final String username = authenticatedUser.get().getUsername();
				activeUser = userService.findByLogin(username);
				if (activeUser != null) {
					setActiveUser(activeUser);
				}
			}
		}
		return Optional.ofNullable(activeUser);
	}

	/**
	 * Gets all available projects for the current user. For now, returns all
	 * projects. Can be enhanced to filter by user permissions.
	 */
	public List<CProject> getAvailableProjects() { return projectService.findAll(); }

	/**
	 * Notifies all registered project change listeners about a project change. This
	 * method safely handles UI access for components that may be in different UIs.
	 * @param newProject The newly selected project
	 */
	private void notifyProjectChangeListeners(final CProject newProject) {
		LOGGER.debug("Notifying {} project change listeners of project change", projectChangeListeners.size());
		// Use UI.access to safely notify listeners that may be in different UI contexts
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				projectChangeListeners.forEach(listener -> {
					try {
						listener.onProjectChanged(newProject);
						LOGGER.debug("Notified listener: {}", listener.getClass().getSimpleName());
					} catch (final Exception e) {
						LOGGER.error("Error notifying project change listener: {}", listener.getClass().getSimpleName(), e);
					}
				});
			});
		}
	}

	/**
	 * Unregisters a component from receiving project change notifications.
	 * Components should call this method when they are detached from the UI.
	 * @param listener The component to unregister
	 */
	public void removeProjectChangeListener(final CProjectChangeListener listener) {
		if (listener != null) {
			projectChangeListeners.remove(listener);
			LOGGER.debug("Project change listener unregistered: {}", listener.getClass().getSimpleName());
		}
	}

	/**
	 * Sets the active project in the session and triggers UI refresh.
	 */
	public void setActiveProject(final CProject project) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, project);
			LOGGER.info("Active project set to: {}", project != null ? project.getName() : "null");
			// Notify all registered project change listeners
			notifyProjectChangeListeners(project);
		}
	}

	/**
	 * Sets the active user in the session.
	 */
	public void setActiveUser(final CUser user) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_USER_KEY, user);
			LOGGER.info("Active user set to: {}", user != null ? user.getLogin() : "null");
		}
	}
}