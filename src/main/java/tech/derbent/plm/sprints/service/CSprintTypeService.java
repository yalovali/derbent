package tech.derbent.plm.sprints.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.plm.sprints.domain.CSprintType;
import tech.derbent.base.session.service.ISessionService;

import java.util.Optional;
import tech.derbent.api.validation.ValidationMessages;

/** CSprintTypeService - Service layer for CSprintType entity. Layer: Service (MVC) Handles business logic for project-aware sprint type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CSprintTypeService extends CTypeEntityService<CSprintType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintTypeService.class);
	@Autowired
	private ISprintRepository sprintRepository;

	public CSprintTypeService(final ISprintTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing sprint type deletion. Prevents deletion if the type is being used by any sprints. Always calls
	 * super.checkDeleteAllowed() first to ensure all parent-level checks (null validation, non-deletable flag) are performed.
	 * @param entity the sprint type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CSprintType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			// Check if any sprints are using this type
			final long usageCount = sprintRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d sprint%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null; // Type can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking sprint type delete dependencies for: {}", entity != null ? entity.getName() : "null", e);
			return "Error checking dependencies. Please try again.";
		}
	}

	@Override
	protected void validateEntity(final CSprintType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CSprintType> existing = ((ISprintTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}

	@Override
	public Class<CSprintType> getEntityClass() { return CSprintType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CSprintTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceSprintType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }
}
