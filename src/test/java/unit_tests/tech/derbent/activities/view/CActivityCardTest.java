package unit_tests.tech.derbent.activities.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.view.CActivityCard;
import tech.derbent.projects.domain.CProject;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Unit tests for CActivityCard component. */
class CActivityCardTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testActivityCardCreation() {
		// Given
		final CProject project = new CProject("asdf");
		project.setName("Test Project");
		final CActivity activity = new CActivity("Test Activity", project);
		// When
		final CActivityCard card = new CActivityCard(activity);
		// Then
		assertNotNull(card);
		assertEquals(activity, card.getActivity());
	}

	@Test
	void testActivityCardRefresh() {
		// Given
		final CProject project = new CProject("asdf");
		project.setName("Test Project");
		final CActivity activity = new CActivity("Test Activity", project);
		final CActivityCard card = new CActivityCard(activity);
		// When
		card.refresh();
		// Then - should not throw exception
		assertNotNull(card);
	}

	@Test
	void testActivityCardWithNullThrowsException() {
		// Given - null activity When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			new CActivityCard(null);
		});
	}
}
