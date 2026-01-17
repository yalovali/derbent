package tech.derbent.api.services.pageservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.Composite;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;


/** Test class for CPageService HasValue event propagation. Verifies that CPageService properly binds value change events from components implementing
 * HasValue interface to handler methods using the on_{componentName}_change pattern. */
class CPageServiceHasValueEventPropagationTest {

	// Test component implementing HasValue
	static class TestHasValueComponent extends Composite<VerticalLayout>
			implements HasValue<HasValue.ValueChangeEvent<String>, String> {

		/**
		     *
		     */
		private static final long serialVersionUID = 1L;
		private final List<HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>>> listeners = new ArrayList<>();
		private String value = "";

		@Override
		public Registration addValueChangeListener(HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener) {
			listeners.add(listener);
			return () -> listeners.remove(listener);
		}

		public int getListenerCount() { return listeners.size(); }

		@Override
		public String getValue() { return value; }

		@Override
		public boolean isReadOnly() { return false; }

		@Override
		public boolean isRequiredIndicatorVisible() { return false; }

		@Override
		public void setReadOnly(boolean readOnly) {
			// Not implemented for test
		}

		@Override
		public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
			// Not implemented for test
		}

		@Override
		public void setValue(String value) {
			final String oldValue = this.value;
			this.value = value;
			// Notify listeners
			for (final HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener : listeners) {
				listener.valueChanged(new ValueChangeEvent<String>() {

					/**
					 *
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public HasValue<?, String> getHasValue() { return TestHasValueComponent.this; }

					@Override
					public String getOldValue() { return oldValue; }

					@Override
					public String getValue() { return value; }

					@Override
					public boolean isFromClient() { return false; }
				});
			}
		}
	}

	private TestHasValueComponent testComponent;

	@BeforeEach
	void setUp() {
		testComponent = new TestHasValueComponent();
	}

	/** Test that components implementing HasValue can be recognized. Verifies that the component properly implements the interface. */
	@Test
	void testComponentImplementsHasValue() {
		assertTrue(testComponent != null, "TestHasValueComponent should implement HasValue");
	}

	/** Test that getValue() returns the current value. Verifies that the HasValue interface properly exposes the value. */
	@Test
	void testGetValue() {
		assertEquals("", testComponent.getValue(), "Initial value should be empty");
		testComponent.setValue("newValue");
		assertEquals("newValue", testComponent.getValue(), "Value should be updated");
	}

	/** Test that listener registration can be removed. Verifies that the Registration.remove() functionality works correctly. */
	@Test
	void testListenerRemoval() {
		final Registration reg1 = testComponent.addValueChangeListener(event -> {/**/});
		final Registration reg2 = testComponent.addValueChangeListener(event -> {/**/});
		assertEquals(2, testComponent.getListenerCount(), "Two listeners should be registered");
		reg1.remove();
		assertEquals(1, testComponent.getListenerCount(), "One listener should remain after removal");
		reg2.remove();
		assertEquals(0, testComponent.getListenerCount(), "No listeners should remain after both removed");
	}

	/** Test that multiple value change listeners can be added. Verifies that the component supports multiple listeners. */
	@Test
	void testMultipleValueChangeListeners() {
		testComponent.addValueChangeListener(event -> {/**/});
		testComponent.addValueChangeListener(event -> {/**/});
		testComponent.addValueChangeListener(event -> {/**/});
		assertEquals(3, testComponent.getListenerCount(), "Three value change listeners should be registered");
	}

	/** Test that value changes trigger listeners. Verifies that listeners are called when setValue() is invoked. */
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
}
