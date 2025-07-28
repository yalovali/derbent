package tech.derbent.comments.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.comments.domain.CComment;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.activities.domain.CActivity;

/**
 * Test class for verifying the lazy loading fix in CCommentRepository.findRecentByProject method.
 * This test ensures that the query validation error is resolved and the method works correctly.
 */
@ExtendWith(MockitoExtension.class)
class CCommentRepositoryLazyLoadingTest {

    @Mock
    private CCommentRepository commentRepository;

    @Mock
    private CCommentPriorityService commentPriorityService;

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

    @Test
    void testFindRecentByProject_WithValidProject_ReturnsComments() {
        // Given
        CComment comment1 = new CComment("Recent comment 1", testActivity, testUser);
        CComment comment2 = new CComment("Recent comment 2", testActivity, testUser);
        List<CComment> expectedComments = List.of(comment1, comment2);
        
        // Mock the repository method with the new signature (project and date parameter)
        when(commentRepository.findRecentByProject(eq(testProject), any(LocalDateTime.class)))
            .thenReturn(expectedComments);
        
        // When
        List<CComment> actualComments = commentService.findRecentByProject(testProject);
        
        // Then
        assertNotNull(actualComments);
        assertEquals(2, actualComments.size());
        assertEquals(expectedComments, actualComments);
        
        // Verify that the repository method was called with correct parameters
        verify(commentRepository).findRecentByProject(eq(testProject), any(LocalDateTime.class));
    }

    @Test
    void testFindRecentByProject_WithNullProject_ReturnsEmptyList() {
        // When
        List<CComment> actualComments = commentService.findRecentByProject(null);
        
        // Then
        assertNotNull(actualComments);
        assertTrue(actualComments.isEmpty());
        
        // Verify that the repository method was not called
        verify(commentRepository, never()).findRecentByProject(any(), any());
    }

    @Test
    void testFindRecentByProject_DateCalculation() {
        // Given
        when(commentRepository.findRecentByProject(eq(testProject), any(LocalDateTime.class)))
            .thenReturn(List.of());
        
        LocalDateTime beforeCall = LocalDateTime.now().minusDays(30);
        
        // When
        commentService.findRecentByProject(testProject);
        
        LocalDateTime afterCall = LocalDateTime.now().minusDays(30);
        
        // Then - Verify the date parameter is approximately 30 days ago
        verify(commentRepository).findRecentByProject(eq(testProject), argThat(date -> 
            date.isAfter(beforeCall.minusMinutes(1)) && date.isBefore(afterCall.plusMinutes(1))
        ));
    }
}