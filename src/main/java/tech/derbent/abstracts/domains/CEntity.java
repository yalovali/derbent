package tech.derbent.abstracts.domains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CEntity<EntityClass> {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected final Class<EntityClass> clazz;

	public CEntity(final Class<EntityClass> clazz) {
		this.clazz = clazz;
	}

	public Class<EntityClass> getClazz() { return clazz; }
}