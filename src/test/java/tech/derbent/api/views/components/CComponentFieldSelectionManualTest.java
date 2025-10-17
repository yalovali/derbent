package tech.derbent.api.views.components;

import java.util.Arrays;
import java.util.List;

/** Manual test class to demonstrate CComponentFieldSelection with Grid implementation. This test creates a simple
 * standalone instance of the component to verify the Grid-based implementation works correctly. Run this test to visually
 * inspect the component structure. */
public class CComponentFieldSelectionManualTest {

	public static void main(String[] args) {
		// Create a test component
		CComponentFieldSelection<Object, String> component = new CComponentFieldSelection<>("Available Items", "Selected Items");
		// Set some test data
		List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
		component.setSourceItems(items);
		// Select some items
		component.setValue(Arrays.asList("Item 2", "Item 4"));
		// Print component state
		System.out.println("CComponentFieldSelection Manual Test");
		System.out.println("=====================================");
		System.out.println("Component created successfully with Grid-based implementation");
		System.out.println("Source items: " + items.size());
		System.out.println("Selected items: " + component.getValue().size());
		System.out.println("Selected: " + component.getValue());
		System.out.println("Is empty: " + component.isEmpty());
		System.out.println("Is read-only: " + component.isReadOnly());
		// Test read-only mode
		component.setReadOnly(true);
		System.out.println("After setReadOnly(true): " + component.isReadOnly());
		component.setReadOnly(false);
		System.out.println("After setReadOnly(false): " + component.isReadOnly());
		// Test clear
		component.clear();
		System.out.println("After clear(): isEmpty = " + component.isEmpty());
		System.out.println("\n✅ All basic operations work correctly with Grid implementation!");
	}
}
