package tech.derbent.api.interfaces;

import java.util.Set;

/** Interface for components that need to be notified of selection changes in child components.
 * <p>
 * This interface provides a standardized pattern for components to notify their containers or owners when selections change. It allows components
 * like backlog, grids, and lists to communicate selection events upward to their container components.
 * <p>
 * Example usage:
 *
 * <pre>
 * public class CPageSprintView implements ISelectionOwner&lt;CActivity&gt; {
 * 
 * 	private CComponentBacklog backlogComponent;
 *
 * 	private void setupComponents() {
 * 		backlogComponent = new CComponentBacklog(sprint);
 * 		backlogComponent.setSelectionOwner(this);
 * 	}
 *
 * 	&#64;Override
 * 	public void onSelectionChanged(Set&lt;CActivity&gt; selectedItems) {
 * 		// Handle selection change - update UI, enable/disable buttons, etc.
 * 		LOGGER.debug("Selection changed: {} items selected", selectedItems.size());
 * 		updateToolbarButtons(selectedItems);
 * 	}
 * }
 * </pre>
 *
 * @param <T> The type of items that can be selected */
public interface ISelectionOwner<T> {

	/** Called when the selection changes in a child component.
	 * @param selectedItems the set of currently selected items */
	void onSelectionChanged(Set<T> selectedItems);
}
