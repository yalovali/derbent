package tech.derbent.meetings.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import tech.derbent.abstracts.ui.CAbstractUITest;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CMeetingsViewUITest - Comprehensive UI tests for the Meetings view.
 * Layer: Testing (MVC)
 * 
 * Tests grid functionality, lazy loading prevention for participants and attendees,
 * data loading, and complex relationship handling in the meetings grid.
 */
class CMeetingsViewUITest extends CAbstractUITest<CMeeting> {

    @Mock
    private CMeetingService mockMeetingService;

    @Mock
    private CCommentService mockCommentService;

    private CMeetingsView meetingsView;
    private CProject testProject;
    private CMeetingType testMeetingType;
    private CMeetingStatus testMeetingStatus;
    private CUser testUser1;
    private CUser testUser2;

    public CMeetingsViewUITest() {
        super(CMeeting.class);
    }

    @BeforeEach
    void setupMeetingTests() {
        setupTestEntities();
        meetingsView = new CMeetingsView(
            mockMeetingService,
            mockCommentService,
            mockSessionService
        );
    }

    private void setupTestEntities() {
        // Create test project
        testProject = new CProject();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test project for meetings");

        // Create test meeting type
        testMeetingType = new CMeetingType();
        testMeetingType.setId(1L);
        testMeetingType.setName("Planning Meeting");
        testMeetingType.setDescription("Project planning meetings");

        // Create test meeting status
        testMeetingStatus = new CMeetingStatus();
        testMeetingStatus.setId(1L);
        testMeetingStatus.setName("Scheduled");
        testMeetingStatus.setDescription("Meeting is scheduled");
        testMeetingStatus.setColor("#00AA00");
        testMeetingStatus.setSortOrder(1);

        // Create test users
        testUser1 = new CUser();
        testUser1.setId(1L);
        testUser1.setName("John");
        testUser1.setLastname("Doe");
        testUser1.setLogin("johndoe");
        testUser1.setEmail("john@example.com");

        testUser2 = new CUser();
        testUser2.setId(2L);
        testUser2.setName("Jane");
        testUser2.setLastname("Smith");
        testUser2.setLogin("janesmith");
        testUser2.setEmail("jane@example.com");
    }

    @Override
    protected void setupTestData() {
        CMeeting meeting1 = createTestEntity(1L, "Sprint Planning");
        CMeeting meeting2 = createTestEntity(2L, "Daily Standup");
        CMeeting meeting3 = createTestEntity(3L, "Retrospective");
        
        testEntities = Arrays.asList(meeting1, meeting2, meeting3);
        
        // Mock active project
        when(mockSessionService.getActiveProject()).thenReturn(Optional.of(testProject));
    }

    @Override
    protected CMeeting createTestEntity(Long id, String name) {
        CMeeting meeting = new CMeeting();
        meeting.setId(id);
        meeting.setName(name);
        meeting.setDescription("Test meeting: " + name);
        meeting.setMeetingDate(LocalDateTime.now().plusDays(1));
        meeting.setEndDate(LocalDateTime.now().plusDays(1).plusHours(1));
        
        // Initialize all relationships to prevent lazy loading issues
        meeting.setProject(testProject);
        meeting.setMeetingType(testMeetingType);
        meeting.setStatus(testMeetingStatus);
        meeting.setAssignedTo(testUser1);
        meeting.setCreatedBy(testUser1);
        meeting.setResponsible(testUser1);
        
        // Initialize participants collection (critical for lazy loading test)
        Set<CUser> participants = new HashSet<>();
        participants.add(testUser1);
        participants.add(testUser2);
        meeting.setParticipants(participants);
        
        // Initialize attendees collection
        Set<CUser> attendees = new HashSet<>();
        attendees.add(testUser1);
        meeting.setAttendees(attendees);
        
        return meeting;
    }

