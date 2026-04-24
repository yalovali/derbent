package tech.derbent.plm.requirements.requirementtype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.requirements.requirement.service.IRequirementRepository;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;

/**
 * Type service for requirements.
 *
 * <p>Requirements can live at multiple hierarchy levels, so this service enforces only the generic
 * invariants that keep level and child-capability settings aligned.</p>
 */
@Service
@Profile ({
		"derbent", "default"
})
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CRequirementTypeService extends CTypeEntityService<CRequirementType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRequirementTypeService.class);
	private final IRequirementRepository requirementRepository;

	public CRequirementTypeService(final IRequirementTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IRequirementRepository requirementRepository) {
		super(repository, clock, sessionService);
		this.requirementRepository = requirementRepository;
	}

	@Override
	public String checkDeleteAllowed(final CRequirementType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		final long usageCount = requirementRepository.countByType(entity);
		if (usageCount > 0) {
			return "Cannot delete. It is being used by %d requirement%s.".formatted(usageCount, usageCount == 1 ? "" : "s");
		}
		return null;
	}

	@Override
	public Class<CRequirementType> getEntityClass() { return CRequirementType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CRequirementTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceRequirementType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (!(entity instanceof final CEntityNamed entityNamed) || entityNamed.getName() != null) {
			return;
		}
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((IRequirementTypeRepository) repository).countByCompany(activeCompany);
		entityNamed.setName("RequirementType %02d".formatted(typeCount + 1));
	}

	@Override
	protected void validateEntity(final CRequirementType entity) {
		super.validateEntity(entity);
		Check.notNull(entity.getLevel(), "Hierarchy level is required");
		if (entity.getLevel() < -1) {
			throw new CValidationException("Hierarchy level cannot be less than -1");
		}
		if (entity.getLevel() == -1 && entity.getCanHaveChildren()) {
			throw new CValidationException("Leaf requirement types cannot allow children");
		}
		validateUniqueNameInCompany((IRequirementTypeRepository) repository, entity, entity.getName(), entity.getCompany());
		LOGGER.debug("Validated requirement type '{}' at level {}", entity.getName(), entity.getLevel());
	}
}
