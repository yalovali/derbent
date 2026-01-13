package tech.derbent.app.issues.issue.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.issues.issue.domain.CIssue;
import tech.derbent.app.issues.issue.domain.EIssueSeverity;

public interface IIssueRepository extends IProjectItemRespository<CIssue> {

	/** Find all issues by linked activity.
	 * @param activity the linked activity
	 * @return list of issues linked to the activity */
	@Query("SELECT e FROM #{#entityName} e WHERE e.linkedActivity = :activity ORDER BY e.id DESC")
	List<CIssue> findByLinkedActivity(@Param("activity") CActivity activity);

	/** Find all issues by severity.
	 * @param severity the issue severity
	 * @return list of issues with the specified severity */
	@Query("SELECT e FROM #{#entityName} e WHERE e.issueSeverity = :severity ORDER BY e.id DESC")
	List<CIssue> findBySeverity(@Param("severity") EIssueSeverity severity);

	/** Count open issues by project ID.
	 * @param projectId the project ID
	 * @return count of open issues */
	@Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.project.id = :projectId AND e.status.name IN ('Open', 'In Progress')")
	Long countOpenIssuesByProjectId(@Param("projectId") Long projectId);
}
