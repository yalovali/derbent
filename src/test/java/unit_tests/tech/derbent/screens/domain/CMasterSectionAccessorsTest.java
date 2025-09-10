package unit_tests.tech.derbent.screens.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tech.derbent.screens.domain.CMasterSection;

/**
 * Test class to verify that CMasterSection has proper getter and setter methods
 * for form binding to work correctly.
 * 
 * This specifically tests the fix for missing accessors that could cause
 * similar binding errors as seen with CGridEntity.
 */
public class CMasterSectionAccessorsTest {

	@Test
	void testSectionDBNameGetterSetter() {
		final CMasterSection entity = new CMasterSection();
		
		// Verify getter returns null initially
		assertNull(entity.getSectionDBName(), "Initial value should be null");
		
		// Test setter and getter
		final String testValue = "test_section_db_name";
		entity.setSectionDBName(testValue);
		
		// Verify the value was set correctly
		assertEquals(testValue, entity.getSectionDBName(), 
				"Getter should return the value set by setter");
		
		// Test setting null value
		entity.setSectionDBName(null);
		assertNull(entity.getSectionDBName(), "Should be able to set null value");
	}

	@Test
	void testSectionTypeGetterSetter() {
		final CMasterSection entity = new CMasterSection();
		
		// Verify getter returns null initially
		assertNull(entity.getSectionType(), "Initial value should be null");
		
		// Test setter and getter
		final String testValue = "GRID";
		entity.setSectionType(testValue);
		
		// Verify the value was set correctly
		assertEquals(testValue, entity.getSectionType(), 
				"Getter should return the value set by setter");
		
		// Test setting null value
		entity.setSectionType(null);
		assertNull(entity.getSectionType(), "Should be able to set null value");
	}

	@Test
	void testEntityInstantiation() {
		// Test that the entity can be instantiated without issues
		final CMasterSection entity = new CMasterSection();
		assertNotNull(entity, "Entity should be instantiable");
		assertNotNull(entity.getDisplayName(), "Display name should not be null");
	}

	@Test
	void testBothFieldsTogether() {
		final CMasterSection entity = new CMasterSection();
		
		// Set both fields
		entity.setSectionDBName("test_db_name");
		entity.setSectionType("LIST");
		
		// Verify both are stored correctly
		assertEquals("test_db_name", entity.getSectionDBName());
		assertEquals("LIST", entity.getSectionType());
	}
}