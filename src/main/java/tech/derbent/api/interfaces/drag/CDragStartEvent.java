package tech.derbent.api.interfaces.drag;

import java.util.List;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

/** Custom drag start event for IHasDragControl components.
 * <p>
 * This event is fired when a drag operation starts on a component implementing IHasDragControl. It provides access to the dragged items and the
 * source component.
 * <p>
 * Unlike Vaadin's GridDragStartEvent which is specific to Grid components, this event works with any component implementing IHasDragControl,
 * enabling a unified drag-drop API across the application.
 * @param <T> The type of items being dragged */
public class CDragStartEvent<T> extends ComponentEvent<Component> {

	private static final long serialVersionUID = 1L;
	private final List<T> draggedItems;

	/** Creates a new drag start event.
	 * @param source       the component that fired the event (drag source)
	 * @param draggedItems the items being dragged
	 * @param fromClient   true if the event originated from the client, false otherwise */
	public CDragStartEvent(final Component source, final List<T> draggedItems, final boolean fromClient) {
		super(source, fromClient);
		this.draggedItems = draggedItems;
	}

	/** Gets the first dragged item (convenience method for single-item drags).
	 * @return the first dragged item, or null if the list is empty */
	public T getDraggedItem() {
		return (draggedItems != null && !draggedItems.isEmpty()) ? draggedItems.get(0) : null;
	}

	/** Gets the list of items being dragged.
	 * @return the dragged items (never null, but may be empty) */
	public List<T> getDraggedItems() { return draggedItems; }
}
