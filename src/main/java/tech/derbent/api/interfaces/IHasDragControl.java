package tech.derbent.api.interfaces;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CDropEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.utils.Check;

/** Unified interface for components that support drag-and-drop functionality.
 * <p>
 * This is the ONLY interface that should be used for drag-and-drop operations in the application. It consolidates all drag-start, drag-end, and drop
 * event handling into a single interface. Components implementing this interface provide complete control over their drag-and-drop state, allowing
 * parent components or page services to enable or disable drag operations dynamically based on application state.
 * <p>
 * <b>IMPORTANT:</b> Do NOT use Vaadin's Grid.addDragStartListener(), Grid.addDragEndListener(), or Grid.addDropListener() directly. Always use
 * IHasDragControl interface methods. CGrid automatically forwards its internal Grid events to IHasDragControl listeners.
 * <p>
 * <b>Recursive Event Propagation Pattern:</b> Components implementing IHasDragControl forward their drag-drop events to parent components up the
 * hierarchy. This enables automatic event bubbling where each level can process events and forward them upward.
 * <p>
 * <b>Hierarchy Pattern:</b>
 *
 * <pre>
 * CGrid (implements IHasDragControl)
 *     ↓ forwards internal Grid events to
 * CComponentListEntityBase (implements IHasDragControl)
 *     ↓ propagates events to
 * Parent Component or PageService
 *     ↓ handles drag-drop logic
 * </pre>
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>
 * // Enable drag-and-drop for a component
 * componentSprintItems.setDragEnabled(true);
 * componentSprintItems.setDropEnabled(true);
 * // Register listeners via IHasDragControl interface
 * componentSprintItems.addDragStartListener(event -> {
 * 	// Handle drag start
 * });
 * componentSprintItems.addDropListener(event -> {
 * 	// Handle drop
 * });
 * // Disable drag-and-drop (e.g., when editing is locked)
 * componentBacklog.setDragEnabled(false);
 * // Check if drag is enabled
 * if (grid.isDragEnabled()) {
 * 	// Handle drag operation
 * }
 * </pre>
 * <p>
 * <b>Implementation Pattern:</b>
 *
 * <pre>
 * public class CComponentListSprintItems extends CComponentListEntityBase implements IHasDragControl {
 *
 * 	private boolean dragEnabled = false;
 *
 * 	&#64;Override
 * 	public void setDragEnabled(boolean enabled) {
 * 		this.dragEnabled = enabled;
 * 		if (grid != null) {
 * 			grid.setDragEnabled(enabled); // Use CGrid's IHasDragControl method
 * 			LOGGER.debug("[DragDebug] Drag {} for {}", enabled ? "enabled" : "disabled", getClass().getSimpleName());
 * 		}
 * 	}
 *
 * 	&#64;Override
 * 	public boolean isDragEnabled() { return dragEnabled; }
 * }
 * </pre>
 */
public interface IHasDragControl {

	static final Logger LOGGER = LoggerFactory.getLogger(IHasDragControl.class);

	/** Adds a drop listener to this component.
	 * <p>
	 * The listener will be notified whenever items are dropped onto this component. Multiple listeners can be registered, and they will all be
	 * notified of drop events.
	 * </p>
	 * @param listener the listener to add
	 * @return a registration object for removing the listener */
	default void addEventListener_dragDrop(ComponentEventListener<CDropEvent<?>> listener) {
		getDropListeners().add(listener);
	}

	/** Adds a listener for drag end events.
	 * @param listener the listener to be notified when drag ends
	 * @return a registration object that can be used to remove the listener */
	default void addEventListener_dragEnd(ComponentEventListener<CDragEndEvent> listener) {
		getDragEndListeners().add(listener);
	}

	/** Adds a listener for drag start events.
	 * @param listener the listener to be notified when drag starts
	 * @return a registration object that can be used to remove the listener */
	default void addEventListener_dragStart(ComponentEventListener<CDragStartEvent<?>> listener) {
		getDragStartListeners().add(listener);
	}

	public List<ComponentEventListener<CDragEndEvent>> getDragEndListeners();
	public List<ComponentEventListener<CDragStartEvent<?>>> getDragStartListeners();
	public List<ComponentEventListener<CDropEvent<?>>> getDropListeners();
	/** Checks whether drag-and-drop functionality is currently enabled.
	 * <p>
	 * This can be used to conditionally show drag handles, change cursor styles, or display UI feedback about the component's drag state.
	 * @return true if drag operations are enabled, false otherwise */
	boolean isDragEnabled();
	/** Checks whether drop functionality is currently enabled.
	 * <p>
	 * This can be used to conditionally show drop zones, change cursor styles, or display UI feedback about the component's drop state.
	 * @return true if drop operations are enabled, false otherwise */
	boolean isDropEnabled();

	/** Notifies all registered drag end listeners.
	 * <p>
	 * This method is called when a drag operation ends. It automatically notifies all registered drag end listeners with proper error handling.
	 * Implementations can call this method to propagate drag end events up the component hierarchy.
	 * @param event The drag end event to notify listeners about */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	default void notifyDragEndListeners(final CDragEndEvent event) {
		if (getDragEndListeners().isEmpty()) {
			return;
		}
		LOGGER.debug("[DragDebug] {} notifying {} drag end listeners", getClass().getSimpleName(), getDragEndListeners().size());
		for (final ComponentEventListener listener : getDragEndListeners()) {
			try {
				listener.onComponentEvent(event);
			} catch (final Exception e) {
				LOGGER.error("[DragDebug] Error notifying drag end listener in {}: {}", getClass().getSimpleName(), e.getMessage());
			}
		}
	}

