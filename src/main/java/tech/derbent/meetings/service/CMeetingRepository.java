package tech.derbent.meetings.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;

public interface CMeetingRepository extends CAbstractRepository<CMeeting> {

    List<CMeeting> findByProject(CProject project);

    Page<CMeeting> findByProject(CProject project, Pageable pageable);

    /**
     * Finds a meeting by ID with eagerly loaded CMeetingType and participants to prevent LazyInitializationException.
     * 
     * @param id the meeting ID
     * @return optional CMeeting with loaded meetingType and participants
     */
    @Query("SELECT m FROM CMeeting m LEFT JOIN FETCH m.meetingType LEFT JOIN FETCH m.participants WHERE m.id = :id")
    Optional<CMeeting> findByIdWithMeetingTypeAndParticipants(@Param("id") Long id);

    /**
     * Finds meetings by project with eagerly loaded relationships to prevent LazyInitializationException.
     * Loads project, meetingType, and participants for grid display.
     * 
     * @param project the project to filter by
     * @param pageable pagination information
     * @return page of meetings with loaded relationships
     */
    @Query("SELECT DISTINCT m FROM CMeeting m " +
           "LEFT JOIN FETCH m.project " +
           "LEFT JOIN FETCH m.meetingType " +
           "LEFT JOIN FETCH m.participants " +
           "WHERE m.project = :project")
    Page<CMeeting> findByProjectWithRelationships(@Param("project") CProject project, Pageable pageable);

    /**
     * Finds meetings by participant user.
     * 
     * @param userId the user ID
     * @return list of meetings where the user is a participant
     */
    @Query("SELECT m FROM CMeeting m JOIN m.participants p WHERE p.id = :userId")
    List<CMeeting> findByParticipantId(@Param("userId") Long userId);
}