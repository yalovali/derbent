package tech.derbent.meetings.view;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;
import tech.derbent.projects.service.CProjectService;

import java.util.HashSet;
import java.util.Set;

/**
 * Test class for meeting participants display issues.
 * Addresses the problem where participants are displayed as JSON strings instead of names.
 */
@SpringBootTest
public class CMeetingParticipantsDisplayTest {

    @Autowired
    private CMeetingService meetingService;
    
    @Autowired
    private CUserService userService;
    
    @Autowired
    private CProjectService projectService;

    @Test
    public void testMeetingParticipantsDisplayCorrectly() {
        // Get a meeting with participants from the sample data
        var meetings = meetingService.list(org.springframework.data.domain.Pageable.unpaged());
        
        assertFalse(meetings.isEmpty(), "Should have sample meetings from CSampleDataInitializer");
        
        // Find a meeting with participants
        CMeeting meetingWithParticipants = null;
        for (CMeeting meeting : meetings) {
            if (meeting.getParticipants() != null && !meeting.getParticipants().isEmpty()) {
                meetingWithParticipants = meeting;
                break;
            }
        }
        
        assertNotNull(meetingWithParticipants, "Should have at least one meeting with participants");
        
        // Test that participants have proper names (not JSON strings)
        Set<CUser> participants = meetingWithParticipants.getParticipants();
        assertFalse(participants.isEmpty(), "Meeting should have participants");
        
        for (CUser participant : participants) {
            // Check that the participant has a proper name, not a JSON-like string
            String participantName = participant.getName();
            assertNotNull(participantName, "Participant should have a name");
            assertFalse(participantName.trim().isEmpty(), "Participant name should not be empty");
            assertFalse(participantName.contains("{"), "Participant name should not contain JSON-like characters");
            assertFalse(participantName.contains("}"), "Participant name should not contain JSON-like characters");
            assertFalse(participantName.startsWith("CUser"), "Participant name should not be a class name");
            
            System.out.println("Participant name: " + participantName);
        }
        
        // Test the same logic that's used in the grid display
        String participantsDisplay = participants.stream()
            .map(user -> user.getName() != null ? user.getName() : "User #" + user.getId())
            .collect(java.util.stream.Collectors.joining(", "));
            
        assertNotNull(participantsDisplay, "Participants display string should not be null");
        assertTrue(participantsDisplay.length() > 0, "Participants display should have content");
        assertFalse(participants.toString().equals(participantsDisplay), 
            "Display string should be different from collection toString()");
            
        System.out.println("Meeting: " + meetingWithParticipants.getName());
        System.out.println("Participants display: " + participantsDisplay);
        System.out.println("Raw participants toString: " + participants.toString());
    }
}