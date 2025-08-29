package tech.derbent.abstracts.domains;

import java.time.Clock;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.session.service.CSessionService;

public abstract class CProjectItemService<EntityClass extends CEntityOfProject<EntityClass>> extends CEntityOfProjectService<EntityClass> {
	public CProjectItemService(final CEntityOfProjectRepository<EntityClass> repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}
}
