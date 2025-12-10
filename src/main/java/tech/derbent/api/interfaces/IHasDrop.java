package tech.derbent.api.interfaces;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.shared.Registration;

/**
 * Interface for components that support drop events.
 * <p>
 * Components implementing this interface can accept items being dropped onto them,
 * typically from drag-and-drop operations. The interface provides methods to add
 * drop listeners that will be notified when items are dropped.
 * </p>
 * <p>
 * This interface is used in conjunction with {@link IHasDragStart} and {@link IHasDragEnd}
 * to create complete drag-and-drop workflows.
 * </p>
 * 
 * @param <T> the type of items that can be dropped
 */
public interface IHasDrop<T> {
	
	/**
	 * Adds a drop listener to this component.
	 * <p>
	 * The listener will be notified whenever items are dropped onto this component.
	 * Multiple listeners can be registered, and they will all be notified of drop events.
	 * </p>
	 * 
	 * @param listener the listener to add
	 * @return a registration object for removing the listener
	 */
	Registration addDropListener(ComponentEventListener<GridDropEvent<T>> listener);
	
	/**
	 * Returns a string representation indicating this component supports drop events.
	 * This method can be used by implementing classes to build their toString() output.
	 * 
	 * @return a string representation indicating drop support
	 */
	default String toDropString() {
		return "dropSupported=true";
	}
}
