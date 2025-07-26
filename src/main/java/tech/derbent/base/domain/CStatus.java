package tech.derbent.base.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CStatus - Abstract base entity for all status types in the system.
 * Layer: Domain (MVC)
 * 
 * This class provides common functionality for status entities including name and description.
 * All status types (like CActivityStatus) should inherit from this class.
 */
@MappedSuperclass
public abstract class CStatus extends CTypeEntity {

    @Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(
        displayName = "Status Name", 
        required = true, 
        readOnly = false,
        defaultValue = "", 
        description = "Name of the status", 
        hidden = false, 
        order = 1, 
        maxLength = MAX_LENGTH_NAME
    )
    private String name;

    @Column(name = "description", nullable = true, length = MAX_LENGTH_DESCRIPTION)
    @Size(max = MAX_LENGTH_DESCRIPTION)
    @MetaData(
        displayName = "Description", 
        required = false, 
        readOnly = false,
        defaultValue = "", 
        description = "Description of the status", 
        hidden = false, 
        order = 2, 
        maxLength = MAX_LENGTH_DESCRIPTION
    )
    private String description;

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
        super();
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("CStatus constructor called with null or empty name");
            throw new IllegalArgumentException("Status name cannot be null or empty");
        }
        this.name = name;
        LOGGER.debug("CStatus constructor called with name: {} for {}", name, getClass().getSimpleName());
    }

    /**
     * Constructor with name and description.
     * @param name        the name of the status
     * @param description the description of the status
     */
    public CStatus(final String name, final String description) {
        this(name);
        this.description = description;
        LOGGER.debug("CStatus constructor called with name: {} and description: {} for {}", 
                    name, description, getClass().getSimpleName());
    }

    /**
     * Gets the description of the status.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the name of the status.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the description of the status.
     * @param description the description to set
     */
    public void setDescription(final String description) {
        LOGGER.debug("Setting description for {}: {}", getClass().getSimpleName(), description);
        this.description = description;
    }

    /**
     * Sets the name of the status.
     * @param name the name to set
     */
    public void setName(final String name) {
        LOGGER.debug("Setting name for {}: {}", getClass().getSimpleName(), name);
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("Attempt to set null or empty name for {}", getClass().getSimpleName());
            throw new IllegalArgumentException("Status name cannot be null or empty");
        }
        this.name = name;
    }

    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }
}