package tech.derbent.base.session.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Primary;
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

/** Service to manage user session state including active user and active project. Uses Vaadin session to store session-specific information. */
@Service ("CSessionService")
@Primary
@ConditionalOnWebApplication
@Profile ("!reset-db")
public class CWebSessionService implements ISessionService {

	// private static final String ACTIVE_COMPANY_KEY = "activeCompany";
	private static final String ACTIVE_ID_ATTRIBUTES_KEY = CWebSessionService.class.getName() + ".activeIdAttributes";
	private static final String ACTIVE_ID_KEY = "activeId";
	private static final String ACTIVE_PROJECT_KEY = "activeProject";
	private static final String ACTIVE_USER_KEY = "activeUser";
	private static final Logger LOGGER = LoggerFactory.getLogger(CWebSessionService.class);
	private static final String PROJECT_CHANGE_LISTENERS_KEY = CWebSessionService.class.getName() + ".projectChangeListeners";
	private static final String PROJECT_LIST_CHANGE_LISTENERS_KEY = CWebSessionService.class.getName() + ".projectListChangeListeners";

	@SuppressWarnings ("unchecked")
	private static Set<String> getActiveIdAttributesIfPresent(final VaadinSession session) {
		return (Set<String>) session.getAttribute(ACTIVE_ID_ATTRIBUTES_KEY);
	}

	@SuppressWarnings ("unchecked")
	private static Set<IProjectChangeListener> getOrCreateProjectChangeListeners(final VaadinSession session) {
		Set<IProjectChangeListener> listeners = (Set<IProjectChangeListener>) session.getAttribute(PROJECT_CHANGE_LISTENERS_KEY);
		if (listeners == null) {
			listeners = ConcurrentHashMap.newKeySet();
			session.setAttribute(PROJECT_CHANGE_LISTENERS_KEY, listeners);
		}
		return listeners;
	}

	@SuppressWarnings ("unchecked")
	private static Set<IProjectListChangeListener> getOrCreateProjectListChangeListeners(final VaadinSession session) {
		Set<IProjectListChangeListener> listeners = (Set<IProjectListChangeListener>) session.getAttribute(PROJECT_LIST_CHANGE_LISTENERS_KEY);
		if (listeners == null) {
			listeners = ConcurrentHashMap.newKeySet();
			session.setAttribute(PROJECT_LIST_CHANGE_LISTENERS_KEY, listeners);
		}
		return listeners;
	}

	@SuppressWarnings ("unchecked")
	private static Set<IProjectChangeListener> getProjectChangeListenersIfPresent(final VaadinSession session) {
		return (Set<IProjectChangeListener>) session.getAttribute(PROJECT_CHANGE_LISTENERS_KEY);
	}

	@SuppressWarnings ("unchecked")
	private static Set<IProjectListChangeListener> getProjectListChangeListenersIfPresent(final VaadinSession session) {
		return (Set<IProjectListChangeListener>) session.getAttribute(PROJECT_LIST_CHANGE_LISTENERS_KEY);
	}

	private CLayoutService layoutService;
	private final IProjectRepository projectRepository;