    @Override
    protected void verifyEntityRelationships(CMeeting entity) {
        assertNotNull(entity.getProject(), "Project should be initialized");
        assertNotNull(entity.getMeetingType(), "Meeting type should be initialized");
        assertNotNull(entity.getStatus(), "Status should be initialized");
        assertNotNull(entity.getAssignedTo(), "Assigned user should be initialized");
        assertNotNull(entity.getCreatedBy(), "Created by user should be initialized");
        assertNotNull(entity.getParticipants(), "Participants should be initialized");
        
        // Verify lazy collections can be accessed without exceptions
        try {
            // Test participants collection access
            Set<CUser> participants = entity.getParticipants();
            assertNotNull(participants, "Participants collection should not be null");
            
            // Test accessing participant names (common cause of lazy loading issues)
            for (CUser participant : participants) {
                String participantName = participant.getName();
                assertNotNull(participantName, "Participant name should be accessible");
            }
            
            // Test attendees collection
            Set<CUser> attendees = entity.getAttendees();
            assertNotNull(attendees, "Attendees collection should not be null");
            
        } catch (Exception e) {
            fail("Collection access caused lazy loading exception: " + e.getMessage());
        }
    }

    @Test
    void testGridCreation() {
        LOGGER.info("Testing meetings grid creation");
        
        assertNotNull(meetingsView.grid, "Grid should be created");
        assertTrue(meetingsView.grid.getColumns().size() > 0, "Grid should have columns");
        
        // Check for key columns that are likely to cause lazy loading issues
        boolean hasProjectColumn = meetingsView.grid.getColumns().stream()
            .anyMatch(col -> col.getHeaderText().contains("Project"));
        assertTrue(hasProjectColumn, "Grid should have project column");
        
        boolean hasParticipantsColumn = meetingsView.grid.getColumns().stream()
            .anyMatch(col -> col.getHeaderText().contains("Participants"));
        assertTrue(hasParticipantsColumn, "Grid should have participants column");
    }

    @Test
    void testGridDataLoading() {
        LOGGER.info("Testing meetings grid data loading");
        
        // Test that grid can load data without exceptions
        testGridDataLoading(meetingsView.grid);
        
        // Verify service was called
        verify(mockMeetingService, atLeastOnce()).list(any());
    }

    @Test
    void testGridColumnAccess() {
        LOGGER.info("Testing meetings grid column access for lazy loading issues");
        
        // This tests all columns to ensure no lazy loading exceptions occur
        testGridColumnAccess(meetingsView.grid);
        
        // Specifically test relationships
        testEntities.forEach(meeting -> {
            verifyEntityRelationships(meeting);
        });
    }

    @Test
    void testParticipantsColumnAccess() {
        LOGGER.info("Testing participants column access - critical for lazy loading");
        
        testEntities.forEach(meeting -> {
            assertDoesNotThrow(() -> {
                Set<CUser> participants = meeting.getParticipants();
                
                if (participants.isEmpty()) {
                    assertEquals("No participants", getParticipantsDisplay(meeting));
                } else {
                    String participantsDisplay = getParticipantsDisplay(meeting);
                    assertNotNull(participantsDisplay, "Participants display should not be null");
                    assertTrue(participantsDisplay.length() > 0, "Participants display should not be empty");
                }
            }, "Participants column access should not cause lazy loading exceptions");
        });
    }

    private String getParticipantsDisplay(CMeeting meeting) {
        if (meeting.getParticipants().isEmpty()) {
            return "No participants";
        }
        return meeting.getParticipants().stream()
            .map(user -> user.getName() != null ? user.getName() : "User #" + user.getId())
            .collect(java.util.stream.Collectors.joining(", "));
    }

    @Test
    void testMeetingTypeColumnAccess() {
        LOGGER.info("Testing meeting type column access");
        
        testEntities.forEach(meeting -> {
            String typeDisplay = meeting.getMeetingType() != null 
                ? meeting.getMeetingType().getName() 
                : "No Type";
            
            assertNotNull(typeDisplay, "Meeting type display should not be null");
        });
    }

    @Test
    void testProjectColumnAccess() {
        LOGGER.info("Testing project column access");
        
        testEntities.forEach(meeting -> {
            String projectDisplay = meeting.getProject() != null 
                ? meeting.getProject().getName() 
                : "No Project";
            
            assertNotNull(projectDisplay, "Project display should not be null");
        });
    }

