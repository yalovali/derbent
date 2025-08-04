package unit_tests.tech.derbent.comments.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.comments.domain.CComment;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Unit tests for CComment domain class functionality. Tests the comment creation,
 * validation, and basic operations.
 */
@DisplayName ("CComment Domain Tests")
class CCommentTest extends CTestBase {

	private CComment comment;

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	@DisplayName ("Should get activity name correctly")
	void testActivityName() {
		// Given
		comment = new CComment("Test comment", testActivity, testUser);
		// Then
		assertEquals("Test Activity", comment.getActivityName());
	}

	@Test
	@DisplayName ("Should get author name correctly")
	void testAuthorName() {
		// Given
		comment = new CComment("Test comment", testActivity, testUser);
		// Then
		assertEquals("John Doe", comment.getAuthorName());
	}

	@Test
	@DisplayName ("Should create comment with required fields")
	void testCommentCreation() {
		// When
		comment = new CComment("This is a test comment", testActivity, testUser);
		// Then
		assertNotNull(comment);
		assertEquals("This is a test comment", comment.getCommentText());
		assertEquals(testActivity, comment.getActivity());
		assertEquals(testUser, comment.getAuthor());
		assertNotNull(comment.getEventDate());
		assertFalse(comment.isImportant());
	}

	@Test
	@DisplayName ("Should create comment with priority")
	void testCommentCreationWithPriority() {
		// When
		comment = new CComment("High priority comment", testActivity, testUser, priority);
		// Then
		assertNotNull(comment);
		assertEquals("High priority comment", comment.getCommentText());
		assertEquals(priority, comment.getPriority());
		assertEquals("High", comment.getPriorityName());
	}

	@Test
	@DisplayName ("Should generate comment preview correctly")
	void testCommentPreview() {
		// Given - Short comment
		comment = new CComment("Short comment", testActivity, testUser);
		// Then
		assertEquals("Short comment", comment.getCommentPreview());
		// Given - Long comment
		final String longText =
			"This is a very long comment text that exceeds the preview limit of 100 characters. It should be truncated with ellipsis at the end.";
		comment.setCommentText(longText);
		// Then
		assertEquals(100, comment.getCommentPreview().length() - 3); // -3 for "..."
		assertTrue(comment.getCommentPreview().endsWith("..."));
	}

	@Test
	@DisplayName ("Should set and get comment text")
	void testCommentTextSetterGetter() {
		// Given
		comment = new CComment("Initial text", testActivity, testUser);
		// When
		comment.setCommentText("Updated comment text");
		// Then
		assertEquals("Updated comment text", comment.getCommentText());
	}

	@Test
	@DisplayName ("Should handle null priority gracefully")
	void testCommentWithNullPriority() {
		// When
		comment = new CComment("Comment without priority", testActivity, testUser);
		// Then
		assertNull(comment.getPriority());
		assertEquals("Normal", comment.getPriorityName()); // Default when null
	}

	@Test
	@DisplayName ("Should set and get important flag")
	void testImportantFlag() {
		// Given
		comment = new CComment("Important comment", testActivity, testUser);
		assertFalse(comment.isImportant()); // Default is false
		// When
		comment.setImportant(true);
		// Then
		assertTrue(comment.isImportant());
	}

	@Test
	@DisplayName ("Should generate meaningful toString")
	void testToString() {
		// Given
		comment = new CComment("Test comment for toString", testActivity, testUser);
		// When
		final String result = comment.toString();
		// Then
		assertTrue(result.contains("CComment"));
		assertTrue(result.contains("Test Activity"));
		assertTrue(result.contains("John Doe"));
		assertTrue(result.contains("Test comment for toString"));
	}
}