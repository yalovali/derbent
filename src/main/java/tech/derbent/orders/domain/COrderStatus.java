package tech.derbent.orders.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityNamed;

/**
 * COrderStatus - Domain entity representing status states of orders in the system.
 * Layer: Domain (MVC)
 * 
 * Defines the various status states an order can have during its lifecycle,
 * such as Draft, Submitted, Approved, In Progress, Completed, Cancelled, etc.
 * 
 * This entity follows the standard CEntityNamed pattern providing automatic
 * name and description fields with MetaData annotation support.
 */
@Entity
@Table(name = "corderstatus")
@AttributeOverride(name = "id", column = @Column(name = "order_status_id"))
public class COrderStatus extends CEntityNamed {

    private static final Logger LOGGER = LoggerFactory.getLogger(COrderStatus.class);

    /**
     * Default constructor for JPA.
     */
    public COrderStatus() {
        super();
        LOGGER.debug("COrderStatus default constructor called");
    }

    /**
     * Constructor with name.
     * 
     * @param name the name of the order status
     */
    public COrderStatus(final String name) {
        super(name);
        LOGGER.debug("COrderStatus constructor called with name: {}", name);
    }

    /**
     * Constructor with name and description.
     * 
     * @param name the name of the order status
     * @param description the description of the order status
     */
    public COrderStatus(final String name, final String description) {
        super(name, description);
        LOGGER.debug("COrderStatus constructor called with name: {} and description: {}", name, description);
    }
}