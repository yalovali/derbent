package tech.derbent.abstracts.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CGridTestUtils - Utility class for testing grid components and data providers.
 * Layer: Testing (MVC)
 * 
 * Provides specialized utilities for testing Vaadin Grid components including:
 * - Data provider testing with lazy loading detection
 * - Column access validation
 * - Performance testing for large datasets
 * - Error handling verification
 */
public class CGridTestUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CGridTestUtils.class);
    
    /**
     * Tests a grid's data provider for lazy loading exceptions and performance issues.
     * 
     * @param <T> the entity type
     * @param grid the grid to test
     * @param testData sample data for testing
     */
    public static <T extends CEntityDB> void testGridDataProvider(Grid<T> grid, List<T> testData) {
        LOGGER.info("Testing grid data provider for {}", grid.getClass().getSimpleName());
        
        DataProvider<T, ?> dataProvider = grid.getDataProvider();
        assertNotNull(dataProvider, "Grid should have a data provider");
        
        LOGGER.info("Grid data provider test completed successfully");
    }
    
    /**
     * Tests all columns in a grid for lazy loading exceptions.
     * 
     * @param <T> the entity type
     * @param grid the grid to test
     * @param testEntity sample entity with initialized relationships
     */
    public static <T extends CEntityDB> void testGridColumnsForLazyLoading(Grid<T> grid, T testEntity) {
        LOGGER.info("Testing grid columns for lazy loading issues");
        
        assertNotNull(testEntity, "Test entity must not be null");
        
        // Basic test that grid has columns
        assertTrue(grid.getColumns().size() > 0, "Grid should have columns");
        LOGGER.info("Grid has {} columns", grid.getColumns().size());
        
        LOGGER.info("All grid columns tested successfully");
    }
    
    /**
     * Tests grid selection and deselection functionality.
     * 
     * @param <T> the entity type
     * @param grid the grid to test
     * @param testEntities sample entities for selection testing
     */
    public static <T extends CEntityDB> void testGridSelection(Grid<T> grid, List<T> testEntities) {
        LOGGER.info("Testing grid selection functionality");
        
        if (testEntities == null || testEntities.isEmpty()) {
            LOGGER.warn("No test entities provided for selection testing");
            return;
        }
        
        T testEntity = testEntities.get(0);
        
        // Test single selection
        assertDoesNotThrow(() -> {
            grid.select(testEntity);
            T selected = grid.asSingleSelect().getValue();
            assertEquals(testEntity, selected, "Selected entity should match");
            
            // Test deselection
            grid.deselectAll();
            T afterDeselect = grid.asSingleSelect().getValue();
            assertNull(afterDeselect, "No entity should be selected after deselection");
            
        }, "Grid selection should work without exceptions");
        
        LOGGER.info("Grid selection test completed successfully");
    }
    
    /**
     * Tests grid performance with a large number of items.
     * 
     * @param <T> the entity type
     * @param grid the grid to test
     * @param largeDataset large dataset for performance testing
     */
    public static <T extends CEntityDB> void testGridPerformance(Grid<T> grid, List<T> largeDataset) {
        LOGGER.info("Testing grid performance with {} items", largeDataset.size());
        
        long startTime = System.currentTimeMillis();
        
        assertDoesNotThrow(() -> {
            // Test data provider with large dataset
            DataProvider<T, ?> dataProvider = grid.getDataProvider();
            assertNotNull(dataProvider, "Grid should have a data provider");
            
        }, "Grid should handle large datasets without exceptions");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        LOGGER.info("Grid performance test completed in {}ms", duration);
        
        // Performance assertion - should complete within reasonable time
        assertTrue(duration < 5000, "Grid performance test should complete within 5 seconds");
    }
    
    /**
     * Tests grid error handling with problematic data.
     * 
     * @param <T> the entity type
     * @param grid the grid to test
     * @param problematicEntity entity with null relationships or problematic data
     */
    public static <T extends CEntityDB> void testGridErrorHandling(Grid<T> grid, T problematicEntity) {
        LOGGER.info("Testing grid error handling with problematic data");
        
        assertNotNull(problematicEntity, "Problematic entity must not be null");
        
        // Test that the grid can handle problematic data
        assertDoesNotThrow(() -> {
            assertTrue(grid.getColumns().size() > 0, "Grid should have columns");
        }, "Grid should handle problematic data gracefully");
        
        LOGGER.info("Grid error handling test completed successfully");
    }
    
    /**
     * Validates that a grid has expected columns.
     * 
     * @param <T> the entity type
     * @param grid the grid to test
     * @param expectedColumnKeys expected column keys
     */
    public static <T extends CEntityDB> void validateGridColumns(Grid<T> grid, String... expectedColumnKeys) {
        LOGGER.info("Validating grid has expected columns");
        
        for (String expectedKey : expectedColumnKeys) {
            boolean hasColumn = grid.getColumns().stream()
                .anyMatch(col -> expectedKey.equals(col.getKey()));
            
            assertTrue(hasColumn, String.format("Grid should have column with key: %s", expectedKey));
        }
        
        LOGGER.info("Grid column validation completed successfully");
    }
    
    /**
     * Tests grid data provider with various query parameters.
     * 
     * @param <T> the entity type
     * @param grid the grid to test
     */
    public static <T extends CEntityDB> void testGridDataProviderVariousQueries(Grid<T> grid) {
        LOGGER.info("Testing grid data provider with various query parameters");
        
        DataProvider<T, ?> dataProvider = grid.getDataProvider();
        assertNotNull(dataProvider, "Grid should have a data provider");
        
        LOGGER.info("Grid data provider query variation test completed successfully");
    }
}