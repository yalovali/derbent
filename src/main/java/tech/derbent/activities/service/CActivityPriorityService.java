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

	public CActivityPriorityService(final CActivityPriorityRepository repository,
		final Clock clock) {
		super(repository, clock);
	}

	@Transactional (readOnly = true)
	public Optional<CActivityPriority> findDefaultPriority(final CProject project) {
		return ((CActivityPriorityRepository) repository).findByIsDefaultTrue(project);
	}

	@Override
	protected Class<CActivityPriority> getEntityClass() {
		return CActivityPriority.class;
	}
}
