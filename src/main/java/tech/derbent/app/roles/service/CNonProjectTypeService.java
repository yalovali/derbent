package tech.derbent.app.roles.service;

import java.time.Clock;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.services.CEntityNamedService;
import tech.derbent.api.services.IAbstractNamedRepository;
import tech.derbent.base.session.service.ISessionService;

public abstract class CNonProjectTypeService<EntityClass extends CEntityNamed<EntityClass>> extends CEntityNamedService<EntityClass> {

	public CNonProjectTypeService(IAbstractNamedRepository<EntityClass> repository, Clock clock, ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null; // No dependencies found by default
	}

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
	}
}
