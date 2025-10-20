package tech.derbent.app.meetings.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.projects.domain.CProject;

public interface IMeetingRepository extends IEntityOfProjectRepository<CMeeting> {

	@Override
	@Query ("""
			   SELECT DISTINCT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.meetingType
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.responsible
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   WHERE m.id = :id
			""")
	Optional<CMeeting> findById(@Param ("id") Long id);
	@Override
	@Query ("""
			   SELECT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.meetingType
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.responsible
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   WHERE m.project = :project
			""")
	Page<CMeeting> listByProject(@Param ("project") CProject project, Pageable pageable);
}
