package tech.derbent.api.services.pageservice;

import java.util.List;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;

/** Wrapper class for drag and drop event data. This class is used to pass drag and drop event information to page service handler methods.
 * <p>
 * The event provides access to:
 * <ul>
 * <li>Dragged items (source of the drag operation)</li>
 * <li>Target item (destination of the drop operation)</li>
 * <li>Drop location (ABOVE, BELOW, ON_TOP relative to target)</li>
 * <li>Drag source and drop target components</li>
 * </ul>
 * <p>
 * Usage in page service handler methods:
 * 
 * <pre>
 * public void on_sprintItems_dragStart(final Component component, final Object value) {
 * 	if (value instanceof CDragDropEvent) {
 * 		final CDragDropEvent event = (CDragDropEvent) value;
 * 		final List&lt;?&gt; draggedItems = event.getDraggedItems();
 * 		// Handle drag start...
 * 	}
 * }
 * 
 * public void on_sprintItems_drop(final Component component, final Object value) {
 * 	if (value instanceof CDragDropEvent) {
 * 		final CDragDropEvent event = (CDragDropEvent) value;
 * 		final Object targetItem = event.getTargetItem();
 * 		final GridDropLocation location = event.getDropLocation();
 * 		// Handle drop...
 * 	}
 * }
 * </pre>
 * @param <T> The type of items being dragged/dropped */
public class CDragDropEvent<T> {

	private final List<T> draggedItems;
	private final Object dragSource;
	private final GridDropLocation dropLocation;
	private final Object dropTarget;
	private final T targetItem;

	/** Constructor for drag start events.
	 * @param draggedItems the list of items being dragged
	 * @param dragSource   the component from which items are being dragged */
	public CDragDropEvent(final List<T> draggedItems, final Object dragSource) {
		this.draggedItems = draggedItems;
		this.dragSource = dragSource;
		this.targetItem = null;
		this.dropLocation = null;
		this.dropTarget = null;
	}

	/** Constructor for drop events.
	 * @param draggedItems the list of items being dragged
	 * @param dragSource   the component from which items are being dragged
	 * @param targetItem   the item at the drop location (can be null)
	 * @param dropLocation the drop location relative to target item
	 * @param dropTarget   the component where items are being dropped */
	public CDragDropEvent(final List<T> draggedItems, final Object dragSource, final T targetItem, final GridDropLocation dropLocation,
			final Object dropTarget) {
		this.draggedItems = draggedItems;
		this.dragSource = dragSource;
		this.targetItem = targetItem;
		this.dropLocation = dropLocation;
		this.dropTarget = dropTarget;
	}

	/** Gets the list of items being dragged.
	 * @return the dragged items */
	public List<T> getDraggedItems() { return draggedItems; }

	/** Gets the first dragged item (convenience method for single-item drags).
	 * @return the first dragged item, or null if the list is empty */
	public T getDraggedItem() { return (draggedItems != null && !draggedItems.isEmpty()) ? draggedItems.get(0) : null; }

	/** Gets the component from which items are being dragged.
	 * @return the drag source component */
	public Object getDragSource() { return dragSource; }

	/** Gets the drop location relative to the target item.
	 * @return the drop location (ABOVE, BELOW, ON_TOP) or null for drag start events */
	public GridDropLocation getDropLocation() { return dropLocation; }

	/** Gets the component where items are being dropped.
	 * @return the drop target component, or null for drag start events */
	public Object getDropTarget() { return dropTarget; }

	/** Gets the item at the drop location.
	 * @return the target item, or null if dropping at the end or for drag start events */
	public T getTargetItem() { return targetItem; }

	/** Checks if this is a drag end event (has drop location and target).
	 * @return true if this is a drop event, false if it's a drag start event */
	public boolean isDropEvent() { return dropLocation != null; }

	/** Checks if this is a drag start event (no drop location).
	 * @return true if this is a drag start event, false if it's a drop event */
	public boolean isDragStartEvent() { return dropLocation == null; }
}
