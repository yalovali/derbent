package tech.derbent.api.entityOfProject.domain;

import java.time.Clock;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.base.session.service.ISessionService;

public abstract class CTypeEntityService<EntityClass extends CTypeEntity<EntityClass>> extends CEntityOfCompanyService<EntityClass> {

	public CTypeEntityService(IEntityOfCompanyRepository<EntityClass> repository, Clock clock, ISessionService sessionService) {
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
		// Intrinsic defaults (color, sortOrder, attributeNonDeletable, canHaveChildren)
		// are initialized in CTypeEntity.initializeDefaults() called by constructor.
		// No additional context-dependent initialization needed for type entities.
	}
}
