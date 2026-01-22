package tech.derbent.api.workflow.service;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Nonnull;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;

public interface IHasStatusAndWorkflowService<EntityClass extends CProjectItem<EntityClass>> {

	Logger LOGGER = LoggerFactory.getLogger(IHasStatusAndWorkflowService.class);

	static CProjectItemStatus getInitialStatus(final IHasStatusAndWorkflow<?> entity, final CProjectItemStatusService projectItemStatusService) {
		// LOGGER.debug("Retrieving initial status for entity of type: {}", entity.getClass().getSimpleName());
		Check.notNull(projectItemStatusService, "projectItemStatusService cannot be null");
		Check.notNull(entity, "entity cannot be null");
		final List<CProjectItemStatus> initialStatuses = projectItemStatusService.getValidNextStatuses(entity);
		Check.notEmpty(initialStatuses, "No statuses returned from getValidNextStatuses for entity type " + entity.getClass().getSimpleName());
		return initialStatuses.get(0);
	}

	static void initializeNewEntity(final IHasStatusAndWorkflow<?> entity, final @NonNull @javax.annotation.Nonnull CProject<?> currentProject,
			final CTypeEntityService<?> typeService, final CProjectItemStatusService projectItemStatusService) {
		if (entity != null) {
			entity.initializeDefaults_IHasStatusAndWorkflow(currentProject, typeService, projectItemStatusService);
		}
	}
}
