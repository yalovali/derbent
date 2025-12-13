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
 * 
 * // Register listeners via IHasDragControl interface
 * componentSprintItems.addDragStartListener(event -> {
 *     // Handle drag start
 * });
 * 
 * componentSprintItems.addDropListener(event -> {
 *     // Handle drop
 * });
 * 
 * // Disable drag-and-drop (e.g., when editing is locked)
 * componentBacklog.setDragEnabled(false);
 * 
 * // Check if drag is enabled
 * if (grid.isDragEnabled()) {
 *     // Handle drag operation
 * }
 * </pre>
 * <p>
 * <b>Implementation Pattern:</b>
 *
 * <pre>
 * public class CComponentListSprintItems extends CComponentListEntityBase implements IHasDragControl {
 *
 *     private boolean dragEnabled = false;
 *
 *     &#64;Override
 *     public void setDragEnabled(boolean enabled) {
 *         this.dragEnabled = enabled;
 *         if (grid != null) {
 *             grid.setDragEnabled(enabled);  // Use CGrid's IHasDragControl method
 *             LOGGER.debug("[DragDebug] Drag {} for {}", enabled ? "enabled" : "disabled", getClass().getSimpleName());
 *         }
 *     }
 *
 *     &#64;Override
 *     public boolean isDragEnabled() { return dragEnabled; }
 * }
 * </pre> */
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
