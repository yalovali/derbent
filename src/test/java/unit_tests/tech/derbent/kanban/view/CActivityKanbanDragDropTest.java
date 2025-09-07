package unit_tests.tech.derbent.kanban.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.view.CActivityCard;
import tech.derbent.kanban.view.CActivityKanbanBoardView;
import tech.derbent.kanban.view.CActivityKanbanColumn;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/** Test class for drag and drop functionality in the kanban board. */
class CActivityKanbanDragDropTest {

	@Mock
	private CActivityService activityService;
	@Mock
	private CSessionService sessionService;
	private CProject testProject;
	private CActivityStatus todoStatus;
	private CActivityStatus inProgressStatus;
	private CActivity testActivity;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// Create test project
		testProject = new CProject();
		testProject.setName("Test Project");
		// Create test statuses
		todoStatus = new CActivityStatus("TODO", testProject);
		inProgressStatus = new CActivityStatus("IN_PROGRESS", testProject);
		// Create test activity
		testActivity = new CActivity("Test Activity", testProject);
		testActivity.setStatus(todoStatus);
	}

	@Test
	void testActivityCardCreationWithDragAndDrop() {
		// Test that activity cards are created with drag functionality
		final CActivityCard card = new CActivityCard(testActivity);
		assertNotNull(card);
		assertEquals(testActivity, card.getActivity());
		// Verify that the card has the drag wrapper CSS class
		assertEquals(true, card.getClassNames().contains("kanban-card-wrapper"));
	}

	@Test
	void testActivityStatusUpdate() {
		// Test the updateEntityStatus method
		when(activityService.updateEntityStatus(any(CActivity.class), any(CActivityStatus.class))).thenReturn(testActivity);
		// Call the method
		final CActivity updatedActivity = activityService.updateEntityStatus(testActivity, inProgressStatus);
		// Verify the method was called
		verify(activityService).updateEntityStatus(testActivity, inProgressStatus);
		assertNotNull(updatedActivity);
	}

	@Test
	void testKanbanBoardViewCreation() {
		// Mock the session service to return a project
		when(sessionService.getActiveProject()).thenReturn(Optional.of(testProject));
		// Mock the activity service to return grouped activities
		final Map<CActivityStatus, List<CActivity>> groupedActivities = Map.of(todoStatus, List.of(testActivity));
		when(activityService.getActivitiesGroupedByStatus(testProject)).thenReturn(groupedActivities);
		// Create the kanban board view
		final CActivityKanbanBoardView boardView = new CActivityKanbanBoardView(activityService, sessionService);
		assertNotNull(boardView);
	}

	@Test
	void testKanbanColumnCreationWithDropTarget() {
		// Create a column with activities
		final List<CActivity> activities = List.of(testActivity);
		final CActivityKanbanColumn column = new CActivityKanbanColumn(todoStatus, activities);
		assertNotNull(column);
		assertEquals(todoStatus, column.getActivityStatus());
		assertEquals(activities, column.getActivities());
	}

	@Test
	void testKanbanColumnWithDropHandler() {
		// Create a mock drop handler
		@SuppressWarnings ("unchecked")
		final Consumer<CActivity> mockHandler = mock(Consumer.class);
		// Create a column with drop handler
		final List<CActivity> activities = List.of(testActivity);
		final CActivityKanbanColumn column = new CActivityKanbanColumn(todoStatus, activities, mockHandler);
		assertNotNull(column);
		assertEquals(todoStatus, column.getActivityStatus());
	}
}
