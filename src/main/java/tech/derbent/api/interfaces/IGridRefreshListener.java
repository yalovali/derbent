package tech.derbent.api.interfaces;

import java.util.function.Consumer;

/** IGridRefreshListener - Interface for components that need to be notified when grid data changes.
 * <p>
 * This interface provides a simple, general-purpose notification pattern for grid components:
 * <ol>
 * <li>Component performs action (add, delete, move, etc.)</li>
 * <li>Component updates its own data and refreshes its grid</li>
 * <li>Component notifies registered listeners that data changed</li>
 * <li>Listeners refresh themselves to reflect the change</li>
 * </ol>
 * <p>
 * This pattern avoids circular dependencies and complex if-statements by:
 * <ul>
 * <li>Making each component responsible for its own refresh</li>
 * <li>Using simple notification callbacks instead of tight coupling</li>
 * <li>Allowing any component to listen to any other component</li>
 * <li>Keeping the pattern generalizable to all entity types</li>
 * </ul>
 * <p>
 * Example usage:
 * 
 * <pre>
 * // Component A (sprint items) notifies listeners when items change
 * componentA.addRefreshListener(() -> {
 *     // This is called AFTER componentA has already refreshed itself
 *     LOGGER.debug("Sprint items changed, other components should refresh");
 * });
 * 
 * // Component B (backlog) listens and refreshes when notified
 * componentA.addRefreshListener(() -> componentB.refreshGrid());
 * </pre>
 * 
 * @param <T> The entity type managed by this component */
public interface IGridRefreshListener<T> {

	/** Adds a listener to be notified when this component's grid data changes. The listener is called AFTER this component has already updated and
	 * refreshed itself.
	 * @param listener Consumer that will be called when data changes (receives the changed item if available, or null) */
	default void addRefreshListener(final Consumer<T> listener) {
		// Default implementation does nothing - components can override if they support notifications
	}

	/** Removes a previously added refresh listener.
	 * @param listener The listener to remove */
	default void removeRefreshListener(final Consumer<T> listener) {
		// Default implementation does nothing - components can override if they support notifications
	}

	/** Notifies all registered listeners that this component's data has changed. This should be called AFTER the component has updated its own data and
	 * refreshed its grid.
	 * @param changedItem The item that changed, or null if multiple items changed or change is general */
	default void notifyRefreshListeners(final T changedItem) {
		// Default implementation does nothing - components can override if they support notifications
	}
}
