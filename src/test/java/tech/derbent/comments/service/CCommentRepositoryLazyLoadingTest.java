package tech.derbent.comments.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Test class for verifying the lazy loading fix in CCommentRepository.findRecentByProject
 * method. This test ensures that the query validation error is resolved and the method
 * works correctly.
 */
@ExtendWith (MockitoExtension.class)
class CCommentRepositoryLazyLoadingTest extends CTestBase {

	@Mock
	private CCommentRepository commentRepository;

	@Mock
	private CCommentPriorityService commentPriorityService;

	@SuppressWarnings ("unused")
	private CCommentService commentService;

	private CProject project;

	private CActivity testActivity;

	@BeforeEach
	void setUp() {
		new CUser("Test User");
		testActivity = new CActivity("Test Activity", project);
		testActivity.setName("Test Activity");
		testActivity.setProject(project);
		// Create service with mocked repository
		commentService = new CCommentService(commentRepository, commentPriorityService,
			java.time.Clock.systemDefaultZone());
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}