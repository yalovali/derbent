package tech.derbent.projects.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.interfaces.CSearchable;
import tech.derbent.projects.view.CProjectsView;
import tech.derbent.users.domain.CUserProjectSettings;

/** CProject - Domain entity representing projects. Layer: Domain (MVC) Inherits from CEntityDB to provide database functionality. */
@Entity
@Table (name = "cproject")
@AttributeOverride (name = "id", column = @Column (name = "project_id"))
public class CProject extends CEntityNamed<CProject> implements CSearchable {

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return "#fd7e14"; // Orange color for project entities
	}

	public static String getIconFilename() { return "vaadin:briefcase"; }

	public static Class<?> getViewClassStatic() { return CProjectsView.class; }

	// lets keep it layzily loaded to avoid loading all user settings at once
	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private final List<CUserProjectSettings> userSettings = new ArrayList<>();

	/** Default constructor for JPA. */
	public CProject() {
		super();
	}

	public CProject(final String name) {
		super(CProject.class, name);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}

	/** Gets the list of user project settings for this project. */
	public List<CUserProjectSettings> getUserSettings() { return userSettings; }

	@Override
	public boolean matches(final String searchText) {
		if ((searchText == null) || searchText.trim().isEmpty()) {
			return true; // Empty search matches all
		}
		final String lowerSearchText = searchText.toLowerCase().trim();
		// Search in name field
		if ((getName() != null) && getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in description field
		if ((getDescription() != null) && getDescription().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in ID as string
		if ((getId() != null) && getId().toString().contains(lowerSearchText)) {
			return true;
		}
		return false;
	}
}
