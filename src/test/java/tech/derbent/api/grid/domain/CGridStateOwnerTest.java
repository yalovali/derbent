package tech.derbent.api.grid.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import elemental.json.JsonObject;
import tech.derbent.api.entity.domain.CEntityDB;

/** Unit tests for CGrid's IStateOwnerComponent implementation.
 * <p>
 * Tests the state management functionality including saving and restoring grid state (selection, scroll position) and collecting state from child
 * components.
 * </p> */
@DisplayName("CGrid State Management Tests")
class CGridStateOwnerTest {

	/** Simple test entity class for testing grid state management. */
	static class TestEntity extends CEntityDB<TestEntity> {

		private static final long serialVersionUID = 1L;
		private String name;

		public TestEntity() {}

		public TestEntity(final Long id, final String name) {
			// Use reflection to set the id since it's protected and has no setter
			try {
				final java.lang.reflect.Field idField = CEntityDB.class.getDeclaredField("id");
				idField.setAccessible(true);
				idField.set(this, id);
			} catch (final Exception e) {
				throw new RuntimeException("Failed to set ID", e);
			}
			this.name = name;
		}

		public String getName() { return name; }

		public void setName(final String name) { this.name = name; }

		@Override
		public String toString() {
			return "TestEntity{id=" + getId() + ", name='" + name + "'}";
		}
	}

	private CGrid<TestEntity> grid;
	private List<TestEntity> testItems;

	@BeforeEach
	void setUp() {
		grid = new CGrid<>(TestEntity.class);

		// Create test data
		testItems = Arrays.asList(new TestEntity(1L, "Item 1"), new TestEntity(2L, "Item 2"), new TestEntity(3L, "Item 3"),
				new TestEntity(4L, "Item 4"), new TestEntity(5L, "Item 5"));
	}

	@Test
	@DisplayName("getStateInformation should return non-null JsonObject")
	void testGetStateInformationReturnsNonNull() {
		// When
		final JsonObject state = grid.getStateInformation();

		// Then
		assertNotNull(state, "State information should not be null");
	}

	@Test
	@DisplayName("getStateInformation should save selected item ID")
	void testGetStateInformationSavesSelectedItem() {
		// Given
		grid.setItems(testItems);
		grid.select(testItems.get(2)); // Select item with ID=3

		// When
		final JsonObject state = grid.getStateInformation();

		// Then
		assertTrue(state.hasKey("selectedItemId"), "State should contain selectedItemId");
		assertEquals(3.0, state.getNumber("selectedItemId"), "Selected item ID should be 3");
	}

	@Test
	@DisplayName("getStateInformation should handle no selection gracefully")
	void testGetStateInformationWithNoSelection() {
		// Given
		grid.setItems(testItems);
		// No selection made

		// When
		final JsonObject state = grid.getStateInformation();

		// Then
		assertNotNull(state, "State should not be null even without selection");
		assertFalse(state.hasKey("selectedItemId"), "State should not contain selectedItemId when nothing is selected");
	}

	@Test
	@DisplayName("restoreStateInformation should restore selected item")
	void testRestoreStateInformationRestoresSelection() {
		// Given
		grid.setItems(testItems);
		grid.select(testItems.get(2)); // Select item with ID=3
		final JsonObject savedState = grid.getStateInformation();

		// When - clear selection and restore
		grid.deselectAll();
		grid.restoreStateInformation(savedState);

		// Then
		final TestEntity selectedItem = grid.asSingleSelect().getValue();
		assertNotNull(selectedItem, "An item should be selected after restore");
		assertEquals(3L, selectedItem.getId(), "Restored selection should be item with ID=3");
	}

	@Test
	@DisplayName("restoreStateInformation should handle null state gracefully")
	void testRestoreStateInformationWithNullState() {
		// Given
		grid.setItems(testItems);

		// When - restore null state (should not throw exception)
		grid.restoreStateInformation(null);

		// Then - no exception should be thrown (test passes if we reach here)
		assertNotNull(grid, "Grid should remain functional after null state restore");
	}

	@Test
	@DisplayName("setItemsWithStatePreservation should preserve selection")
	void testSetItemsWithStatePreservationPreservesSelection() {
		// Given
		grid.setItems(testItems);
		grid.select(testItems.get(2)); // Select item with ID=3

		// Create new list with same items (simulating refresh)
		final List<TestEntity> newItems = Arrays.asList(new TestEntity(1L, "Item 1 Updated"), new TestEntity(2L, "Item 2 Updated"),
				new TestEntity(3L, "Item 3 Updated"), new TestEntity(4L, "Item 4 Updated"), new TestEntity(5L, "Item 5 Updated"));

		// When
		grid.setItemsWithStatePreservation(newItems);

		// Then
		final TestEntity selectedItem = grid.asSingleSelect().getValue();
		assertNotNull(selectedItem, "Selection should be preserved after refresh");
		assertEquals(3L, selectedItem.getId(), "Selected item ID should still be 3");
		assertEquals("Item 3 Updated", selectedItem.getName(), "Selected item should be from new list");
	}

	@Test
	@DisplayName("clearStateInformation should not throw exception")
	void testClearStateInformation() {
		// When - clear state (should not throw exception)
		grid.clearStateInformation();

		// Then - no exception should be thrown (test passes if we reach here)
		assertNotNull(grid, "Grid should remain functional after clearing state");
	}

	@Test
	@DisplayName("State management should work with empty grid")
	void testStateManagementWithEmptyGrid() {
		// Given - empty grid
		grid.setItems(Arrays.asList());

		// When
		final JsonObject state = grid.getStateInformation();
		grid.restoreStateInformation(state);

		// Then - no exception should be thrown
		assertNotNull(state, "State should be created even for empty grid");
		assertFalse(state.hasKey("selectedItemId"), "Empty grid should not have selected item");
	}
}
