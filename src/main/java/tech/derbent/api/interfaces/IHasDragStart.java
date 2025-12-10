package tech.derbent.api.interfaces;

import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.shared.Registration;

/** Interface for components that support drag start events from their internal grid.
 * <p>
 * Components implementing this interface expose drag start events from their underlying grid, allowing external listeners to be notified when a drag
 * operation begins. The component is responsible for binding these events to its internal grid.
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class CComponentListSprintItems implements IHasDragStart&lt;CSprintItem&gt; {
 * 	private CGrid&lt;CSprintItem&gt; grid;
 * 
 * 	&#64;Override
 * 	public Registration addDragStartListener(ComponentEventListener&lt;GridDragStartEvent&lt;CSprintItem&gt;&gt; listener) {
 * 		return grid.addDragStartListener(listener);
 * 	}
 * }
 * 
 * // Usage in page service
 * componentListSprintItems.addDragStartListener(event -&gt; {
 * 	LOGGER.debug("Drag started with {} items", event.getDraggedItems().size());
 * });
 * </pre>
 * @param <T> The type of items that can be dragged */
public interface IHasDragStart<T> {

	/** Adds a listener for drag start events.
	 * @param listener the listener to be notified when drag starts
	 * @return a registration object that can be used to remove the listener */
	Registration addDragStartListener(com.vaadin.flow.component.ComponentEventListener<GridDragStartEvent<T>> listener);
	
	/** Returns a string representation of drag start support for debugging.
	 * <p>
	 * This default method provides a helper for implementing classes to include
	 * drag start state information in their toString() methods.
	 * </p>
	 * @return a string indicating drag start support is available */
	default String toDragStartString() {
		return "dragStartSupported=true";
	}
}
