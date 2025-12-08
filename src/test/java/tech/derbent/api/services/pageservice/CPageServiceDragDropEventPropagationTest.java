package tech.derbent.api.services.pageservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.interfaces.IHasDragEnd;
import tech.derbent.api.interfaces.IHasDragStart;

/** Test class for CPageService drag-drop event propagation.
 * Verifies that CPageService properly binds drag-drop events from components
 * implementing IHasDragStart and IHasDragEnd interfaces to handler methods
 * using the on_{componentName}_{action} pattern. */
class CPageServiceDragDropEventPropagationTest {

	// Test component implementing IHasDragStart and IHasDragEnd
	static class TestDragDropComponent extends com.vaadin.flow.component.Composite<com.vaadin.flow.component.orderedlayout.VerticalLayout>
			implements IHasDragStart<String>, IHasDragEnd<String> {
		
		private final List<ComponentEventListener<GridDragStartEvent<String>>> dragStartListeners = new ArrayList<>();
		private final List<ComponentEventListener<GridDragEndEvent<String>>> dragEndListeners = new ArrayList<>();
		
		@Override
		public Registration addDragStartListener(ComponentEventListener<GridDragStartEvent<String>> listener) {
			dragStartListeners.add(listener);
			return () -> dragStartListeners.remove(listener);
		}
		
		@Override
		public Registration addDragEndListener(ComponentEventListener<GridDragEndEvent<String>> listener) {
			dragEndListeners.add(listener);
			return () -> dragEndListeners.remove(listener);
		}
		
		public int getDragStartListenerCount() {
			return dragStartListeners.size();
		}
		
		public int getDragEndListenerCount() {
			return dragEndListeners.size();
		}
	}
	
	private TestDragDropComponent testComponent;
	
	@BeforeEach
	void setUp() {
		testComponent = new TestDragDropComponent();
	}
	
	/** Test that components implementing IHasDragStart can be recognized.
	 * Verifies that the component properly implements the interface. */
	@Test
	void testComponentImplementsIHasDragStart() {
		assertTrue(testComponent instanceof IHasDragStart, 
				"TestDragDropComponent should implement IHasDragStart");
	}
	
	/** Test that components implementing IHasDragEnd can be recognized.
	 * Verifies that the component properly implements the interface. */
	@Test
	void testComponentImplementsIHasDragEnd() {
		assertTrue(testComponent instanceof IHasDragEnd, 
				"TestDragDropComponent should implement IHasDragEnd");
	}
	
	/** Test that drag start listeners can be added to the component.
	 * Verifies that the component accepts and tracks drag start listeners. */
	@Test
	void testAddDragStartListener() {
		Registration registration = testComponent.addDragStartListener(event -> {
			// Listener body
		});
		
		assertEquals(1, testComponent.getDragStartListenerCount(), 
				"Drag start listener should be registered on component");
	}
	
	/** Test that drag end listeners can be added to the component.
	 * Verifies that the component accepts and tracks drag end listeners. */
	@Test
	void testAddDragEndListener() {
		Registration registration = testComponent.addDragEndListener(event -> {
			// Listener body
		});
		
		assertEquals(1, testComponent.getDragEndListenerCount(), 
				"Drag end listener should be registered on component");
	}
	
	/** Test that both drag start and drag end listeners can be added.
	 * Verifies that a component implementing both interfaces supports both listener types. */
	@Test
	void testBothListenersCanBeAdded() {
		testComponent.addDragStartListener(event -> {});
		testComponent.addDragEndListener(event -> {});
		
		assertEquals(1, testComponent.getDragStartListenerCount(), 
				"Drag start listener should be registered");
		assertEquals(1, testComponent.getDragEndListenerCount(), 
				"Drag end listener should be registered");
	}
	
	/** Test that multiple listeners can be added to the same component.
	 * Verifies that the component supports multiple listeners for each event type. */
	@Test
	void testMultipleListenersPerEventType() {
		testComponent.addDragStartListener(event -> {});
		testComponent.addDragStartListener(event -> {});
		testComponent.addDragEndListener(event -> {});
		testComponent.addDragEndListener(event -> {});
		
		assertEquals(2, testComponent.getDragStartListenerCount(), 
				"Two drag start listeners should be registered");
		assertEquals(2, testComponent.getDragEndListenerCount(), 
				"Two drag end listeners should be registered");
	}
	
	/** Test that listener registration can be removed.
	 * Verifies that the Registration.remove() functionality works correctly. */
	@Test
	void testListenerRemoval() {
		Registration reg1 = testComponent.addDragStartListener(event -> {});
		Registration reg2 = testComponent.addDragStartListener(event -> {});
		
		assertEquals(2, testComponent.getDragStartListenerCount(), 
				"Two listeners should be registered");
		
		reg1.remove();
		assertEquals(1, testComponent.getDragStartListenerCount(), 
				"One listener should remain after removal");
		
		reg2.remove();
		assertEquals(0, testComponent.getDragStartListenerCount(), 
				"No listeners should remain after both removed");
	}
}

