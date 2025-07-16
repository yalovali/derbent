package tech.derbent.users.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize("isAuthenticated()")
public class CUserService extends CAbstractService<CUser> {

	CUserService(final CUserRepository repository, final Clock clock) {
		super(repository, clock);
	}

	@Transactional
	public void createEntity(final String name) {
		if ("fail".equals(name)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		final var entity = new CUser();
		entity.setName(name);
		repository.saveAndFlush(entity);
	}

	public CUser getUserWithProjects(final Long id) {
		return ((CUserRepository) repository).findByIdWithProjects(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
	}
}
