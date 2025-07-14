package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.domains.CEntityDB;

public abstract class CAbstractService<EntityClass extends CEntityDB> {

	protected final Clock clock;
	protected final CAbstractRepository<EntityClass> repository;

	public CAbstractService(final CAbstractRepository<EntityClass> repository, final Clock clock) {
		this.clock = clock;
		this.repository = repository;
	}

	public int count() {
		return (int) repository.count();
	}

	public void delete(final EntityClass entity) {
		repository.delete(entity);
	}

	public void delete(final Long id) {
		repository.deleteById(id);
	}

	public Optional<EntityClass> get(final Long id) {
		return repository.findById(id);
	}
	// public Page<EntityClass> list(final Pageable pageable) { return
	// repository.findAll(pageable); }

	@Transactional(readOnly = true)
	public List<EntityClass> list(final Pageable pageable) {
		return repository.findAllBy(pageable).toList();
	}

	public Page<EntityClass> list(final Pageable pageable, final Specification<EntityClass> filter) {
		return repository.findAll(filter, pageable);
	}

	public CEntityDB save(final EntityClass entity) {
		return repository.save(entity);
	}
}