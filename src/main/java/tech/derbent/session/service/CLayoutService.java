package tech.derbent.session.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import tech.derbent.abstracts.interfaces.CLayoutChangeListener;

/** Service to manage layout state (horizontal vs vertical) for views. Uses Vaadin session to store layout preference. */
@Service
public class CLayoutService {

	public enum LayoutMode {
		HORIZONTAL, VERTICAL
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CLayoutService.class);
	private static final String LAYOUT_MODE_KEY = "layoutMode";
	// Thread-safe set to store layout change listeners
	private final Set<CLayoutChangeListener> layoutChangeListeners = ConcurrentHashMap.newKeySet();

	/** Registers a component to receive notifications when the layout mode changes. */
	public void addLayoutChangeListener(final CLayoutChangeListener listener) {
		Assert.notNull(listener, "Listener cannot be null");
		layoutChangeListeners.add(listener);
	}

	/** Clears all layout change listeners (typically called on session clear). */
	public void clearLayoutChangeListeners() {
		layoutChangeListeners.clear();
	}

	/** Gets the current layout mode from the session. Defaults to VERTICAL if not set. */
	public LayoutMode getCurrentLayoutMode() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session == null) {
			return LayoutMode.VERTICAL; // Default to vertical
		}
		final LayoutMode mode = (LayoutMode) session.getAttribute(LAYOUT_MODE_KEY);
		final LayoutMode result = mode != null ? mode : LayoutMode.VERTICAL;
		return result;
	}

	/** Notifies all registered layout change listeners about a layout mode change. */
	private void notifyLayoutChangeListeners(final LayoutMode newMode) {
		LOGGER.debug("Notifying {} layout change listeners of layout change to {}", layoutChangeListeners.size(), newMode);
		if (newMode == null) {
			LOGGER.warn("Cannot notify listeners - newMode is null");
			return;
		}
		final UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				layoutChangeListeners.forEach(listener -> {
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
				// Force push to update the UI immediately
				ui.push();
			});
		} else {
			// If no UI context, try direct notification
			LOGGER.warn("UI.getCurrent() is null, attempting direct notification");
			layoutChangeListeners.forEach(listener -> {
				if (listener != null) {
					try {
						listener.onLayoutModeChanged(newMode);
						LOGGER.debug("Directly notified layout listener: {}", listener.getClass().getSimpleName());
					} catch (final Exception e) {
						LOGGER.error("Error directly notifying layout change listener: {}", listener.getClass().getSimpleName(), e);
					}
				} else {
					LOGGER.warn("Encountered null listener in the list");
				}
			});
		}
	}

	/** Unregisters a component from receiving layout change notifications. */
	public void removeLayoutChangeListener(final CLayoutChangeListener listener) {
		assert listener != null : "Listener cannot be null";
		layoutChangeListeners.remove(listener);
	}

	/** Sets the layout mode and notifies all registered listeners. */
	public void setLayoutMode(final LayoutMode layoutMode) {
		if (layoutMode == null) {
			LOGGER.warn("Cannot set layout mode - layoutMode is null");
			return;
		}
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute(LAYOUT_MODE_KEY, layoutMode);
			notifyLayoutChangeListeners(layoutMode);
		} else {
			LOGGER.warn("VaadinSession is null, cannot set layout mode");
		}
	}

	/** Toggles between horizontal and vertical layout modes. */
	public void toggleLayoutMode() {
		LOGGER.debug("Toggling layout mode");
		final LayoutMode currentMode = getCurrentLayoutMode();
		final LayoutMode newMode = currentMode == LayoutMode.HORIZONTAL ? LayoutMode.VERTICAL : LayoutMode.HORIZONTAL;
		LOGGER.info("Toggling from {} to {}", currentMode, newMode);
		setLayoutMode(newMode);
	}
}
