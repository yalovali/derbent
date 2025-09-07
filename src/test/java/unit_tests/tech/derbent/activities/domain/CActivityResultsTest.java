package unit_tests.tech.derbent.activities.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import tech.derbent.activities.domain.CActivity;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Test class for CActivity results field functionality. */
class CActivityResultsTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// Test setup if needed
	}

	@Test
	void testActivityResultsField() {
		// Given
		final String activityName = "Test Activity";
		final String results = "Successfully completed with positive outcomes";
		// When
		final CActivity activity = new CActivity(activityName, project);
		activity.setResults(results);
		// Then
		assertNotNull(activity);
		assertEquals(activityName, activity.getName());
		assertEquals(results, activity.getResults());
		assertEquals(project, activity.getProject());
	}

	@Test
	void testActivityResultsFieldEmpty() {
		// Given
		final String activityName = "Test Activity";
		// When
		final CActivity activity = new CActivity(activityName, project);
		// Then
		assertNotNull(activity);
		assertEquals(activityName, activity.getName());
		assertEquals(null, activity.getResults()); // Should be null by default
	}

	@Test
	void testActivityResultsFieldUpdate() {
		// Given
		final String activityName = "Test Activity";
		final String initialResults = "Initial results";
		final String updatedResults = "Updated results with more details";
		// When
		final CActivity activity = new CActivity(activityName, project);
		activity.setResults(initialResults);
		assertEquals(initialResults, activity.getResults());
		activity.setResults(updatedResults);
		// Then
		assertEquals(updatedResults, activity.getResults());
	}
}
