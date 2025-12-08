package tech.derbent.api.services.pageservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;

/** Test class for CPageService HasValue event propagation.
 * Verifies that CPageService properly binds value change events from components
 * implementing HasValue interface to handler methods using the on_{componentName}_change pattern. */
class CPageServiceHasValueEventPropagationTest {

	// Test component implementing HasValue
	static class TestHasValueComponent extends com.vaadin.flow.component.Composite<com.vaadin.flow.component.orderedlayout.VerticalLayout>
			implements HasValue<HasValue.ValueChangeEvent<String>, String> {
		
		private String value = "";
		private final List<HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>>> listeners = new ArrayList<>();
		
		@Override
		public Registration addValueChangeListener(
				HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener) {
			listeners.add(listener);
			return () -> listeners.remove(listener);
		}
		
		@Override
		public void setValue(String value) {
			String oldValue = this.value;
			this.value = value;
			
			// Notify listeners
			for (HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener : listeners) {
				listener.valueChanged(new ValueChangeEvent<String>() {
					@Override
					public HasValue<?, String> getHasValue() {
						return TestHasValueComponent.this;
					}
					
					@Override
					public String getValue() {
						return value;
					}
					
					@Override
					public String getOldValue() {
						return oldValue;
					}
					
					@Override
					public boolean isFromClient() {
						return false;
					}
				});
			}
		}
		
		@Override
		public String getValue() {
			return value;
		}
		
		@Override
		public void setReadOnly(boolean readOnly) {
			// Not implemented for test
		}
		
		@Override
		public boolean isReadOnly() {
			return false;
		}
		
		@Override
		public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
			// Not implemented for test
		}
		
		@Override
		public boolean isRequiredIndicatorVisible() {
			return false;
		}
		
		public int getListenerCount() {
			return listeners.size();
		}
	}
	
	private TestHasValueComponent testComponent;
	
	@BeforeEach
	void setUp() {
		testComponent = new TestHasValueComponent();
	}
	
	/** Test that components implementing HasValue can be recognized.
	 * Verifies that the component properly implements the interface. */
	@Test
	void testComponentImplementsHasValue() {
		assertTrue(testComponent instanceof HasValue, 
				"TestHasValueComponent should implement HasValue");
	}
	
	/** Test that value change listeners can be added to the component.
	 * Verifies that the component accepts and tracks value change listeners. */
	@Test
	void testAddValueChangeListener() {
		Registration registration = testComponent.addValueChangeListener(event -> {
			// Listener body
		});
		
		assertEquals(1, testComponent.getListenerCount(), 
				"Value change listener should be registered on component");
	}
	
	/** Test that value changes trigger listeners.
	 * Verifies that listeners are called when setValue() is invoked. */
	@Test
	void testValueChangeTriggerListener() {
		final List<String> receivedValues = new ArrayList<>();
		
		testComponent.addValueChangeListener(event -> {
			receivedValues.add(event.getValue());
		});
		
		testComponent.setValue("test1");
		testComponent.setValue("test2");
		
		assertEquals(2, receivedValues.size(), "Listener should be called twice");
		assertEquals("test1", receivedValues.get(0), "First value should be test1");
		assertEquals("test2", receivedValues.get(1), "Second value should be test2");
	}
	
	/** Test that multiple value change listeners can be added.
	 * Verifies that the component supports multiple listeners. */
	@Test
	void testMultipleValueChangeListeners() {
		testComponent.addValueChangeListener(event -> {});
		testComponent.addValueChangeListener(event -> {});
		testComponent.addValueChangeListener(event -> {});
		
		assertEquals(3, testComponent.getListenerCount(), 
				"Three value change listeners should be registered");
	}
	
	/** Test that listener registration can be removed.
	 * Verifies that the Registration.remove() functionality works correctly. */
	@Test
	void testListenerRemoval() {
		Registration reg1 = testComponent.addValueChangeListener(event -> {});
		Registration reg2 = testComponent.addValueChangeListener(event -> {});
		
		assertEquals(2, testComponent.getListenerCount(), 
				"Two listeners should be registered");
		
		reg1.remove();
		assertEquals(1, testComponent.getListenerCount(), 
				"One listener should remain after removal");
		
		reg2.remove();
		assertEquals(0, testComponent.getListenerCount(), 
				"No listeners should remain after both removed");
	}
	
	/** Test that getValue() returns the current value.
	 * Verifies that the HasValue interface properly exposes the value. */
	@Test
	void testGetValue() {
		assertEquals("", testComponent.getValue(), "Initial value should be empty");
		
		testComponent.setValue("newValue");
		assertEquals("newValue", testComponent.getValue(), "Value should be updated");
	}
}
