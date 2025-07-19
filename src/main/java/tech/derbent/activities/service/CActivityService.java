package tech.derbent.activities.service;

import java.time.Clock;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;

@Service
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CAbstractService<CActivity> {

	CActivityService(final CActivityRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Transactional
	public void createEntity(final String name) {
		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		final var entity = new CActivity();
		entity.setName(name);
		repository.saveAndFlush(entity);
	}

	/**
	 * Finds activities by project.
	 */
	public List<CActivity> findByProject(final CProject project) {
		return ((CActivityRepository) repository).findByProject(project);
	}

	/**
	 * Gets paginated activities by project.
	 */
	public Page<CActivity> listByProject(final CProject project, final Pageable pageable) {
		return ((CActivityRepository) repository).findByProject(project, pageable);
	}
}
