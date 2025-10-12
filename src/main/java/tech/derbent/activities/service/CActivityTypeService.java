package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;

/** CActivityTypeService - Service layer for CActivityType entity. Layer: Service (MVC) Handles business logic for project-aware activity type
 * operations. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CActivityTypeService extends CEntityOfProjectService<CActivityType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityTypeService.class);
	@Autowired
	private IActivityRepository activityRepository;

	public CActivityTypeService(final IActivityTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IActivityRepository activityRepository) {
		super(repository, clock, sessionService);
		this.activityRepository = activityRepository;
	}

	@Override
	protected Class<CActivityType> getEntityClass() { return CActivityType.class; }

	/** Checks dependencies before allowing activity type deletion. Prevents deletion if the type is being used by any activities.
	 * @param activityType the activity type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDependencies(final CActivityType activityType) {
		Check.notNull(activityType, "Activity type cannot be null");
		Check.notNull(activityType.getId(), "Activity type ID cannot be null");
		try {
			// Check if this type is marked as non-deletable
			if (activityType.getAttributeNonDeletable()) {
				return "This activity type is marked as non-deletable and cannot be removed from the system.";
			}
			// Check if any activities are using this type
			final long usageCount = activityRepository.countByActivityType(activityType);
			if (usageCount > 0) {
				return String.format("Cannot delete activity type. It is being used by %d activit%s.", usageCount, usageCount == 1 ? "y" : "ies");
			}
			return null; // Type can be deleted
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for activity type: {}", activityType.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	/** Initializes a new activity type. Most common fields are initialized by super class.
	 * @param entity the newly created activity type to initialize */
	@Override
	public void initializeNewEntity(final CActivityType entity) {
		super.initializeNewEntity(entity);
		try {
			// Auto-generate name based on count
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long typeCount = ((IActivityTypeRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("ActivityType%02d", typeCount + 1);
				entity.setName(autoName);
			}
			LOGGER.debug("Initialized new activity type");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new activity type", e);
			throw new IllegalStateException("Failed to initialize activity type: " + e.getMessage(), e);
		}
	}
}
