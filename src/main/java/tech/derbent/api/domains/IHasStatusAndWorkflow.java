package tech.derbent.api.domains;

import java.util.List;
import tech.derbent.api.services.CTypeEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;

public interface IHasStatusAndWorkflow<EntityClass extends IHasStatusAndWorkflow<EntityClass>> {

	static void initializeNewEntity(IHasStatusAndWorkflow<?> entity, CProject currentProject, CTypeEntityService<?> typeService,
			CProjectItemStatusService projectItemStatusService) {
		Check.notNull(currentProject, "currentProject cannot be null");
		Check.notNull(entity, "entity cannot be null");
		Check.notNull(typeService, "typeService cannot be null");
		Check.notNull(entity.getWorkflow(), "entity.workflow cannot be null");
		final List<?> availableTypes = typeService.listByProject(currentProject);
		Check.notEmpty(availableTypes, "No activity types available in project " + currentProject.getName() + " - cannot initialize new activity");
		entity.setEntityType((CTypeEntity<?>) availableTypes.get(0));
		final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(entity);
		if (!initialStatuses.isEmpty()) {
			entity.setStatus(initialStatuses.get(0));
			return;
		}
		// Fallback: use first available status if no workflow or no initial statuses
		final List<CProjectItemStatus> availableStatuses = projectItemStatusService.listByProject(currentProject);
		if (!availableStatuses.isEmpty()) {
			entity.setStatus(availableStatuses.get(0));
		}
	}

	CTypeEntity<?> getEntityType();
	CProjectItemStatus getStatus();
	// NOT YET <T extends CTypeEntity<T>> CTypeEntityService<T> getTypeService();
	CWorkflowEntity getWorkflow();
	void setEntityType(CTypeEntity<?> typeEntity);
	void setStatus(CProjectItemStatus status);
}
