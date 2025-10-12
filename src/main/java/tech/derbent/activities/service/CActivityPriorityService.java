package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityPriority;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

@Service
@Transactional
public class CActivityPriorityService extends CEntityOfProjectService<CActivityPriority> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CActivityPriorityService.class);

	public CActivityPriorityService(final IActivityPriorityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findDefaultPriority(final CProject project) {
		return ((IActivityPriorityRepository) repository).findByIsDefaultTrue(project);
	}

	@Override
	protected Class<CActivityPriority> getEntityClass() { return CActivityPriority.class; }

	@Override
	public String checkDependencies(final CActivityPriority activityPriority) {
		final String superCheck = super.checkDependencies(activityPriority);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CActivityPriority entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Activity priority cannot be null");
		tech.derbent.api.utils.Check.notNull(sessionService, "Session service is required for activity priority initialization");
		try {
			java.util.Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
			tech.derbent.api.utils.Check.isTrue(activeProject.isPresent(),
					"No active project in session - project context is required to create activity priorities");
			tech.derbent.projects.domain.CProject currentProject = activeProject.get();
			entity.setProject(currentProject);
			java.util.Optional<tech.derbent.users.domain.CUser> currentUser = sessionService.getActiveUser();
			if (currentUser.isPresent()) {
				entity.setCreatedBy(currentUser.get());
			}
			long priorityCount = ((IActivityPriorityRepository) repository).countByProject(currentProject);
			String autoName = String.format("ActivityPriority%02d", priorityCount + 1);
			entity.setName(autoName);
			entity.setDescription("");
			entity.setColor("#ffc107");
			entity.setSortOrder(100);
			entity.setAttributeNonDeletable(false);
			entity.setIsDefault(false);
			entity.setPriorityLevel(3);
			LOGGER.debug("Initialized new activity priority with auto-generated name: {}", autoName);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new activity priority", e);
			throw new IllegalStateException("Failed to initialize activity priority: " + e.getMessage(), e);
		}
	}
}
