package tech.derbent.api.ui.component.enhanced;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;

/** Unit tests for CComponentEntitySelection to verify HasValue interface implementation. These tests verify that the component properly implements
 * the HasValue interface with correct method signatures and return types. Note: Full functional testing of UI components requires Vaadin UI context
 * initialization and is better suited for integration tests (e.g., Playwright tests). */
class CComponentEntitySelectionHasValueTest {

	@SuppressWarnings ("static-method")
	@Test
	void testHasAddValueChangeListenerMethod() throws Exception {
		// When: checking for addValueChangeListener() method
		final Method addListenerMethod = CComponentEntitySelection.class.getMethod("addValueChangeListener", HasValue.ValueChangeListener.class);
		// Then: method should exist and return Registration
		assertNotNull(addListenerMethod, "addValueChangeListener() method should exist");
		assertTrue(Registration.class.isAssignableFrom(addListenerMethod.getReturnType()), "addValueChangeListener() should return Registration");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasClearMethod() throws Exception {
		// When: checking for clear() method
		final Method clearMethod = CComponentEntitySelection.class.getMethod("clear");
		// Then: method should exist
		assertNotNull(clearMethod, "clear() method should exist");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasGetSelectedItemsMethod() throws Exception {
		// When: checking for getSelectedItems() method (existing method, not part of HasValue)
		final Method getSelectedItemsMethod = CComponentEntitySelection.class.getMethod("getSelectedItems");
		// Then: method should exist and return Set
		assertNotNull(getSelectedItemsMethod, "getSelectedItems() method should exist");
		assertTrue(Set.class.isAssignableFrom(getSelectedItemsMethod.getReturnType()), "getSelectedItems() should return a Set");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasGetValueMethod() throws Exception {
		// When: checking for getValue() method
		final Method getValueMethod = CComponentEntitySelection.class.getMethod("getValue");
		// Then: method should exist and return Set
		assertNotNull(getValueMethod, "getValue() method should exist");
		assertTrue(Set.class.isAssignableFrom(getValueMethod.getReturnType()), "getValue() should return a Set");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasIsEmptyMethod() throws Exception {
		// When: checking for isEmpty() method
		final Method isEmptyMethod = CComponentEntitySelection.class.getMethod("isEmpty");
		// Then: method should exist and return boolean
		assertNotNull(isEmptyMethod, "isEmpty() method should exist");
		assertTrue(isEmptyMethod.getReturnType().equals(boolean.class), "isEmpty() should return boolean");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasIsReadOnlyMethod() throws Exception {
		// When: checking for isReadOnly() method
		final Method isReadOnlyMethod = CComponentEntitySelection.class.getMethod("isReadOnly");
		// Then: method should exist and return boolean
		assertNotNull(isReadOnlyMethod, "isReadOnly() method should exist");
		assertTrue(isReadOnlyMethod.getReturnType().equals(boolean.class), "isReadOnly() should return boolean");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasIsRequiredIndicatorVisibleMethod() throws Exception {
		// When: checking for isRequiredIndicatorVisible() method
		final Method isRequiredMethod = CComponentEntitySelection.class.getMethod("isRequiredIndicatorVisible");
		// Then: method should exist and return boolean
		assertNotNull(isRequiredMethod, "isRequiredIndicatorVisible() method should exist");
		assertTrue(isRequiredMethod.getReturnType().equals(boolean.class), "isRequiredIndicatorVisible() should return boolean");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasSetReadOnlyMethod() throws Exception {
		// When: checking for setReadOnly(boolean) method
		final Method setReadOnlyMethod = CComponentEntitySelection.class.getMethod("setReadOnly", boolean.class);
		// Then: method should exist
		assertNotNull(setReadOnlyMethod, "setReadOnly(boolean) method should exist");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasSetRequiredIndicatorVisibleMethod() throws Exception {
		// When: checking for setRequiredIndicatorVisible(boolean) method
		final Method setRequiredMethod = CComponentEntitySelection.class.getMethod("setRequiredIndicatorVisible", boolean.class);
		// Then: method should exist
		assertNotNull(setRequiredMethod, "setRequiredIndicatorVisible(boolean) method should exist");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testHasSetValueMethod() throws Exception {
		// When: checking for setValue(Set) method
		final Method setValueMethod = CComponentEntitySelection.class.getMethod("setValue", Set.class);
		// Then: method should exist with correct parameter
		assertNotNull(setValueMethod, "setValue(Set) method should exist");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testImplementsHasValueInterface() {
		// When: checking if CComponentEntitySelection implements HasValue
		final Class<?>[] interfaces = CComponentEntitySelection.class.getInterfaces();
		// Then: should implement HasValue interface
		boolean implementsHasValue = false;
		for (final Class<?> iface : interfaces) {
			if (iface.equals(HasValue.class)) {
				implementsHasValue = true;
				break;
			}
		}
		assertTrue(implementsHasValue, "CComponentEntitySelection should implement HasValue interface");
	}
}
