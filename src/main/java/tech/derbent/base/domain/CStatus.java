package tech.derbent.base.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.MappedSuperclass;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

/**
 * CStatus - Abstract base entity for all status types in the system. Layer: Domain (MVC) This class provides common
 * functionality for status entities including name and description. All status types (like CActivityStatus) should
 * inherit from this class.
 */
@MappedSuperclass
public abstract class CStatus<EntityType> extends CTypeEntity<EntityType> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(CStatus.class);

    /**
     * Default constructor for JPA.
     */
    protected CStatus() {
        super();
    }

    /**
     * Constructor with name (required field).
     * 
     * @param name
     *            the name of the status
     */
    protected CStatus(final Class<EntityType> clazz, final String name, final CProject project) {
        super(clazz, name, project);
    }
}
