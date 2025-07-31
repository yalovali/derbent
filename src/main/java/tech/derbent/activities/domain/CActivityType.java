package tech.derbent.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityType - Domain entity representing activity types. Layer: Domain (MVC)
 * Inherits from CTypeEntity to provide project-aware type functionality for activities.
 */
@Entity
@Table(name = "cactivitytype")
@AttributeOverride(name = "id", column = @Column(name = "cactivitytype_id"))
public class CActivityType extends CTypeEntity {

    /**
     * Default constructor for JPA.
     */
    public CActivityType() {
        super();
    }

    /**
     * Constructor with required fields only.
     * @param name    the name of the activity type
     * @param project the project this type belongs to
     */
    public CActivityType(final String name, final CProject project) {
        super(name, project);
    }
}