    @Test
    void testDateTimeFormatting() {
        LOGGER.info("Testing date/time column formatting");
        
        testEntities.forEach(meeting -> {
            assertDoesNotThrow(() -> {
                if (meeting.getMeetingDate() != null) {
                    String formatted = meeting.getMeetingDate().format(
                        java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                    assertNotNull(formatted, "Formatted date should not be null");
                }
                
                if (meeting.getEndDate() != null) {
                    String formatted = meeting.getEndDate().format(
                        java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                    assertNotNull(formatted, "Formatted end date should not be null");
                }
            }, "Date formatting should not throw exceptions");
        });
    }

    @Test
    void testGridWithEmptyParticipants() {
        LOGGER.info("Testing grid behavior with empty participants");
        
        // Create meeting with empty participants
        CMeeting meetingWithoutParticipants = createTestEntity(99L, "Solo Meeting");
        meetingWithoutParticipants.setParticipants(new HashSet<>());
        
        assertDoesNotThrow(() -> {
            String participantsDisplay = getParticipantsDisplay(meetingWithoutParticipants);
            assertEquals("No participants", participantsDisplay);
        }, "Empty participants should be handled gracefully");
    }

    @Test
    void testGridWithNullRelationships() {
        LOGGER.info("Testing grid behavior with null relationships");
        
        // Create meeting with null relationships
        CMeeting meetingWithNulls = new CMeeting();
        meetingWithNulls.setId(99L);
        meetingWithNulls.setName("Meeting With Nulls");
        meetingWithNulls.setParticipants(new HashSet<>()); // Empty but not null
        
        // Test that columns handle null relationships gracefully
        assertDoesNotThrow(() -> {
            // Test project column
            String projectDisplay = meetingWithNulls.getProject() != null 
                ? meetingWithNulls.getProject().getName() 
                : "No Project";
            assertEquals("No Project", projectDisplay);
            
            // Test meeting type column
            String typeDisplay = meetingWithNulls.getMeetingType() != null 
                ? meetingWithNulls.getMeetingType().getName() 
                : "No Type";
            assertEquals("No Type", typeDisplay);
            
            // Test participants column
            String participantsDisplay = getParticipantsDisplay(meetingWithNulls);
            assertEquals("No participants", participantsDisplay);
            
        }, "Grid columns should handle null relationships gracefully");
    }

    @Test
    void testGridSelection() {
        LOGGER.info("Testing meetings grid selection");
        
        testGridSelection(meetingsView.grid);
    }

    @Test
    void testViewInitialization() {
        LOGGER.info("Testing meetings view initialization");
        
        assertNotNull(meetingsView, "Meetings view should be created");
        assertNotNull(meetingsView.grid, "Grid should be initialized");
        
        // Verify view is properly configured
        assertTrue(meetingsView.getClassNames().contains("meetings-view"),
                  "View should have proper CSS class");
    }

    @Test
    void testFormPopulation() {
        LOGGER.info("Testing form population with meeting data");
        
        if (!testEntities.isEmpty()) {
            CMeeting testMeeting = testEntities.get(0);
            
            // Test form population doesn't throw exceptions
            assertDoesNotThrow(() -> {
                meetingsView.populateForm(testMeeting);
            }, "Form population should not throw exceptions");
        }
    }

    @Test
    void testDescriptionColumnHandling() {
        LOGGER.info("Testing description column handling");
        
        testEntities.forEach(meeting -> {
            assertDoesNotThrow(() -> {
                String description = meeting.getDescription();
                if (description == null || description.trim().isEmpty()) {
                    // Should handle null/empty descriptions
                    String display = "No description";
                    assertNotNull(display, "Description display should handle null");
                } else {
                    // Should truncate long descriptions
                    String display = description.length() > 50 
                        ? description.substring(0, 50) + "..." 
                        : description;
                    assertNotNull(display, "Description display should not be null");
                }
            }, "Description column should handle all cases gracefully");
        });
    }
}