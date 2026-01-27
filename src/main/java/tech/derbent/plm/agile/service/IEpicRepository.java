package tech.derbent.plm.agile.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.agile.domain.CEpic;
import tech.derbent.plm.agile.domain.CEpicType;
import tech.derbent.plm.sprints.domain.CSprint;

public interface IEpicRepository extends IAgileRepository<CEpic> {

	/** Counts the number of epics that use the specified epic type using generic pattern */
	@Query ("SELECT COUNT(e) FROM #{#entityName} e WHERE e.entityType = :type")
	long countByType(@Param ("type") CEpicType type);

	@Override
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			LEFT JOIN FETCH e.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH e.status
			LEFT JOIN FETCH e.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE e.id = :id
			""")
	Optional<CEpic> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			LEFT JOIN FETCH e.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH e.status
			LEFT JOIN FETCH e.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE e.project = :project
			ORDER BY e.id DESC
			""")
	Page<CEpic> listByProject(@Param ("project") CProject<?> project, Pageable pageable);

	@Override
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			LEFT JOIN FETCH e.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH e.status
			LEFT JOIN FETCH e.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE e.project = :project
			ORDER BY e.id DESC
			""")
	List<CEpic> listByProjectForPageView(@Param ("project") CProject<?> project);

	/** Find all epics that are in the backlog (not assigned to any sprint).
	 * @param project the project
	 * @return list of epics in backlog ordered by sprint item order */
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			LEFT JOIN FETCH e.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH e.status
			LEFT JOIN FETCH e.sprintItem si
			WHERE e.project = :project
			and (si.sprint IS NULL OR si.sprint.id IS NULL)
			ORDER BY si.itemOrder ASC NULLS LAST, e.id DESC
			""")
	List<CEpic> listForProjectBacklog(@Param ("project") CProject<?> project);

	/** Find all epics that are members of a specific sprint (via sprintItem relation).
	 * @param sprint the sprint
	 * @return list of epics ordered by sprint item order */
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			LEFT JOIN FETCH e.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH e.status
			LEFT JOIN FETCH e.sprintItem si
			LEFT JOIN FETCH si.sprint s
			WHERE s = :sprint
			ORDER BY si.itemOrder ASC
			""")
	List<CEpic> listForSprint(@Param ("sprint") CSprint sprint);

	/** Find epic by sprint item ID - loads without sprint item to prevent circular loading.
	 * @param sprintItemId the sprint item ID
	 * @return the epic if found */
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			LEFT JOIN FETCH e.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH e.status
			WHERE e.sprintItem.id = :sprintItemId
			""")
	Optional<CEpic> findBySprintItemId(@Param ("sprintItemId") Long sprintItemId);

	/** Find epic by name and project.
	 * @param name    the name
	 * @param project the project
	 * @return the epic if found */
	@Override
	@Query ("SELECT e FROM #{#entityName} e WHERE e.name = :name AND e.project = :project")
	Optional<CEpic> findByNameAndProject(@Param ("name") String name, @Param ("project") CProject<?> project);
}
