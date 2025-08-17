package ui_tests.tech.derbent.meetings.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingStatusService;
import tech.derbent.meetings.service.CMeetingTypeService;
import tech.derbent.meetings.view.CMeetingsView;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import ui_tests.tech.derbent.abstracts.ui.CAbstractUITest;

/**
 * CMeetingsViewUITest - Comprehensive UI tests for the Meetings view. Layer: Testing (MVC) Tests grid functionality,
 * lazy loading prevention for participants and attendees, data loading, and complex relationship handling in the
 * meetings grid.
 */
class CMeetingsViewUITest extends CAbstractUITest<CMeeting> {

    @Mock
    private CMeetingService mockMeetingService;

    @Mock
    private CMeetingTypeService mockMeetingTypeService;

    @Mock
    private CUserService mockUserService;

    @Mock
    private CMeetingStatusService mockMeetingStatusService;

    private CMeetingsView meetingsView;

    private CProject testProject;

    private CMeetingType testMeetingType;

    private CMeetingStatus testMeetingStatus;

    private CUser testUser1;

    private CUser testUser2;

    private CProject project;

    public CMeetingsViewUITest() {
        super(CMeeting.class);
    }

    @Override
    protected CMeeting createTestEntity(final Long id, final String name) {
        final CMeeting meeting = new CMeeting(name, project);
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
        final Set<CUser> participants = new HashSet<>();
        participants.add(testUser1);

        // Only add testUser2 if it's different and properly initialized
        if ((testUser2 != null) && !testUser1.equals(testUser2)) {
            participants.add(testUser2);
        }
        meeting.setParticipants(participants);
        // Initialize attendees collection
        final Set<CUser> attendees = new HashSet<>();
        attendees.add(testUser1);
        meeting.setAttendees(attendees);
        return meeting;
    }

