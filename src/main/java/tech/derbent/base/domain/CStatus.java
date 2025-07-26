package tech.derbent.base.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.MappedSuperclass;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CStatus - Abstract base entity for all status types in the system. Layer: Domain (MVC)
 * This class provides common functionality for status entities including name and
 * description. All status types (like CActivityStatus) should inherit from this class.
 */
@MappedSuperclass
public abstract class CStatus extends CTypeEntity {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CStatus.class);

	/**
	 * Default constructor for JPA.
	 */
	public CStatus() {
		super();
		LOGGER.debug("CStatus constructor called for {}", getClass().getSimpleName());
	}

	/**
	 * Constructor with name.
	 * @param name the name of the status
	 */
	public CStatus(final String name) {
		super(name);
	}

	/**
	 * Constructor with name and description.
	 * @param name        the name of the status
	 * @param description the description of the status
	 */
	public CStatus(final String name, final String description) {
		super(name, description);
		LOGGER.debug(
			"CStatus constructor called with name: {} and description: {} for {}", name,
			description, getClass().getSimpleName());
	}

	@Override
	public String toString() {
		return super.toString();
	}
}