package tech.derbent.comments.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for CComment domain class functionality. Tests the comment creation, validation, and basic operations.
 */
@DisplayName("CComment Domain Tests")
class CCommentTest extends CTestBase {

    private CComment comment;

    private CActivity activity;

    private CProject project;

    private CUser author;

    private CCommentPriority priority;

    @BeforeEach
    void setUp() {
        // Create test project
        project = new CProject("Test Project");
        // Create test activity
        activity = new CActivity("Test Activity", project);
        activity.setName("Test Activity");
        activity.setProject(project);
        // Create test user (author)
        author = new CUser("John Doe");
        author.setLogin("john.doe");
        // Create test priority
        priority = new CCommentPriority("High", project);
        priority.setColor("#FF0000");
    }

    @Test
    @DisplayName("Should get activity name correctly")
    void testActivityName() {
        // Given
        comment = new CComment("Test comment", activity, author);
        // Then
        assertEquals("Test Activity", comment.getActivityName());
    }

    @Test
    @DisplayName("Should get author name correctly")
    void testAuthorName() {
        // Given
        comment = new CComment("Test comment", activity, author);
        // Then
        assertEquals("John Doe", comment.getAuthorName());
    }

    @Test
    @DisplayName("Should create comment with required fields")
    void testCommentCreation() {
        // When
        comment = new CComment("This is a test comment", activity, author);
        // Then
        assertNotNull(comment);
        assertEquals("This is a test comment", comment.getCommentText());
        assertEquals(activity, comment.getActivity());
        assertEquals(author, comment.getAuthor());
        assertNotNull(comment.getEventDate());
        assertFalse(comment.isImportant());
    }

    @Test
    @DisplayName("Should create comment with priority")
    void testCommentCreationWithPriority() {
        // When
        comment = new CComment("High priority comment", activity, author, priority);
        // Then
        assertNotNull(comment);
        assertEquals("High priority comment", comment.getCommentText());
        assertEquals(priority, comment.getPriority());
        assertEquals("High", comment.getPriorityName());
    }

    @Test
    @DisplayName("Should generate comment preview correctly")
    void testCommentPreview() {
        // Given - Short comment
        comment = new CComment("Short comment", activity, author);
        // Then
        assertEquals("Short comment", comment.getCommentPreview());
        // Given - Long comment
        final String longText = "This is a very long comment text that exceeds the preview limit of 100 characters. It should be truncated with ellipsis at the end.";
        comment.setCommentText(longText);
        // Then
        assertEquals(100, comment.getCommentPreview().length() - 3); // -3 for "..."
        assertTrue(comment.getCommentPreview().endsWith("..."));
    }

    @Test
    @DisplayName("Should set and get comment text")
    void testCommentTextSetterGetter() {
        // Given
        comment = new CComment("Initial text", activity, author);
        // When
        comment.setCommentText("Updated comment text");
        // Then
        assertEquals("Updated comment text", comment.getCommentText());
    }

    @Test
    @DisplayName("Should handle null priority gracefully")
    void testCommentWithNullPriority() {
        // When
        comment = new CComment("Comment without priority", activity, author);
        // Then
        assertNull(comment.getPriority());
        assertEquals("Normal", comment.getPriorityName()); // Default when null
    }

    @Test
    @DisplayName("Should set and get important flag")
    void testImportantFlag() {
        // Given
        comment = new CComment("Important comment", activity, author);
        assertFalse(comment.isImportant()); // Default is false
        // When
        comment.setImportant(true);
        // Then
        assertTrue(comment.isImportant());
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void testToString() {
        // Given
        comment = new CComment("Test comment for toString", activity, author);
        // When
        final String result = comment.toString();
        // Then
        assertTrue(result.contains("CComment"));
        assertTrue(result.contains("Test Activity"));
        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("Test comment for toString"));
    }

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub

    }
}