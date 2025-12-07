package tech.derbent.api.interfaces;

import java.util.Set;

/** Interface for components that need to be notified when drag operations start in child components.
 * <p>
 * This interface provides a standardized pattern for components to notify their containers or owners when drag operations begin. It allows components
 * like backlog, grids, and lists to communicate drag start events upward to their container components.
 * <p>
 * Example usage:
 * 
 * <pre>
 * public class CPageSprintView implements IDragOwner&lt;CActivity&gt; {
 * 	private CComponentBacklog backlogComponent;
 * 
 * 	private void setupComponents() {
 * 		backlogComponent = new CComponentBacklog(sprint);
 * 		backlogComponent.setDragOwner(this);
 * 	}
 * 
 * 	&#64;Override
 * 	public void onDragStart(Set&lt;CActivity&gt; draggedItems) {
 * 		// Handle drag start - prepare drop targets, update UI state, etc.
 * 		LOGGER.debug("Drag started with {} items", draggedItems.size());
 * 		enableDropTargets();
 * 	}
 * }
 * </pre>
 * @param <T> The type of items that can be dragged */
public interface IDragOwner<T> {

	/** Called when a drag operation starts in a child component.
	 * @param draggedItems the set of items being dragged */
	void onDragStart(Set<T> draggedItems);
}
