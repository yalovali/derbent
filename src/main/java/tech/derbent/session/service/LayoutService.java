package tech.derbent.session.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import tech.derbent.abstracts.interfaces.CLayoutChangeListener;

/**
 * Service to manage layout state (horizontal vs vertical) for views.
 * Uses Vaadin session to store layout preference.
 */
@Service
public class LayoutService {

    public enum LayoutMode {
        HORIZONTAL,
        VERTICAL
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutService.class);
    private static final String LAYOUT_MODE_KEY = "layoutMode";
    
    // Thread-safe set to store layout change listeners
    private final Set<CLayoutChangeListener> layoutChangeListeners = ConcurrentHashMap.newKeySet();

    /**
     * Gets the current layout mode from the session. Defaults to VERTICAL if not set.
     */
    public LayoutMode getCurrentLayoutMode() {
        final VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return LayoutMode.VERTICAL; // Default to vertical
        }
        
        final LayoutMode mode = (LayoutMode) session.getAttribute(LAYOUT_MODE_KEY);
        return mode != null ? mode : LayoutMode.VERTICAL;
    }

    /**
     * Sets the layout mode and notifies all registered listeners.
     */
    public void setLayoutMode(final LayoutMode layoutMode) {
        LOGGER.info("Setting layout mode to: {}", layoutMode);
        final VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(LAYOUT_MODE_KEY, layoutMode);
            notifyLayoutChangeListeners(layoutMode);
        }
    }

    /**
     * Toggles between horizontal and vertical layout modes.
     */
    public void toggleLayoutMode() {
        final LayoutMode currentMode = getCurrentLayoutMode();
        final LayoutMode newMode = currentMode == LayoutMode.HORIZONTAL 
            ? LayoutMode.VERTICAL 
            : LayoutMode.HORIZONTAL;
        setLayoutMode(newMode);
    }

    /**
     * Registers a component to receive notifications when the layout mode changes.
     */
    public void addLayoutChangeListener(final CLayoutChangeListener listener) {
        if (listener != null) {
            layoutChangeListeners.add(listener);
            LOGGER.debug("Layout change listener registered: {}", listener.getClass().getSimpleName());
        }
    }

    /**
     * Unregisters a component from receiving layout change notifications.
     */
    public void removeLayoutChangeListener(final CLayoutChangeListener listener) {
        if (listener != null) {
            layoutChangeListeners.remove(listener);
            LOGGER.debug("Layout change listener unregistered: {}", listener.getClass().getSimpleName());
        }
    }

    /**
     * Clears all layout change listeners (typically called on session clear).
     */
    public void clearLayoutChangeListeners() {
        layoutChangeListeners.clear();
        LOGGER.debug("Layout change listeners cleared");
    }

    /**
     * Notifies all registered layout change listeners about a layout mode change.
     */
    private void notifyLayoutChangeListeners(final LayoutMode newMode) {
        LOGGER.debug("Notifying {} layout change listeners of layout change to {}", 
                    layoutChangeListeners.size(), newMode);
        
        final UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                layoutChangeListeners.forEach(listener -> {
                    try {
                        listener.onLayoutModeChanged(newMode);
                        LOGGER.debug("Notified layout listener: {}", listener.getClass().getSimpleName());
                    } catch (final Exception e) {
                        LOGGER.error("Error notifying layout change listener: {}", 
                                   listener.getClass().getSimpleName(), e);
                    }
                });
            });
        }
    }
}