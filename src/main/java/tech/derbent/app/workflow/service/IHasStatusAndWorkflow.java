package tech.derbent.app.workflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItemStatus;
import tech.derbent.app.workflow.domain.CWorkflowEntity;

/** IHasStatusAndWorkflow - Interface for entities that support workflow-based status management.
 * <p>
 * This interface provides a unified approach to initializing entities with workflow-aware status management. Entities implementing this interface can
 * leverage:
 * <ul>
 * <li>Automatic type assignment from available entity types in a project</li>
 * <li>Workflow-based initial status assignment (prioritizing workflow initial statuses)</li>
 * <li>Fallback to project-level available statuses if workflow is not configured</li>
 * </ul>
 * <p>
 * Key Design Principles:
 * <ul>
 * <li>Workflows define valid status transitions based on user roles</li>
 * <li>Initial statuses are marked in workflow status relations (CWorkflowStatusRelation.initialStatus = true)</li>
 * <li>When creating a new entity, the workflow's initial status is automatically assigned</li>
 * <li>If no workflow is configured, the first available status in the project is used as fallback</li>
 * </ul>
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

	void setStatus(CProjectItemStatus status);
}
