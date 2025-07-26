package tech.derbent.abstracts.domains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CEntity {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	// protected final String className = getClass().getSimpleName();

	public CEntity() {
		LOGGER.debug("CEntity constructor called for {}", getClass().getSimpleName());
	}
}