package tech.derbent.api.workflow.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.utils.Check;

public interface IHasStatusAndWorkflowService<EntityClass extends CProjectItem<EntityClass>> {

	Logger LOGGER = LoggerFactory.getLogger(IHasStatusAndWorkflowService.class);

	static CProjectItemStatus getInitialStatus(final IHasStatusAndWorkflow<?> entity, final CProjectItemStatusService statusService) {
		// LOGGER.debug("Retrieving initial status for entity of type: {}", entity.getClass().getSimpleName());
		Check.notNull(statusService, "statusService cannot be null");
		Check.notNull(entity, "entity cannot be null");
		final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(entity);
		Check.notEmpty(initialStatuses, "No statuses returned from getValidNextStatuses for entity type " + entity.getClass().getSimpleName());
		return initialStatuses.get(0);
	}

	default void initializeNewEntity_IHasStatusAndWorkflow(final IHasStatusAndWorkflow<?> entity, final CCompany currentCompany,
			final CTypeEntityService<?> typeService, final CProjectItemStatusService statusService) {
		Check.notNull(currentCompany, "currentCompany cannot be null");
		Check.notNull(typeService, "typeService cannot be null");
		Check.notNull(statusService, "statusService cannot be null");
		// Step 1: Assign entity type (which determines Workflow)
		final List<?> availableTypes = typeService.listByCompany(currentCompany);
		Check.notEmpty(availableTypes, "No entity types available in company " + currentCompany.getName() + " for entity class "
				+ getClass().getSimpleName() + " - cannot initialize entity");
		// Select first type as default
		final CTypeEntity<?> selectedType = (CTypeEntity<?>) availableTypes.get(0);
		entity.setEntityType(selectedType);
		// Verify Workflow is set (via Type)
		Check.notNull(entity.getWorkflow(), "Workflow cannot be null for entity type " + getClass().getSimpleName());
		// Step 2: Assign Initial Status
		final List<CProjectItemStatus> initialStatuses = statusService.getValidNextStatuses(entity);
		Check.notEmpty(initialStatuses, "No statuses returned from getValidNextStatuses for entity type " + getClass().getSimpleName());
		// Select first status
		final CProjectItemStatus initialStatus = initialStatuses.get(0);
		Check.notNull(initialStatus, "Initial status cannot be null for entity type " + getClass().getSimpleName());
		entity.setStatus(initialStatus);
	}
}
