package unit_tests.tech.derbent.comments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.derbent.comments.domain.CComment;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Unit tests for CCommentService. Tests service layer business logic with mocked dependencies. */
@DisplayName ("CCommentService Tests")
@ExtendWith (MockitoExtension.class)
class CCommentServiceTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
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
	@DisplayName ("Should find comments by activity ordered by date")
	void testFindByActivityOrderByEventDateAsc() {
		// Given
		final CComment comment1 = new CComment("First comment", testActivity, testUser);
		final CComment comment2 = new CComment("Second comment", testActivity, testUser);
		final List<CComment> expectedComments = Arrays.asList(comment1, comment2);
		when(commentRepository.findByActivity(testActivity)).thenReturn(expectedComments);
		// When
		final List<CComment> result = commentService.findByActivity(testActivity);
		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(expectedComments, result);
	}

	@Test
	@DisplayName ("Should handle null activity gracefully")
	void testFindByActivityWithNullActivity() {
		// When
		final List<CComment> result = commentService.findByActivity(null);
		// Then
		assertNotNull(result);
		assertEquals(0, result.size());
	}
}
