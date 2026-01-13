package tech.derbent.api.projects.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.AssociationOverride;
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
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CCompanyService;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
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
@AssociationOverride (name = "company", joinColumns = @JoinColumn (name = "company_id", nullable = false))
public class CProject extends CEntityOfCompany<CProject> implements ISearchable, IHasStatusAndWorkflow<CProject>, IHasAttachments {

	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - organizational entity
	public static final String DEFAULT_ICON = "vaadin:folder-open";
	public static final String ENTITY_TITLE_PLURAL = "Projects";
	public static final String ENTITY_TITLE_SINGULAR = "Project";
	public static final String VIEW_NAME = "Projects View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "project_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Project documentation and files", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// Type Management - concrete implementation of IHasStatusAndWorkflow
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Project Type", required = false, readOnly = false, description = "Type category of the project", hidden = false,
			dataProviderBean = "CProjectTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectType entityType;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "kanban_line_id")
	@AMetaData (
			displayName = "Kanban Line", required = false, readOnly = false, description = "Default Kanban line used to visualize project sprints",
			hidden = false
	)
	private CKanbanLine kanbanLine;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "status_id")
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current status of the project", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getAvailableStatusesForProjectItem", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectItemStatus status;
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
		super(CProject.class, name, company);
	}

	/** Add a user setting to this project and maintain bidirectional relationship.
	 * @param userSettings1 the user settings to add */
	public void addUserSettings(final CUserProjectSettings userSettings1) {
		Check.notNull(userSettings1, "User settings cannot be null");
		if (userSettings1.getProject() != null && !userSettings1.getProject().equals(this)) {
			throw new IllegalArgumentException("User settings already assigned to a different project");
		}
		if (!userSettings.contains(userSettings1)) {
			userSettings.add(userSettings1);
			userSettings1.setProject(this);
		}
	}

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	public Long getCompanyId() { return getCompany() != null ? getCompany().getId() : null; }

	public CCompany getCompanyInstance(CCompanyService service) {
		if (getCompanyId() == null) {
			return null;
		}
		final CCompany company1 =
				service.getById(getCompanyId()).orElseThrow(() -> new IllegalStateException("Company with ID " + getCompanyId() + " not found"));
		return company1;
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public CKanbanLine getKanbanLine() { return kanbanLine; }

	@Override
	public CProjectItemStatus getStatus() { return status; }

	/** Gets the list of user project settings for this project. */
	public List<CUserProjectSettings> getUserSettings() { return userSettings; }

	// IHasStatusAndWorkflow implementation
	@Override
	public CWorkflowEntity getWorkflow() {
		if (entityType == null) {
			return null;
		}
		return entityType.getWorkflow();
	}

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
		if (fieldNames != null && fieldNames.remove("company") && getCompany() != null
				&& getCompany().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
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

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null for a project");
		super.setCompany(company);
		if (kanbanLine != null) {
			Check.isSameCompany(this, kanbanLine);
		}
	}

	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		if (typeEntity != null) {
			Check.instanceOf(typeEntity, CProjectType.class, "Type entity must be an instance of CProjectType");
			Check.notNull(getCompany(), "Company must be set before assigning project type");
			Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning project type");
			Check.isTrue(typeEntity.getCompany().getId().equals(getCompany().getId()),
					"Type entity company id " + typeEntity.getCompany().getId() + " does not match project company id " + getCompany().getId());
		}
		entityType = (CProjectType) typeEntity;
		updateLastModified();
	}

	public void setKanbanLine(final CKanbanLine kanbanLine) {
		if (kanbanLine != null) {
			Check.isSameCompany(this, kanbanLine);
		}
		this.kanbanLine = kanbanLine;
	}

	@Override
	public void setStatus(final CProjectItemStatus status) {
		Check.notNull(status, "Status cannot be null - projects must always have a valid status");
		Check.notNull(getCompany(), "Company must be set before applying status");
		Check.isSameCompany(this, status);
		this.status = status;
		updateLastModified();
	}
}
