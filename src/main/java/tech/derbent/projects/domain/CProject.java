package tech.derbent.projects.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityNamed;

/**
 * CProject - Domain entity representing projects. Layer: Domain (MVC) Inherits from
 * CEntityDB to provide database functionality.
 */
@Entity
@Table (name = "cproject") // table name for the entity as the default is the class name
							// in lowercase
@AttributeOverride (name = "id", column = @Column (name = "project_id")) // Override the
																			// default
																			// column name
																			// for the ID
																			// field
public class CProject extends CEntityNamed {
}
