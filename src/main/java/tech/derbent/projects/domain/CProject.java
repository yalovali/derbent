package tech.derbent.projects.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityNamed;

/**
 * CProject - Domain entity representing projects. Layer: Domain (MVC) Inherits from CEntityDB to provide database
 * functionality.
 */
@Entity
@Table(name = "cproject")
@AttributeOverride(name = "id", column = @Column(name = "project_id"))
public class CProject extends CEntityNamed<CProject> {

    /**
     * Default constructor for JPA.
     */
    public CProject() {
        super();
    }

    public CProject(final String name) {
        super(CProject.class, name);
    }
}
