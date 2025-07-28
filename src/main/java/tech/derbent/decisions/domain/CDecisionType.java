package tech.derbent.decisions.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CDecisionType - Domain entity representing decision type categories.
 * Layer: Domain (MVC)
 * 
 * Inherits from CTypeEntity to provide type functionality for decision categorization.
 * Supports classification of decisions by type such as Strategic, Operational, 
 * Technical, Financial, etc.
 */
@Entity
@Table(name = "cdecisiontype")
@AttributeOverride(name = "id", column = @Column(name = "decision_type_id"))
public class CDecisionType extends CTypeEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionType.class);

    @Column(name = "color", nullable = true, length = 7)
    @Size(max = 7)
    @MetaData(
        displayName = "Color", required = false, readOnly = false,
        defaultValue = "#4A90E2",
        description = "Hex color code for decision type visualization (e.g., #4A90E2)",
        hidden = false, order = 3, maxLength = 7
    )
    private String color = "#4A90E2";

    @Column(name = "sort_order", nullable = false)
    @MetaData(
        displayName = "Sort Order", required = true, readOnly = false,
        defaultValue = "100", description = "Display order for decision type sorting",
        hidden = false, order = 4
    )
    private Integer sortOrder = 100;

    @Column(name = "requires_approval", nullable = false)
    @MetaData(
        displayName = "Requires Approval", required = true, readOnly = false,
        defaultValue = "true",
        description = "Indicates if decisions of this type require approval workflow",
        hidden = false, order = 5
    )
    private boolean requiresApproval = true;

    @Column(name = "is_active", nullable = false)
    @MetaData(
        displayName = "Is Active", required = true, readOnly = false,
        defaultValue = "true",
        description = "Indicates if this decision type is currently active and available",
        hidden = false, order = 6
    )
    private boolean isActive = true;

    /**
     * Default constructor for JPA.
     */
    public CDecisionType() {
        super();
        LOGGER.debug("CDecisionType() - Creating new decision type instance");
    }

    /**
     * Constructor with name.
     * @param name the name of the decision type - must not be null or empty
     */
    public CDecisionType(final String name) {
        super(name);
        LOGGER.debug("CDecisionType constructor called with name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            LOGGER.warn("CDecisionType constructor - Name parameter is null or empty");
        }
    }

    /**
     * Constructor with name and description.
     * @param name the name of the decision type - must not be null or empty
     * @param description detailed description of the decision type - can be null
     */
    public CDecisionType(final String name, final String description) {
        super(name, description);
        LOGGER.debug("CDecisionType constructor called with name: {}, description: {}", 
                    name, description);
    }

    /**
     * Constructor with all main fields.
     * @param name the name of the decision type - must not be null or empty
     * @param description detailed description of the decision type - can be null
     * @param color the hex color code - can be null, defaults to blue
     * @param requiresApproval whether decisions of this type require approval
     * @param sortOrder the display sort order
     */
    public CDecisionType(final String name, final String description, final String color,
                        final boolean requiresApproval, final Integer sortOrder) {
        super(name, description);
        LOGGER.debug("CDecisionType constructor called with name: {}, description: {}, color: {}, requiresApproval: {}, sortOrder: {}", 
                    name, description, color, requiresApproval, sortOrder);

        this.color = color != null ? color : "#4A90E2";
        this.requiresApproval = requiresApproval;
        this.sortOrder = sortOrder != null ? sortOrder : 100;
    }

    // Getters and Setters

    public String getColor() {
        return color != null ? color : "#4A90E2";
    }

    public void setColor(final String color) {
        this.color = color != null ? color : "#4A90E2";
        updateLastModified();
    }

    public Integer getSortOrder() {
        return sortOrder != null ? sortOrder : 100;
    }

    public void setSortOrder(final Integer sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : 100;
        updateLastModified();
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(final boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
        updateLastModified();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean isActive) {
        this.isActive = isActive;
        updateLastModified();
    }

    // Business Logic Methods

    /**
     * Checks if this decision type is available for use.
     * @return true if the decision type is active
     */
    public boolean isAvailable() {
        return isActive;
    }

    /**
     * Activates this decision type.
     */
    public void activate() {
        LOGGER.debug("activate called for decision type: {}", getName());
        this.isActive = true;
        updateLastModified();
    }

    /**
     * Deactivates this decision type.
     */
    public void deactivate() {
        LOGGER.debug("deactivate called for decision type: {}", getName());
        this.isActive = false;
        updateLastModified();
    }

    @Override
    public String toString() {
        return getName() != null ? getName() : super.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CDecisionType)) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}