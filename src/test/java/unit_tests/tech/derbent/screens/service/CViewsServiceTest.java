package unit_tests.tech.derbent.screens.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.screens.service.CViewsService;

/**
 * Test class for CViewsService focusing on the new entity line type functionality.
 */
class CViewsServiceTest {

	private CViewsService viewsService;

	@BeforeEach
	void setUp() {
		viewsService = new CViewsService();
	}

	@Test
	void testGetAvailableBaseTypes() {
		final List<String> baseTypes = viewsService.getAvailableBaseTypes();
		
		assertEquals(5, baseTypes.size());
		assertTrue(baseTypes.contains("CActivity"));
		assertTrue(baseTypes.contains("CMeeting"));
		assertTrue(baseTypes.contains("CRisk"));
		assertTrue(baseTypes.contains("CProject"));
		assertTrue(baseTypes.contains("CUser"));
	}

	@Test
	void testGetAvailableEntityLineTypesForActivity() {
		final List<String> entityLineTypes = viewsService.getAvailableEntityLineTypes("CActivity");
		
		assertFalse(entityLineTypes.isEmpty());
		assertTrue(entityLineTypes.contains("CActivity")); // Base entity itself
		assertTrue(entityLineTypes.contains("Project of Activity"));
		assertTrue(entityLineTypes.contains("Assigned User of Activity"));
		assertTrue(entityLineTypes.contains("Created User of Activity"));
		assertTrue(entityLineTypes.contains("Activity Type of Activity"));
		assertTrue(entityLineTypes.contains("Activity Status of Activity"));
		assertTrue(entityLineTypes.contains("Activity Priority of Activity"));
		assertTrue(entityLineTypes.contains("Parent Activity of Activity"));
	}

	@Test
	void testGetAvailableEntityLineTypesForProject() {
		final List<String> entityLineTypes = viewsService.getAvailableEntityLineTypes("CProject");
		
		assertFalse(entityLineTypes.isEmpty());
		assertTrue(entityLineTypes.contains("CProject")); // Base entity itself
		assertTrue(entityLineTypes.contains("Created User of Project"));
	}

	@Test
	void testGetAvailableEntityLineTypesForRisk() {
		final List<String> entityLineTypes = viewsService.getAvailableEntityLineTypes("CRisk");
		
		assertFalse(entityLineTypes.isEmpty());
		assertTrue(entityLineTypes.contains("CRisk")); // Base entity itself
		assertTrue(entityLineTypes.contains("Project of Risk"));
		assertTrue(entityLineTypes.contains("Assigned User of Risk"));
		assertTrue(entityLineTypes.contains("Created User of Risk"));
		assertTrue(entityLineTypes.contains("Risk Status of Risk"));
		assertTrue(entityLineTypes.contains("Risk Severity of Risk"));
	}

	@Test
	void testGetEntityClassNameForLineType() {
		// Test direct entity types
		assertEquals("CActivity", viewsService.getEntityClassNameForLineType("CActivity"));
		assertEquals("CProject", viewsService.getEntityClassNameForLineType("CProject"));
		assertEquals("CUser", viewsService.getEntityClassNameForLineType("CUser"));
		
		// Test related entity types
		assertEquals("CProject", viewsService.getEntityClassNameForLineType("Project of Activity"));
		assertEquals("CUser", viewsService.getEntityClassNameForLineType("Assigned User of Activity"));
		assertEquals("CUser", viewsService.getEntityClassNameForLineType("Created User of Activity"));
		assertEquals("CActivityType", viewsService.getEntityClassNameForLineType("Activity Type of Activity"));
		assertEquals("CActivityStatus", viewsService.getEntityClassNameForLineType("Activity Status of Activity"));
		assertEquals("CActivityPriority", viewsService.getEntityClassNameForLineType("Activity Priority of Activity"));
		assertEquals("CActivity", viewsService.getEntityClassNameForLineType("Parent Activity of Activity"));
		
		// Test risk types
		assertEquals("CRiskStatus", viewsService.getEntityClassNameForLineType("Risk Status of Risk"));
		assertEquals("CRiskSeverity", viewsService.getEntityClassNameForLineType("Risk Severity of Risk"));
	}

	@Test
	void testGetAvailableEntityLineTypesForUnknownEntity() {
		final List<String> entityLineTypes = viewsService.getAvailableEntityLineTypes("UnknownEntity");
		
		// Should contain only the base entity type itself
		assertEquals(1, entityLineTypes.size());
		assertTrue(entityLineTypes.contains("UnknownEntity"));
	}

	@Test
	void testGetEntityClassNameForUnknownLineType() {
		final String className = viewsService.getEntityClassNameForLineType("Unknown Line Type");
		
		// Should fallback to the original type
		assertEquals("Unknown Line Type", className);
	}
}