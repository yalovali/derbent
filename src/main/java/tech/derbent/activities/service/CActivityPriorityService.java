package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityPriority;
import tech.derbent.projects.domain.CProject;

@Service
@Transactional
public class CActivityPriorityService extends CEntityOfProjectService<CActivityPriority> {

	private final CActivityPriorityRepository activityPriorityRepository;

	public CActivityPriorityService(final CActivityPriorityRepository repository,
		final Clock clock) {
		super(repository, clock);
		this.activityPriorityRepository = repository;
	}

	@Override
	protected CActivityPriority createNewEntityInstance() {
		return new CActivityPriority();
	}

	/**
	 * Creates a new activity priority.
	 * @param name    the priority name - must not be null or empty
	 * @param project the project - must not be null
	 * @return the created priority
	 */
	@Transactional
	public CActivityPriority createPriority(final String name, final CProject project) {
		LOGGER.info("createPriority called with name: {}, project: {}", name,
			project != null ? project.getName() : "null");

		if ((name == null) || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Priority name cannot be null or empty");
		}

		if (project == null) {
			throw new IllegalArgumentException("Project cannot be null");
		}
		final CActivityPriority priority = new CActivityPriority(name.trim(), project);
		return repository.saveAndFlush(priority);
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findByName(final String name) {

		if ((name == null) || name.trim().isEmpty()) {
			return Optional.empty();
		}
		return activityPriorityRepository.findByNameIgnoreCase(name.trim());
	}

	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findDefaultPriority() {
		return activityPriorityRepository.findByIsDefaultTrue();
	}
}
