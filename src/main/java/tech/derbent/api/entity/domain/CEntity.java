package tech.derbent.api.entity.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Transient;

public abstract class CEntity<EntityClass> {

	/** Ignore in JPA */
	@Transient
	protected final Class<EntityClass> clazz;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/** Default constructor for JPA. Uses reflection to determine the entity class. */
	@SuppressWarnings ("unchecked")
	protected CEntity() {
		// For JPA compatibility - derive class from generic type information
		clazz = (Class<EntityClass>) getClass();
	}

	public CEntity(final Class<EntityClass> clazz) {
		this.clazz = clazz;
	}

	public Class<EntityClass> getClazz() { return clazz; }
}
