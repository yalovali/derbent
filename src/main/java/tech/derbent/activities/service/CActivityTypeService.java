package tech.derbent.activities.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.api.services.CTypeEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

/** CActivityTypeService - Service layer for CActivityType entity. Layer: Service (MVC) Handles business logic for project-aware activity type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CActivityTypeService extends CTypeEntityService<CActivityType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeService.class);
	@Autowired
	private IActivityRepository activityRepository;

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
			final long usageCount = activityRepository.countByActivityType(entity);
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
	protected Class<CActivityType> getEntityClass() { return CActivityType.class; }

	/** Initializes a new activity type. Most common fields are initialized by super class.
	 * @param entity the newly created activity type to initialize */
	@Override
	public void initializeNewEntity(final CActivityType entity) {
		super.initializeNewEntity(entity);
		CProject activeProject = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project in session"));
		long typeCount = ((IActivityTypeRepository) repository).countByProject(activeProject);
		String autoName = String.format("ActivityType %02d", typeCount + 1);
		entity.setName(autoName);
	}
}
