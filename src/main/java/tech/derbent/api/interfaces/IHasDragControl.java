package tech.derbent.api.interfaces;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.shared.Registration;

/** Interface for components that support enabling/disabling drag-and-drop functionality.
 * <p>
 * Components implementing this interface provide control over their drag-and-drop state, allowing parent components or page services to enable or
 * disable drag operations dynamically based on application state.
 * <p>
 * This interface should be implemented alongside {@link IHasDragStart} and {@link IHasDragEnd} to provide complete drag-and-drop control.
 * <p>
 * <b>Owner Registration Pattern:</b> This interface supports an owner registration mechanism where components register with their owner (typically a
 * parent component or page service) for automatic drag-drop event binding. The owner binds all drag operations to itself for immediate notification.
 * Third-party classes should NOT register directly to component fields.
 * <p>
 * <b>Hierarchy Pattern:</b>
 *
 * <pre>
 * Component implements IHasDragStart, IHasDragEnd, IHasDragControl
 *     ↓ registers with owner via setDragDropOwner()
 * Owner (Parent Component or PageService)
 *     ↓ binds drag events via registerWithOwner()
 * Internal Grid with drag-enabled state
 *     ↓ propagates events to
 * Owner's event handlers
 * </pre>
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>
 * // Enable drag-and-drop for a component
 * componentSprintItems.setDragEnabled(true);
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
 * public class CComponentListSprintItems extends CComponentListEntityBase implements IHasDragStart, IHasDragEnd, IHasDragControl {
 *
 * 	private boolean dragEnabled = false;
 *
 * 	&#64;Override
 * 	public void setDragEnabled(boolean enabled) {
 * 		this.dragEnabled = enabled;
 * 		if (grid != null) {
 * 			grid.setRowsDraggable(enabled);
 * 			LOGGER.debug("[DragDebug] Drag {} for {}", enabled ? "enabled" : "disabled", getClass().getSimpleName());
 * 		}
 * 	}
 *
 * 	&#64;Override
 * 	public boolean isDragEnabled() { return dragEnabled; }
 * }
 * </pre>
 *
 * @see IHasDragStart
 * @see IHasDragEnd */
public interface IHasDragControl {

	static final Logger LOGGER = LoggerFactory.getLogger(IHasDragControl.class);

	/** Adds a listener for drag end events.
	 * @param listener the listener to be notified when drag ends
	 * @return a registration object that can be used to remove the listener */
	Registration addDragEndListener(ComponentEventListener<GridDragEndEvent<?>> listener);
	/** Adds a listener for drag start events.
	 * @param listener the listener to be notified when drag starts
	 * @return a registration object that can be used to remove the listener */
	Registration addDragStartListener(ComponentEventListener<GridDragStartEvent<?>> listener);
	/** Adds a drop listener to this component.
	 * <p>
	 * The listener will be notified whenever items are dropped onto this component. Multiple listeners can be registered, and they will all be
	 * notified of drop events.
	 * </p>
	 * @param listener the listener to add
	 * @return a registration object for removing the listener */
	Registration addDropListener(ComponentEventListener<GridDropEvent<?>> listener);
	public List<ComponentEventListener<GridDragEndEvent<?>>> getDragEndListeners();
	public List<ComponentEventListener<GridDragStartEvent<?>>> getDragStartListeners();
	public List<ComponentEventListener<GridDropEvent<?>>> getDropListeners();
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

	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	default void notifyEvents(ComponentEvent<?> event) {
		try {
			if (event instanceof GridDragStartEvent<?> && !getDragStartListeners().isEmpty()) {
				for (final ComponentEventListener listener : getDragStartListeners()) {
					listener.onComponentEvent(event);
				}
			} else if (event instanceof GridDropEvent<?> && !getDropListeners().isEmpty()) {
				for (final ComponentEventListener listener : getDropListeners()) {
					listener.onComponentEvent(event);
				}
			} else if (event instanceof GridDragEndEvent<?> && !getDragEndListeners().isEmpty()) {
				for (final ComponentEventListener listener : getDragEndListeners()) {
					listener.onComponentEvent(event);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error in notifyevents for event:" + event.toString());
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
}
