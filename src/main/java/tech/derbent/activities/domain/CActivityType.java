package tech.derbent.activities.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CActivityType - Domain entity representing activity types.
 * Layer: Domain (MVC)
 * Inherits from CTypeEntity to provide type functionality for activities.
 */
@Entity
@Table(name = "cactivitytype")
public class CActivityType extends CTypeEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Type Name", required = true, readOnly = false, defaultValue = "", description = "Name of the activity type", hidden = false, order = 1, maxLength = MAX_LENGTH_NAME)
    private String name;

    @Column(name = "description", nullable = true, length = MAX_LENGTH_DESCRIPTION)
    @Size(max = MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Description", required = false, readOnly = false, defaultValue = "", description = "Description of the activity type", hidden = false, order = 2, maxLength = MAX_LENGTH_DESCRIPTION)
    private String description;

    /**
     * Default constructor for JPA.
     */
    public CActivityType() {
        super();
    }

    /**
     * Constructor with name.
     * @param name the name of the activity type
     */
    public CActivityType(final String name) {
        super();
        this.name = name;
    }

    /**
     * Constructor with name and description.
     * @param name the name of the activity type
     * @param description the description of the activity type
     */
    public CActivityType(final String name, final String description) {
        super();
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CActivityType)) {
            return false;
        }
        final CActivityType that = (CActivityType) o;
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}