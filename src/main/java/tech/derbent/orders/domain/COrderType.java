package tech.derbent.orders.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/**
 * COrderType - Domain entity representing types of orders in the system.
 * Layer: Domain (MVC)
 * 
 * Defines the various categories of orders that can be created in the system,
 * such as Service Orders, Purchase Orders, Maintenance Orders, etc.
 * 
 * This entity follows the CEntityOfProject pattern providing project-aware
 * functionality with automatic name and description fields.
 */
@Entity
@Table(name = "cordertype")
@AttributeOverride(name = "id", column = @Column(name = "order_type_id"))
public class COrderType extends CEntityOfProject {

    private static final Logger LOGGER = LoggerFactory.getLogger(COrderType.class);

    /**
     * Default constructor for JPA.
     */
    public COrderType() {
        super();
        LOGGER.debug("COrderType default constructor called");
    }

    /**
     * Constructor with name and project.
     * 
     * @param name the name of the order type
     * @param project the project this type belongs to
     */
    public COrderType(final String name, final CProject project) {
        super(name, project);
        LOGGER.debug("COrderType constructor called with name: {} for project: {}", 
            name, project.getName());
    }

    /**
     * Constructor with name, description and project.
     * 
     * @param name the name of the order type
     * @param description the description of the order type
     * @param project the project this type belongs to
     */
    public COrderType(final String name, final String description, final CProject project) {
        super(name, project);
        setDescription(description);
        LOGGER.debug("COrderType constructor called with name: {} and description: {} for project: {}", 
            name, description, project.getName());
    }
}