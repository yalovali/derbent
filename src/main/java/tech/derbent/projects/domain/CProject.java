package tech.derbent.projects.domain;

import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.activities.domain.CActivity;

@Entity
@Table(name = "cproject") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "project_id")) // Override the default column name for the ID field
public class CProject extends CEntityDB {

	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	private String name;
	// One project has many activities
	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CActivity> activities;

	public List<CActivity> getActivities() { return activities; }

	public String getName() { return name; }

	public void setActivities(final List<CActivity> activities) { this.activities = activities; }

	public void setName(final String name) { this.name = name; }
}
