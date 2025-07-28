package tech.derbent.orders.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityNamed;

/**
 * COrderType - Domain entity representing types of orders in the system.
 * Layer: Domain (MVC)
 * 
 * Defines the various categories of orders that can be created in the system,
 * such as Service Orders, Purchase Orders, Maintenance Orders, etc.
 * 
 * This entity follows the standard CEntityNamed pattern providing automatic
 * name and description fields with MetaData annotation support.
 */
@Entity
@Table(name = "cordertype")
@AttributeOverride(name = "id", column = @Column(name = "order_type_id"))
public class COrderType extends CEntityNamed {

    private static final Logger LOGGER = LoggerFactory.getLogger(COrderType.class);

    /**
     * Default constructor for JPA.
     */
    public COrderType() {
        super();
        LOGGER.debug("COrderType default constructor called");
    }

    /**
     * Constructor with name.
     * 
     * @param name the name of the order type
     */
    public COrderType(final String name) {
        super(name);
        LOGGER.debug("COrderType constructor called with name: {}", name);
    }

    /**
     * Constructor with name and description.
     * 
     * @param name the name of the order type
     * @param description the description of the order type
     */
    public COrderType(final String name, final String description) {
        super(name, description);
        LOGGER.debug("COrderType constructor called with name: {} and description: {}", name, description);
    }
}