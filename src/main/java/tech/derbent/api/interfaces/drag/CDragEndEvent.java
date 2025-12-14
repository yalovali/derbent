package tech.derbent.api.interfaces.drag;

import com.vaadin.flow.component.Component;

/** Custom drag end event for IHasDragControl components.
 * <p>
 * This event is fired when a drag operation ends on a component implementing IHasDragControl, regardless of whether the items were successfully
 * dropped or the drag was cancelled.
 * <p>
 * Unlike Vaadin's GridDragEndEvent which is specific to Grid components, this event works with any component implementing IHasDragControl, enabling a
 * unified drag-drop API across the application.
 * <p>
 * Note: This event does not include the dragged items. If you need to track items across drag start and drag end, store them in the drag start
 * handler. */
public class CDragEndEvent extends CDragBaseEvent {

	private static final long serialVersionUID = 1L;

	/** Creates a new drag end event.
	 * @param source     the component that fired the event (drag source)
	 * @param fromClient true if the event originated from the client, false otherwise */
	public CDragEndEvent(final Component source, final boolean fromClient) {
		super(source, fromClient);
	}
}
