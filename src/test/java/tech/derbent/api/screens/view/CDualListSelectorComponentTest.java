package tech.derbent.api.screens.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tech.derbent.api.screens.view.CDualListSelectorComponent;

/** Unit tests for CDualListSelectorComponent to verify basic functionality. */
public class CDualListSelectorComponentTest {

	@Test
	public void testClear() {
		CDualListSelectorComponent<String> component = new CDualListSelectorComponent<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setItems(items);
		component.setValue(Set.of("Item 1", "Item 2"));
		assertFalse(component.isEmpty());
		component.clear();
		assertTrue(component.isEmpty());
		assertEquals(0, component.getValue().size());
	}

	@Test
	public void testComponentInitialization() {
		CDualListSelectorComponent<String> component = new CDualListSelectorComponent<>("Available", "Selected");
		assertNotNull(component);
		assertTrue(component.isEmpty());
		assertNotNull(component.getValue());
		assertTrue(component.getValue().isEmpty());
	}

	@Test
	public void testGetSelectedItems() {
		CDualListSelectorComponent<String> component = new CDualListSelectorComponent<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setItems(items);
		component.setValue(Set.of("Item 2", "Item 1")); // Order may vary in Set
		List<String> selectedItems = component.getSelectedItems();
		assertEquals(2, selectedItems.size());
		assertTrue(selectedItems.contains("Item 1"));
		assertTrue(selectedItems.contains("Item 2"));
	}

	@Test
	public void testReadOnlyMode() {
		CDualListSelectorComponent<String> component = new CDualListSelectorComponent<>("Available", "Selected");
		assertFalse(component.isReadOnly());
		component.setReadOnly(true);
		assertTrue(component.isReadOnly());
		component.setReadOnly(false);
		assertFalse(component.isReadOnly());
	}

	@Test
	public void testSetItems() {
		CDualListSelectorComponent<String> component = new CDualListSelectorComponent<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setItems(items);
		// Initially no items should be selected
		assertTrue(component.isEmpty());
	}

	@Test
	public void testSetValue() {
		CDualListSelectorComponent<String> component = new CDualListSelectorComponent<>("Available", "Selected");
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
		component.setItems(items);
		// Select some items
		Set<String> selectedItems = Set.of("Item 1", "Item 3");
		component.setValue(selectedItems);
		assertEquals(2, component.getValue().size());
		assertTrue(component.getValue().contains("Item 1"));
		assertTrue(component.getValue().contains("Item 3"));
		assertFalse(component.isEmpty());
	}
}
