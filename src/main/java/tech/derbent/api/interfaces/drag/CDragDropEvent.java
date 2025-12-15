package tech.derbent.api.interfaces.drag;

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
public class CDragDropEvent<T> extends CDragBaseEvent {

	private static final long serialVersionUID = 1L;
	private final GridDropLocation dropLocation;
	private final T targetItem;

	/** Creates a new drop event.
	 * @param source       the component that fired the event (drop target)
	 * @param draggedItems the items being dropped
	 * @param dragSource   the component from which items were dragged
	 * @param targetItem   the item at the drop location (may be null)
	 * @param dropLocation the location relative to the target item
	 * @param fromClient   true if the event originated from the client, false otherwise */
	public CDragDropEvent(final Component source, final Component dragSource, final T targetItem, final GridDropLocation dropLocation,
			final boolean fromClient) {
		super(source, fromClient);
		this.targetItem = targetItem;
		this.dropLocation = dropLocation;
	}

	/** Gets the drop location relative to the target item.
	 * @return the drop location (ABOVE, BELOW, ON_TOP, or EMPTY) */
	public GridDropLocation getDropLocation() { return dropLocation; }

	/** Gets the component where items are being dropped (convenience method).
	 * <p>
	 * This is equivalent to getSource() but provides clearer semantics for drop operations.
	 * @return the drop target component (same as getSource()) */
	public Component getDropTarget() { return getSource(); }

	/** Gets the item at the drop location.
	 * @return Optional containing the target item, or empty if dropping at the end or in an empty grid */
	public Optional<T> getDropTargetItem() {
		return Optional.ofNullable(targetItem);
	}

	/** Gets the item at the drop location (direct access).
	 * @return the target item, or null if dropping at the end or in an empty grid */
	public T getTargetItem() { return targetItem; }
}
