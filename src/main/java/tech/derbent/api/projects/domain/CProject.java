package tech.derbent.api.projects.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.users.domain.CUserProjectSettings;

/** CProject - Abstract base class for project entities. Layer: Domain (MVC) Concrete implementations: CProject_Derbent, CProject_BAB */
@Entity
@Table (name = "cproject", uniqueConstraints = {
		@jakarta.persistence.UniqueConstraint (columnNames = {
				"company_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "project_id"))
@AssociationOverride (name = "company", joinColumns = @JoinColumn (name = "company_id", nullable = false))
@jakarta.persistence.Inheritance (strategy = jakarta.persistence.InheritanceType.SINGLE_TABLE)
@jakarta.persistence.DiscriminatorColumn (name = "project_type_discriminator", discriminatorType = jakarta.persistence.DiscriminatorType.STRING)
public abstract class CProject<EntityClass extends CProject<EntityClass>> extends CEntityOfCompany<EntityClass>
		implements ISearchable, IHasStatusAndWorkflow<EntityClass> {

	// Type Management - concrete implementation of IHasStatusAndWorkflow
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Project Type", required = false, readOnly = false, description = "Type category of the project", hidden = false,
			dataProviderBean = "CProjectTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectType entityType;
	// Kanban line moved to CProject_Derbent - variant-specific field
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
	protected CProject() {}

	protected CProject(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name, company);
		initializeDefaults();
	}

	/** Add a user setting to this project and maintain bidirectional relationship.
	 * @param userSettings1 the user settings to add */
	public void addUserSettings(final CUserProjectSettings userSettings1) {
		Check.notNull(userSettings1, "User settings cannot be null");
		if (userSettings1.getProject() != null && !userSettings1.getProject().equals(this)) {
			throw new IllegalArgumentException("User settings already assigned to a different project");
		}
		if (userSettings.contains(userSettings1)) {
			return;
		}
		userSettings.add(userSettings1);
		userSettings1.setProject(this);
	}

	@Override
	protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings ("rawtypes") final CAbstractService serviceTarget,
			final CCloneOptions options) {
		super.copyEntityTo(target, serviceTarget, options);
		if (!(target instanceof final CProject<?> targetProject)) {
			return;
		}
		copyField(this::getEntityType, targetProject::setEntityType);
		if (options.isCloneStatus()) {
			copyField(this::getStatus, targetProject::setStatus);
		}
		// Do NOT copy userSettings - these are user-specific
	}

	public Long getCompanyId() { return getCompany() != null ? getCompany().getId() : null; }

	public CCompany getCompanyInstance(CCompanyService service) {
		if (getCompanyId() == null) {
			return null;
		}
		return service.getById(getCompanyId()).orElseThrow(() -> new IllegalStateException("Company with ID " + getCompanyId() + " not found"));
	}
	// Kanban line getter removed - moved to CProject_Derbent

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

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

	private final void initializeDefaults() {}

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
	public void setCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null for a project");
		super.setCompany(company);
	}
	// Kanban line setter removed - moved to CProject_Derbent

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

	@Override
	public void setStatus(final CProjectItemStatus status) {
		Check.notNull(status, "Status cannot be null - projects must always have a valid status");
		Check.notNull(getCompany(), "Company must be set before applying status");
		Check.isSameCompany(this, status);
		this.status = status;
		updateLastModified();
	}
}
