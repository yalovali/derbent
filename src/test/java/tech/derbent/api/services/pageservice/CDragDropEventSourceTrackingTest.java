package tech.derbent.api.services.pageservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** Test class for CDragDropEvent drag source tracking and hierarchy methods. Verifies that drag source information and component hierarchy are
 * properly captured and accessible. */
class CDragDropEventSourceTrackingTest {

	private VerticalLayout rootContainer;
	private VerticalLayout middleContainer;
	private VerticalLayout dragSourceComponent;
	private VerticalLayout dropTargetComponent;

	@BeforeEach
	void setUp() {
		// Create component hierarchy: root -> middle -> dragSource
		rootContainer = new VerticalLayout();
		middleContainer = new VerticalLayout();
		dragSourceComponent = new VerticalLayout();
		dropTargetComponent = new VerticalLayout();
		rootContainer.add(middleContainer);
		middleContainer.add(dragSourceComponent);
	}

	/** Test that drag source is accessible in drop events. */
	@Test
	void testDragSourceAccessible() {
		final List<String> draggedItems = Arrays.asList("item1", "item2");
		final CDragDropEvent<String> event = new CDragDropEvent<>(draggedItems, dragSourceComponent, "targetItem", GridDropLocation.ABOVE,
				dropTargetComponent);
		assertEquals(dragSourceComponent, event.getDragSource(), "Drag source should be the source component");
	}

	/** Test that drag source can be null for events without source tracking. */
	@Test
	void testDragSourceCanBeNull() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, null, "targetItem", GridDropLocation.ABOVE, dropTargetComponent);
		assertNull(event.getDragSource(), "Drag source should be null when not tracked");
	}

	/** Test that dragged items are accessible in drop events. */
	@Test
	void testDraggedItemsAccessible() {
		final List<String> draggedItems = Arrays.asList("item1", "item2", "item3");
		final CDragDropEvent<String> event = new CDragDropEvent<>(draggedItems, dragSourceComponent, "targetItem", GridDropLocation.ABOVE,
				dropTargetComponent);
		assertEquals(3, event.getDraggedItems().size(), "Should have 3 dragged items");
		assertEquals("item1", event.getDraggedItems().get(0), "First item should be 'item1'");
	}

	/** Test that dragged items can be null. */
	@Test
	void testDraggedItemsCanBeNull() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, dragSourceComponent, "targetItem", GridDropLocation.ABOVE,
				dropTargetComponent);
		assertNull(event.getDraggedItems(), "Dragged items should be null when not provided");
	}

	/** Test getDraggedItem() convenience method returns first item. */
	@Test
	void testGetDraggedItemReturnsFirstItem() {
		final List<String> draggedItems = Arrays.asList("item1", "item2", "item3");
		final CDragDropEvent<String> event = new CDragDropEvent<>(draggedItems, dragSourceComponent);
		assertEquals("item1", event.getDraggedItem(), "Should return first item");
	}

	/** Test getDraggedItem() returns null for empty list. */
	@Test
	void testGetDraggedItemReturnsNullForEmptyList() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(new ArrayList<>(), dragSourceComponent);
		assertNull(event.getDraggedItem(), "Should return null for empty list");
	}

	/** Test getDraggedItem() returns null when items are null. */
	@Test
	void testGetDraggedItemReturnsNullWhenItemsNull() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, dragSourceComponent);
		assertNull(event.getDraggedItem(), "Should return null when items are null");
	}

	/** Test that component hierarchy is properly extracted from drag source. */
	@Test
	void testGetDragSourceHierarchy() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, dragSourceComponent, null, GridDropLocation.ABOVE, dropTargetComponent);
		final List<Component> hierarchy = event.getDragSourceHierarchy();
		assertNotNull(hierarchy, "Hierarchy should not be null");
		assertEquals(3, hierarchy.size(), "Hierarchy should contain 3 components (source, middle, root)");
		assertEquals(dragSourceComponent, hierarchy.get(0), "First component should be drag source");
		assertEquals(middleContainer, hierarchy.get(1), "Second component should be middle container");
		assertEquals(rootContainer, hierarchy.get(2), "Third component should be root container");
	}

	/** Test that getDragSourceHierarchy returns empty list when drag source is null. */
	@Test
	void testGetDragSourceHierarchyWithNullSource() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, null, null, GridDropLocation.ABOVE, dropTargetComponent);
		final List<Component> hierarchy = event.getDragSourceHierarchy();
		assertNotNull(hierarchy, "Hierarchy should not be null");
		assertTrue(hierarchy.isEmpty(), "Hierarchy should be empty when drag source is null");
	}

	/** Test that getDragSourceHierarchy returns empty list when drag source is not a Component. */
	@Test
	void testGetDragSourceHierarchyWithNonComponentSource() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, "NotAComponent", null, GridDropLocation.ABOVE, dropTargetComponent);
		final List<Component> hierarchy = event.getDragSourceHierarchy();
		assertNotNull(hierarchy, "Hierarchy should not be null");
		assertTrue(hierarchy.isEmpty(), "Hierarchy should be empty when drag source is not a Component");
	}

	/** Test that hierarchy correctly handles component with no parent. */
	@Test
	void testGetDragSourceHierarchyWithNoParent() {
		final VerticalLayout standaloneComponent = new VerticalLayout();
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, standaloneComponent, null, GridDropLocation.ABOVE, dropTargetComponent);
		final List<Component> hierarchy = event.getDragSourceHierarchy();
		assertNotNull(hierarchy, "Hierarchy should not be null");
		assertEquals(1, hierarchy.size(), "Hierarchy should contain only the component itself");
		assertEquals(standaloneComponent, hierarchy.get(0), "Component should be the only item");
	}

	/** Test isDropEvent() returns true for drop events. */
	@Test
	void testIsDropEvent() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, dragSourceComponent, "targetItem", GridDropLocation.ABOVE,
				dropTargetComponent);
		assertTrue(event.isDropEvent(), "Should be identified as drop event");
	}

	/** Test isDragStartEvent() returns true for drag start events. */
	@Test
	void testIsDragStartEvent() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(Arrays.asList("item1"), dragSourceComponent);
		assertTrue(event.isDragStartEvent(), "Should be identified as drag start event");
	}

	/** Test that drop target is accessible. */
	@Test
	void testDropTargetAccessible() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, dragSourceComponent, "targetItem", GridDropLocation.ABOVE,
				dropTargetComponent);
		assertEquals(dropTargetComponent, event.getDropTarget(), "Drop target should be accessible");
	}

	/** Test that target item is accessible. */
	@Test
	void testTargetItemAccessible() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, dragSourceComponent, "targetItem", GridDropLocation.ABOVE,
				dropTargetComponent);
		assertEquals("targetItem", event.getTargetItem(), "Target item should be accessible");
	}

	/** Test that drop location is accessible. */
	@Test
	void testDropLocationAccessible() {
		final CDragDropEvent<String> event = new CDragDropEvent<>(null, dragSourceComponent, "targetItem", GridDropLocation.ABOVE,
				dropTargetComponent);
		assertEquals(GridDropLocation.ABOVE, event.getDropLocation(), "Drop location should be ABOVE");
	}
}
