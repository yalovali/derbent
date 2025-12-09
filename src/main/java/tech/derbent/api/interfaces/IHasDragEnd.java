package tech.derbent.api.interfaces;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.shared.Registration;

/** Interface for components that support drag end events from their internal grid.
 * <p>
 * Components implementing this interface expose drag end events from their underlying grid, allowing external listeners to be notified when a drag
 * operation ends. The component is responsible for binding these events to its internal grid.
 * <p>
 * Example usage:
 *
 * <pre>
 * public class CComponentListSprintItems implements IHasDragEnd&lt;CSprintItem&gt; {
 * 
 * 	private CGrid&lt;CSprintItem&gt; grid;
 *
 * 	&#64;Override
 * 	public Registration addDragEndListener(ComponentEventListener&lt;GridDragEndEvent&lt;CSprintItem&gt;&gt; listener) {
 * 		return grid.addDragEndListener(listener);
 * 	}
 * }
 * // Usage in page service
 * componentListSprintItems.addDragEndListener(event -&gt; {
 * 	LOGGER.debug("Drag ended");
 * });
 * </pre>
 *
 * @param <T> The type of items that can be dragged */
public interface IHasDragEnd<T> {

	/** Adds a listener for drag end events.
	 * @param listener the listener to be notified when drag ends
	 * @return a registration object that can be used to remove the listener */
	Registration addDragEndListener(ComponentEventListener<GridDragEndEvent<T>> listener);
}
