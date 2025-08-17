package unit_tests.tech.derbent.meetings.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.users.domain.CUser;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for meeting participants display issues. Addresses the problem where participants are displayed as JSON
 * strings instead of names.
 */
@SpringBootTest
public class CMeetingParticipantsDisplayTest extends CTestBase {

    @Autowired
    private CMeetingService meetingService;

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub
    }

    @Test
    public void testMeetingParticipantsDisplayCorrectly() {
        // Get a meeting with participants from the sample data
        final var meetings = meetingService.list(org.springframework.data.domain.Pageable.unpaged());
        assertFalse(meetings.isEmpty(), "Should have sample meetings from CSampleDataInitializer");
        // Find a meeting with participants
        CMeeting meetingWithParticipants = null;

        for (final CMeeting meeting : meetings) {

            if ((meeting.getParticipants() != null) && !meeting.getParticipants().isEmpty()) {
                meetingWithParticipants = meeting;
                break;
            }
        }
        assertNotNull(meetingWithParticipants, "Should have at least one meeting with participants");
        // Test that participants have proper names (not JSON strings)
        final Set<CUser> participants = meetingWithParticipants.getParticipants();
        assertFalse(participants.isEmpty(), "Meeting should have participants");

        for (final CUser participant : participants) {
            // Check that the participant has a proper name, not a JSON-like string
            final String participantName = participant.getName();
            assertNotNull(participantName, "Participant should have a name");
            assertFalse(participantName.trim().isEmpty(), "Participant name should not be empty");
            assertFalse(participantName.contains("{"), "Participant name should not contain JSON-like characters");
            assertFalse(participantName.contains("}"), "Participant name should not contain JSON-like characters");
            assertFalse(participantName.startsWith("CUser"), "Participant name should not be a class name");
            System.out.println("Participant name: " + participantName);
        }
        // Test the same logic that's used in the grid display
        final String participantsDisplay = participants.stream()
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