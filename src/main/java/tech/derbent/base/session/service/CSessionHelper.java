package tech.derbent.base.session.service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.server.VaadinSession;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.interfaces.IProjectListChangeListener;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;

/** Static utility class for accessing and managing Vaadin session state. This class provides static methods to access session data without needing to
 * inject a service. All session data is stored in VaadinSession, ensuring proper multi-user isolation. */
public final class CSessionHelper {

	private static final String ACTIVE_ID_ATTRIBUTES_KEY = CSessionHelper.class.getName() + ".activeIdAttributes";
	private static final String ACTIVE_ID_KEY = "activeId";
	private static final String ACTIVE_PROJECT_KEY = "activeProject";
	private static final String ACTIVE_USER_KEY = "activeUser";
	private static final Logger LOGGER = LoggerFactory.getLogger(CSessionHelper.class);
	private static final String PROJECT_CHANGE_LISTENERS_KEY = CSessionHelper.class.getName() + ".projectChangeListeners";
	private static final String PROJECT_LIST_CHANGE_LISTENERS_KEY = CSessionHelper.class.getName() + ".projectListChangeListeners";

	private CSessionHelper() {
		// Utility class - no instantiation
	}

	/** Registers a component to receive notifications when the active project changes. Components should call this method when they are attached to
	 * the UI.
	 * @param listener The component that wants to be notified of project changes */
	public static void addProjectChangeListener(final IProjectChangeListener listener) {
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
	public static void addProjectListChangeListener(final IProjectListChangeListener listener) {
		if (listener != null) {
			final VaadinSession session = VaadinSession.getCurrent();
			if (session == null) {
				LOGGER.warn("VaadinSession is null, cannot add project list change listener {}", listener.getClass().getSimpleName());
				return;
			}
			getOrCreateProjectListChangeListeners(session).add(listener);
		}
	}

	/** Clears session data. This should be called on logout. Note: This only clears basic session data. Services that manage additional session state
	 * (like CLayoutService) need to be cleared separately via CWebSessionService. */
	public static void clearSession() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(ACTIVE_PROJECT_KEY, null);
			session.setAttribute(ACTIVE_USER_KEY, null);
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
	}

	/** Gets the currently active company from the session. */
	public static Optional<CCompany> getActiveCompany() {
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "Vaadin session must not be null");
		CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
		Check.notNull(activeUser, "Active user must not be null to get company");
		CCompany company = activeUser.getCompany();
		return Optional.ofNullable(company);
	}

	public static @Nullable Long getActiveId(final String entityType) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			return (Long) session.getAttribute(ACTIVE_ID_KEY + "_" + entityType);
		}
		return null;
	}

	@SuppressWarnings ("unchecked")
	private static @Nullable Set<String> getActiveIdAttributesIfPresent(final VaadinSession session) {
		return (Set<String>) session.getAttribute(ACTIVE_ID_ATTRIBUTES_KEY);
	}

	/** Gets the currently active project from the session. If no project is set, returns empty Optional. */
	public static Optional<CProject> getActiveProject() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return Optional.empty();
		}
		CProject activeProject = (CProject) session.getAttribute(ACTIVE_PROJECT_KEY);
		return Optional.ofNullable(activeProject);
	}

	/** Gets the currently active user from the session. If no user is set, returns empty Optional. */
	public static Optional<CUser> getActiveUser() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return Optional.empty();
		}
		CUser activeUser = (CUser) session.getAttribute(ACTIVE_USER_KEY);
		return Optional.ofNullable(activeUser);
	}

	/** Gets the current company (convenience method). */
	public static @Nullable CCompany getCurrentCompany() {
		return getActiveCompany().orElse(null);
	}

	@SuppressWarnings ("unchecked")
	private static @Nullable Set<String> getOrCreateActiveIdAttributes(final VaadinSession session) {
		Set<String> attributes = (Set<String>) session.getAttribute(ACTIVE_ID_ATTRIBUTES_KEY);
		if (attributes == null) {
			attributes = ConcurrentHashMap.newKeySet();
			session.setAttribute(ACTIVE_ID_ATTRIBUTES_KEY, attributes);
		}
		return attributes;
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
	static @Nullable Set<IProjectChangeListener> getProjectChangeListenersIfPresent(final VaadinSession session) {
		return (Set<IProjectChangeListener>) session.getAttribute(PROJECT_CHANGE_LISTENERS_KEY);
	}

	@SuppressWarnings ("unchecked")
	static @Nullable Set<IProjectListChangeListener> getProjectListChangeListenersIfPresent(final VaadinSession session) {
		return (Set<IProjectListChangeListener>) session.getAttribute(PROJECT_LIST_CHANGE_LISTENERS_KEY);
	}

	/** Unregisters a component from receiving project change notifications. Components should call this method when they are detached from the UI.
	 * @param listener The component to unregister */
	public static void removeProjectChangeListener(final IProjectChangeListener listener) {
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
	public static void removeProjectListChangeListener(final IProjectListChangeListener listener) {
		if (listener != null) {
			final VaadinSession session = VaadinSession.getCurrent();
			if (session == null) {
				LOGGER.warn("VaadinSession is null, cannot remove project list change listener {}", listener.getClass().getSimpleName());
				return;
			}
			getOrCreateProjectListChangeListeners(session).remove(listener);
		}
	}

	public static void setActiveId(final String entityType, final Long id) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return;
		}
		final String key = ACTIVE_ID_KEY + "_" + entityType;
		session.setAttribute(key, id);
		LOGGER.debug("Active ID set to: {}", id);
		final Set<String> attributes = getOrCreateActiveIdAttributes(session);
		if (attributes != null) {
			attributes.add(key);
		}
	}

	/** Sets the active project in the session. Note: This does not trigger UI refresh or notify listeners. For full project change handling with
	 * listener notification, use CWebSessionService.setActiveProject().
	 * @param project The project to set as active */
	public static void setActiveProject(final @Nullable CProject project) {
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "Vaadin session must not be null");
		session.setAttribute(ACTIVE_PROJECT_KEY, project);
		if (project != null) {
			LOGGER.info("Active project set to: {}:{}", project.getId(), project.getName());
		}
	}

	/** Sets the active user in the session. Note: This does not clear session or set default project. For full user setup, use
	 * CWebSessionService.setActiveUser().
	 * @param user the user to set as active */
	public static void setActiveUser(final @Nullable CUser user) {
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "Vaadin session must not be null");
		session.setAttribute(ACTIVE_USER_KEY, user);
		if (user != null) {
			LOGGER.debug("Active user set to: {}", user.getUsername());
		}
	}
}
