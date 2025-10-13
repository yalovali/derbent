package tech.derbent.api.services;

import java.time.Clock;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.session.service.ISessionService;

public abstract class CTypeEntityService<EntityClass extends CTypeEntity<EntityClass>> extends CEntityOfProjectService<EntityClass> {

	public CTypeEntityService(IEntityOfProjectRepository<EntityClass> repository, Clock clock, ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks if a type entity can be deleted. Validates that the entity is not marked as non-deletable. Always calls super.checkDeleteAllowed()
	 * first to ensure all parent-level checks are performed.
	 * @param entity the type entity to check for deletion
	 * @return null if entity can be deleted, or an error message if it cannot be deleted */
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
}
