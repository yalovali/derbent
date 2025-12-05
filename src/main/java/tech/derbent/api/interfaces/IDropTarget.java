package tech.derbent.api.interfaces;

import java.util.function.Consumer;

/** Interface for components that can receive dropped items from drag and drop operations.
 * <p>
 * This interface provides a standardized pattern for components that can act as drop targets. It allows external components to register handlers for
 * when items are dropped into this component.
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class MyListComponent implements IDropTarget&lt;MyEntity&gt; {
 * 	private Consumer&lt;MyEntity&gt; dropHandler;
 * 
 * 	&#64;Override
 * 	public void setDropHandler(Consumer&lt;MyEntity&gt; handler) {
 * 		this.dropHandler = handler;
 * 		grid.setDropMode(GridDropMode.BETWEEN);
 * 		grid.addDropListener(e -&gt; {
 * 			if (handler != null &amp;&amp; e.getDropTargetItem().isPresent()) {
 * 				handler.accept(e.getDropTargetItem().get());
 * 			}
 * 		});
 * 	}
 * 
 * 	&#64;Override
 * 	public boolean isDropEnabled() {
 * 		return dropHandler != null;
 * 	}
 * }
 * </pre>
 * @param <T> The type of items that can be dropped */
public interface IDropTarget<T> {

	/** Gets the current drop handler.
	 * @return the drop handler, or null if not set */
	Consumer<T> getDropHandler();

	/** Checks if dropping is currently enabled for this component.
	 * @return true if drops are enabled, false otherwise */
	boolean isDropEnabled();

	/** Sets the handler to be called when an item is dropped into this component.
	 * @param handler the consumer to handle dropped items */
	void setDropHandler(Consumer<T> handler);
}
