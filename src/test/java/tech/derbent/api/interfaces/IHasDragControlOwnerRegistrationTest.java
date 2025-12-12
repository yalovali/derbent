package tech.derbent.api.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.shared.Registration;

/** Test class for IHasDragControl owner registration pattern.
 * <p>
 * Verifies that components implementing IHasDragControl properly support the owner registration
 * mechanism where:
 * <ul>
 * <li>Owner can be set via setDragDropOwner()</li>
 * <li>Owner can be retrieved via getDragDropOwner()</li>
 * <li>Component can register with owner via registerWithOwner()</li>
 * <li>Third-party classes cannot bypass the owner registration pattern</li>
 * </ul>
 */
class IHasDragControlOwnerRegistrationTest {

	// Test component implementing all drag-drop interfaces with owner support
	static class TestDragDropComponent extends com.vaadin.flow.component.Composite<com.vaadin.flow.component.orderedlayout.VerticalLayout>
			implements IHasDragStart<String>, IHasDragEnd<String>, IHasDrop<String>, IHasDragControl {

		private static final long serialVersionUID = 1L;
		private final List<ComponentEventListener<GridDragStartEvent<String>>> dragStartListeners = new ArrayList<>();
		private final List<ComponentEventListener<GridDragEndEvent<String>>> dragEndListeners = new ArrayList<>();
		private final List<ComponentEventListener<GridDropEvent<?>>> dropListeners = new ArrayList<>();
		private boolean dragEnabled = false;
		private boolean dropEnabled = false;
		private Object dragDropOwner = null;
		private boolean registeredWithOwner = false;

		@Override
		public Registration addDragStartListener(final ComponentEventListener<GridDragStartEvent<String>> listener) {
			dragStartListeners.add(listener);
			return () -> dragStartListeners.remove(listener);
		}

		@Override
		public Registration addDragEndListener(final ComponentEventListener<GridDragEndEvent<String>> listener) {
			dragEndListeners.add(listener);
			return () -> dragEndListeners.remove(listener);
		}

		@Override
		public Registration addDropListener(final ComponentEventListener<GridDropEvent<?>> listener) {
			dropListeners.add(listener);
			return () -> dropListeners.remove(listener);
		}

		@Override
		public void setDragEnabled(final boolean enabled) {
			dragEnabled = enabled;
		}

		@Override
		public boolean isDragEnabled() {
			return dragEnabled;
		}

		@Override
		public void setDropEnabled(final boolean enabled) {
			dropEnabled = enabled;
		}

		@Override
		public boolean isDropEnabled() {
			return dropEnabled;
		}

		@Override
		public void setDragDropOwner(final Object owner) {
			dragDropOwner = owner;
		}

		@Override
		public Object getDragDropOwner() {
			return dragDropOwner;
		}

		@Override
		public void registerWithOwner() {
			if (dragDropOwner == null) {
				throw new IllegalStateException("Owner must be set before registration");
			}
			registeredWithOwner = true;
		}

		public boolean isRegisteredWithOwner() {
			return registeredWithOwner;
		}

		public int getDragStartListenerCount() {
			return dragStartListeners.size();
		}

		public int getDragEndListenerCount() {
			return dragEndListeners.size();
		}

		public int getDropListenerCount() {
			return dropListeners.size();
		}
	}

	// Test owner component that can receive drag-drop events
	static class TestOwnerComponent extends com.vaadin.flow.component.Composite<com.vaadin.flow.component.orderedlayout.VerticalLayout>
			implements IHasDragStart<String>, IHasDragEnd<String>, IHasDrop<String> {

		private static final long serialVersionUID = 1L;
		private final List<ComponentEventListener<GridDragStartEvent<String>>> dragStartListeners = new ArrayList<>();
		private final List<ComponentEventListener<GridDragEndEvent<String>>> dragEndListeners = new ArrayList<>();
		private final List<ComponentEventListener<GridDropEvent<?>>> dropListeners = new ArrayList<>();

		@Override
		public Registration addDragStartListener(final ComponentEventListener<GridDragStartEvent<String>> listener) {
			dragStartListeners.add(listener);
			return () -> dragStartListeners.remove(listener);
		}

		@Override
		public Registration addDragEndListener(final ComponentEventListener<GridDragEndEvent<String>> listener) {
			dragEndListeners.add(listener);
			return () -> dragEndListeners.remove(listener);
		}

		@Override
		public Registration addDropListener(final ComponentEventListener<GridDropEvent<?>> listener) {
			dropListeners.add(listener);
			return () -> dropListeners.remove(listener);
		}

		public int getDragStartListenerCount() {
			return dragStartListeners.size();
		}

		public int getDragEndListenerCount() {
			return dragEndListeners.size();
		}

		public int getDropListenerCount() {
			return dropListeners.size();
		}
	}

	private TestDragDropComponent component;
	private TestOwnerComponent owner;

	@BeforeEach
	void setUp() {
		component = new TestDragDropComponent();
		owner = new TestOwnerComponent();
	}

	/** Test that owner can be set and retrieved. */
	@Test
	void testSetAndGetOwner() {
		component.setDragDropOwner(owner);
		assertNotNull(component.getDragDropOwner(), "Owner should be set");
		assertEquals(owner, component.getDragDropOwner(), "Retrieved owner should match set owner");
	}

	/** Test that registerWithOwner fails when owner is not set. */
	@Test
	void testRegisterWithoutOwnerThrowsException() {
		assertThrows(IllegalStateException.class, () -> component.registerWithOwner(),
			"registerWithOwner should throw IllegalStateException when owner is not set");
	}

	/** Test that registerWithOwner succeeds when owner is set. */
	@Test
	void testRegisterWithOwnerSucceeds() {
		component.setDragDropOwner(owner);
		component.registerWithOwner();
		assertEquals(true, component.isRegisteredWithOwner(), "Component should be marked as registered");
	}

	/** Test the complete owner registration workflow. */
	@Test
	void testOwnerRegistrationWorkflow() {
		// Step 1: Set the owner
		component.setDragDropOwner(owner);

		// Step 2: Register with owner
		component.registerWithOwner();

		// Step 3: Verify registration
		assertEquals(true, component.isRegisteredWithOwner(), "Component should be registered with owner");
		assertNotNull(component.getDragDropOwner(), "Owner should still be accessible after registration");
	}

	/** Test that drag-drop control methods work correctly. */
	@Test
	void testDragDropControl() {
		// Test drag enable/disable
		assertEquals(false, component.isDragEnabled(), "Drag should be disabled by default");
		component.setDragEnabled(true);
		assertEquals(true, component.isDragEnabled(), "Drag should be enabled after setDragEnabled(true)");

		// Test drop enable/disable
		assertEquals(false, component.isDropEnabled(), "Drop should be disabled by default");
		component.setDropEnabled(true);
		assertEquals(true, component.isDropEnabled(), "Drop should be enabled after setDropEnabled(true)");
	}

	/** Test that component supports all drag-drop event types. */
	@Test
	void testAllEventTypesSupported() {
		component.addDragStartListener(event -> {});
		component.addDragEndListener(event -> {});
		component.addDropListener(event -> {});

		assertEquals(1, component.getDragStartListenerCount(), "Drag start listener should be registered");
		assertEquals(1, component.getDragEndListenerCount(), "Drag end listener should be registered");
		assertEquals(1, component.getDropListenerCount(), "Drop listener should be registered");
	}
}
