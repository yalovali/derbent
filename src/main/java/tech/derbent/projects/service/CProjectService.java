package tech.derbent.projects.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.projects.domain.CProject;

@Service
@PreAuthorize("isAuthenticated()")
public class CProjectService extends CAbstractService<CProject> {

	CProjectService(final CProjectRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Transactional
	public void createEntity(final String name) {
		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		final var entity = new CProject();
		entity.setName(name);
		repository.saveAndFlush(entity);
	}
}
