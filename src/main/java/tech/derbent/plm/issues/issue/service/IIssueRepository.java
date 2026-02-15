package tech.derbent.plm.issues.issue.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issue.domain.EIssueSeverity;
import tech.derbent.plm.issues.issuetype.domain.CIssueType;
import tech.derbent.plm.sprints.domain.CSprint;

public interface IIssueRepository extends IProjectItemRespository<CIssue> {

	/** Counts the number of issues that use the specified issue type */
	@Query ("SELECT COUNT(i) FROM #{#entityName} i WHERE i.entityType = :type")
	long countByType(@Param ("type") CIssueType type);
	/** Count open issues by project ID.
	 * @param projectId the project ID
	 * @return count of open issues */
	@Query ("SELECT COUNT(i) FROM #{#entityName} i WHERE i.project.id = :projectId AND i.status.name IN ('Open', 'In Progress')")
	Long countOpenIssuesByProjectId(@Param ("projectId") Long projectId);
	@Override
	@Query ("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.attachments
			LEFT JOIN FETCH i.comments
			LEFT JOIN FETCH i.linkedActivity
			  LEFT JOIN FETCH i.links
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			LEFT JOIN FETCH i.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE i.id = :id
			""")
	Optional<CIssue> findById(@Param ("id") Long id);
	/** Find all issues by linked activity.
	 * @param activity the linked activity
	 * @return list of issues linked to the activity */
	@Query ("SELECT i FROM #{#entityName} i WHERE i.linkedActivity = :activity ORDER BY i.id DESC")
	List<CIssue> findByLinkedActivity(@Param ("activity") CActivity activity);
	/** Find all issues by severity.
	 * @param severity the issue severity
	 * @return list of issues with the specified severity */
	@Query ("SELECT i FROM #{#entityName} i WHERE i.issueSeverity = :severity ORDER BY i.id DESC")
	List<CIssue> findBySeverity(@Param ("severity") EIssueSeverity severity);
	/** Find issue by sprint item ID.
	 * @param sprintItemId the sprint item ID
	 * @return the issue if found */
	@Query ("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.attachments
			LEFT JOIN FETCH i.comments
			  LEFT JOIN FETCH i.links
			  LEFT JOIN FETCH i.linkedActivity
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			WHERE i.sprintItem.id = :sprintItemId
			""")
	Optional<CIssue> findBySprintItemId(@Param ("sprintItemId") Long sprintItemId);
	@Override
	@Query ("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.attachments
			LEFT JOIN FETCH i.comments
			  LEFT JOIN FETCH i.links
			  LEFT JOIN FETCH i.linkedActivity
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			LEFT JOIN FETCH i.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE i.project = :project
			ORDER BY i.id DESC
			""")
	Page<CIssue> listByProject(@Param ("project") CProject<?> project, Pageable pageable);
	@Override
	@Query ("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.attachments
			LEFT JOIN FETCH i.comments
			  LEFT JOIN FETCH i.links
			  LEFT JOIN FETCH i.linkedActivity
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			LEFT JOIN FETCH i.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE i.project = :project
			ORDER BY i.id DESC
			""")
	List<CIssue> listByProjectForPageView(@Param ("project") CProject<?> project);
	/** Find all issues of projects where the user's company owns the project */
	@Query ("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project p
			LEFT JOIN FETCH i.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE p IN (SELECT us.project FROM CUserProjectSettings us WHERE us.user = :user)
			ORDER BY i.id DESC
			""")
	List<CIssue> getDataProviderValuesOfUser(@Param ("user") CUser user);
	/** Find all issues that are in the backlog (not assigned to any sprint).
	 * @param project the project
	 * @return list of issues in backlog ordered by sprint item order */
	@Query ("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.attachments
			LEFT JOIN FETCH i.comments
			  LEFT JOIN FETCH i.links
			  LEFT JOIN FETCH i.linkedActivity
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			LEFT JOIN FETCH i.sprintItem si
			WHERE i.project = :project
			AND (si.sprint IS NULL OR si.sprint.id IS NULL)
			ORDER BY si.itemOrder ASC NULLS LAST, i.id DESC
			""")
	List<CIssue> listForProjectBacklog(@Param ("project") CProject<?> project);
	/** Find all issues that are members of a specific sprint.
	 * @param sprint the sprint
	 * @return list of issues ordered by sprint item order */
	@Query ("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.attachments
			LEFT JOIN FETCH i.comments
			  LEFT JOIN FETCH i.links
			  LEFT JOIN FETCH i.linkedActivity
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			LEFT JOIN FETCH i.sprintItem si
			LEFT JOIN FETCH si.sprint s
			WHERE s = :sprint
			ORDER BY si.itemOrder ASC
			""")
	List<CIssue> listForSprint(@Param ("sprint") CSprint sprint);
}
