package tech.derbent.meetings.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;

public interface CMeetingRepository extends CEntityOfProjectRepository<CMeeting> {

	/**
	 * Finds meetings by attendee user with eager loading of associations.
	 * @param userId the user ID
	 * @return list of meetings where the user is an attendee
	 */
	@Query ("SELECT DISTINCT m FROM CMeeting m " +
			"LEFT JOIN FETCH m.meetingType " +
			"LEFT JOIN FETCH m.status " +
			"LEFT JOIN FETCH m.responsible " +
			"LEFT JOIN FETCH m.relatedActivity " +
			"JOIN m.attendees a WHERE a.id = :userId")
	List<CMeeting> findByAttendeeId(@Param ("userId") Long userId);
	
	/**
	 * Finds meetings by participant user with eager loading of associations.
	 * @param userId the user ID
	 * @return list of meetings where the user is a participant
	 */
	@Query ("SELECT DISTINCT m FROM CMeeting m " +
			"LEFT JOIN FETCH m.meetingType " +
			"LEFT JOIN FETCH m.status " +
			"LEFT JOIN FETCH m.responsible " +
			"LEFT JOIN FETCH m.relatedActivity " +
			"JOIN m.participants p WHERE p.id = :userId")
	List<CMeeting> findByParticipantId(@Param ("userId") Long userId);
	
	/**
	 * Find meeting by ID with eager loading of commonly accessed associations.
	 * This prevents N+1 queries when accessing meeting details.
	 * @param id the meeting ID
	 * @return Optional containing the meeting with eagerly loaded associations
	 */
	@Query ("SELECT m FROM CMeeting m " +
			"LEFT JOIN FETCH m.meetingType " +
			"LEFT JOIN FETCH m.status " +
			"LEFT JOIN FETCH m.responsible " +
			"LEFT JOIN FETCH m.relatedActivity " +
			"LEFT JOIN FETCH m.project " +
			"WHERE m.id = :id")
	Optional<CMeeting> findByIdWithEagerLoading(@Param ("id") Long id);
	
	/**
	 * Find meetings by project with eager loading for list view.
	 * Optimized for displaying meeting lists without loading collections.
	 * @param project the project
	 * @param pageable pagination parameters
	 * @return list of meetings with eager loaded associations
	 */
	@Override
	@Query ("SELECT m FROM CMeeting m " +
			"LEFT JOIN FETCH m.meetingType " +
			"LEFT JOIN FETCH m.status " +
			"LEFT JOIN FETCH m.responsible " +
			"LEFT JOIN FETCH m.project " +
			"WHERE m.project = :project")
	List<CMeeting> findByProject(@Param ("project") CProject project, Pageable pageable);
}