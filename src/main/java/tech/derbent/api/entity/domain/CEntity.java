package tech.derbent.api.entity.domain;

import jakarta.persistence.Transient;

public abstract class CEntity<EntityClass> {

	/** Ignore in JPA */
	@Transient
	private final Class<EntityClass> clazz;
	private final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(getClass());

	/** Default constructor for JPA. Uses reflection to determine the entity class. */
	@SuppressWarnings ("unchecked")
	protected CEntity() {
		// For JPA compatibility - derive class from generic type information
		clazz = (Class<EntityClass>) getClass();
		LOGGER.debug("Initialized entity of type {} with derived class {}.", getClass().getSimpleName(), clazz.getSimpleName());
	}

	public CEntity(final Class<EntityClass> clazz) {
		this.clazz = clazz;
	}

	public Class<EntityClass> getClazz() { return clazz; }
}
