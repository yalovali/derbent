package tech.derbent.api.roles.service;

import java.time.Clock;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.session.service.ISessionService;

public abstract class CNonProjectTypeService<EntityClass extends CEntityNamed<EntityClass>> extends CEntityNamedService<EntityClass> {

	public CNonProjectTypeService(IAbstractNamedRepository<EntityClass> repository, Clock clock, ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null; // No dependencies found by default
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}
}
