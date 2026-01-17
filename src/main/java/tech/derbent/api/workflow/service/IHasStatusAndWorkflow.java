package tech.derbent.api.workflow.service;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;

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
 * @see CWorkflowStatusRelation */
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
	 * This default implementation enforces null-check validation only.
	 * Implementing classes MUST override this method to actually set the status field,
	 * and SHOULD call this default implementation (via super or direct validation) to
	 * ensure the null check is enforced.
	 * <p>
	 * Example implementation in entity class:
	 * <pre>
	 * {@code
	 * @Override
	 * public void setStatus(CProjectItemStatus status) {
	 *     IHasStatusAndWorkflow.super.setStatus(status); // Enforce null check
	 *     this.status = status;
	 *     updateLastModified();
	 * }
	 * }
	 * </pre>
	 * 
	 * @param status the new status (must not be null)
	 * @throws NullPointerException if status is null */
	default void setStatus(CProjectItemStatus status) {
		Objects.requireNonNull(status, "Status cannot be null - workflow entities must always have a valid status");
	}
	// void setWorkflow(CWorkflowEntity workflow);

	/** Copy status and workflow from source to target if both implement IHasStatusAndWorkflow and options allow. This default method reduces code
	 * duplication by providing a standard implementation of status/workflow copying.
	 * @param source the source entity
	 * @param target the target entity
	 * @param options copy options controlling whether status and workflow are copied
	 * @return true if status/workflow were copied, false if skipped */
	static boolean copyStatusAndWorkflowTo(final CEntityDB<?> source,
			final CEntityDB<?> target, final CCloneOptions options) {
		// Check if both source and target implement IHasStatusAndWorkflow
		if (!(source instanceof IHasStatusAndWorkflow) || !(target instanceof IHasStatusAndWorkflow)) {
			return false; // Skip silently if target doesn't support status/workflow
		}
		try {
			final IHasStatusAndWorkflow<?> sourceWithStatus = (IHasStatusAndWorkflow<?>) source;
			final IHasStatusAndWorkflow<?> targetWithStatus = (IHasStatusAndWorkflow<?>) target;
			// Copy status if options allow
			if (options.isCloneStatus() && sourceWithStatus.getStatus() != null) {
				source.copyField(sourceWithStatus::getStatus, targetWithStatus::setStatus);
			}
			// Copy workflow if options allow (currently workflow setter is not in interface)
			if (options.isCloneWorkflow() && sourceWithStatus.getWorkflow() != null) {
				// Workflow copy skipped - no setter in interface
				LOGGER.debug("Workflow copy requested but no setter available in interface");
			}
			return true;
		} catch (final Exception e) {
			// Log and skip on error - don't fail entire copy operation
			LOGGER.warn("Failed to copy status/workflow: {}", e.getMessage());
			return false;
		}
	}
}
