package tech.derbent.api.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CDropEvent;

/** Test class to verify that the infinite loop issue in setupChildDragDropForwarding is fixed.
 * <p>
 * This test ensures that:
 * <ol>
 * <li>setupChildDragDropForwarding() properly accepts a child parameter</li>
 * <li>Events are forwarded from child to parent without infinite loops</li>
 * <li>Event notification only happens once per event</li>
 * </ol>
 */
class IHasDragControlInfiniteLoopTest {

	/** Mock child component that implements IHasDragControl for testing. */
	private static class MockChildComponent implements IHasDragControl {

		private boolean dragEnabled = false;
		private final List<ComponentEventListener<CDragEndEvent>> dragEndListeners = new ArrayList<>();
		private final List<ComponentEventListener<CDragStartEvent<?>>> dragStartListeners = new ArrayList<>();
		private boolean dropEnabled = false;
		private final List<ComponentEventListener<CDropEvent<?>>> dropListeners = new ArrayList<>();

		@Override
		public List<ComponentEventListener<CDragEndEvent>> getDragEndListeners() { return dragEndListeners; }

		@Override
		public List<ComponentEventListener<CDragStartEvent<?>>> getDragStartListeners() { return dragStartListeners; }

		@Override
		public List<ComponentEventListener<CDropEvent<?>>> getDropListeners() { return dropListeners; }

		@Override
		public boolean isDragEnabled() { return dragEnabled; }

		@Override
		public boolean isDropEnabled() { return dropEnabled; }

		@Override
		public void setDragEnabled(final boolean enabled) { this.dragEnabled = enabled; }

		@Override
		public void setDropEnabled(final boolean enabled) { this.dropEnabled = enabled; }
	}

	/** Mock parent component that implements IHasDragControl for testing. */
	private static class MockParentComponent implements IHasDragControl {

		private boolean dragEnabled = false;
		private final List<ComponentEventListener<CDragEndEvent>> dragEndListeners = new ArrayList<>();
		private final List<ComponentEventListener<CDragStartEvent<?>>> dragStartListeners = new ArrayList<>();
		private boolean dropEnabled = false;
		private final List<ComponentEventListener<CDropEvent<?>>> dropListeners = new ArrayList<>();
		private int notifyCallCount = 0;

		@Override
		public List<ComponentEventListener<CDragEndEvent>> getDragEndListeners() { return dragEndListeners; }

		@Override
		public List<ComponentEventListener<CDragStartEvent<?>>> getDragStartListeners() { return dragStartListeners; }

		@Override
		public List<ComponentEventListener<CDropEvent<?>>> getDropListeners() { return dropListeners; }

		public int getNotifyCallCount() { return notifyCallCount; }

		@Override
		public boolean isDragEnabled() { return dragEnabled; }

		@Override
		public boolean isDropEnabled() { return dropEnabled; }

		@Override
		public void notifyDragEndListeners(final CDragEndEvent event) {
			notifyCallCount++;
			IHasDragControl.super.notifyDragEndListeners(event);
		}

		@Override
		public void notifyDragStartListeners(final CDragStartEvent<?> event) {
			notifyCallCount++;
			IHasDragControl.super.notifyDragStartListeners(event);
		}

		@Override
		public void notifyDropListeners(final CDropEvent<?> event) {
			notifyCallCount++;
			IHasDragControl.super.notifyDropListeners(event);
		}

		public void resetNotifyCallCount() { notifyCallCount = 0; }

		@Override
		public void setDragEnabled(final boolean enabled) { this.dragEnabled = enabled; }

		@Override
		public void setDropEnabled(final boolean enabled) { this.dropEnabled = enabled; }
	}

	/** Test that events are forwarded from child to parent without infinite loops. */
	@Test
	void testEventForwardingNoInfiniteLoop() {
		final MockParentComponent parent = new MockParentComponent();
		final MockChildComponent child = new MockChildComponent();
		// Setup forwarding from child to parent
		parent.setupChildDragDropForwarding(child);
		// Track how many times the parent listener is called
		final AtomicInteger parentListenerCallCount = new AtomicInteger(0);
		// Add a listener to the parent that counts calls
		parent.addEventListener_dragStart(event -> {
			parentListenerCallCount.incrementAndGet();
		});
		// Create a mock drag start event
		final Grid<String> mockGrid = new Grid<>();
		final CDragStartEvent<String> dragStartEvent = new CDragStartEvent<>(mockGrid, List.of("item1"), true);
		// Trigger the event on the child
		child.notifyDragStartListeners(dragStartEvent);
		// Verify:
		// 1. Parent's notifyDragStartListeners was called exactly once
		assertEquals(1, parent.getNotifyCallCount(), "Parent notify method should be called exactly once");
		// 2. Parent's listener was called exactly once
		assertEquals(1, parentListenerCallCount.get(), "Parent listener should be called exactly once");
	}

