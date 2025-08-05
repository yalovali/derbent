package tech.derbent.projects.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * CProject - Domain entity representing projects. Layer: Domain (MVC) Inherits from
 * CEntityDB to provide database functionality.
 */
@Entity
@Table (name = "cproject")
@AttributeOverride (name = "id", column = @Column (name = "project_id"))
public class CProject extends CEntityNamed<CProject> {

	public static String getIconColorCode() {
		return "#fd7e14"; // Orange color for project entities
	}

	public static String getIconFilename() { return "vaadin:briefcase"; }

	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<CUserProjectSettings> userSettings = new ArrayList<>();

	/**
	 * Default constructor for JPA.
	 */
	public CProject() {
		super();
	}

	public CProject(final String name) {
		super(CProject.class, name);
	}

	/**
	 * Gets the list of user project settings for this project.
	 */
	public List<CUserProjectSettings> getUserSettings() {
		return userSettings;
	}
}