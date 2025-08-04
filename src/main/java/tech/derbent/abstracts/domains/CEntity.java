package tech.derbent.abstracts.domains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CEntity<EntityClass> implements CInterfaceIconSet {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected final Class<EntityClass> clazz;

	/**
	 * Default constructor for JPA. Uses reflection to determine the entity class.
	 */
	@SuppressWarnings ("unchecked")
	protected CEntity() {
		// For JPA compatibility - derive class from generic type information
		this.clazz = (Class<EntityClass>) getClass();
	}

	public CEntity(final Class<EntityClass> clazz) {
		this.clazz = clazz;
	}

	public Class<EntityClass> getClazz() { return clazz; }
}