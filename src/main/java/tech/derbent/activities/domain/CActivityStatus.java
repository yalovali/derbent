package tech.derbent.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.base.domain.CStatus;

/**
 * CActivityStatus - Domain entity representing activity status types.
 * Layer: Domain (MVC)
 * 
 * Inherits from CStatus to provide status functionality for activities.
 * This entity defines the possible statuses an activity can have (e.g., TODO, IN_PROGRESS, DONE).
 */
@Entity
@Table(name = "cactivitystatus")
@AttributeOverride(name = "id", column = @Column(name = "cactivitystatus_id"))
public class CActivityStatus extends CStatus {

    /**
     * Default constructor for JPA.
     */
    public CActivityStatus() {
        super();
    }

    /**
     * Constructor with name.
     * @param name the name of the activity status
     */
    public CActivityStatus(final String name) {
        super(name);
    }

    /**
     * Constructor with name and description.
     * @param name        the name of the activity status
     * @param description the description of the activity status
     */
    public CActivityStatus(final String name, final String description) {
        super(name, description);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CActivityStatus)) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return getName() != null ? getName() : super.toString();
    }
}