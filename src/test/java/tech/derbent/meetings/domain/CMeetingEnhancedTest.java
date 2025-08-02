package tech.derbent.meetings.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for CMeeting domain enhancements
 */
class CMeetingEnhancedTest extends CTestBase {

	private CMeeting meeting;

	private CProject project;

	private CMeetingType meetingType;

	private CMeetingStatus status;

	private CUser responsible;

	private CUser participant1;

	private CUser participant2;

	private CUser attendee1;

	private CActivity relatedActivity;

	@BeforeEach
	void setUp() {
		// Create test project
		project = new CProject("Test Project");
		project.setDescription("Test project description");
		// Create test meeting type
		meetingType = new CMeetingType("Test Meeting Type", project);
		// Create test meeting status
		status = new CMeetingStatus("SCHEDULED", project);
		// Create test users
		responsible = new CUser("John Doe");
		responsible.setEmail("john@example.com");
		participant1 = new CUser("Jane Smith");
		participant1.setEmail("jane@example.com");
		participant2 = new CUser("Bob Johnson");
		participant2.setEmail("bob@example.com");
		attendee1 = new CUser("Alice Brown");
		attendee1.setEmail("alice@example.com");
		// Create test activity
		relatedActivity = new CActivity("Test Activity", project);
		// Create test meeting
		meeting = new CMeeting("Test Meeting", project, meetingType);
	}

	@Test
	void testAttendeeManagement() {
		// Test adding attendees
		meeting.addAttendee(attendee1);
		meeting.addAttendee(participant1);
		assertEquals(2, meeting.getAttendees().size());
		assertTrue(meeting.isAttendee(attendee1));
		assertTrue(meeting.isAttendee(participant1));
		// Test removing attendee
		meeting.removeAttendee(attendee1);
		assertEquals(1, meeting.getAttendees().size());
		assertFalse(meeting.isAttendee(attendee1));
		assertTrue(meeting.isAttendee(participant1));
		// Test null safety
		meeting.addAttendee(null);
		assertEquals(1, meeting.getAttendees().size());
		assertFalse(meeting.isAttendee(null));
	}

	@Test
	void testMeetingBasicFields() {
		assertNotNull(meeting);
		assertEquals("Test Meeting", meeting.getName());
		assertEquals(project, meeting.getProject());
		assertEquals(meetingType, meeting.getMeetingType());
	}

	@Test
	void testMeetingDateTime() {
		final LocalDateTime startTime = LocalDateTime.of(2025, 1, 20, 9, 0);
		final LocalDateTime endTime = LocalDateTime.of(2025, 1, 20, 11, 0);
		meeting.setMeetingDate(startTime);
		meeting.setEndDate(endTime);
		assertEquals(startTime, meeting.getMeetingDate());
		assertEquals(endTime, meeting.getEndDate());
	}

	@Test
	void testMeetingEnhancedFields() {
		// Test location
		meeting.setLocation("Conference Room A");
		assertEquals("Conference Room A", meeting.getLocation());
		// Test agenda
		meeting.setAgenda("1. Review progress 2. Plan next steps");
		assertEquals("1. Review progress 2. Plan next steps", meeting.getAgenda());
		// Test status
		meeting.setStatus(status);
		assertEquals(status, meeting.getStatus());
		// Test responsible
		meeting.setResponsible(responsible);
		assertEquals(responsible, meeting.getResponsible());
		// Test minutes
		meeting.setMinutes("Meeting completed successfully");
		assertEquals("Meeting completed successfully", meeting.getMinutes());
		// Test linked element
		meeting.setLinkedElement("DOC-123");
		assertEquals("DOC-123", meeting.getLinkedElement());
		// Test related activity
		meeting.setRelatedActivity(relatedActivity);
		assertEquals(relatedActivity, meeting.getRelatedActivity());
	}

	@Test
	void testParticipantManagement() {
		// Test adding participants
		meeting.addParticipant(participant1);
		meeting.addParticipant(participant2);
		assertEquals(2, meeting.getParticipants().size());
		assertTrue(meeting.isParticipant(participant1));
		assertTrue(meeting.isParticipant(participant2));
		// Test removing participant
		meeting.removeParticipant(participant1);
		assertEquals(1, meeting.getParticipants().size());
		assertFalse(meeting.isParticipant(participant1));
		assertTrue(meeting.isParticipant(participant2));
		// Test null safety
		meeting.addParticipant(null);
		assertEquals(1, meeting.getParticipants().size());
		assertFalse(meeting.isParticipant(null));
	}

	@Test
	void testSetAttendeesCollection() {
		final Set<CUser> attendees = new HashSet<>();
		attendees.add(attendee1);
		attendees.add(participant1);
		meeting.setAttendees(attendees);
		assertEquals(2, meeting.getAttendees().size());
		assertTrue(meeting.isAttendee(attendee1));
		assertTrue(meeting.isAttendee(participant1));
		// Test null safety
		meeting.setAttendees(null);
		assertNotNull(meeting.getAttendees());
		assertEquals(0, meeting.getAttendees().size());
	}

	@Test
	void testSetParticipantsCollection() {
		final Set<CUser> participants = new HashSet<>();
		participants.add(participant1);
		participants.add(participant2);
		meeting.setParticipants(participants);
		assertEquals(2, meeting.getParticipants().size());
		assertTrue(meeting.isParticipant(participant1));
		assertTrue(meeting.isParticipant(participant2));
		// Test null safety
		meeting.setParticipants(null);
		assertNotNull(meeting.getParticipants());
		assertEquals(0, meeting.getParticipants().size());
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}