package tech.derbent.app.workflow.service;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.app.workflow.domain.CWorkflowEntity;

/** IHasStatusAndWorkflow - Interface for entities that support workflow-based status management.
 * <p>
 * This interface provides a unified approach to initializing entities with workflow-aware status management. Entities implementing this interface can
 * leverage:
 * <ul>
 * <li>Automatic type assignment from available entity types in a company</li>
 * <li>Workflow-based initial status assignment (prioritizing workflow initial statuses)</li>
 * <li>Fallback to project-level available statuses if workflow is not configured</li>
 * </ul>
 * <p>
 * Key Design Principles:
 * <ul>
 * <li>Workflows define valid status transitions based on user roles</li>
 * <li>Initial statuses are marked in workflow status relations (CWorkflowStatusRelation.initialStatus = true)</li>
 * <li>When creating a new entity, the workflow's initial status is automatically assigned</li>
 * <li>If no workflow is configured, the first available status in the company is used as fallback</li>
 * <li><strong>CRITICAL: Status can NEVER be set to null once initialized</strong> - prevents workflow state corruption</li>
 * </ul>
 * <p>
 * <strong>Status Initialization Rules (MANDATORY):</strong>
 * <ol>
 * <li>All entities implementing this interface MUST initialize status during entity creation</li>
 * <li>Status initialization MUST use {@link IHasStatusAndWorkflowService#initializeNewEntity}</li>
 * <li>Status MUST NEVER be set to null after initialization</li>
 * <li>Status changes MUST follow workflow transitions (use {@link tech.derbent.api.entityOfCompany.service.CProjectItemStatusService#getValidNextStatuses})</li>
 * </ol>
 * @param <EntityClass> The entity class implementing this interface
 * @see CWorkflowEntity
 * @see CProjectItemStatus
 * @see tech.derbent.app.workflow.domain.CWorkflowStatusRelation */
public interface IHasStatusAndWorkflow<EntityClass extends IHasStatusAndWorkflow<EntityClass>> {

	Logger LOGGER = LoggerFactory.getLogger(IHasStatusAndWorkflow.class);

	CTypeEntity<?> getEntityType();
	CProjectItemStatus getStatus();
	CWorkflowEntity getWorkflow();
	void setEntityType(CTypeEntity<?> typeEntity);
	
	/** Sets the status for this entity.
	 * <p>
	 * <strong>CRITICAL RULE: Status can NEVER be set to null once entity is initialized.</strong>
	 * <p>
	 * This default implementation enforces the rule that status cannot be set to null, 
	 * which prevents workflow state corruption. All entities implementing this interface
	 * inherit this validation automatically.
	 * <p>
	 * Subclasses can override this method to add additional validation (e.g., company checks),
	 * but MUST call this default implementation or enforce the same null check.
	 * 
	 * @param status the new status (must not be null)
	 * @throws NullPointerException if status is null */
	default void setStatus(CProjectItemStatus status) {
		Objects.requireNonNull(status, "Status cannot be null - workflow entities must always have a valid status");
	}
	// void setWorkflow(CWorkflowEntity workflow);
}
