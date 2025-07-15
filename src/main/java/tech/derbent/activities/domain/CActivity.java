package tech.derbent.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;

@Entity
@Table(name = "cactivity") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "activity_id")) // Override the default column name for the ID field
public class CActivity extends CEntityDB {

	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	private String name;
	// Many activities belong to one project
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private CProject project;

	public CActivity() {
		this.project = new CProject();
	}

	public CActivity(final String name, final CProject project) {
		this.name = name;
		this.project = project;
	}

	public String getName() { return name; }

	public CProject getProject() { return project; }

	public void setName(final String name) { this.name = name; }

	public void setProject(final CProject project) { this.project = project; }
}
