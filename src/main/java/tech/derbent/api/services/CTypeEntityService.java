package tech.derbent.api.services;

import java.time.Clock;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.session.service.ISessionService;

public abstract class CTypeEntityService<EntityClass extends CTypeEntity<EntityClass>> extends CEntityOfProjectService<EntityClass> {

	public CTypeEntityService(IEntityOfProjectRepository<EntityClass> repository, Clock clock, ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(EntityClass entity) {
		String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}
}
