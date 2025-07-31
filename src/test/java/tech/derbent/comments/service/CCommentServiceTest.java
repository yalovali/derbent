package tech.derbent.comments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.comments.domain.CComment;
import tech.derbent.comments.domain.CCommentPriority;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for CCommentService. Tests service layer business logic with mocked
 * dependencies.
 */
@DisplayName ("CCommentService Tests")
@ExtendWith (MockitoExtension.class)
class CCommentServiceTest {

	@Mock
	private CCommentRepository commentRepository;

	@Mock
	private CCommentPriorityService commentPriorityService;

	private CCommentService commentService;

	private CActivity testActivity;

	private CUser testUser;

	private CProject testProject;

	private CCommentPriority testPriority;

	private final Clock fixedClock =
		Clock.fixed(Instant.parse("2024-01-15T10:30:00Z"), ZoneId.systemDefault());

	@BeforeEach
	void setUp() {
		commentService =
			new CCommentService(commentRepository, commentPriorityService, fixedClock);
		// Set up test data
		testProject = new CProject();
		testProject.setName("Test Project");
		testActivity = new CActivity();
		testActivity.setName("Test Activity");
		testActivity.setProject(testProject);
		testUser = new CUser();
		testUser.setName("Test User");
		testUser.setLogin("test.user");
		testPriority = new CCommentPriority("Normal", new CProject());
	}

	@Test
	@DisplayName ("Should count comments by activity")
	void testCountByActivity() {
		// Given
		final long expectedCount = 5L;
		when(commentRepository.countByActivity(testActivity)).thenReturn(expectedCount);
		// When
		final long result = commentService.countByActivity(testActivity);
		// Then
		assertEquals(expectedCount, result);
	}

	@Test
	@DisplayName ("Should create comment with validation")
	void testCreateComment() {
		// Given
		final String commentText = "Test comment text";
		final CComment expectedComment =
			new CComment(commentText, testActivity, testUser);
		when(commentPriorityService.findDefaultPriority())
			.thenReturn(Optional.of(testPriority));
		when(commentRepository.save(any(CComment.class))).thenReturn(expectedComment);
		// When
		final CComment result =
			commentService.createComment(commentText, testActivity, testUser);
		// Then
		assertNotNull(result);
		assertEquals(expectedComment, result);
	}

	@Test
	@DisplayName ("Should find comments by activity ordered by date")
	void testFindByActivityOrderByEventDateAsc() {
		// Given
		final CComment comment1 = new CComment("First comment", testActivity, testUser);
		final CComment comment2 = new CComment("Second comment", testActivity, testUser);
		final List<CComment> expectedComments = Arrays.asList(comment1, comment2);
		when(commentRepository.findByActivityOrderByEventDateAsc(testActivity))
			.thenReturn(expectedComments);
		// When
		final List<CComment> result =
			commentService.findByActivityOrderByEventDateAsc(testActivity);
		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(expectedComments, result);
	}

	@Test
	@DisplayName ("Should handle null activity gracefully")
	void testFindByActivityWithNullActivity() {
		// When
		final List<CComment> result =
			commentService.findByActivityOrderByEventDateAsc(null);
		// Then
		assertNotNull(result);
		assertEquals(0, result.size());
	}
}