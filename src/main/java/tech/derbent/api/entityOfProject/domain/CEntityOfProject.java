package tech.derbent.api.entityOfProject.domain;

import java.util.Arrays;
import java.util.Collection;
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
		project = null;
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

	@Override
	public boolean matchesFilter(final String searchValue, @Nullable Collection<String> fieldNames) {
		if ((searchValue == null) || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		if (fieldNames.remove("project") && (getProject() != null) && getProject().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		if (fieldNames.remove("assignedTo") && (getAssignedTo() != null) && getAssignedTo().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		if (fieldNames.remove("createdBy") && (getCreatedBy() != null) && getCreatedBy().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
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
}
