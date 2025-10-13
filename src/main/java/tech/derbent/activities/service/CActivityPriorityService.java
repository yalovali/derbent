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
	public String checkDeleteAllowed(final CActivityPriority activityPriority) {
		final String superCheck = super.checkDeleteAllowed(activityPriority);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CActivityPriority entity) {
		super.initializeNewEntity(entity);
		try {
			Optional<CProject> activeProject = sessionService.getActiveProject();
			if (activeProject.isPresent()) {
				long priorityCount = ((IActivityPriorityRepository) repository).countByProject(activeProject.get());
				String autoName = String.format("ActivityPriority%02d", priorityCount + 1);
				entity.setName(autoName);
			}
			LOGGER.debug("Initialized new cactivitypriority");
		} catch (final Exception e) {
			LOGGER.error("Error initializing new cactivitypriority", e);
			throw new IllegalStateException("Failed to initialize cactivitypriority: " + e.getMessage(), e);
		}
	}
}
