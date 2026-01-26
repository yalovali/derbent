package tech.derbent.plm.agile.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.domain.CUserStoryType;
import tech.derbent.plm.sprints.domain.CSprint;

public interface IUserStoryRepository extends IAgileRepository<CUserStory> {

	/** Counts the number of user stories that use the specified user story type using generic pattern */
	@Query ("SELECT COUNT(u) FROM #{#entityName} u WHERE u.entityType = :type")
	long countByType(@Param ("type") CUserStoryType type);

	@Override
	@Query ("""
			SELECT u FROM #{#entityName} u
			LEFT JOIN FETCH u.project
			LEFT JOIN FETCH u.assignedTo
			LEFT JOIN FETCH u.createdBy
			LEFT JOIN FETCH u.attachments
			LEFT JOIN FETCH u.comments
			LEFT JOIN FETCH u.links
			LEFT JOIN FETCH u.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH u.status
			LEFT JOIN FETCH u.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE u.id = :id
			""")
	Optional<CUserStory> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT u FROM #{#entityName} u
			LEFT JOIN FETCH u.project
			LEFT JOIN FETCH u.assignedTo
			LEFT JOIN FETCH u.createdBy
			LEFT JOIN FETCH u.attachments
			LEFT JOIN FETCH u.comments
			LEFT JOIN FETCH u.links
			LEFT JOIN FETCH u.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH u.status
			LEFT JOIN FETCH u.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE u.project = :project
			ORDER BY u.id DESC
			""")
	Page<CUserStory> listByProject(@Param ("project") CProject<?> project, Pageable pageable);

	@Override
	@Query ("""
			SELECT u FROM #{#entityName} u
			LEFT JOIN FETCH u.project
			LEFT JOIN FETCH u.assignedTo
			LEFT JOIN FETCH u.createdBy
			LEFT JOIN FETCH u.attachments
			LEFT JOIN FETCH u.comments
			LEFT JOIN FETCH u.links
			LEFT JOIN FETCH u.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH u.status
			LEFT JOIN FETCH u.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE u.project = :project
			ORDER BY u.id DESC
			""")
	List<CUserStory> listByProjectForPageView(@Param ("project") CProject<?> project);

	/** Find all user stories that are in the backlog (not assigned to any sprint).
	 * @param project the project
	 * @return list of user stories in backlog ordered by sprint item order */
	@Query ("""
			SELECT u FROM #{#entityName} u
			LEFT JOIN FETCH u.project
			LEFT JOIN FETCH u.assignedTo
			LEFT JOIN FETCH u.createdBy
			LEFT JOIN FETCH u.attachments
			LEFT JOIN FETCH u.comments
			LEFT JOIN FETCH u.links
			LEFT JOIN FETCH u.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH u.status
			LEFT JOIN FETCH u.sprintItem si
			WHERE u.project = :project
			and (si.sprint IS NULL OR si.sprint.id IS NULL)
			ORDER BY si.itemOrder ASC NULLS LAST, u.id DESC
			""")
	List<CUserStory> listForProjectBacklog(@Param ("project") CProject<?> project);

	/** Find all user stories that are members of a specific sprint (via sprintItem relation).
	 * @param sprint the sprint
	 * @return list of user stories ordered by sprint item order */
	@Query ("""
			SELECT u FROM #{#entityName} u
			LEFT JOIN FETCH u.project
			LEFT JOIN FETCH u.assignedTo
			LEFT JOIN FETCH u.createdBy
			LEFT JOIN FETCH u.attachments
			LEFT JOIN FETCH u.comments
			LEFT JOIN FETCH u.links
			LEFT JOIN FETCH u.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH u.status
			LEFT JOIN FETCH u.sprintItem si
			LEFT JOIN FETCH si.sprint s
			WHERE s = :sprint
			ORDER BY si.itemOrder ASC
			""")
	List<CUserStory> listForSprint(@Param ("sprint") CSprint sprint);

	/** Find user story by sprint item ID - loads without sprint item to prevent circular loading.
	 * @param sprintItemId the sprint item ID
	 * @return the user story if found */
	@Query ("""
			SELECT u FROM #{#entityName} u
			LEFT JOIN FETCH u.project
			LEFT JOIN FETCH u.assignedTo
			LEFT JOIN FETCH u.createdBy
			LEFT JOIN FETCH u.attachments
			LEFT JOIN FETCH u.comments
			LEFT JOIN FETCH u.links
			LEFT JOIN FETCH u.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH u.status
			WHERE u.sprintItem.id = :sprintItemId
			""")
	Optional<CUserStory> findBySprintItemId(@Param ("sprintItemId") Long sprintItemId);

	/** Find user story by name and project.
	 * @param name    the name
	 * @param project the project
	 * @return the user story if found */
	@Query ("SELECT u FROM #{#entityName} u WHERE u.name = :name AND u.project = :project")
	Optional<CUserStory> findByNameAndProject(@Param ("name") String name, @Param ("project") CProject<?> project);
}
