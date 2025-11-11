package tech.derbent.api.services;

import java.time.Clock;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.api.domains.IHasStatusAndWorkflowService;
import tech.derbent.app.activities.service.CProjectItemStatusService;
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
	}
}
