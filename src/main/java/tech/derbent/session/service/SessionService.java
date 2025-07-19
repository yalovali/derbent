package tech.derbent.session.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;

import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * Service to manage user session state including active user and active project.
 * Uses Vaadin session to store session-specific information.
 */
@Service
public class SessionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);
	private static final String ACTIVE_PROJECT_KEY = "activeProject";
	private static final String ACTIVE_USER_KEY = "activeUser";

	private final AuthenticationContext authenticationContext;
	private final CUserService userService;
	private final CProjectService projectService;

	public SessionService(final AuthenticationContext authenticationContext, final CUserService userService, final CProjectService projectService) {
		this.authenticationContext = authenticationContext;
		this.userService = userService;
		this.projectService = projectService;
	}

	/**
	 * Gets the currently active project from the session.
	 * If no project is set, returns the first available project.
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
	 * Sets the active project in the session and triggers UI refresh.
	 */
	public void setActiveProject(final CProject project) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, project);
			LOGGER.info("Active project set to: {}", project != null ? project.getName() : "null");
			
			// Trigger UI refresh for all open UIs
			refreshProjectAwareComponents();
		}
	}

	/**
	 * Gets the currently active user from the session.
	 */
	public Optional<CUser> getActiveUser() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return Optional.empty();
		}

		CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
		if (activeUser == null) {
			// Try to load user from authentication context
			final Optional<org.springframework.security.core.userdetails.User> authenticatedUser = 
				authenticationContext.getAuthenticatedUser(org.springframework.security.core.userdetails.User.class);
			
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
	 * Sets the active user in the session.
	 */
	public void setActiveUser(final CUser user) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_USER_KEY, user);
			LOGGER.info("Active user set to: {}", user != null ? user.getLogin() : "null");
		}
	}

	/**
	 * Gets all available projects for the current user.
	 * For now, returns all projects. Can be enhanced to filter by user permissions.
	 */
	public List<CProject> getAvailableProjects() {
		return projectService.findAll();
	}

	/**
	 * Triggers refresh of project-aware components when project changes.
	 */
	private void refreshProjectAwareComponents() {
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				// Broadcast a project change event that project-aware components can listen to
				ui.getSession().setAttribute("projectChanged", System.currentTimeMillis());
				LOGGER.debug("Project change event broadcasted");
			});
		}
	}

	/**
	 * Triggers UI refresh to update components when project changes.
	 * @deprecated Use refreshProjectAwareComponents() instead for better performance
	 */
	@Deprecated
	private void refreshUI() {
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				// Trigger a navigation to the current route to refresh components
				ui.getPage().getHistory().replaceState(null, "");
				ui.getPage().reload();
			});
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
	}
}