	public CWebSessionService(@SuppressWarnings ("unused") final AuthenticationContext authenticationContext,
			@SuppressWarnings ("unused") final IUserRepository userRepository, final IProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	/** Registers a component to receive notifications when the active project changes. Components should call this method when they are attached to
	 * the UI.
	 * @param listener The component that wants to be notified of project changes */
	@Override
	public void addProjectChangeListener(final IProjectChangeListener listener) {
		if (listener != null) {
			final VaadinSession session = VaadinSession.getCurrent();
			if (session == null) {
				LOGGER.warn("VaadinSession is null, cannot add project change listener {}", listener.getClass().getSimpleName());
				return;
			}
			getOrCreateProjectChangeListeners(session).add(listener);
		}
	}

	/** Registers a component to receive notifications when the project list changes. Components should call this method when they are attached to the
	 * UI.
	 * @param listener The component that wants to be notified of project list changes */
	@Override
	public void addProjectListChangeListener(final IProjectListChangeListener listener) {
		if (listener != null) {
			final VaadinSession session = VaadinSession.getCurrent();
			if (session == null) {
				LOGGER.warn("VaadinSession is null, cannot add project list change listener {}", listener.getClass().getSimpleName());
				return;
			}
			getOrCreateProjectListChangeListeners(session).add(listener);
		}
	}

	/** Clears session data on logout. */
	@Override
	public void clearSession() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, null);
			session.setAttribute(ACTIVE_USER_KEY, null);
			// session.setAttribute(ACTIVE_COMPANY_KEY, null);
			session.setAttribute(ACTIVE_ID_KEY, null);
			final Set<String> activeIdKeys = getActiveIdAttributesIfPresent(session);
			if (activeIdKeys != null) {
				activeIdKeys.forEach(attributeName -> session.setAttribute(attributeName, null));
				activeIdKeys.clear();
			}
			final Set<IProjectChangeListener> projectListeners = getProjectChangeListenersIfPresent(session);
			if (projectListeners != null) {
				projectListeners.clear();
			}
			final Set<IProjectListChangeListener> projectListListeners = getProjectListChangeListenersIfPresent(session);
			if (projectListListeners != null) {
				projectListListeners.clear();
			}
		} else {
			LOGGER.debug("clearSession called without active VaadinSession");
		}
		if (layoutService != null) {
			CLayoutService.clearLayoutChangeListeners();
		}
	}

	/** Gets the currently active company from the session. */
	@Override
	public Optional<CCompany> getActiveCompany() {
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "Vaadin session must not be null");
		final CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
		Check.notNull(activeUser, "Active user must not be null to get company");
		final CCompany company = activeUser.getCompany();
		return Optional.ofNullable(company);
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
		final CProject activeProject = (CProject) session.getAttribute(ACTIVE_PROJECT_KEY);
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
		final CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
		return Optional.ofNullable(activeUser);
	}

	/** Gets all available projects for the current user. Filters by company if available. */
	@Override
	public List<CProject> getAvailableProjects() {
		// Get current company from session
		final CCompany currentCompany = getCurrentCompany();
		if (currentCompany != null) {
			// LOGGER.debug("Filtering available projects by company: {}", currentCompany.getName());
			// change this to findByUserId if you want to filter by user as well
			return projectRepository.findByCompanyId(currentCompany.getId());
		}
		// Fallback to all projects if no company context
		LOGGER.debug("No company context, returning all projects");
		return projectRepository.findAll();
	}

	/** Gets the current company (convenience method). */
	@Override
	public CCompany getCurrentCompany() { return getActiveCompany().orElse(null); }

	@SuppressWarnings ("static-method")
	private Set<IProjectChangeListener> getCurrentProjectChangeListeners() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.debug("No active VaadinSession; returning empty project change listener set");
			return Collections.emptySet();
		}
		final Set<IProjectChangeListener> listeners = getProjectChangeListenersIfPresent(session);
		return listeners != null ? listeners : Collections.emptySet();
	}

	@SuppressWarnings ("static-method")
	private Set<IProjectListChangeListener> getCurrentProjectListChangeListeners() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.debug("No active VaadinSession; returning empty project list change listener set");
			return Collections.emptySet();
		}
		final Set<IProjectListChangeListener> listeners = getProjectListChangeListenersIfPresent(session);
		return listeners != null ? listeners : Collections.emptySet();
	}

	@SuppressWarnings ({
			"unchecked", "static-method"
	})
	private Set<String> getOrCreateActiveIdAttributes(final VaadinSession session) {
		Set<String> attributes = (Set<String>) session.getAttribute(ACTIVE_ID_ATTRIBUTES_KEY);
		if (attributes == null) {
			attributes = ConcurrentHashMap.newKeySet();
			session.setAttribute(ACTIVE_ID_ATTRIBUTES_KEY, attributes);
		}
		return attributes;
	}

	/** Event listener for project list changes. This method is called when projects are created, updated, or deleted to notify all registered
	 * listeners.
	 * @param event The project list change event */
	@Override
	@EventListener
	public void handleProjectListChange(final ProjectListChangeEvent event) {
		notifyProjectListChanged();
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
		if (listener != null) {
			final VaadinSession session = VaadinSession.getCurrent();
			if (session == null) {
				LOGGER.warn("VaadinSession is null, cannot remove project change listener {}", listener.getClass().getSimpleName());
				return;
			}
			getOrCreateProjectChangeListeners(session).remove(listener);
		}
	}

	/** Unregisters a component from receiving project list change notifications. Components should call this method when they are detached from the
	 * UI.
	 * @param listener The component to unregister */
	@Override
	public void removeProjectListChangeListener(final IProjectListChangeListener listener) {
		if (listener != null) {
			final VaadinSession session = VaadinSession.getCurrent();
			if (session == null) {
				LOGGER.warn("VaadinSession is null, cannot remove project list change listener {}", listener.getClass().getSimpleName());
				return;
			}
			getOrCreateProjectListChangeListeners(session).remove(listener);
		}
	}

	@Override
	public void setActiveCompany(final CCompany company) {
		// dont call it
		// use set user or set project instead
		Check.fail("setActiveCompany should not be called directly; use setActiveUser or setActiveProject instead");
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
		getOrCreateActiveIdAttributes(session).add(key);
	}

	/** Sets the active project in the session and triggers UI refresh. */
	@Override
	public void setActiveProject(final CProject project) {
		// reset active entity ID when changing project
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "Vaadin session must not be null");
		if (project == null && getActiveProject().orElse(null) == null) {
			return;
		}
		if (project != null && getActiveProject().orElse(null) != null && project.getId().equals(getActiveProject().orElse(null).getId())) {
			// LOGGER.debug("setActiveProject called with same project, no action taken");
			return;
		}
		session.setAttribute(ACTIVE_PROJECT_KEY, project);
		// LOGGER.info("Active project set to: {}:{}", project.getId(), project.getName());
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

	/** Sets both company and user in the session atomically. This ensures company is always set before user and validates that the user is a member
	 * of the company.
	 * @param company the company to set as active
	 * @param user    the user to set as active (must be a member of the company) */
	@Override
	public void setActiveUser(final CUser user) {
		// LOGGER.debug("setActiveUser called");
		Check.notNull(user, "User must not be null");
		clearSession(); // Clear session data before setting new user
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "Vaadin session must not be null");
		session.setAttribute(ACTIVE_USER_KEY, user);
		// Set first available project
		final List<CProject> availableProjects = getAvailableProjects();
		if (!availableProjects.isEmpty()) {
			final CProject activeProject = availableProjects.get(0);
			setActiveProject(activeProject);
		}
	}

	/** Sets the layout service. This is called after bean creation to avoid circular dependency. */
	@Override
	public void setLayoutService(final CLayoutService layoutService) { this.layoutService = layoutService; }
}
