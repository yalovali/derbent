package tech.derbent.api.interfaces;

import java.util.function.Consumer;

/** Interface for components that support drag and drop of grid items.
 * <p>
 * This interface provides a standardized pattern for enabling drag and drop functionality on grids. Components implementing this interface can enable
 * or disable drag and drop, and provide handlers for drop operations.
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class MyComponent implements IGridDragDropSupport&lt;MyEntity&gt; {
 * 	&#64;Override
 * 	public void setDragEnabled(boolean enabled) {
 * 		grid.setRowsDraggable(enabled);
 * 	}
 * 
 * 	&#64;Override
 * 	public void setDropHandler(Consumer&lt;MyEntity&gt; handler) {
 * 		this.dropHandler = handler;
 * 	}
 * }
 * </pre>
 * @param <T> The type of items in the grid */
public interface IGridDragDropSupport<T> {

	/** Gets the current drop handler.
	 * @return the drop handler, or null if not set */
	Consumer<T> getDropHandler();

	/** Checks if drag is currently enabled on the grid.
	 * @return true if drag is enabled, false otherwise */
	boolean isDragEnabled();

	/** Enables or disables dragging of grid rows.
	 * @param enabled true to enable dragging, false to disable */
	void setDragEnabled(boolean enabled);

	/** Sets the handler to be called when an item is dropped.
	 * @param handler the consumer to handle dropped items */
	void setDropHandler(Consumer<T> handler);
}
