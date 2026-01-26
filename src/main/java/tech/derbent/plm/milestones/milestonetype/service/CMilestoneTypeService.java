package tech.derbent.plm.milestones.milestonetype.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.milestones.milestone.service.IMilestoneRepository;
import tech.derbent.plm.milestones.milestonetype.domain.CMilestoneType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CMilestoneTypeService extends CTypeEntityService<CMilestoneType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMilestoneTypeService.class);
	@Autowired
	private final IMilestoneRepository milestoneRepository;

	public CMilestoneTypeService(final IMilestoneTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IMilestoneRepository milestoneRepository) {
		super(repository, clock, sessionService);
		this.milestoneRepository = milestoneRepository;
	}

	@Override
	public String checkDeleteAllowed(final CMilestoneType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = milestoneRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for milestone type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CMilestoneType> getEntityClass() { return CMilestoneType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CMilestoneTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceMilestoneType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (entity instanceof final CEntityNamed entityCasted && entityCasted.getName() == null) {
			final CCompany activeCompany =
					sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
			final long typeCount = ((IMilestoneTypeRepository) repository).countByCompany(activeCompany);
			final String autoName = String.format("MilestoneType %02d", typeCount + 1);
			((CEntityNamed<?>) entity).setName(autoName);
		}
	}

	@Override
	protected void validateEntity(final CMilestoneType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CMilestoneType> existing = ((IMilestoneTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
