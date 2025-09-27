package tech.derbent.api.domains;

import java.time.Clock;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.session.service.CSessionService;

public abstract class CProjectItemService<EntityClass extends CEntityOfProject<EntityClass>> extends CEntityOfProjectService<EntityClass> {

	public CProjectItemService(final IEntityOfProjectRepository<EntityClass> repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}
}
