package tech.derbent.plm.activities.service;

import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.base.session.service.ISessionService;
import org.slf4j.LoggerFactory;


import tech.derbent.api.entity.domain.CValidationException;
import tech.derbent.api.validation.ValidationMessages;

@Service
@Transactional
public class CActivityPriorityService extends CTypeEntityService<CActivityPriority> implements IEntityRegistrable, IEntityWithView {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CActivityPriorityService.class);

	public CActivityPriorityService(final IActivityPriorityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing activity priority deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level
	 * checks (null validation, non-deletable flag) are performed.
	 * @param entity the activity priority entity to check
	 * @return null if priority can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CActivityPriority entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	protected void validateEntity(final CActivityPriority entity) throws CValidationException {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CActivityPriority> existing = ((IActivityPriorityRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new CValidationException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}

	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findDefaultPriority(final CCompany company) {
		return ((IActivityPriorityRepository) repository).findByIsDefaultTrue(company);
	}

	@Override
	public Class<CActivityPriority> getEntityClass() { return CActivityPriority.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CActivityPriorityInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceActivityPriority.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CActivityPriority entity) {
		super.initializeNewEntity(entity);
		try {
			entity.setIsDefault(false);
			entity.setPriorityLevel(3);
			setNameOfEntity(entity, "Activity Priority");
		} catch (final Exception e) {
			LOGGER.error(e.getMessage());
			throw e;
		}
	}
}
