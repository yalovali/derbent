package tech.derbent.app.workflow.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.IAbstractRepository;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;

/** Repository interface for CWorkflowStatusRelation entity. Provides data access methods for workflow-status relationships. */
@Repository
public interface IWorkflowStatusRelationRepository extends IAbstractRepository<CWorkflowStatusRelation> {

	/** Find all workflow status relations for a specific workflow with eager loading of workflow, statuses, and role. */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.role WHERE r.workflow.id = :workflowId"
	)
	List<CWorkflowStatusRelation> findByWorkflowId(@Param ("workflowId") Long workflowId);
	/** Find all workflow status relations for a specific status (as from status) with eager loading */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.role WHERE r.fromStatus.id = :statusId"
	)
	List<CWorkflowStatusRelation> findByFromStatusId(@Param ("statusId") Long statusId);
	/** Find all workflow status relations for a specific status (as to status) with eager loading */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.role WHERE r.toStatus.id = :statusId"
	)
	List<CWorkflowStatusRelation> findByToStatusId(@Param ("statusId") Long statusId);
	/** Find a specific workflow status relation by workflow, from status, to status, and role */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.role WHERE r.workflow.id = :workflowId AND r.fromStatus.id = :fromStatusId AND r.toStatus.id = :toStatusId AND (r.role.id = :roleId OR (r.role IS NULL AND :roleId IS NULL))"
	)
	Optional<CWorkflowStatusRelation> findByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId(@Param ("workflowId") Long workflowId,
			@Param ("fromStatusId") Long fromStatusId, @Param ("toStatusId") Long toStatusId, @Param ("roleId") Long roleId);
	/** Check if a relationship exists between workflow and statuses */
	@Query (
		"SELECT COUNT(r) > 0 FROM #{#entityName} r WHERE r.workflow.id = :workflowId AND r.fromStatus.id = :fromStatusId AND r.toStatus.id = :toStatusId AND (r.role.id = :roleId OR (r.role IS NULL AND :roleId IS NULL))"
	)
	boolean existsByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId(@Param ("workflowId") Long workflowId, @Param ("fromStatusId") Long fromStatusId,
			@Param ("toStatusId") Long toStatusId, @Param ("roleId") Long roleId);
	/** Find all relations by role using generic pattern */
	@Query (
		"SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.role WHERE r.role.id = :roleId"
	)
	List<CWorkflowStatusRelation> findByRoleId(@Param ("roleId") Long roleId);
	/** Count relations for a specific workflow using generic pattern */
	@Query ("SELECT COUNT(r) FROM #{#entityName} r WHERE r.workflow.id = :workflowId")
	long countByWorkflowId(@Param ("workflowId") Long workflowId);
	/** Delete a specific workflow-status relationship by workflow, from status, to status, and role IDs using generic pattern */
	@Modifying
	@Transactional
	@Query (
		"DELETE FROM #{#entityName} r WHERE r.workflow.id = :workflowId AND r.fromStatus.id = :fromStatusId AND r.toStatus.id = :toStatusId AND (r.role.id = :roleId OR (r.role IS NULL AND :roleId IS NULL))"
	)
	void deleteByWorkflowIdAndFromStatusIdAndToStatusIdAndRoleId(@Param ("workflowId") Long workflowId, @Param ("fromStatusId") Long fromStatusId,
			@Param ("toStatusId") Long toStatusId, @Param ("roleId") Long roleId);
	/** Delete all workflow-status relationships for a specific workflow using generic pattern */
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.workflow.id = :workflowId")
	void deleteByWorkflowId(@Param ("workflowId") Long workflowId);
}
