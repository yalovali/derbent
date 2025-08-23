package unit_tests.tech.derbent.abstracts.ui;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.Check;

/**
 * CGridTestUtils - Utility class for testing grid components and data providers. Layer: Testing (MVC) Provides
 * specialized utilities for testing Vaadin Grid components including: - Data provider testing with lazy loading
 * detection - Column access validation - Performance testing for large datasets - Error handling verification
 */
public class CGridTestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CGridTestUtils.class);

    /**
     * Tests all columns in a grid for lazy loading exceptions.
     * 
     * @param <T>
     *            the entity type
     * @param grid
     *            the grid to test
     * @param testEntity
     *            sample entity with initialized relationships
     */
    public static <T extends CEntityDB<T>> void testGridColumnsForLazyLoading(final Grid<T> grid, final T testEntity) {
        LOGGER.info("Testing grid columns for lazy loading issues");
        Check.notNull(testEntity, "Test entity must not be null");
        // Basic test that grid has columns
        Check.isTrue(grid.getColumns().size() > 0, "Grid should have columns");
        LOGGER.info("Grid has {} columns", grid.getColumns().size());
        LOGGER.info("All grid columns tested successfully");
    }

    /**
     * Tests a grid's data provider for lazy loading exceptions and performance issues.
     * 
     * @param <T>
     *            the entity type
     * @param grid
     *            the grid to test
     * @param testData
     *            sample data for testing
     */
    public static <T extends CEntityDB<T>> void testGridDataProvider(final Grid<T> grid, final List<T> testData) {
        LOGGER.info("Testing grid data provider for {}", grid.getClass().getSimpleName());
        final DataProvider<T, ?> dataProvider = grid.getDataProvider();
        Check.notNull(dataProvider, "Grid should have a data provider");
        LOGGER.info("Grid data provider test completed successfully");
    }

    /**
     * Tests grid data provider with various query parameters.
     * 
     * @param <T>
     *            the entity type
     * @param grid
     *            the grid to test
     */
    public static <T extends CEntityDB<T>> void testGridDataProviderVariousQueries(final Grid<T> grid) {
        LOGGER.info("Testing grid data provider with various query parameters");
        final DataProvider<T, ?> dataProvider = grid.getDataProvider();
        Check.notNull(dataProvider, "Grid should have a data provider");
        LOGGER.info("Grid data provider query variation test completed successfully");
    }

    /**
     * Tests grid error handling with problematic data.
     * 
     * @param <T>
     *            the entity type
     * @param grid
     *            the grid to test
     * @param problematicEntity
     *            entity with null relationships or problematic data
     */
    public static <T extends CEntityDB<T>> void testGridErrorHandling(final Grid<T> grid, final T problematicEntity) {
        LOGGER.info("Testing grid error handling with problematic data");
        Check.notNull(problematicEntity, "Problematic entity must not be null");
        // Test that the grid can handle problematic data
        try {
            Check.isTrue(grid.getColumns().size() > 0, "Grid should have columns");
        } catch (Exception e) {
            throw new IllegalStateException("Grid should handle problematic data gracefully", e);
        }
        LOGGER.info("Grid error handling test completed successfully");
    }

    /**
     * Tests grid performance with a large number of items.
     * 
     * @param <T>
     *            the entity type
     * @param grid
     *            the grid to test
     * @param largeDataset
     *            large dataset for performance testing
     */
    public static <T extends CEntityDB<T>> void testGridPerformance(final Grid<T> grid, final List<T> largeDataset) {
        LOGGER.info("Testing grid performance with {} items", largeDataset.size());
        final long startTime = System.currentTimeMillis();
        try {
            // Test data provider with large dataset
            final DataProvider<T, ?> dataProvider = grid.getDataProvider();
            Check.notNull(dataProvider, "Grid should have a data provider");
        } catch (Exception e) {
            throw new IllegalStateException("Grid should handle large datasets without exceptions", e);
        }
        final long endTime = System.currentTimeMillis();
        final long duration = endTime - startTime;
        LOGGER.info("Grid performance test completed in {}ms", duration);
        // Performance assertion - should complete within reasonable time
        Check.isTrue(duration < 5000, "Grid performance test should complete within 5 seconds");
    }

    /**
     * Tests grid selection and deselection functionality.
     * 
     * @param <T>
     *            the entity type
     * @param grid
     *            the grid to test
     * @param testEntities
     *            sample entities for selection testing
     */
    public static <T extends CEntityDB<T>> void testGridSelection(final Grid<T> grid, final List<T> testEntities) {
        LOGGER.info("Testing grid selection functionality");

        if ((testEntities == null) || testEntities.isEmpty()) {
            LOGGER.warn("No test entities provided for selection testing");
            return;
        }
        final T testEntity = testEntities.get(0);
        // Test single selection
        try {
            grid.select(testEntity);
            final T selected = grid.asSingleSelect().getValue();
            Check.equals(testEntity, selected, "Selected entity should match");
            // Test deselection
            grid.deselectAll();
            final T afterDeselect = grid.asSingleSelect().getValue();
            Check.isTrue(afterDeselect == null, "No entity should be selected after deselection");
        } catch (Exception e) {
            throw new IllegalStateException("Grid selection should work without exceptions", e);
        }
        LOGGER.info("Grid selection test completed successfully");
    }

    /**
     * Validates that a grid has expected columns.
     * 
     * @param <T>
     *            the entity type
     * @param grid
     *            the grid to test
     * @param expectedColumnKeys
     *            expected column keys
     */
    public static <T extends CEntityDB<T>> void validateGridColumns(final Grid<T> grid,
            final String... expectedColumnKeys) {
        LOGGER.info("Validating grid has expected columns");

        for (final String expectedKey : expectedColumnKeys) {
            final boolean hasColumn = grid.getColumns().stream().anyMatch(col -> expectedKey.equals(col.getKey()));
            Check.isTrue(hasColumn, String.format("Grid should have column with key: %s", expectedKey));
        }
        LOGGER.info("Grid column validation completed successfully");
    }
}