	/** Test that setupChildDragDropForwarding properly registers listeners on the child component. */
	@Test
	void testSetupChildDragDropForwardingRegistersListeners() {
		final MockParentComponent parent = new MockParentComponent();
		final MockChildComponent child = new MockChildComponent();
		// Initially, child should have no listeners
		assertTrue(child.getDragStartListeners().isEmpty(), "Child should have no drag start listeners initially");
		assertTrue(child.getDragEndListeners().isEmpty(), "Child should have no drag end listeners initially");
		assertTrue(child.getDropListeners().isEmpty(), "Child should have no drop listeners initially");
		// Setup forwarding
		parent.setupChildDragDropForwarding(child);
		// After setup, child should have 3 listeners (one for each event type)
		assertEquals(1, child.getDragStartListeners().size(), "Child should have 1 drag start listener after setup");
		assertEquals(1, child.getDragEndListeners().size(), "Child should have 1 drag end listener after setup");
		assertEquals(1, child.getDropListeners().size(), "Child should have 1 drop listener after setup");
		// Parent should still have no direct listeners (listeners are on the child)
		assertTrue(parent.getDragStartListeners().isEmpty(), "Parent should have no drag start listeners directly");
		assertTrue(parent.getDragEndListeners().isEmpty(), "Parent should have no drag end listeners directly");
		assertTrue(parent.getDropListeners().isEmpty(), "Parent should have no drop listeners directly");
	}

	/** Test that the fix prevents the old buggy behavior where listeners were added to self. */
	@Test
	void testSetupChildDragDropForwardingRequiresChildParameter() {
		final MockParentComponent parent = new MockParentComponent();
		// Before the fix, setupChildDragDropForwarding() had no parameters and added listeners to itself
		// After the fix, it requires a child parameter
		// This test verifies that calling it with a child parameter doesn't add listeners to parent itself
		final MockChildComponent child = new MockChildComponent();
		parent.setupChildDragDropForwarding(child);
		// Verify parent doesn't have listeners on itself
		assertTrue(parent.getDragStartListeners().isEmpty(),
				"Parent should not register listeners on itself when setting up child forwarding");
		assertTrue(parent.getDragEndListeners().isEmpty(),
				"Parent should not register listeners on itself when setting up child forwarding");
		assertTrue(parent.getDropListeners().isEmpty(),
				"Parent should not register listeners on itself when setting up child forwarding");
		// Verify child has the forwarding listeners
		assertFalse(child.getDragStartListeners().isEmpty(), "Child should have forwarding listener for drag start");
		assertFalse(child.getDragEndListeners().isEmpty(), "Child should have forwarding listener for drag end");
		assertFalse(child.getDropListeners().isEmpty(), "Child should have forwarding listener for drop");
	}

	/** Test that multiple events don't cause cascading notifications. */
	@Test
	void testMultipleEventsNoCascading() {
		final MockParentComponent parent = new MockParentComponent();
		final MockChildComponent child = new MockChildComponent();
		parent.setupChildDragDropForwarding(child);
		final Grid<String> mockGrid = new Grid<>();
		// Fire 3 different events
		child.notifyDragStartListeners(new CDragStartEvent<>(mockGrid, List.of("item1"), true));
		parent.resetNotifyCallCount();
		child.notifyDragEndListeners(new CDragEndEvent(mockGrid, true));
		parent.resetNotifyCallCount();
		child.notifyDropListeners(new CDropEvent<>(mockGrid, List.of("item1"), mockGrid, null, null, true));
		// Each event should have caused exactly 1 notification
		assertEquals(1, parent.getNotifyCallCount(), "Each event should trigger exactly one notification");
	}

	/** Basic sanity test that the interface is available and functional. */
	@Test
	void testInterfaceAvailable() {
		final MockParentComponent component = new MockParentComponent();
		assertNotNull(component, "Component implementing IHasDragControl should be created");
		assertNotNull(component.getDragStartListeners(), "getDragStartListeners should not return null");
		assertNotNull(component.getDragEndListeners(), "getDragEndListeners should not return null");
		assertNotNull(component.getDropListeners(), "getDropListeners should not return null");
	}
}
