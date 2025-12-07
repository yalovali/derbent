package tech.derbent.api.interfaces;

import java.util.Set;

/** Interface for components that need to be notified when drop operations complete in child components.
 * <p>
 * This interface provides a standardized pattern for components to notify their containers or owners when drop operations complete. It allows components
 * like backlog, grids, and lists to communicate drop events upward to their container components.
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class CPageSprintView implements IDropOwner&lt;CActivity&gt; {
 * 	private CComponentBacklog backlogComponent;
 * 	private CComponentListSprintItems sprintItemsComponent;
 * 
 * 	private void setupComponents() {
 * 		backlogComponent = new CComponentBacklog(sprint);
 * 		backlogComponent.setDropOwner(this);
 * 
 * 		sprintItemsComponent = new CComponentListSprintItems(sprint);
 * 		sprintItemsComponent.setDropOwner(this);
 * 	}
 * 
 * 	&#64;Override
 * 	public void onDropComplete(Set&lt;CActivity&gt; droppedItems, Object targetComponent) {
 * 		// Handle drop completion - update related components, refresh data, etc.
 * 		LOGGER.debug("Drop completed: {} items on {}", droppedItems.size(), targetComponent.getClass().getSimpleName());
 * 		refreshRelatedComponents();
 * 	}
 * }
 * </pre>
 * @param <T> The type of items that can be dropped */
public interface IDropOwner<T> {

	/** Called when a drop operation completes in a child component.
	 * @param droppedItems    the set of items that were dropped
	 * @param targetComponent the component where the items were dropped (can be null) */
	void onDropComplete(Set<T> droppedItems, Object targetComponent);
}
