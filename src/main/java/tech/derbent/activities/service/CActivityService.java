package tech.derbent.activities.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.activities.domain.CActivity;

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
}
