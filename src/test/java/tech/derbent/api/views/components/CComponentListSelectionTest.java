package tech.derbent.api.views.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import tech.derbent.api.ui.component.enhanced.CComponentListSelection;

/** Unit tests for CComponentListSelection to verify List-based selection functionality without ordering. */

public class CComponentListSelectionTest {

	@Test
	public static void testBinderIntegration_ValueChangeListener() {
		// Test that value change listeners are notified when binder triggers setValue
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		final List<String> allItems = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setSourceItems(allItems);
		// Track value changes
		final int[] changeCount = {
				0
		};
		@SuppressWarnings ({
				"unchecked"
		})
		final List<String>[] lastOldValue = new List[1];
		@SuppressWarnings ({
				"unchecked"
		})
		final List<String>[] lastNewValue = new List[1];
		component.addValueChangeListener(event -> {
			changeCount[0]++;
			lastOldValue[0] = event.getOldValue();
			lastNewValue[0] = event.getValue();
		});
		// First setValue - should trigger listener
		component.setValue(Arrays.asList("Item 1"));
		assertEquals(1, changeCount[0], "Should have one value change");
		assertEquals(0, lastOldValue[0].size(), "Old value should be empty");
		assertEquals(1, lastNewValue[0].size(), "New value should have one item");
		// Second setValue with different value - should trigger listener
		component.setValue(Arrays.asList("Item 1", "Item 2"));
		assertEquals(2, changeCount[0], "Should have two value changes");
		assertEquals(1, lastOldValue[0].size(), "Old value should have one item");
		assertEquals(2, lastNewValue[0].size(), "New value should have two items");
		// Third setValue with same value - should NOT trigger listener (no change)
		component.setValue(Arrays.asList("Item 1", "Item 2"));
		assertEquals(2, changeCount[0], "Should still have two value changes (no change)");
	}

	@Test
	public void testBinderIntegration_SimulateEntitySwitch() {
		// This test simulates how the binder behaves when switching between entities
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		// Setup: Set source items (this would typically be done once by FormBuilder)
		final List<String> allItems = Arrays.asList("Role 1", "Role 2", "Role 3", "Role 4", "Role 5");
		component.setSourceItems(allItems);
		// Simulate binder.setBean(entity1) - entity1 has roles [Role 1, Role 3]
		component.setValue(Arrays.asList("Role 1", "Role 3"));
		assertEquals(2, component.getValue().size());
		assertTrue(component.getValue().contains("Role 1"));
		assertTrue(component.getValue().contains("Role 3"));
		// Simulate binder.setBean(entity2) - entity2 has roles [Role 2, Role 4, Role 5]
		component.setValue(Arrays.asList("Role 2", "Role 4", "Role 5"));
		assertEquals(3, component.getValue().size());
		assertTrue(component.getValue().contains("Role 2"));
		assertTrue(component.getValue().contains("Role 4"));
		assertTrue(component.getValue().contains("Role 5"));
		assertFalse(component.getValue().contains("Role 1"));
		assertFalse(component.getValue().contains("Role 3"));
		// Simulate binder.setBean(entity3) - entity3 has no roles
		component.setValue(Arrays.asList());
		assertEquals(0, component.getValue().size());
		assertTrue(component.isEmpty());
		// Simulate binder.setBean(entity4) - entity4 has all roles
		component.setValue(allItems);
		assertEquals(5, component.getValue().size());
		assertEquals(allItems.size(), component.getValue().size());
	}

	@Test
	public void testClear() {
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		final List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setSourceItems(items);
		component.setValue(Arrays.asList("Item 1", "Item 2"));
		assertFalse(component.isEmpty());
		component.clear();
		assertTrue(component.isEmpty());
		assertEquals(0, component.getValue().size());
	}

	@Test
	public void testComponentInitialization() {
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		assertNotNull(component);
		assertTrue(component.isEmpty());
		assertNotNull(component.getValue());
		assertTrue(component.getValue().isEmpty());
	}

	@Test
	public void testListSeparation() {
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		final List<String> allItems = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
		component.setSourceItems(allItems);
		// Select some items
		final List<String> selected = Arrays.asList("Item 2", "Item 4");
		component.setValue(selected);
		// Verify selected items
		final List<String> selectedResult = component.getValue();
		assertEquals(2, selectedResult.size());
		assertTrue(selectedResult.contains("Item 2"));
		assertTrue(selectedResult.contains("Item 4"));
		// After setValue, the component should have selected items
		assertFalse(component.isEmpty());
	}

	@Test
	public void testReadOnlyMode() {
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		assertFalse(component.isReadOnly());
		component.setReadOnly(true);
		assertTrue(component.isReadOnly());
		component.setReadOnly(false);
		assertFalse(component.isReadOnly());
	}

	@Test
	public void testSetSourceItems() {
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		final List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setSourceItems(items);
		// Initially no items should be selected
		assertTrue(component.isEmpty());
	}

	@Test
	public void testSetValue() {
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		final List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4");
		component.setSourceItems(items);
		// Select items
		final List<String> selectedItems = Arrays.asList("Item 3", "Item 1", "Item 4");
		component.setValue(selectedItems);
		// Verify size
		assertEquals(3, component.getValue().size());
		// Verify all items are selected
		final List<String> result = component.getValue();
		assertTrue(result.contains("Item 3"));
		assertTrue(result.contains("Item 1"));
		assertTrue(result.contains("Item 4"));
		assertFalse(component.isEmpty());
	}

	@Test
	public void testValueChangeWithNullHandling() {
		final CComponentListSelection<Object, String> component = new CComponentListSelection<>(null, null, null, "Items", String.class);
		final List<String> items = Arrays.asList("Item 1", "Item 2");
		component.setSourceItems(items);
		// Set null value should be handled gracefully
		component.setValue(null);
		assertTrue(component.isEmpty());
		assertEquals(0, component.getValue().size());
	}
}