	/** Notifies all registered drag start listeners.
	 * <p>
	 * This method is called when a drag operation starts. It automatically notifies all registered drag start listeners with proper error handling.
	 * Implementations can call this method to propagate drag start events up the component hierarchy.
	 * @param event The drag start event to notify listeners about */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	default void notifyDragStartListeners(final CDragStartEvent<?> event) {
		if (getDragStartListeners().isEmpty()) {
			return;
		}
		LOGGER.debug("[DragDebug] {} notifying {} drag start listeners", getClass().getSimpleName(), getDragStartListeners().size());
		for (final ComponentEventListener listener : getDragStartListeners()) {
			try {
				listener.onComponentEvent(event);
			} catch (final Exception e) {
				LOGGER.error("[DragDebug] Error notifying drag start listener in {}: {}", getClass().getSimpleName(), e.getMessage());
			}
		}
	}

	/** Notifies all registered drop listeners.
	 * <p>
	 * This method is called when items are dropped onto this component. It automatically notifies all registered drop listeners with proper error
	 * handling. Implementations can call this method to propagate drop events up the component hierarchy.
	 * @param event The drop event to notify listeners about */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	default void notifyDropListeners(final CDropEvent<?> event) {
		if (getDropListeners().isEmpty()) {
			return;
		}
		LOGGER.debug("[DragDebug] {} notifying {} drop listeners", getClass().getSimpleName(), getDropListeners().size());
		for (final ComponentEventListener listener : getDropListeners()) {
			try {
				listener.onComponentEvent(event);
			} catch (final Exception e) {
				LOGGER.error("[DragDebug] Error notifying drop listener in {}: {}", getClass().getSimpleName(), e.getMessage());
			}
		}
	}

	/** Automatically notifies listeners based on event type.
	 * <p>
	 * This is a convenience method that automatically detects the event type and calls the appropriate notify method (notifyDragStartListeners,
	 * notifyDragEndListeners, or notifyDropListeners). This simplifies event propagation in component hierarchies.
	 * <p>
	 * Usage example:
	 *
	 * <pre>
	 * addEventListener_dragStart(event -> notifyEvents(event));
	 * addEventListener_dragEnd(event -> notifyEvents(event));
	 * addEventListener_dragDrop(event -> notifyEvents(event));
	 * </pre>
	 *
	 * @param event The component event to process and notify listeners about */
	@SuppressWarnings ({})
	default void notifyEvents(final CEvent event) {
		try {
			if (event instanceof CDragStartEvent<?>) {
				notifyDragStartListeners((CDragStartEvent<?>) event);
			} else if (event instanceof CDropEvent<?>) {
				notifyDropListeners((CDropEvent<?>) event);
			} else if (event instanceof CDragEndEvent) {
				notifyDragEndListeners((CDragEndEvent) event);
			}
		} catch (final Exception e) {
			LOGGER.error("Error in notifyEvents for event: {}", event.toString(), e);
			throw e;
		}
	}

	/** Enables or disables drag-and-drop functionality for this component.
	 * <p>
	 * When disabled, the component should not allow drag operations to start, but should still support drop operations if configured as a drop
	 * target.
	 * <p>
	 * Implementations should:
	 * <ol>
	 * <li>Update internal state to track enabled/disabled status</li>
	 * <li>Enable/disable drag on the underlying grid or component</li>
	 * <li>Log the state change for debugging</li>
	 * </ol>
	 * @param enabled true to enable drag operations, false to disable */
	void setDragEnabled(boolean enabled);
	/** Enables or disables drop functionality for this component.
	 * <p>
	 * When disabled, the component should not accept drop operations. This is independent of drag enable/disable - a component can accept drops
	 * without being draggable itself.
	 * <p>
	 * Implementations should:
	 * <ol>
	 * <li>Update internal state to track enabled/disabled status</li>
	 * <li>Configure drop mode on the underlying grid or component</li>
	 * <li>Log the state change for debugging</li>
	 * </ol>
	 * @param enabled true to enable drop operations, false to disable */
	void setDropEnabled(boolean enabled);

	/** Sets up automatic forwarding of drag-drop events from a child component to this parent component.
	 * <p>
	 * This is the standard pattern for event propagation in the component hierarchy. When a child component fires drag-drop events, they are
	 * automatically forwarded to this parent's listeners, enabling recursive event bubbling up to the page service level.
	 * <p>
	 * <b>Usage in parent components:</b>
	 *
	 * <pre>
	 *
	 * // In parent component's initialization
	 * protected void setupChildComponents() {
	 * 	CGrid<Entity> grid = new CGrid<>(Entity.class);
	 * 	setupChildDragDropForwarding(grid); // Automatically forwards grid's events to parent
	 * }
	 * </pre>
	 * <p>
	 * <b>This eliminates the need for manual forwarding code like:</b>
	 *
	 * <pre>
	 * grid.addDragStartListener(event -> notifyDragStartListeners(event));
	 * grid.addDragEndListener(event -> notifyDragEndListeners(event));
	 * grid.addDropListener(event -> notifyDropListeners(event));
	 * </pre>
	 *
	 * @param child The child component implementing IHasDragControl whose events should be forwarded to this parent */
	@SuppressWarnings ({})
	default void setupChildDragDropForwarding(final IHasDragControl child) {
		Check.notNull(child, "Child component cannot be null");
		// Forward drag start events from child to parent
		child.addEventListener_dragStart(event -> {
			notifyDragStartListeners(event);
		});
		// Forward drag end events from child to parent
		child.addEventListener_dragEnd(event -> {
			notifyDragEndListeners(event);
		});
		// Forward drop events from child to parent
		child.addEventListener_dragDrop(event -> {
			notifyDropListeners(event);
		});
	}
}
