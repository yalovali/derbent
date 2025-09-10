package integration_tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tech.derbent.screens.service.CViewsService;

/** Integration test for CViewsService that runs with a full Spring context. This test demonstrates the actual functionality with real service
 * beans. */
@SpringBootTest (classes = tech.derbent.Application.class)
@ActiveProfiles ("h2") // Use H2 database profile for testing
class CViewsServiceIntegrationTest {

	@Autowired
	private CViewsService viewsService;

	@Test
	void testGetEntityFieldsForServiceWithRealSpringContext() {
		// Get available service beans
		final List<String> availableBeans = viewsService.getAvailableBeans();
		assertNotNull(availableBeans, "Should return a list of service beans");
		assertFalse(availableBeans.isEmpty(), "Should have at least some service beans in Spring context");
		// Look for CActivityService or similar service
		String testServiceName = null;
		for (String beanName : availableBeans) {
			if (beanName.contains("ActivityService") || beanName.contains("ProjectService") || beanName.contains("UserService")) {
				testServiceName = beanName;
				break;
			}
		}
		// If we found a service, test the field extraction
		if (testServiceName != null) {
			final List<String> fields = viewsService.getEntityFieldsForService(testServiceName);
			// Basic validations
			assertNotNull(fields, "Should return a list of field names");
			assertFalse(fields.isEmpty(), "Should have at least some fields for the entity");
			// Log the results for manual verification
			System.out.println("Service: " + testServiceName);
			System.out.println("Fields found: " + fields);
			// Verify that we have some typical entity fields
			// Most entities should have at least some of these common fields
			boolean hasExpectedFields = fields.stream().anyMatch(field -> field.equals("name") || field.equals("id") || field.equals("description")
					|| field.contains("Date") || field.contains("Type") || field.contains("Status"));
			assertTrue(hasExpectedFields, "Should contain some typical entity fields like name, id, description, etc. Found fields: " + fields);
		} else {
			// If no suitable service was found, just verify the method works without errors
			final List<String> fields = viewsService.getEntityFieldsForService("CActivityService");
			assertNotNull(fields, "Should return a list even if service doesn't exist");
		}
	}

	@Test
	void testGetEntityFieldsForSpecificServices() {
		// Test specific services that should exist in the application
		String[] servicesToTest = {
				"CActivityService", "CProjectService", "CUserService", "CMeetingService"
		};
		for (String serviceName : servicesToTest) {
			final List<String> fields = viewsService.getEntityFieldsForService(serviceName);
			assertNotNull(fields, "Should return a list for service: " + serviceName);
			// If the service exists and has fields, print them for verification
			if (!fields.isEmpty()) {
				System.out.println("\nService: " + serviceName);
				System.out.println("Entity fields: " + fields);
				// Verify fields don't contain obvious internal/static fields
				boolean hasValidFields = fields.stream().noneMatch(field -> field.equals("LOGGER") || field.startsWith("$"));
				assertTrue(hasValidFields, "Should not contain internal fields like LOGGER for service: " + serviceName);
			}
		}
	}
}
