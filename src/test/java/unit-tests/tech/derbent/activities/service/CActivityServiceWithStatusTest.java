package tech.derbent.activities.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;

/**
 * Test class for CActivityService with new CActivityStatus relationship. Tests lazy
 * loading functionality for both CActivityType and CActivityStatus.
 */
class CActivityServiceWithStatusTest extends CTestBase {

	@Override
	protected void setupForTest() {}

	@Test
	void testInitializeLazyFieldsWithBothRelationships() {
		assertDoesNotThrow(() -> activityService.initializeLazyFields(testActivity));
	}

	@Test
	void testInitializeLazyFieldsWithNullActivityStatus() {
		// Given Create test project for the type
		project.setName("Test Project");
		final CActivity activity = new CActivity("Test Activity", project);
		final CActivityType type = new CActivityType("Development", project);
		activity.setActivityType(type);
		// activityStatus is null When/Then - should not throw exception
		assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
	}

	@Test
	void testInitializeLazyFieldsWithNullActivityType() {
		// Given
		final CActivity activity = new CActivity("Test Activity", project);
		activity.setName("Test Activity");
		final CActivityStatus status = new CActivityStatus("TODO", project);
		activity.setStatus(status);
		// activityType is null When/Then - should not throw exception
		assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
	}

	@Test
	void testInitializeLazyFieldsWithNullBothRelationships() {
		// Given
		final CActivity activity = new CActivity("Test Activity", project);
		activity.setName("Test Activity");
		// Both activityType and activityStatus are null When/Then - should not throw
		// exception
		assertDoesNotThrow(() -> activityService.initializeLazyFields(activity));
	}

	@Test
	void testInitializeLazyFieldsWithNullEntity() {
		// When/Then - should not throw exception
		assertDoesNotThrow(() -> activityService.initializeLazyFields(null));
	}
}