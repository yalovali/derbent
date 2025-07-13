package tech.derbent.users.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.users.domain.CUser;

@Service
@PreAuthorize("isAuthenticated()")
public class CUserService extends CAbstractService<CUser> {

	CUserService(final CUserRepository repository, final Clock clock) {
		super(repository, clock);
	}

	public int count() {
		return (int) repository.count();
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

	public void delete(final Long id) {
		repository.deleteById(id);
	}

	public Optional<CUser> get(final Long id) {
		return repository.findById(id);
	}

	/*
	 * @Transactional(readOnly = true) public List<CUser> list(final Pageable
	 * pageable) { return repository.findAllBy(pageable).toList(); }
	 */
	public Page<CUser> list(final Pageable pageable) {
		return repository.findAll(pageable);
	}

	public Page<CUser> list(final Pageable pageable, final Specification<CUser> filter) {
		return repository.findAll(filter, pageable);
	}

	public CEntityDB save(final CUser entity) {
		return repository.save(entity);
	}
}
