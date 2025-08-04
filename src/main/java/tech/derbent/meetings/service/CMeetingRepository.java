package tech.derbent.meetings.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.meetings.domain.CMeeting;

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
     * Finds meetings by participant user.
     * 
     * @param userId
     *            the user ID
     * @return list of meetings where the user is a participant
     */
    @Query("SELECT m FROM CMeeting m JOIN m.participants p WHERE p.id = :userId")
    List<CMeeting> findByParticipantId(@Param("userId") Long userId);
}