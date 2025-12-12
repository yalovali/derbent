package tech.derbent.api.interfaces;

/**
 * Interface for components that support enabling/disabling drag-and-drop functionality.
 * <p>
 * Components implementing this interface provide control over their drag-and-drop state,
 * allowing parent components or page services to enable or disable drag operations
 * dynamically based on application state.
 * <p>
 * This interface should be implemented alongside {@link IHasDragStart} and {@link IHasDragEnd}
 * to provide complete drag-and-drop control.
 * <p>
 * <b>Owner Registration Pattern:</b>
 * This interface supports an owner registration mechanism where components register with their
 * owner (typically a parent component or page service) for automatic drag-drop event binding.
 * The owner binds all drag operations to itself for immediate notification. Third-party classes
 * should NOT register directly to component fields.
 * <p>
 * <b>Hierarchy Pattern:</b>
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
 * <pre>
 * // Enable drag-and-drop for a component
 * componentSprintItems.setDragEnabled(true);
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
 * <pre>
 * public class CComponentListSprintItems extends CComponentListEntityBase
 *         implements IHasDragStart, IHasDragEnd, IHasDragControl {
 *     
 *     private boolean dragEnabled = false;
 *     
 *     &#64;Override
 *     public void setDragEnabled(boolean enabled) {
 *         this.dragEnabled = enabled;
 *         if (grid != null) {
 *             grid.setRowsDraggable(enabled);
 *             LOGGER.debug("[DragDebug] Drag {} for {}", 
 *                 enabled ? "enabled" : "disabled", 
 *                 getClass().getSimpleName());
 *         }
 *     }
 *     
 *     &#64;Override
 *     public boolean isDragEnabled() {
 *         return dragEnabled;
 *     }
 * }
 * </pre>
 * 
 * @see IHasDragStart
 * @see IHasDragEnd
 */
public interface IHasDragControl {

	/**
	 * Enables or disables drag-and-drop functionality for this component.
	 * <p>
	 * When disabled, the component should not allow drag operations to start,
	 * but should still support drop operations if configured as a drop target.
	 * <p>
	 * Implementations should:
	 * <ol>
	 * <li>Update internal state to track enabled/disabled status</li>
	 * <li>Enable/disable drag on the underlying grid or component</li>
	 * <li>Log the state change for debugging</li> 
	 * </ol>
	 * 
	 * @param enabled true to enable drag operations, false to disable
	 */
	void setDragEnabled(boolean enabled);

	/**
	 * Checks whether drag-and-drop functionality is currently enabled.
	 * <p>
	 * This can be used to conditionally show drag handles, change cursor styles,
	 * or display UI feedback about the component's drag state.
	 * 
	 * @return true if drag operations are enabled, false otherwise
	 */
	boolean isDragEnabled();
	
	/**
	 * Enables or disables drop functionality for this component.
	 * <p>
	 * When disabled, the component should not accept drop operations.
	 * This is independent of drag enable/disable - a component can accept
	 * drops without being draggable itself.
	 * <p>
	 * Implementations should:
	 * <ol>
	 * <li>Update internal state to track enabled/disabled status</li>
	 * <li>Configure drop mode on the underlying grid or component</li>
	 * <li>Log the state change for debugging</li>
	 * </ol>
	 * 
	 * @param enabled true to enable drop operations, false to disable
	 */
	void setDropEnabled(boolean enabled);

	/**
	 * Checks whether drop functionality is currently enabled.
	 * <p>
	 * This can be used to conditionally show drop zones, change cursor styles,
	 * or display UI feedback about the component's drop state.
	 * 
	 * @return true if drop operations are enabled, false otherwise
	 */
	boolean isDropEnabled();
	
	/** Returns a string representation of drag control state for debugging.
	 * <p>
	 * This default method provides a helper for implementing classes to include
	 * drag control state information in their toString() methods.
	 * </p>
	 * @return a string showing drag and drop enabled states, e.g., "dragEnabled=true, dropEnabled=false" */
	default String toDragControlString() {
		return "dragEnabled=" + isDragEnabled() + ", dropEnabled=" + isDropEnabled();
	}


}
