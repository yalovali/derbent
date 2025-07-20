package tech.derbent.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

@Entity
@Table(name = "cactivity") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "activity_id")) // Override the default column name for the ID field
public class CActivity extends CEntityOfProject {

	public CActivity() {
		super();
		// Default constructor - project will be set later
	}

	public CActivity(final String name, final CProject project) {
		super(name, project);
	}
}
