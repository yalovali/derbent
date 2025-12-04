package tech.derbent.api.entityOfProject.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;

// @FilterDef (name = "byProject", parameters = @ParamDef (name = "projectId", type = Long.class))
// @Filters (@Filter (name = "byProject", condition = "project_id = :projectId"))
@MappedSuperclass
public abstract class CEntityOfProject<EntityClass> extends CEntityNamed<EntityClass> {

	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "assigned_to_id", nullable = true)
	@AMetaData (
			displayName = "Assigned To", required = false, readOnly = false, description = "User assigned to this activity", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser assignedTo;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "created_by_id", nullable = true)
	@AMetaData (
			displayName = "Created By", required = false, readOnly = true, description = "User who created this activity", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser createdBy;
	// Many risks belong to one project
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "project_id", nullable = false)
	@OnDelete (action = OnDeleteAction.CASCADE)
	@AMetaData (displayName = "Project", required = true, readOnly = true, description = "Project of this entity", hidden = false)
	private CProject project;

	/** Default constructor for JPA. */
	protected CEntityOfProject() {
		super();
		// Initialize with default values for JPA
		this.project = null;
	}

	public CEntityOfProject(final Class<EntityClass> clazz, final String name, final CProject project) {
		super(clazz, name);
		this.project = project;
	}

	/** Gets the assigned user for this entity.
	 * @return the assigned user */
	public CUser getAssignedTo() { return assignedTo; }

	/** Gets the user who created this entity.
	 * @return the creator user */
	public CUser getCreatedBy() { return createdBy; }

	/** Gets the project this entity belongs to.
	 * @return the project */
	public CProject getProject() { return project; }

	public String getProjectName() { return (project != null) ? project.getName() : "No Project"; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	/** Sets the assigned user for this entity.
	 * @param assignedTo the user to assign */
	public void setAssignedTo(final CUser assignedTo) { this.assignedTo = assignedTo; }

	/** Sets the user who created this entity.
	 * @param createdBy the creator user */
	public void setCreatedBy(final CUser createdBy) { this.createdBy = createdBy; }

	/** Sets the project this entity belongs to.
	 * @param project the project to set */
	public void setProject(final CProject project) { this.project = project; }

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CEntityNamed to also search in
	 * project-related fields (project name, assignedTo name, createdBy name). For entity-type fields (project, assignedTo, createdBy), only
	 * their name is searched.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: "id",
	 *                    "active", "name", "description", "project", "assignedTo", "createdBy"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, final java.util.@Nullable Collection<String> fieldNames) {
		if ((searchValue == null) || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// If no field names specified, default to "name" only (inherited from parent)
		final java.util.Collection<String> fieldsToSearch = ((fieldNames == null) || fieldNames.isEmpty()) ? java.util.List.of("name")
				: fieldNames;
		// Check fields specific to CEntityOfProject
		// For entity fields, only search their name (not description, etc.)
		// Check project field if requested - search only project name
		if (fieldsToSearch.contains("project")) {
			final CProject entityProject = getProject();
			if (entityProject != null) {
				final String projectName = entityProject.getName();
				if ((projectName != null) && projectName.toLowerCase().contains(lowerSearchValue)) {
					return true;
				}
			}
		}
		// Check assignedTo field if requested - search only user name
		if (fieldsToSearch.contains("assignedTo")) {
			final CUser assignedToUser = getAssignedTo();
			if (assignedToUser != null) {
				final String userName = assignedToUser.getName();
				if ((userName != null) && userName.toLowerCase().contains(lowerSearchValue)) {
					return true;
				}
			}
		}
		// Check createdBy field if requested - search only user name
		if (fieldsToSearch.contains("createdBy")) {
			final CUser createdByUser = getCreatedBy();
			if (createdByUser != null) {
				final String userName = createdByUser.getName();
				if ((userName != null) && userName.toLowerCase().contains(lowerSearchValue)) {
					return true;
				}
			}
		}
		// Delegate to parent class for inherited fields (id, active, name, description)
		final java.util.Set<String> parentFields = new java.util.HashSet<>(fieldsToSearch);
		parentFields.retainAll(java.util.List.of("id", "active", "name", "description"));
		if (!parentFields.isEmpty()) {
			return super.matchesFilter(searchValue, parentFields);
		}
		return false;
	}
}
