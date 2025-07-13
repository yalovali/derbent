package tech.derbent.abstracts.services;

import java.time.Clock;

import tech.derbent.abstracts.domains.CEntityDB;

public abstract class CAbstractService<EntityClass extends CEntityDB> {

	protected final Clock clock;
	protected final CAbstractRepository<EntityClass> repository;

	public CAbstractService(final CAbstractRepository<EntityClass> repository, final Clock clock) {
		this.clock = clock;
		this.repository = repository;
	}
}