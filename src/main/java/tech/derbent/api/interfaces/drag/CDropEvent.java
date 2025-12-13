package tech.derbent.api.interfaces.drag;

import java.util.List;
import java.util.Optional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;

/** Custom drop event for IHasDragControl components.
 * <p>
 * This event is fired when items are dropped onto a component implementing IHasDragControl. It provides access to the dragged items, drop location,
 * target item, and both the drag source and drop target components.
 * <p>
 * Unlike Vaadin's GridDropEvent which is specific to Grid components, this event works with any component implementing IHasDragControl, enabling a
 * unified drag-drop API across the application.
 * @param <T> The type of items being dropped */
public class CDropEvent<T> extends CEvent {

	private static final long serialVersionUID = 1L;
	private final List<T> draggedItems;
	private final Component dragSource;
	private final GridDropLocation dropLocation;
	private final T targetItem;

	/** Creates a new drop event.
	 * @param source       the component that fired the event (drop target)
	 * @param draggedItems the items being dropped
	 * @param dragSource   the component from which items were dragged
	 * @param targetItem   the item at the drop location (may be null)
	 * @param dropLocation the location relative to the target item
	 * @param fromClient   true if the event originated from the client, false otherwise */
	public CDropEvent(final Component source, final List<T> draggedItems, final Component dragSource, final T targetItem,
			final GridDropLocation dropLocation, final boolean fromClient) {
		super(source, fromClient);
		this.draggedItems = draggedItems;
		this.dragSource = dragSource;
		this.targetItem = targetItem;
		this.dropLocation = dropLocation;
	}

	/** Gets the first dragged item (convenience method for single-item drops).
	 * @return the first dragged item, or null if the list is empty */
	public T getDraggedItem() {
		return draggedItems != null && !draggedItems.isEmpty() ? draggedItems.get(0) : null;
	}

	/** Gets the list of items being dropped.
	 * @return the dragged items (never null, but may be empty) */
	public List<T> getDraggedItems() { return draggedItems; }

	/** Gets the component from which items were dragged.
	 * @return the drag source component */
	public Component getDragSource() { return dragSource; }

	/** Gets the drop location relative to the target item.
	 * @return the drop location (ABOVE, BELOW, ON_TOP, or EMPTY) */
	public GridDropLocation getDropLocation() { return dropLocation; }

	/** Gets the item at the drop location.
	 * @return Optional containing the target item, or empty if dropping at the end or in an empty grid */
	public Optional<T> getDropTargetItem() {
		return Optional.ofNullable(targetItem);
	}

	/** Gets the item at the drop location (direct access).
	 * @return the target item, or null if dropping at the end or in an empty grid */
	public T getTargetItem() { return targetItem; }
}
