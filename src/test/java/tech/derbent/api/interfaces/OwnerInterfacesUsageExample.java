package tech.derbent.api.interfaces;

import java.util.Set;
import org.junit.jupiter.api.Test;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.ui.component.enhanced.CComponentBacklog;
import tech.derbent.app.sprints.domain.CSprint;

/** Example usage and tests for the new owner notification interfaces.
 * <p>
 * This class demonstrates how to use ISelectionOwner, IDragOwner, and IDropOwner interfaces to receive notifications from child components. */
public class OwnerInterfacesUsageExample {

	/** Example of a parent component that implements all owner interfaces. */
	public static class CPageSprintView implements ISelectionOwner<CProjectItem<?>>, IDragOwner<CProjectItem<?>>, IDropOwner<CProjectItem<?>> {

		private CComponentBacklog backlogComponent;
		private int selectionChangeCount = 0;
		private int dragStartCount = 0;
		private int dropCompleteCount = 0;

		public CPageSprintView(final CSprint sprint) {
			// Create backlog component
			backlogComponent = new CComponentBacklog(sprint);

			// Register this page as the owner for all events
			backlogComponent.setSelectionOwner(this);
			backlogComponent.setDragOwner(this);
			backlogComponent.setDropOwner(this);
		}

		@Override
		public void onSelectionChanged(final Set<CProjectItem<?>> selectedItems) {
			// Handle selection change - update UI, enable/disable buttons, etc.
			selectionChangeCount++;
			System.out.println("Selection changed: " + selectedItems.size() + " items selected");

			// Example: Enable/disable toolbar buttons based on selection
			final boolean hasSelection = !selectedItems.isEmpty();
			// updateToolbarButtons(hasSelection);
		}

		@Override
		public void onDragStart(final Set<CProjectItem<?>> draggedItems) {
			// Handle drag start - prepare drop targets, update UI state, etc.
			dragStartCount++;
			System.out.println("Drag started with " + draggedItems.size() + " items");

			// Example: Enable drop targets when drag starts
			// enableDropTargets();
			// highlightValidDropZones();
		}

		@Override
		public void onDropComplete(final Set<CProjectItem<?>> droppedItems, final Object targetComponent) {
			// Handle drop completion - update related components, refresh data, etc.
			dropCompleteCount++;
			System.out.println("Drop completed: " + droppedItems.size() + " items on " + (targetComponent != null ? targetComponent.getClass().getSimpleName() : "null"));

			// Example: Refresh related components after drop
			// refreshRelatedComponents();
			// updateStatistics();
		}

		// Getters for testing
		public int getSelectionChangeCount() { return selectionChangeCount; }

		public int getDragStartCount() { return dragStartCount; }

		public int getDropCompleteCount() { return dropCompleteCount; }
	}

	/** Example of using individual owner interfaces selectively. */
	public static class CPageActivityView implements ISelectionOwner<CProjectItem<?>> {

		private CComponentBacklog backlogComponent;

		public CPageActivityView(final CSprint sprint) {
			backlogComponent = new CComponentBacklog(sprint);

			// Only register for selection events (not drag/drop)
			backlogComponent.setSelectionOwner(this);
		}

		@Override
		public void onSelectionChanged(final Set<CProjectItem<?>> selectedItems) {
			System.out.println("Activity view - selection changed: " + selectedItems.size() + " items");
			// Custom handling for activity view
		}
	}

	@Test
	public void demonstrateOwnerInterfaceUsage() {
		// This test demonstrates the usage pattern but won't execute actual UI operations
		// In a real scenario, you would:
		// 1. Create a parent component/view that implements one or more owner interfaces
		// 2. Create child components (like CComponentBacklog)
		// 3. Set the parent as the owner using setXxxOwner methods
		// 4. When user interacts with child component (select, drag, drop), parent gets notified
		// 5. Parent can react by updating UI, coordinating between components, etc.

		System.out.println("Owner Interfaces Usage Pattern:");
		System.out.println("1. Parent implements ISelectionOwner, IDragOwner, IDropOwner");
		System.out.println("2. Parent creates child components");
		System.out.println("3. Parent registers itself: component.setSelectionOwner(this)");
		System.out.println("4. Child notifies parent on events: notifySelectionOwner()");
		System.out.println("5. Parent receives callbacks and reacts accordingly");
	}

	@Test
	public void demonstrateClickNotifierUsage() {
		// ClickNotifier is already provided by Vaadin for all container components
		// CDiv, CVerticalLayout, CHorizontalLayout, etc. inherit it from their base classes

		System.out.println("ClickNotifier Usage (already available):");
		System.out.println("1. All C-prefixed layout components inherit ClickNotifier from Vaadin");
		System.out.println("2. Use addClickListener on any layout component:");
		System.out.println("   layout.addClickListener(e -> handleClick());");
		System.out.println("3. No need to implement ClickNotifier - it's already there!");
	}
}
