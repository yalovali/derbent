package tech.derbent.users.service;

import java.time.Clock;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize("isAuthenticated()")
public class CUserService extends CAbstractService {

	private final CUserRepository repository;

	CUserService(final CUserRepository repository, final Clock clock) {
		super(clock);
		this.repository = repository;
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

	@Transactional(readOnly = true)
	public List<CUser> list(final Pageable pageable) {
		return repository.findAllBy(pageable).toList();
	}
}
