package tech.derbent.plm.activities.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityType;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.api.users.domain.CUser;

public interface IActivityRepository extends IProjectItemRespository<CActivity> {

	/** Counts the number of activities that use the specified activity type using generic pattern */
	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :type")
	long countByType(@Param ("entityType") CActivityType type);
	@Override
	@Query ("""
			SELECT a FROM #{#entityName} a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.assignedTo
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH a.status
			LEFT JOIN FETCH a.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE a.id = :id
			""")
	Optional<CActivity> findById(@Param ("id") Long id);
	@Override
	@Query ("""
			SELECT a FROM #{#entityName} a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.assignedTo
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH a.status
			LEFT JOIN FETCH a.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE a.project = :project
			ORDER BY a.id DESC
			""")
	Page<CActivity> listByProject(@Param ("project") CProject<?> project, Pageable pageable);
	@Override
	@Query ("""
			SELECT a FROM #{#entityName} a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.assignedTo
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH a.status
			LEFT JOIN FETCH a.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE a.project = :project
			ORDER BY a.id DESC
			""")
	List<CActivity> listByProjectForPageView(@Param ("project") CProject<?> project);
	// find all activities of projects where the user's company owns the project
	@Query ("""
				SELECT a FROM #{#entityName} a
				LEFT JOIN FETCH a.project p
				LEFT JOIN FETCH a.sprintItem si
				LEFT JOIN FETCH si.sprint
				WHERE p IN (SELECT us.project FROM CUserProjectSettings us WHERE us.user = :user)
				ORDER BY a.id DESC
				""")
	List<CActivity> listByUser(@Param ("user") CUser user);
	/** Find all activities that are in the backlog (not assigned to any sprint).
	 * In the new composition pattern, backlog items have sprintItem.sprint = null (not in any sprint).
	 * @param project the project
	 * @return list of activities in backlog ordered by sprint item order */
	@Query ("""
			SELECT a FROM #{#entityName} a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.assignedTo
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH a.status
			LEFT JOIN FETCH a.sprintItem si
			WHERE a.project = :project
			and (si.sprint IS NULL OR si.sprint.id IS NULL)
			ORDER BY si.itemOrder ASC NULLS LAST, a.id DESC
			""")
	List<CActivity> listForProjectBacklog(@Param ("project") CProject<?> project);

	/** Find all activities that are members of a specific sprint (via sprintItem relation).
	 * @param sprint the sprint
	 * @return list of activities ordered by sprint item order */
	@Query ("""
			SELECT a FROM #{#entityName} a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.assignedTo
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH a.status
			LEFT JOIN FETCH a.sprintItem si
			LEFT JOIN FETCH si.sprint s
			WHERE s = :sprint
			ORDER BY si.itemOrder ASC
			""")
	List<CActivity> listForSprint(@Param ("sprint") CSprint sprint);

	/** Find activity by sprint item ID - loads without sprint item to prevent circular loading.
	 * @param sprintItemId the sprint item ID
	 * @return the activity if found */
	@Query ("""
			SELECT a FROM #{#entityName} a
			LEFT JOIN FETCH a.project
			LEFT JOIN FETCH a.assignedTo
			LEFT JOIN FETCH a.createdBy
			LEFT JOIN FETCH a.attachments
			LEFT JOIN FETCH a.comments
			LEFT JOIN FETCH a.links
			LEFT JOIN FETCH a.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH a.status
			WHERE a.sprintItem.id = :sprintItemId
			""")
	Optional<CActivity> findBySprintItemId(@Param ("sprintItemId") Long sprintItemId);
}
