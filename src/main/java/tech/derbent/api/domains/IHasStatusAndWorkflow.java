package tech.derbent.api.domains;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.CTypeEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.projects.domain.CProject;
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

	/** Gets the initial/default status from the entity's workflow.
	 * <p>
	 * This method retrieves the workflow's designated initial status, which is the status that should be assigned to new entities when they are
	 * created. The initial status is defined in the workflow's status relations where CWorkflowStatusRelation.initialStatus = true.
	 * @param entity                   the entity to get initial status for
	 * @param projectItemStatusService the service to resolve initial statuses
	 * @return Optional containing the initial status if found, empty otherwise */
	static Optional<CProjectItemStatus> getInitialStatus(IHasStatusAndWorkflow<?> entity, CProjectItemStatusService projectItemStatusService) {
		if (entity == null || entity.getWorkflow() == null) {
			return Optional.empty();
		}
		final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(entity);
		if (!initialStatuses.isEmpty()) {
			LOGGER.debug("Found workflow initial status: {} for entity type: {}", initialStatuses.get(0).getName(),
					entity.getEntityType() != null ? entity.getEntityType().getName() : "null");
			return Optional.of(initialStatuses.get(0));
		}
		return Optional.empty();
	}

	/** Initializes a new entity with default values based on workflow and project configuration.
	 * <p>
	 * Initialization Process:
	 * <ol>
	 * <li>Validates required parameters (project, entity, typeService)</li>
	 * <li>Assigns the first available entity type from the project</li>
	 * <li>Attempts to assign workflow's initial status (marked in workflow status relations)</li>
	 * <li>Falls back to first available project status if workflow is not configured or has no initial status</li>
	 * </ol>
	 * @param entity                   the entity to initialize
	 * @param currentProject           the project context for initialization
	 * @param typeService              service to get available entity types
	 * @param projectItemStatusService service to resolve statuses and workflow initial statuses
	 * @throws IllegalArgumentException if required parameters are null or empty */
	static void initializeNewEntity(IHasStatusAndWorkflow<?> entity, CProject currentProject, CTypeEntityService<?> typeService,
			CProjectItemStatusService projectItemStatusService) {
		Check.notNull(currentProject, "currentProject cannot be null");
		Check.notNull(entity, "entity cannot be null");
		Check.notNull(typeService, "typeService cannot be null");
		Check.notNull(projectItemStatusService, "projectItemStatusService cannot be null");
		// Step 1: Assign entity type
		final List<?> availableTypes = typeService.listByProject(currentProject);
		Check.notEmpty(availableTypes, "No entity types available in project " + currentProject.getName() + " - cannot initialize entity");
		final CTypeEntity<?> selectedType = (CTypeEntity<?>) availableTypes.get(0);
		entity.setEntityType(selectedType);
		LOGGER.debug("Assigned entity type: {} to new entity", selectedType.getName());
		// Step 2: Verify workflow is set (should be set by type assignment)
		if (entity.getWorkflow() == null) {
			LOGGER.warn("Workflow is null after type assignment for entity type: {} in project: {}", selectedType.getName(),
					currentProject.getName());
		}
		// Step 3: Assign initial status from workflow
		final Optional<CProjectItemStatus> initialStatus = getInitialStatus(entity, projectItemStatusService);
		if (initialStatus.isPresent()) {
			entity.setStatus(initialStatus.get());
			LOGGER.debug("Assigned workflow initial status: {} to new entity", initialStatus.get().getName());
			return;
		}
		// Step 4: Fallback - use first available status if no workflow or no initial statuses
		LOGGER.debug("No workflow initial status found, using fallback status assignment");
		final List<CProjectItemStatus> availableStatuses = projectItemStatusService.listByProject(currentProject);
		if (!availableStatuses.isEmpty()) {
			entity.setStatus(availableStatuses.get(0));
			LOGGER.debug("Assigned fallback status: {} to new entity", availableStatuses.get(0).getName());
		} else {
			LOGGER.warn("No statuses available in project: {} - entity created without status", currentProject.getName());
		}
	}

	CTypeEntity<?> getEntityType();
	CProjectItemStatus getStatus();
	CWorkflowEntity getWorkflow();
	void setEntityType(CTypeEntity<?> typeEntity);
	void setStatus(CProjectItemStatus status);
}
