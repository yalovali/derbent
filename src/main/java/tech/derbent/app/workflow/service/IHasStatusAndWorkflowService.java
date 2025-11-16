package tech.derbent.app.workflow.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.projects.domain.CProject;

public interface IHasStatusAndWorkflowService<EntityClass extends CProjectItem<EntityClass>> {

	Logger LOGGER = LoggerFactory.getLogger(IHasStatusAndWorkflowService.class);

	static Optional<CProjectItemStatus> getInitialStatus(final IHasStatusAndWorkflow<?> entity,
			final CProjectItemStatusService projectItemStatusService) {
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

	static void initializeNewEntity(final IHasStatusAndWorkflow<?> entity, final CProject currentProject, final CTypeEntityService<?> typeService,
			final CProjectItemStatusService projectItemStatusService) {
		Check.notNull(currentProject, "currentProject cannot be null");
		Check.notNull(entity, "entity cannot be null");
		Check.notNull(typeService, "typeService cannot be null");
		Check.notNull(projectItemStatusService, "projectItemStatusService cannot be null");
		// Step 1: Assign entity type
		final List<?> availableTypes = typeService.listByProject(currentProject);
		Check.notEmpty(availableTypes, "No entity types available in project " + currentProject.getName() + " for entity class "
				+ entity.getClass().getSimpleName() + " - cannot initialize entity");
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
		final List<CProjectItemStatus> availableStatuses = projectItemStatusService.listByCompany(currentProject.getCompany());
		if (!availableStatuses.isEmpty()) {
			entity.setStatus(availableStatuses.get(0));
			LOGGER.debug("Assigned fallback status: {} to new entity", availableStatuses.get(0).getName());
		} else {
			LOGGER.warn("No statuses available in project: {} - entity created without status", currentProject.getName());
		}
	}
}
