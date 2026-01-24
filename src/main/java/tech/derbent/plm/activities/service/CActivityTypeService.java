package tech.derbent.plm.activities.service;

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
import tech.derbent.plm.activities.domain.CActivityType;

/** CActivityTypeService - Service layer for CActivityType entity. Layer: Service (MVC) Handles business logic for project-aware activity type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CActivityTypeService extends CTypeEntityService<CActivityType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeService.class);
	@Autowired
	private final IActivityRepository activityRepository;

	public CActivityTypeService(final IActivityTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IActivityRepository activityRepository) {
		super(repository, clock, sessionService);
		this.activityRepository = activityRepository;
	}

	/** Checks dependencies before allowing activity type deletion. Prevents deletion if the type is being used by any activities. Always calls
	 * super.checkDeleteAllowed() first to ensure all parent-level checks (null validation, non-deletable flag) are performed.
	 * @param entity the activity type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CActivityType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			// Check if any activities are using this type
			final long usageCount = activityRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d activit%s.", usageCount, usageCount == 1 ? "y" : "ies");
			}
			return null; // Type can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for activity type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CActivityType> getEntityClass() { return CActivityType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CActivityTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceActivityType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Initializes a new activity type. Most common fields are initialized by super class.
	 * @param entity the newly created activity type to initialize */
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((IActivityTypeRepository) repository).countByCompany(activeCompany);
		final String autoName = String.format("ActivityType %02d", typeCount + 1);
		((CEntityNamed<?>) entity).setName(autoName);
	}

	@Override
	protected void validateEntity(final CActivityType entity) {
		super.validateEntity(entity);
		// 1. Required Fields (Name checked in base)
		// 2. Unique Checks
		final Optional<CActivityType> existing = ((IActivityTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
