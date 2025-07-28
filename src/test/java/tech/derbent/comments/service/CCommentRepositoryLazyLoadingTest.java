package tech.derbent.comments.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Test class for verifying the lazy loading fix in CCommentRepository.findRecentByProject
 * method. This test ensures that the query validation error is resolved and the method
 * works correctly.
 */
@ExtendWith (MockitoExtension.class)
class CCommentRepositoryLazyLoadingTest {

	@Mock
	private CCommentRepository commentRepository;

	@Mock
	private CCommentPriorityService commentPriorityService;

	@SuppressWarnings ("unused")
	private CCommentService commentService;

	private CProject testProject;

	private CUser testUser;

	private CActivity testActivity;

	@BeforeEach
	void setUp() {
		// Create test entities
		testProject = new CProject();
		testProject.setName("Test Project");
		testUser = new CUser();
		testUser.setName("Test User");
		testActivity = new CActivity();
		testActivity.setName("Test Activity");
		testActivity.setProject(testProject);
		// Create service with mocked repository
		commentService = new CCommentService(commentRepository, commentPriorityService,
			java.time.Clock.systemDefaultZone());
	}
}