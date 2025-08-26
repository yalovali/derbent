package tech.derbent.abstracts.domains;

import java.time.Clock;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.abstracts.services.CEntityOfProjectService;

public abstract class CProjectItemService<EntityClass extends CEntityOfProject<EntityClass>> extends CEntityOfProjectService<EntityClass> {
	public CProjectItemService(final CEntityOfProjectRepository<EntityClass> repository, final Clock clock) {
		super(repository, clock);
	}
}
