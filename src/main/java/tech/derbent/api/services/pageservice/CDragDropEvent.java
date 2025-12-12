package tech.derbent.api.services.pageservice;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import tech.derbent.api.utils.Check;

/** Wrapper class for drag and drop event data. This class is used to pass drag and drop event information to page service handler methods.
 * <p>
 * The event provides access to:
 * <ul>
 * <li>Dragged items (source of the drag operation)</li>
 * <li>Target item (destination of the drop operation)</li>
 * <li>Drop location (ABOVE, BELOW, ON_TOP relative to target)</li>
 * <li>Drag source and drop target components</li>
 * <li>Component hierarchy from drag source to root</li>
 * </ul>
 * <p>
 * <strong>Automatic Drag Source Tracking:</strong> Starting from this version, CPageService automatically tracks the drag source and dragged items
 * during drag operations. Drop handlers no longer receive null for dragSource - they receive the actual component from which items were dragged.
 * <p>
 * Usage in page service handler methods:
 *
 * <pre>
 *
 * public void on_sprintItems_dragStart(final Component component, final Object value) {
 * 	if (value instanceof CDragDropEvent) {
 * 		final CDragDropEvent event = (CDragDropEvent) value;
 * 		final List&lt;?&gt; draggedItems = event.getDraggedItems();
 * 		// Handle drag start...
 * 	}
 * }
 *
 * public void on_sprintGrid_drop(final Component component, final Object value) {
 * 	if (value instanceof CDragDropEvent) {
 * 		final CDragDropEvent event = (CDragDropEvent) value;
 * 		// NEW: Access drag source to determine where items came from
 * 		final Object dragSource = event.getDragSource();
 * 		if (dragSource instanceof CComponentBacklog) {
 * 			// Handle backlog to sprint drop
 * 		}
 * 		// Access drop target information
 * 		final Object targetItem = event.getTargetItem();
 * 		final GridDropLocation location = event.getDropLocation();
 * 	}
 * }
 * </pre>
 *
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
		Check.notNull(draggedItems, "draggedItems cannot be null");
		Check.notNull(dragSource, "dragSource cannot be null");
		this.draggedItems = draggedItems;
		this.dragSource = dragSource;
		targetItem = null;
		dropLocation = null;
		dropTarget = null;
	}

	/** Constructor for drop events.
	 * @param draggedItems the list of items being dragged
	 * @param dragSource   the component from which items are being dragged
	 * @param targetItem   the item at the drop location (can be null)
	 * @param dropLocation the drop location relative to target item
	 * @param dropTarget   the component where items are being dropped */
	public CDragDropEvent(final List<T> draggedItems, final Object dragSource, final T targetItem, final GridDropLocation dropLocation,
			final Object dropTarget) {
		Check.notNull(draggedItems, "draggedItems cannot be null");
		Check.notNull(dragSource, "dragSource cannot be null");
		this.draggedItems = draggedItems;
		this.dragSource = dragSource;
		this.targetItem = targetItem;
		this.dropLocation = dropLocation;
		this.dropTarget = dropTarget;
	}

	/** Gets the first dragged item (convenience method for single-item drags).
	 * @return the first dragged item, or null if the list is empty */
	public T getDraggedItem() {
		return (draggedItems != null && !draggedItems.isEmpty()) ? draggedItems.get(0) : null;
	}

	/** Gets the list of items being dragged.
	 * @return the dragged items */
	public List<T> getDraggedItems() { return draggedItems; }

	/** Gets the component from which items are being dragged.
	 * @return the drag source component */
	public Object getDragSource() { return dragSource; }

	/** Gets the component hierarchy chain from the drag source to its root container.
	 * <p>
	 * Returns a list of components starting from the drag source, then its parent, then the parent's parent, etc., up to the root component. This
	 * allows drop handlers to understand the complete context of where the drag originated.
	 * </p>
	 * <p>
	 * Example: If dragging from a Grid inside a VerticalLayout inside a Dialog, the chain would be: [Grid, VerticalLayout, Dialog, ...]
	 * </p>
	 * @return list of components from source to root, or empty list if drag source is null or not a Component */
	public List<Component> getDragSourceHierarchy() {
		final List<Component> hierarchy = new ArrayList<>();
		if (dragSource instanceof Component) {
			Component current = (Component) dragSource;
			while (current != null) {
				hierarchy.add(current);
				current = current.getParent().orElse(null);
			}
		}
		return hierarchy;
	}

	/** Gets the drop location relative to the target item.
	 * @return the drop location (ABOVE, BELOW, ON_TOP) or null for drag start events */
	public GridDropLocation getDropLocation() {
		return dropLocation;
	}

	/** Gets the component where items are being dropped.
	 * @return the drop target component, or null for drag start events */
	public Object getDropTarget() { return dropTarget; }

	/** Gets the item at the drop location.
	 * @return the target item, or null if dropping at the end or for drag start events */
	public T getTargetItem() { return targetItem; }

	/** Checks if this is a drag start event (no drop location).
	 * @return true if this is a drag start event, false if it's a drop event */
	public boolean isDragStartEvent() { return dropLocation == null; }

	/** Checks if this is a drag end event (has drop location and target).
	 * @return true if this is a drop event, false if it's a drag start event */
	public boolean isDropEvent() { return dropLocation != null; }
}