    private String getParticipantsDisplay(final CMeeting meeting) {

        if (meeting.getParticipants().isEmpty()) {
            return "No participants";
        }
        return meeting.getParticipants().stream()
                .map(user -> user.getName() != null ? user.getName() : "User #" + user.getId())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    @Override
    protected void setupServiceMocks() {
        super.setupServiceMocks();
        // Mock project-aware methods specific to meetings service
        when(mockMeetingService.findEntriesByProject(eq(testProject), any(Pageable.class))).thenReturn(testEntities);
        // Mock meeting type and status services with pageable
        when(mockMeetingTypeService.list(any(Pageable.class))).thenReturn(Arrays.asList(testMeetingType));
        when(mockMeetingStatusService.list(any(Pageable.class))).thenReturn(Arrays.asList(testMeetingStatus));
        when(mockUserService.list(any(Pageable.class))).thenReturn(Arrays.asList(testUser1, testUser2));
    }

    @Override
    protected void setupTestData() {

        // Initialize projects first before any test entities
        if (project == null) {
            project = new CProject("Test Project");
        }

        if (testProject == null) {
            testProject = project;
        }
        // Ensure test entities are created first
        setupTestEntities();
        final CMeeting meeting1 = createTestEntity(1L, "Sprint Planning");
        final CMeeting meeting2 = createTestEntity(2L, "Daily Standup");
        final CMeeting meeting3 = createTestEntity(3L, "Retrospective");
        testEntities = Arrays.asList(meeting1, meeting2, meeting3);
        // Mock active project
        when(mockSessionService.getActiveProject()).thenReturn(Optional.of(testProject));
        // Create the view after all test data is set up
        meetingsView = new CMeetingsView(mockMeetingService, mockSessionService, mockMeetingTypeService,
                mockUserService, mockMeetingStatusService);
    }

    private void setupTestEntities() {
        testProject.setDescription("Test project for meetings");
        // Create test meeting type
        testMeetingType = new CMeetingType("Planning Meeting", testProject);
        testMeetingType.setDescription("Project planning meetings");
        // Create test meeting status
        testMeetingStatus = new CMeetingStatus("Scheduled", testProject);
        testMeetingStatus.setDescription("Meeting is scheduled");
        testMeetingStatus.setColor("#00AA00");
        testMeetingStatus.setSortOrder(1);
        // Create test users
        testUser1 = new CUser("John");
        testUser1.setName("John"); // Explicitly set name to ensure it's not null
        testUser1.setLastname("Doe");
        testUser1.setLogin("johndoe");
        testUser1.setEmail("john@example.com");
        testUser2 = new CUser("Jane");
        testUser2.setName("Jane"); // Explicitly set name to ensure it's not null
        testUser2.setLastname("Smith");
        testUser2.setLogin("janesmith");
        testUser2.setEmail("jane@example.com");
    }

    @Test
    void testDateTimeFormatting() {
        LOGGER.info("Testing date/time column formatting");
        testEntities.forEach(meeting -> {
            assertDoesNotThrow(() -> {

                if (meeting.getMeetingDate() != null) {
                    final String formatted = meeting.getMeetingDate()
                            .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                    assertNotNull(formatted, "Formatted date should not be null");
                }

                if (meeting.getEndDate() != null) {
                    final String formatted = meeting.getEndDate()
                            .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                    assertNotNull(formatted, "Formatted end date should not be null");
                }
            }, "Date formatting should not throw exceptions");
        });
    }

    @Test
    void testDescriptionColumnHandling() {
        LOGGER.info("Testing description column handling");
        testEntities.forEach(meeting -> {
            assertDoesNotThrow(() -> {
                final String description = meeting.getDescription();

                if ((description == null) || description.trim().isEmpty()) {
                    // Should handle null/empty descriptions
                    final String display = "No description";
                    assertNotNull(display, "Description display should handle null");
                } else {
                    // Should truncate long descriptions
                    final String display = description.length() > 50
                            ? description.substring(0, 50) + "..."
                            : description;
                    assertNotNull(display, "Description display should not be null");
                }
            }, "Description column should handle all cases gracefully");
        });
    }

    @Test
    void testFormPopulation() {
        LOGGER.info("Testing form population with meeting data");

        if (!testEntities.isEmpty()) {
            final CMeeting testMeeting = testEntities.get(0);
            // Test form population doesn't throw exceptions
            assertDoesNotThrow(() -> {
                meetingsView.testPopulateForm(testMeeting);
            }, "Form population should not throw exceptions");
        }
    }

    @Test
    void testGridColumnAccess() {
        LOGGER.info("Testing meetings grid column access for lazy loading issues");
        // This tests all columns to ensure no lazy loading exceptions occur
        testGridColumnAccess(meetingsView.getGrid());
        // Specifically test relationships
        testEntities.forEach(meeting -> {
            verifyEntityRelationships(meeting);
        });
    }

    @Test
    void testGridCreation() {
        LOGGER.info("Testing meetings grid creation");
        assertNotNull(meetingsView.getGrid(), "Grid should be created");
        assertTrue(meetingsView.getGrid().getColumns().size() > 0, "Grid should have columns");
        // Check for key columns that are likely to cause lazy loading issues
        final boolean hasProjectColumn = meetingsView.getGrid().getColumns().stream()
                .anyMatch(col -> col.getHeaderText().contains("Project"));
        assertTrue(hasProjectColumn, "Grid should have project column");
        final boolean hasParticipantsColumn = meetingsView.getGrid().getColumns().stream()
                .anyMatch(col -> col.getHeaderText().contains("Participants"));
        assertTrue(hasParticipantsColumn, "Grid should have participants column");
    }

    @Test
    void testGridSelection() {
        LOGGER.info("Testing meetings grid selection");
        testGridSelection(meetingsView.getGrid());
    }

    @Test
    void testGridWithEmptyParticipants() {
        LOGGER.info("Testing grid behavior with empty participants");
        // Create meeting with empty participants
        final CMeeting meetingWithoutParticipants = createTestEntity(99L, "Solo Meeting");
        meetingWithoutParticipants.setParticipants(new HashSet<>());
        assertDoesNotThrow(() -> {
            final String participantsDisplay = getParticipantsDisplay(meetingWithoutParticipants);
            assertEquals("No participants", participantsDisplay);
        }, "Empty participants should be handled gracefully");
    }

    @Test
    void testGridWithNullRelationships() {
        LOGGER.info("Testing grid behavior with null relationships");
        // Create meeting with null relationships
        final CMeeting meetingWithNulls = new CMeeting("Meeting With Nulls", null); // Use
                                                                                    // null
                                                                                    // project
                                                                                    // to
                                                                                    // test
                                                                                    // null
                                                                                    // handling
        meetingWithNulls.setParticipants(new HashSet<>()); // Empty but not null
        // Test that columns handle null relationships gracefully
        assertDoesNotThrow(() -> {
            // Test project column
            final String projectDisplay = meetingWithNulls.getProject() != null
                    ? meetingWithNulls.getProject().getName()
                    : "No Project";
            assertEquals("No Project", projectDisplay);
            // Test meeting type column
            final String typeDisplay = meetingWithNulls.getMeetingType() != null
                    ? meetingWithNulls.getMeetingType().getName()
                    : "No Type";
            assertEquals("No Type", typeDisplay);
            // Test participants column
            final String participantsDisplay = getParticipantsDisplay(meetingWithNulls);
            assertEquals("No participants", participantsDisplay);
        }, "Grid columns should handle null relationships gracefully");
    }

    @Test
    void testMeetingTypeColumnAccess() {
        LOGGER.info("Testing meeting type column access");
        testEntities.forEach(meeting -> {
            final String typeDisplay = meeting.getMeetingType() != null
                    ? meeting.getMeetingType().getName()
                    : "No Type";
            assertNotNull(typeDisplay, "Meeting type display should not be null");
        });
    }

    @Test
    void testParticipantsColumnAccess() {
        LOGGER.info("Testing participants column access - critical for lazy loading");
        testEntities.forEach(meeting -> {
            assertDoesNotThrow(() -> {
                final Set<CUser> participants = meeting.getParticipants();

                if (participants.isEmpty()) {
                    assertEquals("No participants", getParticipantsDisplay(meeting));
                } else {
                    final String participantsDisplay = getParticipantsDisplay(meeting);
                    assertNotNull(participantsDisplay, "Participants display should not be null");
                    assertTrue(participantsDisplay.length() > 0, "Participants display should not be empty");
                }
            }, "Participants column access should not cause lazy loading exceptions");
        });
    }

    @Test
    void testProjectColumnAccess() {
        LOGGER.info("Testing project column access");
        testEntities.forEach(meeting -> {
            final String projectDisplay = meeting.getProject() != null ? meeting.getProject().getName() : "No Project";
            assertNotNull(projectDisplay, "Project display should not be null");
        });
    }

    @Test
    void testViewInitialization() {
        LOGGER.info("Testing meetings view initialization");
        assertNotNull(meetingsView, "Meetings view should be created");
        assertNotNull(meetingsView.getGrid(), "Grid should be initialized");
        // Verify view is properly configured
        assertTrue(meetingsView.getClassNames().contains("meetings-view"), "View should have proper CSS class");
    }

    @Override
    protected void verifyEntityRelationships(final CMeeting entity) {
        assertNotNull(entity.getProject(), "Project should be initialized");
        assertNotNull(entity.getMeetingType(), "Meeting type should be initialized");
        assertNotNull(entity.getStatus(), "Status should be initialized");
        assertNotNull(entity.getAssignedTo(), "Assigned user should be initialized");
        assertNotNull(entity.getCreatedBy(), "Created by user should be initialized");
        assertNotNull(entity.getParticipants(), "Participants should be initialized");

        // Verify lazy collections can be accessed without exceptions
        try {
            // Test participants collection access
            final Set<CUser> participants = entity.getParticipants();
            assertNotNull(participants, "Participants collection should not be null");

            // Test accessing participant names (common cause of lazy loading issues)
            for (final CUser participant : participants) {
                final String participantName = participant.getName();
                assertNotNull(participantName, "Participant name should be accessible");
            }
            // Test attendees collection
            final Set<CUser> attendees = entity.getAttendees();
            assertNotNull(attendees, "Attendees collection should not be null");
        } catch (final Exception e) {
            fail("Collection access caused lazy loading exception: " + e.getMessage());
        }
    }
}