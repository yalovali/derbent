package tech.derbent.comments.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.comments.domain.CComment;
import tech.derbent.comments.service.CCommentService;

/**
 * CCommentViewTest - Test class for CCommentView component. Layer: Test (MVC) Tests the
 * UI component functionality including: - Constructor with comment entity - Constructor
 * with comment ID and service - Error handling for null parameters - Comment display
 * functionality
 */
class CCommentViewTest extends CTestBase {

	private CActivity testActivity;

	private CComment testComment;

	private CCommentService mockCommentService;

	@Override
	protected void setupForTest() {
		// Create test entities
		testActivity = new CActivity("Test Activity", project);
		testComment = new CComment("Test comment text", testActivity, testUser);
		// Mock the comment service
		mockCommentService = mock(CCommentService.class);
	}

	@Test
	void testConstructorWithComment() {
		// Test: Create CCommentView with comment entity (read-only)
		final CCommentView commentView = new CCommentView(testComment);
		// Verify: Component was created successfully
		assertNotNull(commentView);
		assertEquals(testComment, commentView.getComment());
		assertTrue(commentView.getElement().hasAttribute("class"));
		assertTrue(
			commentView.getElement().getAttribute("class").contains("comment-view"));
		assertFalse(commentView.isEditing());
	}

	@Test
	void testConstructorWithCommentAndService() {
		// Test: Create CCommentView with comment entity and service (editable)
		final CCommentView commentView =
			new CCommentView(testComment, mockCommentService);
		// Verify: Component was created successfully
		assertNotNull(commentView);
		assertEquals(testComment, commentView.getComment());
		assertTrue(commentView.getElement().hasAttribute("class"));
		assertTrue(
			commentView.getElement().getAttribute("class").contains("comment-view"));
		assertFalse(commentView.isEditing());
	}

	@Test
	void testConstructorWithCommentId() {
		// Given: Mock service returns the test comment
		when(mockCommentService.getById(1L)).thenReturn(Optional.of(testComment));
		// Test: Create CCommentView with comment ID and service
		final CCommentView commentView = new CCommentView(1L, mockCommentService);
		// Verify: Component was created successfully with loaded comment
		assertNotNull(commentView);
		assertEquals(testComment, commentView.getComment());
		assertTrue(commentView.getElement().hasAttribute("class"));
		assertTrue(
			commentView.getElement().getAttribute("class").contains("comment-view"));
		assertFalse(commentView.isEditing());
	}

	@Test
	void testConstructorWithNonExistentCommentId() {
		// Given: Mock service returns empty for non-existent comment
		when(mockCommentService.getById(anyLong())).thenReturn(Optional.empty());
		// Test: Create CCommentView with non-existent comment ID
		final IllegalArgumentException exception =
			assertThrows(IllegalArgumentException.class,
				() -> new CCommentView(999L, mockCommentService));
		// Verify: Appropriate exception is thrown
		assertEquals("Comment with ID 999 not found", exception.getMessage());
	}

	@Test
	void testConstructorWithNullComment() {
		// Test: Create CCommentView with null comment
		final IllegalArgumentException exception =
			assertThrows(IllegalArgumentException.class, () -> new CCommentView(null));
		// Verify: Appropriate exception is thrown
		assertEquals("Comment cannot be null", exception.getMessage());
	}

	@Test
	void testConstructorWithNullCommentId() {
		// Test: Create CCommentView with null comment ID
		final Long nullId = null;
		final IllegalArgumentException exception =
			assertThrows(IllegalArgumentException.class,
				() -> new CCommentView(nullId, mockCommentService));
		// Verify: Appropriate exception is thrown
		assertEquals("Comment ID cannot be null", exception.getMessage());
	}

	@Test
	void testConstructorWithNullService() {
		// Test: Create CCommentView with null service
		final IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class, () -> new CCommentView(1L, null));
		// Verify: Appropriate exception is thrown
		assertEquals("Comment service cannot be null", exception.getMessage());
	}

	@Test
	void testIsEditingInitiallyFalse() {
		// Test: Create comment views
		final CCommentView readOnlyView = new CCommentView(testComment);
		final CCommentView editableView =
			new CCommentView(testComment, mockCommentService);
		// Verify: Both views are not in editing mode initially
		assertFalse(readOnlyView.isEditing());
		assertFalse(editableView.isEditing());
	}

	@Test
	void testRefresh() {
		// Given: CCommentView with test comment
		final CCommentView commentView = new CCommentView(testComment);
		// Test: Call refresh method
		assertDoesNotThrow(() -> commentView.refresh());
		// Verify: Component still displays the same comment
		assertEquals(testComment, commentView.getComment());
	}
}