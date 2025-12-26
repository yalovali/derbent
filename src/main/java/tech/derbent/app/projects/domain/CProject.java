package tech.derbent.app.projects.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.companies.service.CCompanyService;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.base.users.domain.CUserProjectSettings;

/** CProject - Domain entity representing projects. Layer: Domain (MVC) Inherits from CEntityDB to provide database functionality. */
@Entity
@Table (name = "cproject", uniqueConstraints = {
		@jakarta.persistence.UniqueConstraint (columnNames = {
				"company_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "project_id"))
public class CProject extends CEntityNamed<CProject> implements ISearchable {

	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - organizational entity
	public static final String DEFAULT_ICON = "vaadin:folder-open";
	public static final String ENTITY_TITLE_PLURAL = "Projects";
	public static final String ENTITY_TITLE_SINGULAR = "Project";
	public static final String VIEW_NAME = "Projects View";
	// Many projects can belong to one company
	@AMetaData (displayName = "Company", required = true, readOnly = false, description = "The company this project belongs to", hidden = false)
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "company_id", nullable = false)
	@OnDelete (action = OnDeleteAction.CASCADE)
	private CCompany company;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "kanban_line_id")
	@AMetaData (
			displayName = "Kanban Line", required = false, readOnly = false, description = "Default Kanban line used to visualize project sprints",
			hidden = false
	)
	private CKanbanLine kanbanLine;
	// lets keep it layzily loaded to avoid loading all user settings at once
	@OneToMany (mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "User Settings", required = false, readOnly = false, description = "User project settings for this project", hidden = false,
			createComponentMethod = "createProjectUserSettingsComponent"
	)
	private final List<CUserProjectSettings> userSettings = new ArrayList<>();

	/** Default constructor for JPA. */
	public CProject() {
		super();
	}

	public CProject(final String name, CCompany company) {
		super(CProject.class, name);
		this.company = company;
	}

	/** Add a user setting to this project and maintain bidirectional relationship.
	 * @param userSettings1 the user settings to add */
	public void addUserSettings(final CUserProjectSettings userSettings1) {
		if (userSettings1 == null) {
			return;
		}
		if (!userSettings.contains(userSettings1)) {
			userSettings.add(userSettings1);
			userSettings1.setProject(this);
		}
	}

	public CCompany getCompany() { return company; }

	public Long getCompanyId() { return company != null ? company.getId() : null; }

	public CCompany getCompanyInstance(CCompanyService service) {
		if (getCompanyId() == null) {
			return null;
		}
		final CCompany company1 =
				service.getById(getCompanyId()).orElseThrow(() -> new IllegalStateException("Company with ID " + getCompanyId() + " not found"));
		return company1;
	}

	public CKanbanLine getKanbanLine() { return kanbanLine; }

	/** Gets the list of user project settings for this project. */
	public List<CUserProjectSettings> getUserSettings() { return userSettings; }

	@Override
	public boolean matches(final String searchText) {
		if (searchText == null || searchText.trim().isEmpty()) {
			return true; // Empty search matches all
		}
		final String lowerSearchText = searchText.toLowerCase().trim();
		// Search in name field
		if (getName() != null && getName().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in description field
		if (getDescription() != null && getDescription().toLowerCase().contains(lowerSearchText)) {
			return true;
		}
		// Search in ID as string
		if (getId() != null && getId().toString().contains(lowerSearchText)) {
			return true;
		}
		return false;
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CEntityNamed to also search in
	 * company field.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: all parent
	 *                    fields plus "company"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, final Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check entity field
		if (fieldNames.remove("company") && getCompany() != null && getCompany().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
	}

	/** Remove a user setting from this project and maintain bidirectional relationship.
	 * @param userSettings1 the user settings to remove */
	public void removeUserSettings(final CUserProjectSettings userSettings1) {
		Check.notNull(userSettings1, "User settings cannot be null");
		if (userSettings.remove(userSettings1)) {
			userSettings1.setProject(null);
		}
	}

	public void setCompany(final CCompany company) { this.company = company; }

	public void setKanbanLine(final CKanbanLine kanbanLine) { this.kanbanLine = kanbanLine; }
}
