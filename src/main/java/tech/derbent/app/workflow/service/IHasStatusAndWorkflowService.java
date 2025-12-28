package tech.derbent.app.workflow.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;

public interface IHasStatusAndWorkflowService<EntityClass extends CProjectItem<EntityClass>> {

	Logger LOGGER = LoggerFactory.getLogger(IHasStatusAndWorkflowService.class);

        static CProjectItemStatus getInitialStatus(final IHasStatusAndWorkflow<?> entity, final CProjectItemStatusService projectItemStatusService) {
                LOGGER.debug("Retrieving initial status for entity of type: {}", entity.getClass().getSimpleName());
                Check.notNull(projectItemStatusService, "projectItemStatusService cannot be null");
                Check.notNull(entity, "entity cannot be null");
                final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(entity);
                Check.notEmpty(initialStatuses, "No statuses returned from getValidNextStatuses for entity type " + entity.getClass().getSimpleName());
                return initialStatuses.get(0);
        }

	static void initializeNewEntity(final IHasStatusAndWorkflow<?> entity, final CProject currentProject, final CTypeEntityService<?> typeService,
			final CProjectItemStatusService projectItemStatusService) {
		LOGGER.debug("Initializing new entity of type: {} in project: {}", entity.getClass().getSimpleName(),
				currentProject != null ? currentProject.getName() : "null");
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
                Check.notNull(entity.getWorkflow(), "Workflow cannot be null for entity type " + entity.getClass().getSimpleName());
                final CProjectItemStatus initialStatus = getInitialStatus(entity, projectItemStatusService);
                Check.notNull(initialStatus, "Initial status cannot be null for entity type " + entity.getClass().getSimpleName());
                entity.setStatus(initialStatus);
        }
}
