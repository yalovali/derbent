package tech.derbent.app.meetings.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;

public interface IMeetingRepository extends IEntityOfProjectRepository<CMeeting> {

	@Override
	@Query ("""
			   SELECT DISTINCT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
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
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.responsible
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   WHERE m.project = :project
			   ORDER BY m.id DESC
			""")
	Page<CMeeting> listByProject(@Param ("project") CProject project, Pageable pageable);
	@Override
	@Query ("""
			   SELECT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.responsible
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   WHERE m.project = :project
			   ORDER BY m.id DESC
			""")
	List<CMeeting> listByProjectForPageView(@Param ("project") CProject project);
	/** Find all meetings by project ordered by sprint order for sprint-aware components. Null sprintOrder values will appear last.
	 * @param project the project
	 * @return list of meetings ordered by sprintOrder ASC, id DESC */
	@Query ("""
			   SELECT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.responsible
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   WHERE m.project = :project and m.sprintItem IS NULL
			   ORDER BY m.sprintOrder ASC NULLS LAST, m.id DESC
			""")
	List<CMeeting> listForProjectBacklog(@Param ("project") CProject project);

	/** Find all meetings that are members of a specific sprint (via sprintItem relation).
	 * @param sprint the sprint
	 * @return list of meetings ordered by sprint item order */
	@Query ("""
			   SELECT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.responsible
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   LEFT JOIN FETCH m.sprintItem si
			   LEFT JOIN FETCH si.sprint s
			   WHERE s = :sprint
			   ORDER BY si.itemOrder ASC
			""")
	List<CMeeting> listForSprint(@Param ("sprint") CSprint sprint);
}
