package tech.derbent.api.services;

import java.time.Clock;
import java.util.List;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

public abstract class CProjectItemService<EntityClass extends CProjectItem<EntityClass>> extends CEntityOfProjectService<EntityClass> {

	protected CProjectItemStatusService projectItemStatusService;

	public CProjectItemService(final IEntityOfProjectRepository<EntityClass> repository, final Clock clock, final ISessionService sessionService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService);
		this.projectItemStatusService = projectItemStatusService;
	}

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
		final CProject currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize risk"));
		final List<CProjectItemStatus> availableStatuses = projectItemStatusService.listByProject(currentProject);
		if (!availableStatuses.isEmpty()) {
			entity.setStatus(availableStatuses.get(0));
		}
	}
}
