package tech.derbent.meetings.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;

public interface CMeetingRepository extends CEntityOfProjectRepository<CMeeting> {

    /**
     * Finds meetings by attendee user.
     * 
     * @param userId
     *            the user ID
     * @return list of meetings where the user is an attendee
     */
    @Query("SELECT m FROM CMeeting m JOIN m.attendees a WHERE a.id = :userId")
    List<CMeeting> findByAttendeeId(@Param("userId") Long userId);

    /**
     * Finds a meeting by ID with eagerly loaded relationships to prevent LazyInitializationException. Only fetches
     * relationships that are still lazy (participants, attendees, relatedActivity).
     * 
     * @param id
     *            the meeting ID
     * @return optional CMeeting with loaded relationships
     */
    @Query("SELECT m FROM CMeeting m " + "LEFT JOIN FETCH m.project " + "LEFT JOIN FETCH m.participants "
            + "LEFT JOIN FETCH m.attendees " + "LEFT JOIN FETCH m.relatedActivity " + "WHERE m.id = :id")
    Optional<CMeeting> findByIdWithAllRelationships(@Param("id") Long id);

    /**
     * Finds meetings by participant user.
     * 
     * @param userId
     *            the user ID
     * @return list of meetings where the user is a participant
     */
    @Query("SELECT m FROM CMeeting m JOIN m.participants p WHERE p.id = :userId")
    List<CMeeting> findByParticipantId(@Param("userId") Long userId);

    /**
     * Finds meetings by project with eagerly loaded relationships to prevent LazyInitializationException. Only fetches
     * relationships that are still lazy.
     * 
     * @param project
     *            the project to filter by
     * @param pageable
     *            pagination information
     * @return page of meetings with loaded relationships
     */
    @Query("SELECT DISTINCT m FROM CMeeting m " + "LEFT JOIN FETCH m.project " + "LEFT JOIN FETCH m.participants "
            + "LEFT JOIN FETCH m.attendees " + "LEFT JOIN FETCH m.relatedActivity " + "WHERE m.project = :project")
    Page<CMeeting> findByProjectWithAllRelationships(@Param("project") CProject project, Pageable pageable);

    /**
     * Finds meetings by project with eagerly loaded relationships (non-paginated version).
     * 
     * @param project
     *            the project to filter by
     * @return list of meetings with loaded relationships
     */
    @Query("SELECT DISTINCT m FROM CMeeting m " + "LEFT JOIN FETCH m.project " + "LEFT JOIN FETCH m.participants "
            + "LEFT JOIN FETCH m.attendees " + "LEFT JOIN FETCH m.relatedActivity " + "WHERE m.project = :project")
    List<CMeeting> findByProjectWithAllRelationships(@Param("project") CProject project);
}