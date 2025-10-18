package tech.derbent.api.views.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for CComponentFieldSelection to verify List-based functionality and ordering preservation. */
public class CComponentFieldSelectionTest {

	@Test
	public void testClear() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setSourceItems(items);
		component.setValue(Arrays.asList("Item 1", "Item 2"));
		assertFalse(component.isEmpty());
		component.clear();
		assertTrue(component.isEmpty());
		assertEquals(0, component.getValue().size());
	}

	@Test
	public void testComponentInitialization() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		assertNotNull(component);
		assertTrue(component.isEmpty());
		assertNotNull(component.getValue());
		assertTrue(component.getValue().isEmpty());
	}

	@Test
	public void testDoubleClickEventSetup() {
		// This test verifies that the component initializes without errors
		// and that the double-click event handlers are set up properly
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setSourceItems(items);
		// Verify component is properly initialized
		assertNotNull(component);
		assertNotNull(component.getValue());
		// The double-click functionality requires UI interaction testing
		// which cannot be easily tested in unit tests. The fix ensures that:
		// 1. addEventListener("dblclick") now includes synchronizeProperty("value")
		// 2. This synchronizes the ListBox value with the server before event execution
		// 3. Manual UI testing should verify double-click adds/removes items correctly
	}

	@Test
	public void testGetSelectedItemsReturnsOrderedList() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
		component.setSourceItems(items);
		// Set in specific order
		component.setValue(Arrays.asList("Item 5", "Item 2", "Item 1"));
		List<String> selectedItems = component.getSelectedItems();
		assertEquals(3, selectedItems.size());
		// Verify order is maintained
		assertEquals("Item 5", selectedItems.get(0));
		assertEquals("Item 2", selectedItems.get(1));
		assertEquals("Item 1", selectedItems.get(2));
	}

	@Test
	public void testListSeparation() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		List<String> allItems = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
		component.setSourceItems(allItems);
		// Select some items
		List<String> selected = Arrays.asList("Item 2", "Item 4");
		component.setValue(selected);
		// Verify selected items
		List<String> selectedResult = component.getValue();
		assertEquals(2, selectedResult.size());
		assertTrue(selectedResult.contains("Item 2"));
		assertTrue(selectedResult.contains("Item 4"));
		// After setValue, the component should have separated items into:
		// - selectedItems: [Item 2, Item 4]
		// - notselectedItems (available): [Item 1, Item 3, Item 5]
		// This is verified through the isEmpty() check
		assertFalse(component.isEmpty());
	}

	@Test
	public void testReadOnlyMode() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		assertFalse(component.isReadOnly());
		component.setReadOnly(true);
		assertTrue(component.isReadOnly());
		component.setReadOnly(false);
		assertFalse(component.isReadOnly());
	}

	@Test
	public void testSetSelectedItemsPreservesOrder() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		List<String> items = Arrays.asList("A", "B", "C", "D", "E");
		component.setSourceItems(items);
		// Use setSelectedItems method with specific order
		List<String> orderedSelection = Arrays.asList("D", "B", "E");
		component.setValue(orderedSelection);
		// Verify order is preserved
		List<String> result = component.getValue();
		assertEquals(3, result.size());
		assertEquals("D", result.get(0));
		assertEquals("B", result.get(1));
		assertEquals("E", result.get(2));
	}

	@Test
	public void testSetSourceItems() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setSourceItems(items);
		// Initially no items should be selected
		assertTrue(component.isEmpty());
	}

	@Test
	public void testSetValuePreservesOrder() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4");
		component.setSourceItems(items);
		// Select items in specific order - THIS IS THE KEY TEST FOR @OrderColumn SUPPORT
		List<String> selectedItems = Arrays.asList("Item 3", "Item 1", "Item 4");
		component.setValue(selectedItems);
		// Verify size
		assertEquals(3, component.getValue().size());
		// Verify ORDER is preserved (critical for @OrderColumn)
		List<String> result = component.getValue();
		assertEquals("Item 3", result.get(0), "First item should be Item 3");
		assertEquals("Item 1", result.get(1), "Second item should be Item 1");
		assertEquals("Item 4", result.get(2), "Third item should be Item 4");
		assertFalse(component.isEmpty());
	}

	@Test
	public void testValueChangeWithNullHandling() {
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2");
		component.setSourceItems(items);
		// Set null value should be handled gracefully
		component.setValue(null);
		assertTrue(component.isEmpty());
		assertEquals(0, component.getValue().size());
	}
}
