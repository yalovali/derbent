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
import com.vaadin.flow.component.html.Div;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IStateOwnerComponent;

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
	
	/** Test widget that implements IStateOwnerComponent for testing state collection. */
	static class TestWidget extends Div implements IStateOwnerComponent {
		
		private static final long serialVersionUID = 1L;
		private boolean expanded = false;
		private String customValue = "";
		private final TestEntity entity;
		
		public TestWidget(final TestEntity entity) {
			this.entity = entity;
			setText("Widget for " + entity.getName());
		}
		
		public void setExpanded(final boolean expanded) {
			this.expanded = expanded;
		}
		
		public boolean isExpanded() {
			return expanded;
		}
		
		public void setCustomValue(final String value) {
			this.customValue = value;
		}
		
		public String getCustomValue() {
			return customValue;
		}
		
		@Override
		public JsonObject getStateInformation() {
			final JsonObject state = Json.createObject();
			state.put("expanded", expanded);
			state.put("customValue", customValue);
			state.put("widgetEntityId", entity.getId().doubleValue());
			return state;
		}
		
		@Override
		public void restoreStateInformation(final JsonObject state) {
			if (state == null) return;
			
			if (state.hasKey("expanded")) {
				expanded = state.getBoolean("expanded");
			}
			if (state.hasKey("customValue")) {
				customValue = state.getString("customValue");
			}
		}
		
		@Override
		public void clearStateInformation() {
			expanded = false;
			customValue = "";
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
	@DisplayName("Manual state save and restore should preserve selection")
	void testManualStateSaveRestorePreservesSelection() {
		// Given
		grid.setItems(testItems);
		grid.select(testItems.get(2)); // Select item with ID=3

		// Create new list with same items (simulating refresh)
		final List<TestEntity> newItems = Arrays.asList(new TestEntity(1L, "Item 1 Updated"), new TestEntity(2L, "Item 2 Updated"),
				new TestEntity(3L, "Item 3 Updated"), new TestEntity(4L, "Item 4 Updated"), new TestEntity(5L, "Item 5 Updated"));

		// When - explicit pattern: save, change, restore
		final JsonObject savedState = grid.getStateInformation();
		grid.setItems(newItems);
		grid.restoreStateInformation(savedState);

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
	
	@Test
	@DisplayName("getStateInformation should collect state from widget columns")
	void testGetStateInformationCollectsWidgetStates() {
		// Given - grid with widget column that implements IStateOwnerComponent
		grid.setItems(testItems);
		
		// Add a widget column
		grid.addWidgetColumn(entity -> new TestWidget(entity));
		
		// When - get state
		final JsonObject state = grid.getStateInformation();
		
		// Then - should have childStates
		assertTrue(state.hasKey("childStates"), "State should contain childStates");
		final JsonArray childStates = state.getArray("childStates");
		assertEquals(testItems.size(), childStates.length(), 
				"Should have collected state from all widget rows");
		
		// Verify first child state has required metadata
		final JsonObject firstChildState = childStates.getObject(0);
		assertTrue(firstChildState.hasKey("rowIndex"), "Child state should have rowIndex");
		assertTrue(firstChildState.hasKey("itemId"), "Child state should have itemId");
		assertTrue(firstChildState.hasKey("columnKey"), "Child state should have columnKey");
	}
	
	@Test
	@DisplayName("getStateInformation should not collect state from non-IStateOwnerComponent widgets")
	void testGetStateInformationSkipsNonStateOwnerWidgets() {
		// Given - grid with regular widget column (not IStateOwnerComponent)
		grid.setItems(testItems);
		
		// Add a regular widget column that doesn't implement IStateOwnerComponent
		grid.addWidgetColumn(entity -> {
			final Div div = new Div();
			div.setText("Non-state widget for " + entity.getName());
			return div;
		});
		
		// When - get state
		final JsonObject state = grid.getStateInformation();
		
		// Then - should not have childStates (or empty array)
		if (state.hasKey("childStates")) {
			final JsonArray childStates = state.getArray("childStates");
			assertEquals(0, childStates.length(), 
					"Should not collect state from non-IStateOwnerComponent widgets");
		}
	}
	
	@Test
	@DisplayName("Widget state should be preserved across grid refresh")
	void testWidgetStatePreservationAcrossRefresh() {
		// Given - grid with widget column
		grid.setItems(testItems);
		
		// Track widget instances to verify state
		final java.util.Map<Long, TestWidget> widgetTracker = new java.util.HashMap<>();
		
		// Add widget column with state tracking
		grid.addWidgetColumn(entity -> {
			final TestWidget widget = new TestWidget(entity);
			// Simulate user interaction - expand second item's widget
			if (entity.getId().equals(2L)) {
				widget.setExpanded(true);
				widget.setCustomValue("custom-value-2");
			}
			widgetTracker.put(entity.getId(), widget);
			return widget;
		});
		
		// Get initial state
		final JsonObject state = grid.getStateInformation();
		
		// Verify widget state was collected
		assertTrue(state.hasKey("childStates"), "State should contain childStates");
		final JsonArray childStates = state.getArray("childStates");
		
		// Find the state for entity ID 2
		JsonObject entityTwoState = null;
		for (int i = 0; i < childStates.length(); i++) {
			final JsonObject childState = childStates.getObject(i);
			if (childState.hasKey("widgetEntityId") && 
					(long) childState.getNumber("widgetEntityId") == 2L) {
				entityTwoState = childState;
				break;
			}
		}
		
		assertNotNull(entityTwoState, "Should have collected state for entity ID 2");
		assertTrue(entityTwoState.getBoolean("expanded"), "Widget should be expanded");
		assertEquals("custom-value-2", entityTwoState.getString("customValue"), 
				"Widget should have custom value");
	}
}
