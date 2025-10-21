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

	/** Find all workflow status relations for a specific workflow with eager loading of workflow, statuses, and roles. */
	@Query (
		"SELECT DISTINCT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.roles WHERE r.workflow.id = :workflowId"
	)
	List<CWorkflowStatusRelation> findByWorkflowId(@Param ("workflowId") Long workflowId);
	/** Find all workflow status relations for a specific status (as from status) with eager loading */
	@Query (
		"SELECT DISTINCT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.roles WHERE r.fromStatus.id = :statusId"
	)
	List<CWorkflowStatusRelation> findByFromStatusId(@Param ("statusId") Long statusId);
	/** Find all workflow status relations for a specific status (as to status) with eager loading */
	@Query (
		"SELECT DISTINCT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.roles WHERE r.toStatus.id = :statusId"
	)
	List<CWorkflowStatusRelation> findByToStatusId(@Param ("statusId") Long statusId);
	/** Find a specific workflow status relation by workflow, from status, and to status */
	@Query (
		"SELECT DISTINCT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.roles WHERE r.workflow.id = :workflowId AND r.fromStatus.id = :fromStatusId AND r.toStatus.id = :toStatusId"
	)
	Optional<CWorkflowStatusRelation> findByWorkflowIdAndFromStatusIdAndToStatusId(@Param ("workflowId") Long workflowId,
			@Param ("fromStatusId") Long fromStatusId, @Param ("toStatusId") Long toStatusId);
	/** Check if a relationship exists between workflow and statuses */
	@Query (
		"SELECT COUNT(r) > 0 FROM #{#entityName} r WHERE r.workflow.id = :workflowId AND r.fromStatus.id = :fromStatusId AND r.toStatus.id = :toStatusId"
	)
	boolean existsByWorkflowIdAndFromStatusIdAndToStatusId(@Param ("workflowId") Long workflowId, @Param ("fromStatusId") Long fromStatusId,
			@Param ("toStatusId") Long toStatusId);
	/** Find all relations that include a specific role using generic pattern */
	@Query (
		"SELECT DISTINCT r FROM #{#entityName} r LEFT JOIN FETCH r.workflow LEFT JOIN FETCH r.fromStatus LEFT JOIN FETCH r.toStatus LEFT JOIN FETCH r.roles role WHERE role.id = :roleId"
	)
	List<CWorkflowStatusRelation> findByRoleId(@Param ("roleId") Long roleId);
	/** Count relations for a specific workflow using generic pattern */
	@Query ("SELECT COUNT(r) FROM #{#entityName} r WHERE r.workflow.id = :workflowId")
	long countByWorkflowId(@Param ("workflowId") Long workflowId);
	/** Delete a specific workflow-status relationship by workflow, from status, and to status IDs using generic pattern */
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.workflow.id = :workflowId AND r.fromStatus.id = :fromStatusId AND r.toStatus.id = :toStatusId")
	void deleteByWorkflowIdAndFromStatusIdAndToStatusId(@Param ("workflowId") Long workflowId, @Param ("fromStatusId") Long fromStatusId,
			@Param ("toStatusId") Long toStatusId);
	/** Delete all workflow-status relationships for a specific workflow using generic pattern */
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.workflow.id = :workflowId")
	void deleteByWorkflowId(@Param ("workflowId") Long workflowId);
}
