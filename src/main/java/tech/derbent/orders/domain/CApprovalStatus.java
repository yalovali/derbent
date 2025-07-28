package tech.derbent.orders.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityNamed;

/**
 * CApprovalStatus - Domain entity representing approval status states in the system.
 * Layer: Domain (MVC)
 * 
 * Defines the various approval status states that can be assigned to order approvals,
 * such as Pending, Approved, Rejected, Under Review, etc.
 * 
 * This entity follows the standard CEntityNamed pattern providing automatic
 * name and description fields with MetaData annotation support.
 */
@Entity
@Table(name = "capprovalstatus")
@AttributeOverride(name = "id", column = @Column(name = "approval_status_id"))
public class CApprovalStatus extends CEntityNamed {

    private static final Logger LOGGER = LoggerFactory.getLogger(CApprovalStatus.class);

    /**
     * Default constructor for JPA.
     */
    public CApprovalStatus() {
        super();
        LOGGER.debug("CApprovalStatus default constructor called");
    }

    /**
     * Constructor with name.
     * 
     * @param name the name of the approval status
     */
    public CApprovalStatus(final String name) {
        super(name);
        LOGGER.debug("CApprovalStatus constructor called with name: {}", name);
    }

    /**
     * Constructor with name and description.
     * 
     * @param name the name of the approval status
     * @param description the description of the approval status
     */
    public CApprovalStatus(final String name, final String description) {
        super(name, description);
        LOGGER.debug("CApprovalStatus constructor called with name: {} and description: {}", name, description);
    }
}