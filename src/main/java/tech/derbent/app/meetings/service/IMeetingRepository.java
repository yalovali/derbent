package tech.derbent.app.meetings.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;

public interface IMeetingRepository extends IEntityOfProjectRepository<CMeeting> {

	@Override
	@Query ("""
			   SELECT DISTINCT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.assignedTo
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   LEFT JOIN FETCH m.sprintItem si
			   LEFT JOIN FETCH si.sprint
			   WHERE m.id = :id
			""")
	Optional<CMeeting> findById(@Param ("id") Long id);
	/** Find meeting by sprint item ID - loads without sprint item to prevent circular loading.
	 * @param sprintItemId the sprint item ID
	 * @return the meeting if found */
	@Query ("""
			   SELECT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.assignedTo
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   WHERE m.sprintItem.id = :sprintItemId
			""")
	Optional<CMeeting> findBySprintItemId(@Param ("sprintItemId") Long sprintItemId);
	@Override
	@Query ("""
			   SELECT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.assignedTo
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   LEFT JOIN FETCH m.sprintItem si
			   LEFT JOIN FETCH si.sprint
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
			   LEFT JOIN FETCH m.assignedTo
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   LEFT JOIN FETCH m.sprintItem si
			   LEFT JOIN FETCH si.sprint
			   WHERE m.project = :project
			   ORDER BY m.id DESC
			""")
	List<CMeeting> listByProjectForPageView(@Param ("project") CProject project);
	/** Find all meetings that are in the backlog (not assigned to any sprint). In the new composition pattern, backlog items have sprintItem.sprint =
	 * null (not in any sprint).
	 * @param project the project
	 * @return list of meetings in backlog ordered by sprint item order */
	@Query ("""
			   SELECT m FROM #{#entityName} m
			   LEFT JOIN FETCH m.project
			   LEFT JOIN FETCH m.entityType et
			   LEFT JOIN FETCH et.workflow
			   LEFT JOIN FETCH m.status
			   LEFT JOIN FETCH m.assignedTo
			   LEFT JOIN FETCH m.relatedActivity
			   LEFT JOIN FETCH m.attendees
			   LEFT JOIN FETCH m.participants
			   LEFT JOIN FETCH m.sprintItem si
			   WHERE m.project = :project and (si.sprint IS NULL OR si.sprint.id IS NULL)
			   ORDER BY si.itemOrder ASC NULLS LAST, m.id DESC
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
			   LEFT JOIN FETCH m.assignedTo
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
