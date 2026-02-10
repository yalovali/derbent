package tech.derbent.api.session.service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import tech.derbent.api.interfaces.ILayoutChangeListener;
import tech.derbent.api.utils.Check;

/** Service to manage layout state (horizontal vs vertical) for views. Uses Vaadin session to store layout preference. */
@Service
public class CLayoutService {

	public enum LayoutMode {
		HORIZONTAL, VERTICAL
	}

	private static final String LAYOUT_LISTENERS_KEY = CLayoutService.class.getName() + ".layoutChangeListeners";
	private static final String LAYOUT_MODE_KEY = "layoutMode";
	private static final Logger LOGGER = LoggerFactory.getLogger(CLayoutService.class);

	/** Registers a component to receive notifications when the layout mode changes. */
	public static void addLayoutChangeListener(final ILayoutChangeListener listener) {
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(listener, "Listener cannot be null");
		Check.notNull(session, "VaadinSession must not be null");
		getOrCreateLayoutListeners(session).add(listener);
	}

	/** Clears all layout change listeners (typically called on session clear). */
	public static void clearLayoutChangeListeners() {
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "VaadinSession must not be null");
		getOrCreateLayoutListeners(session).clear();
	}

	private static Set<ILayoutChangeListener> getCurrentLayoutListeners() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			LOGGER.debug("No active VaadinSession; returning empty layout listener set");
			return Collections.emptySet();
		}
		return getOrCreateLayoutListeners(session);
	}

	/** Gets the current layout mode from the session. Defaults to VERTICAL if not set. */
	public static LayoutMode getCurrentLayoutMode() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return LayoutMode.VERTICAL; // Default to vertical
		}
		final LayoutMode mode = (LayoutMode) session.getAttribute(LAYOUT_MODE_KEY);
		final LayoutMode result = mode != null ? mode : LayoutMode.VERTICAL;
		return result;
	}

	@SuppressWarnings ("unchecked")
	private static Set<ILayoutChangeListener> getOrCreateLayoutListeners(final VaadinSession session) {
		Set<ILayoutChangeListener> listeners = (Set<ILayoutChangeListener>) session.getAttribute(LAYOUT_LISTENERS_KEY);
		if (listeners == null) {
			listeners = ConcurrentHashMap.newKeySet();
			session.setAttribute(LAYOUT_LISTENERS_KEY, listeners);
		}
		return listeners;
	}

	/** Notifies all registered layout change listeners about a layout mode change. */
	private static void notifyLayoutChangeListeners(final LayoutMode newMode) {
		final Set<ILayoutChangeListener> listeners = getCurrentLayoutListeners();
		LOGGER.debug("Notifying {} layout change listeners of layout change to {}", listeners.size(), newMode);
		if (newMode == null) {
			LOGGER.warn("Cannot notify listeners - newMode is null");
			return;
		}
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				listeners.forEach(listener -> {
					if (listener != null) {
						try {
							listener.onLayoutModeChanged(newMode);
							LOGGER.debug("Notified layout listener: {}", listener.getClass().getSimpleName());
						} catch (final Exception e) {
							LOGGER.error("Error notifying layout change listener: {}", listener.getClass().getSimpleName(), e);
						}
					} else {
						LOGGER.warn("Encountered null listener in the list");
					}
				});
				// Try to push UI updates, but handle case where push is not enabled
				try {
					ui.push();
					LOGGER.debug("UI push successful after layout change");
				} catch (final IllegalStateException e) {
					if (e.getMessage() != null && e.getMessage().contains("Push not enabled")) {
						LOGGER.debug("Push not enabled, layout change will be reflected on next user interaction");
					} else {
						LOGGER.error("Error during UI push: {}", e.getMessage());
					}
				} catch (final Exception e) {
					LOGGER.error("Error during UI push: {}", e.getMessage());
				}
			});
		} else {
			// If no UI context, try direct notification
			LOGGER.warn("UI.getCurrent() is null, attempting direct notification");
			listeners.forEach(listener -> {
				Check.notNull(listener, "Listener in the list cannot be null");
				try {
					listener.onLayoutModeChanged(newMode);
					LOGGER.debug("Directly notified layout listener: {}", listener.getClass().getSimpleName());
				} catch (final Exception e) {
					LOGGER.error("Error directly notifying layout change listener: {}", listener.getClass().getSimpleName(), e);
				}
			});
		}
	}

	/** Unregisters a component from receiving layout change notifications. */
	public static void removeLayoutChangeListener(final ILayoutChangeListener listener) {
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(listener, "Listener must not be null");
		Check.notNull(session, "VaadinSession must not be null");
		getOrCreateLayoutListeners(session).remove(listener);
	}

	/** Sets the layout mode and notifies all registered listeners. */
	public static void setLayoutMode(final LayoutMode layoutMode) {
		if (layoutMode == null) {
			LOGGER.warn("Cannot set layout mode - layoutMode is null");
			return;
		}
		final VaadinSession session = VaadinSession.getCurrent();
		Check.notNull(session, "VaadinSession must not be null");
		session.setAttribute(LAYOUT_MODE_KEY, layoutMode);
		notifyLayoutChangeListeners(layoutMode);
	}

	/** Toggles between horizontal and vertical layout modes. */
	public static void toggleLayoutMode() {
		LOGGER.debug("Toggling layout mode");
		final LayoutMode currentMode = getCurrentLayoutMode();
		final LayoutMode newMode = currentMode == LayoutMode.HORIZONTAL ? LayoutMode.VERTICAL : LayoutMode.HORIZONTAL;
		LOGGER.info("Toggling from {} to {}", currentMode, newMode);
		setLayoutMode(newMode);
	}
}
