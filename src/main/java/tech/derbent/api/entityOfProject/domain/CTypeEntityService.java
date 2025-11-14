package tech.derbent.api.entityOfProject.domain;

import java.time.Clock;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.base.session.service.ISessionService;

public abstract class CTypeEntityService<EntityClass extends CTypeEntity<EntityClass>> extends CEntityOfProjectService<EntityClass> {

	public CTypeEntityService(IEntityOfProjectRepository<EntityClass> repository, Clock clock, ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// Check if this type entity is marked as non-deletable
		if (entity.getAttributeNonDeletable()) {
			return "This entity is marked as non-deletable and cannot be removed from the system.";
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final EntityClass entity) {
		super.initializeNewEntity(entity);
		entity.setColor("#4A90E2");
		entity.setSortOrder(100);
		entity.setAttributeNonDeletable(false);
	}
}
