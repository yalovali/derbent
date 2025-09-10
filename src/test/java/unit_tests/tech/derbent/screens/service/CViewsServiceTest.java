package unit_tests.tech.derbent.screens.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.derbent.screens.service.CViewsService;

/** Test class for CViewsService focusing on the new entity line type functionality. */
class CViewsServiceTest {

	private CViewsService viewsService;

	@BeforeEach
	void setUp() {
		viewsService = new CViewsService();
	}

	@Test
	void testGetAvailableBaseTypes() {
		final List<String> baseTypes = viewsService.getAvailableBaseTypes();
		assertEquals(6, baseTypes.size());
		assertTrue(baseTypes.contains("CActivity"));
		assertTrue(baseTypes.contains("CMeeting"));
		assertTrue(baseTypes.contains("CRisk"));
		assertTrue(baseTypes.contains("CProject"));
		assertTrue(baseTypes.contains("CUser"));
		assertTrue(baseTypes.contains("CProjectGannt"));
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

	@Test
	void testGetEntityFieldsForService() {
		// Test with a known service that should exist in the application context
		// This test checks if the method can extract field names from an entity class
		// Note: This test assumes CActivityService exists and follows the expected pattern
		// First, let's get the available beans to see what services are available
		final List<String> availableBeans = viewsService.getAvailableBeans();
		// In unit tests, ApplicationContext might not be available, so we just check the method works
		// without throwing exceptions and returns appropriate empty lists
		assertNotNull(availableBeans, "Should return a list (even if empty in unit tests)");
		// If no beans are available (like in unit tests), we can't test the actual functionality
		// but we can verify the method handles the case gracefully
		if (availableBeans.isEmpty()) {
			// Test that the method returns empty list for non-existent service when no context is available
			final List<String> fields = viewsService.getEntityFieldsForService("CActivityService");
			assertTrue(fields.isEmpty(), "Should return empty list when ApplicationContext is not available");
		} else {
			// If we do have beans available, test with a real service
			// Look for services that end with "Service" and might correspond to entities
			String testServiceName = null;
			for (String beanName : availableBeans) {
				if (beanName.equals("CActivityService") || beanName.equals("cActivityService")) {
					testServiceName = beanName;
					break;
				}
			}
			// If we found a service to test with, test the field extraction
			if (testServiceName != null) {
				final List<String> fields = viewsService.getEntityFieldsForService(testServiceName);
				// Basic validations
				assertFalse(fields.isEmpty(), "Should have at least some fields for the entity");
				// Check that common entity fields are present (these should exist in most entities)
				// Note: These are based on the CActivity class structure we examined
				boolean hasCommonFields = fields.stream().anyMatch(field -> field.equals("name") || field.equals("description") || field.equals("id")
						|| field.equals("activityType") || field.equals("status") || field.equals("estimatedHours"));
				assertTrue(hasCommonFields, "Should contain some expected entity fields like name, id, etc. Found fields: " + fields);
			}
		}
	}

	@Test
	void testGetEntityFieldsForServiceWithInvalidService() {
		// Test with a non-existent service
		final List<String> fields = viewsService.getEntityFieldsForService("NonExistentService");
		assertTrue(fields.isEmpty(), "Should return empty list for non-existent service");
	}

	@Test
	void testGetEntityFieldsForServiceWithNullInput() {
		// Test with null input
		final List<String> fields = viewsService.getEntityFieldsForService(null);
		assertTrue(fields.isEmpty(), "Should return empty list for null service name");
	}
}
