package tech.derbent.api.entityOfProject.domain;

import java.time.Clock;
import tech.derbent.api.domains.CStatus;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.base.session.service.ISessionService;

/** CStatusService - Abstract service class for entities that extend CStatus. Layer: Service (MVC) Provides common business logic operations for
 * status entities that extend CTypeEntity and include status-specific functionality. */
public abstract class CStatusService<EntityClass extends CStatus<EntityClass>> extends CEntityOfProjectService<EntityClass> {

	public CStatusService(final IEntityOfProjectRepository<EntityClass> repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks if a status entity can be deleted. Inherits all checks from CTypeEntityService (null validation, non-deletable flag). Always calls
	 * super.checkDeleteAllowed() first to ensure all parent-level checks are performed.
	 * @param entity the status entity to check for deletion
	 * @return null if entity can be deleted, or an error message if it cannot be deleted */
	@Override
	public String checkDeleteAllowed(final EntityClass entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// No additional status-specific checks by default
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
