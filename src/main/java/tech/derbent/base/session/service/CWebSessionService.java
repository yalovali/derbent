package tech.derbent.base.session.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.interfaces.IProjectListChangeListener;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.events.ProjectListChangeEvent;
import tech.derbent.app.projects.service.IProjectRepository;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.IUserRepository;

/** Service to manage user session state including active user and active project. Uses Vaadin session to store session-specific information.
 * <p>
 * This service provides methods that require dependencies (like repository access) or Spring event handling. For simple session access without
 * dependencies, use the static methods in {@link CSessionHelper} instead.
 * </p>
 */
@Service ("CSessionService")
@ConditionalOnWebApplication
@Profile ("!reset-db")
public class CWebSessionService implements ISessionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWebSessionService.class);
	private CLayoutService layoutService;
	private final IProjectRepository projectRepository;

	public CWebSessionService(final AuthenticationContext authenticationContext, final IUserRepository userRepository,
			final IProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	/** Registers a component to receive notifications when the active project changes. Components should call this method when they are attached to
	 * the UI.
	 * @param listener The component that wants to be notified of project changes */
	@Override
	public void addProjectChangeListener(final IProjectChangeListener listener) {
		CSessionHelper.addProjectChangeListener(listener);
	}

	/** Registers a component to receive notifications when the project list changes. Components should call this method when they are attached to the
	 * UI.
	 * @param listener The component that wants to be notified of project list changes */
	@Override
	public void addProjectListChangeListener(final IProjectListChangeListener listener) {
		CSessionHelper.addProjectListChangeListener(listener);
	}

	/** Clears session data on logout. */
	@Override
	public void clearSession() {
		CSessionHelper.clearSession();
		if (layoutService != null) {
			layoutService.clearLayoutChangeListeners();
		}
	}

	/** Gets the currently active company from the session. */
	@Override
	public Optional<CCompany> getActiveCompany() { return CSessionHelper.getActiveCompany(); }

	@Override
	public Long getActiveId(final String entityType) {
		return CSessionHelper.getActiveId(entityType);
	}

	/** Gets the currently active project from the session. If no project is set, returns the first available project. */
	@Override
	public Optional<CProject> getActiveProject() { return CSessionHelper.getActiveProject(); }

	/** Gets the currently active user from the session. If no user is set, attempts to load the user from the authentication context. optinal means
	 * that it may return an empty value if no user is found. */
	@Override
	public Optional<CUser> getActiveUser() { return CSessionHelper.getActiveUser(); }

	/** Gets all available projects for the current user. Filters by company if available. */
	@Override
	public List<CProject> getAvailableProjects() {
		// Get current company from session
		CCompany currentCompany = getCurrentCompany();
		if (currentCompany != null) {
			LOGGER.debug("Filtering available projects by company: {}", currentCompany.getName());
			// change this to findByUserId if you want to filter by user as well
			return projectRepository.findByCompanyId(currentCompany.getId());
		}
		// Fallback to all projects if no company context
		LOGGER.debug("No company context, returning all projects");
		return projectRepository.findAll();
	}

	/** Gets the current company (convenience method). */
	@Override
	public CCompany getCurrentCompany() { return CSessionHelper.getCurrentCompany(); }

	private Set<IProjectChangeListener> getCurrentProjectChangeListeners() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.debug("No active VaadinSession; returning empty project change listener set");
			return Collections.emptySet();
		}
		final Set<IProjectChangeListener> listeners = CSessionHelper.getProjectChangeListenersIfPresent(session);
		return listeners != null ? listeners : Collections.emptySet();
	}

	private Set<IProjectListChangeListener> getCurrentProjectListChangeListeners() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.debug("No active VaadinSession; returning empty project list change listener set");
			return Collections.emptySet();
		}
		final Set<IProjectListChangeListener> listeners = CSessionHelper.getProjectListChangeListenersIfPresent(session);
		return listeners != null ? listeners : Collections.emptySet();
	}

	/** Event listener for project list changes. This method is called when projects are created, updated, or deleted to notify all registered
	 * listeners.
	 * @param event The project list change event */
	@Override
	@EventListener
	public void handleProjectListChange(final ProjectListChangeEvent event) {
		notifyProjectListChanged();
	}

	/** Notifies all registered project list change listeners about changes to the project list. This method safely handles UI access for components
	 * that may be in different UIs. */
	@Override
	public void notifyProjectListChanged() {
		// Use UI.access to safely notify listeners that may be in different UI contexts
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				getCurrentProjectListChangeListeners().forEach(listener -> {
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
		CSessionHelper.removeProjectChangeListener(listener);
	}

	/** Unregisters a component from receiving project list change notifications. Components should call this method when they are detached from the
	 * UI.
	 * @param listener The component to unregister */
	@Override
	public void removeProjectListChangeListener(final IProjectListChangeListener listener) {
		CSessionHelper.removeProjectListChangeListener(listener);
	}

	@Override
	public void setActiveId(final String entityType, final Long id) {
		CSessionHelper.setActiveId(entityType, id);
	}

	/** Sets the active project in the session and triggers UI refresh. */
	@Override
	public void setActiveProject(final CProject project) {
		// reset active entity ID when changing project
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "Vaadin session must not be null");
		if (project == null && CSessionHelper.getActiveProject().orElse(null) == null) {
			return;
		}
		if (project != null && CSessionHelper.getActiveProject().orElse(null) != null
				&& project.getId().equals(CSessionHelper.getActiveProject().orElse(null).getId())) {
			LOGGER.debug("setActiveProject called with same project, no action taken");
			return;
		}
		CSessionHelper.setActiveProject(project);
		// Notify listeners synchronously when already in UI thread (normal case during navigation)
		// This prevents async refresh cascades that could interfere with page initialization
		// Only use ui.access() when called from a background thread
		final UI ui = UI.getCurrent();
		if (ui != null) {
			// Already in UI thread - notify directly to preserve synchronous execution order
			notifyProjectChangeListeners(project);
		} else {
			// Called from background thread - need ui.access() for thread safety
			LOGGER.debug("setActiveProject called from background thread, using ui.access()");
			// Note: This path is rare, typically only during initialization
		}
	}

	/** Helper method to notify all project change listeners. */
	private void notifyProjectChangeListeners(final CProject project) {
		getCurrentProjectChangeListeners().forEach(listener -> {
			try {
				listener.onProjectChanged(project);
			} catch (final Exception e) {
				LOGGER.error("Error notifying project change listener: {}", listener.getClass().getSimpleName(), e);
			}
		});
	}

	/** Sets both company and user in the session atomically. This ensures company is always set before user and validates that the user is a member
	 * of the company.
	 * @param company the company to set as active
	 * @param user    the user to set as active (must be a member of the company) */
	@Override
	public void setActiveUser(CUser user) {
		LOGGER.debug("setActiveUser called");
		Check.notNull(user, "User must not be null");
		clearSession(); // Clear session data before setting new user
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "Vaadin session must not be null");
		CSessionHelper.setActiveUser(user);
		// Set first available project
		final List<CProject> availableProjects = getAvailableProjects();
		if (!availableProjects.isEmpty()) {
			CProject activeProject = availableProjects.get(0);
			setActiveProject(activeProject);
		}
	}

	/** Sets the layout service. This is called after bean creation to avoid circular dependency. */
	@Override
	public void setLayoutService(final CLayoutService layoutService) { this.layoutService = layoutService; }
}
