package tech.derbent.api.entityOfProject.service;

import java.time.Clock;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

public abstract class CProjectItemService<EntityClass extends CProjectItem<EntityClass>> extends CEntityOfProjectService<EntityClass>
		implements IHasStatusAndWorkflowService<EntityClass> {

	protected CProjectItemStatusService projectItemStatusService;

	public CProjectItemService(final IEntityOfProjectRepository<EntityClass> repository, final Clock clock, final ISessionService sessionService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService);
		this.projectItemStatusService = projectItemStatusService;
	}

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
		if (entity.getStatus() != null) {
			return;
		}
		final var project = entity.getProject();
		Check.notNull(project, "Project must be set before initializing status");
		final var defaultStatus = projectItemStatusService.findDefaultStatus(project).orElseGet(() -> {
			Check.notNull(project.getCompany(), "Company must be set before initializing status");
			final var available = projectItemStatusService.listByCompany(project.getCompany());
			Check.notEmpty(available, "No project item statuses available for company " + project.getCompany().getName());
			return available.get(0);
		});
		entity.setStatus(defaultStatus);
	}

	@Override
	protected void validateEntity(final EntityClass entity) {
		super.validateEntity(entity);
		// Validate status is set for IHasStatusAndWorkflow entities
		if (entity instanceof tech.derbent.app.workflow.service.IHasStatusAndWorkflow) {
			Check.notNull(entity.getStatus(), 
				"Status cannot be null for " + entity.getClass().getSimpleName() + 
				". All entities implementing IHasStatusAndWorkflow must have status initialized before saving.");
		}
	}

	@SuppressWarnings ("unchecked")
	public final void revokeSave(final CProjectItem<?> rawEntity) {
		save((EntityClass) rawEntity);
	}
}
