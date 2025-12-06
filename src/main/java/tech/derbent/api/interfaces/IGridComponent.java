package tech.derbent.api.interfaces;

import tech.derbent.api.grid.domain.CGrid;

/**
 * IGridComponent - Interface for components that contain a CGrid.
 * <p>
 * This interface defines standard methods for grid-based components to ensure consistent behavior
 * and API across the application. Components implementing this interface should provide:
 * <ul>
 * <li>Access to the underlying grid instance</li>
 * <li>Methods to refresh/reload grid data</li>
 * <li>Methods to clear grid contents</li>
 * </ul>
 * <p>
 * Implementation Notes:
 * <ul>
 * <li>Grid configuration should be done via the configureGrid() method pattern</li>
 * <li>Use CGrid helper methods for column creation (addIdColumn, addShortTextColumn, etc.)</li>
 * <li>All public methods should validate parameters and log appropriately</li>
 * </ul>
 * 
 * @param <T> The entity type displayed in the grid
 */
public interface IGridComponent<T> {

	/**
	 * Gets the underlying CGrid instance.
	 * <p>
	 * This allows external components to interact with the grid directly
	 * when needed (e.g., for drag-and-drop configuration, selection handling).
	 * 
	 * @return The CGrid instance (never null after component initialization)
	 */
	CGrid<T> getGrid();

	/**
	 * Refreshes the grid to display updated data.
	 * <p>
	 * This method should reload data from the data source and update the grid display.
	 * It should maintain the current selection if possible.
	 * <p>
	 * Typical use cases:
	 * <ul>
	 * <li>After adding/editing/deleting items</li>
	 * <li>After external data changes</li>
	 * <li>When filters or search criteria change</li>
	 * </ul>
	 */
	void refreshGrid();

	/**
	 * Clears all items from the grid.
	 * <p>
	 * This method should remove all items from the grid display and clear any selection.
	 * It does not affect the underlying data source.
	 * <p>
	 * Typical use cases:
	 * <ul>
	 * <li>When no master entity is selected</li>
	 * <li>When resetting the component state</li>
	 * <li>Before loading new data</li>
	 * </ul>
	 */
	void clearGrid();

	/**
	 * Configures the grid columns and appearance.
	 * <p>
	 * This method should use CGrid helper methods (addIdColumn, addShortTextColumn, etc.)
	 * to create columns consistently. It should:
	 * <ul>
	 * <li>Validate the grid parameter is not null</li>
	 * <li>Log debug information about configuration</li>
	 * <li>Use CGrid.styleColumnHeader() for all column headers</li>
	 * <li>Set appropriate widths and flex grow properties</li>
	 * </ul>
	 * <p>
	 * Note: This method is typically called during grid initialization.
	 * Subclasses should implement this to define their specific column structure.
	 * 
	 * @param grid The grid to configure (must not be null)
	 */
	void configureGrid(CGrid<T> grid);
}
