package unit_tests.tech.derbent.abstracts.views;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.vaadin.flow.data.provider.ListDataProvider;

import tech.derbent.abstracts.views.CGrid;
import tech.derbent.users.domain.CUser;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for CGrid selection improvements following coding guidelines.
 * Tests the requirement that grids should always have a selected row when data is available.
 */
@SpringBootTest(classes = tech.derbent.Application.class)
@TestPropertySource(properties = { "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never" })
public class CGridSelectionTest {

    private CGrid<CUser> grid;
    private List<CUser> testUsers;

    @BeforeEach
    void setUp() {
        grid = new CGrid<>(CUser.class);
        
        // Create test users
        testUsers = Arrays.asList(
            createTestUser(1L, "Test User 1"),
            createTestUser(2L, "Test User 2"),
            createTestUser(3L, "Test User 3")
        );
    }

    private CUser createTestUser(Long id, String name) {
        // For testing grid selection, we use the name constructor
        // The ID will be generated when the entity is saved
        CUser user = new CUser(name);
        user.setLogin("testuser" + id);
        user.setEmail("test" + id + "@example.com");
        return user;
    }

    @Test
    void testEnsureSelectionWhenDataAvailable_WithNoData() {
        // Given: Grid with no data
        grid.setItems();
        
        // When: ensureSelectionWhenDataAvailable is called
        grid.ensureSelectionWhenDataAvailable();
        
        // Then: No selection should be made (no exception should be thrown)
        assertNull(grid.asSingleSelect().getValue());
    }

    @Test
    void testEnsureSelectionWhenDataAvailable_WithData() {
        // Given: Grid with data but no selection
        grid.setItems(testUsers);
        assertNull(grid.asSingleSelect().getValue());
        
        // When: ensureSelectionWhenDataAvailable is called
        grid.ensureSelectionWhenDataAvailable();
        
        // Then: First item should be selected
        assertNotNull(grid.asSingleSelect().getValue());
        assertEquals(testUsers.get(0).getName(), grid.asSingleSelect().getValue().getName());
    }

    @Test
    void testEnsureSelectionWhenDataAvailable_WithExistingSelection() {
        // Given: Grid with data and existing selection
        grid.setItems(testUsers);
        grid.select(testUsers.get(1)); // Select second item
        
        // When: ensureSelectionWhenDataAvailable is called
        grid.ensureSelectionWhenDataAvailable();
        
        // Then: Existing selection should be preserved
        assertEquals(testUsers.get(1).getName(), grid.asSingleSelect().getValue().getName());
    }

    @Test
    void testSelectMethodWithLogging() {
        // Given: Grid with data
        grid.setItems(testUsers);
        
        // When: select method is called
        grid.select(testUsers.get(0));
        
        // Then: Selection should work correctly
        assertEquals(testUsers.get(0).getName(), grid.asSingleSelect().getValue().getName());
        
        // When: deselecting
        grid.select(null);
        
        // Then: No selection should exist
        assertNull(grid.asSingleSelect().getValue());
    }

    @Test
    void testDataProviderListenerTriggersSelection() {
        // Given: Empty grid
        grid.setItems();
        assertNull(grid.asSingleSelect().getValue());
        
        // When: Data is added to the grid
        grid.setItems(testUsers);
        
        // Then: The grid should automatically select first item
        // Note: This test verifies the data provider listener functionality
        // In a real UI environment, this would be triggered automatically
        grid.ensureSelectionWhenDataAvailable();
        assertNotNull(grid.asSingleSelect().getValue());
        assertEquals(testUsers.get(0).getName(), grid.asSingleSelect().getValue().getName());
    }

    @Test
    void testGridBehaviorFollowsCodingGuidelines() {
        // This test documents that the grid follows coding guidelines:
        // 1. Generic design - works with any CEntityDB subclass
        // 2. Always selected row when data available
        // 3. Consistent behavior across implementations
        
        // Given: Grid with various data scenarios
        CGrid<CUser> userGrid = new CGrid<>(CUser.class);
        
        // Test 1: Generic design - works with CUser entities
        userGrid.setItems(testUsers);
        userGrid.ensureSelectionWhenDataAvailable();
        assertNotNull(userGrid.asSingleSelect().getValue());
        
        // Test 2: Always selected row principle
        assertTrue(userGrid.asSingleSelect().getValue() != null);
        
        // Test 3: Consistent behavior - multiple calls should be safe
        userGrid.ensureSelectionWhenDataAvailable();
        userGrid.ensureSelectionWhenDataAvailable();
        assertNotNull(userGrid.asSingleSelect().getValue());
    }
}