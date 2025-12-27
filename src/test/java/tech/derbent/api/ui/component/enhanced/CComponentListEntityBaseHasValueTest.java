package tech.derbent.api.ui.component.enhanced;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;

/** Unit tests for CComponentListEntityBase to verify HasValue interface implementation. These tests verify that the component properly implements the
 * HasValue interface with correct method signatures and return types. Note: Full functional testing of UI components requires Vaadin UI context
 * initialization and is better suited for integration tests (e.g., Playwright tests). */
class CComponentListEntityBaseHasValueTest {

	
	@Test
	void testChildClassesInheritHasValue() {
		// When: checking if child classes inherit HasValue
		// Then: CComponentListSprintItems should implement HasValue
		assertTrue(HasValue.class.isAssignableFrom(CComponentListSprintItems.class),
				"CComponentListSprintItems should implement/inherit HasValue interface");
		// Then: CComponentListDetailLines should implement HasValue
		assertTrue(HasValue.class.isAssignableFrom(CComponentListDetailLines.class),
				"CComponentListDetailLines should implement/inherit HasValue interface");
	}

	
	@Test
	void testHasAddValueChangeListenerMethod() throws Exception {
		// When: checking for addValueChangeListener() method
		final Method addListenerMethod = CComponentListEntityBase.class.getMethod("addValueChangeListener", HasValue.ValueChangeListener.class);
		// Then: method should exist and return Registration
		assertNotNull(addListenerMethod, "addValueChangeListener() method should exist");
		assertTrue(Registration.class.isAssignableFrom(addListenerMethod.getReturnType()), "addValueChangeListener() should return Registration");
	}

	
	@Test
	void testHasClearMethod() throws Exception {
		// When: checking for clear() method
		final Method clearMethod = CComponentListEntityBase.class.getMethod("clear");
		// Then: method should exist
		assertNotNull(clearMethod, "clear() method should exist");
	}

	
	@Test
	void testHasGetValueMethod() throws Exception {
		// When: checking for getValue() method
		final Method getValueMethod = CComponentListEntityBase.class.getMethod("getValue");
		// Then: method should exist
		assertNotNull(getValueMethod, "getValue() method should exist");
	}

	
	@Test
	void testHasIsEmptyMethod() throws Exception {
		// When: checking for isEmpty() method
		final Method isEmptyMethod = CComponentListEntityBase.class.getMethod("isEmpty");
		// Then: method should exist and return boolean
		assertNotNull(isEmptyMethod, "isEmpty() method should exist");
		assertTrue(isEmptyMethod.getReturnType().equals(boolean.class), "isEmpty() should return boolean");
	}

	
	@Test
	void testHasIsReadOnlyMethod() throws Exception {
		// When: checking for isReadOnly() method
		final Method isReadOnlyMethod = CComponentListEntityBase.class.getMethod("isReadOnly");
		// Then: method should exist and return boolean
		assertNotNull(isReadOnlyMethod, "isReadOnly() method should exist");
		assertTrue(isReadOnlyMethod.getReturnType().equals(boolean.class), "isReadOnly() should return boolean");
	}

	@Test
	void testHasIsRequiredIndicatorVisibleMethod() throws Exception {
		// When: checking for isRequiredIndicatorVisible() method
		final Method method = CComponentListEntityBase.class.getMethod("isRequiredIndicatorVisible");
		// Then: method should exist and return boolean
		assertNotNull(method, "isRequiredIndicatorVisible() method should exist");
		assertTrue(method.getReturnType().equals(boolean.class), "isRequiredIndicatorVisible() should return boolean");
	}

	
	@Test
	void testHasSetReadOnlyMethod() throws Exception {
		// When: checking for setReadOnly(boolean) method
		final Method setReadOnlyMethod = CComponentListEntityBase.class.getMethod("setReadOnly", boolean.class);
		// Then: method should exist
		assertNotNull(setReadOnlyMethod, "setReadOnly(boolean) method should exist");
	}

	
	@Test
	void testHasSetRequiredIndicatorVisibleMethod() throws Exception {
		// When: checking for setRequiredIndicatorVisible(boolean) method
		final Method method = CComponentListEntityBase.class.getMethod("setRequiredIndicatorVisible", boolean.class);
		// Then: method should exist
		assertNotNull(method, "setRequiredIndicatorVisible(boolean) method should exist");
	}

	
	@Test
	void testHasSetValueMethod() throws Exception {
		// When: checking for setValue(Object) method with generic parameter
		// Note: Due to type erasure, we check for the method by name and parameter count
		boolean hasSetValueMethod = false;
		for (final Method method : CComponentListEntityBase.class.getMethods()) {
			if (method.getName().equals("setValue") && method.getParameterCount() == 1) {
				hasSetValueMethod = true;
				break;
			}
		}
		// Then: method should exist
		assertTrue(hasSetValueMethod, "setValue() method should exist");
	}

	
	@Test
	void testImplementsHasValueInterface() {
		// When: checking if CComponentListEntityBase implements HasValue
		final Class<?>[] interfaces = CComponentListEntityBase.class.getInterfaces();
		// Then: should implement HasValue interface
		boolean implementsHasValue = false;
		for (final Class<?> iface : interfaces) {
			if (iface.equals(HasValue.class)) {
				implementsHasValue = true;
				break;
			}
		}
		assertTrue(implementsHasValue, "CComponentListEntityBase should implement HasValue interface");
	}
}
