package unit_tests.tech.derbent.screens.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tech.derbent.screens.domain.CGridEntity;

/**
 * Test class to verify that CGridEntity has proper getter and setter methods
 * for form binding to work correctly.
 * 
 * This specifically tests the fix for the binding error where
 * dataServiceBeanName field was not being saved due to missing accessors.
 */
public class CGridEntityAccessorsTest {

	@Test
	void testDataServiceBeanNameGetterSetter() {
		// Create a new CGridEntity instance
		final CGridEntity entity = new CGridEntity();
		
		// Verify getter returns null initially
		assertNull(entity.getDataServiceBeanName(), "Initial value should be null");
		
		// Test setter and getter
		final String testValue = "testServiceBean";
		entity.setDataServiceBeanName(testValue);
		
		// Verify the value was set correctly
		assertEquals(testValue, entity.getDataServiceBeanName(), 
				"Getter should return the value set by setter");
		
		// Test setting null value
		entity.setDataServiceBeanName(null);
		assertNull(entity.getDataServiceBeanName(), "Should be able to set null value");
		
		// Test setting empty string
		entity.setDataServiceBeanName("");
		assertEquals("", entity.getDataServiceBeanName(), 
				"Should be able to set empty string");
	}

	@Test
	void testEntityInstantiation() {
		// Test that the entity can be instantiated without issues
		final CGridEntity entity = new CGridEntity();
		assertNotNull(entity, "Entity should be instantiable");
		assertNotNull(entity.getDisplayName(), "Display name should not be null");
	}

	@Test
	void testEntityWithProject() {
		// Test parameterized constructor (requires project, but we'll test without for now)
		final CGridEntity entity = new CGridEntity();
		entity.setDataServiceBeanName("CViewsService");
		
		assertEquals("CViewsService", entity.getDataServiceBeanName(),
				"Should properly store and retrieve service bean name");
	}
}