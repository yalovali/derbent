package tech.derbent.api.domains;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;

// @FilterDef (name = "byProject", parameters = @ParamDef (name = "projectId", type = Long.class))
// @Filters (@Filter (name = "byProject", condition = "project_id = :projectId"))
@MappedSuperclass
public abstract class CEntityOfProject<EntityClass> extends CEntityNamed<EntityClass> {

	// Many risks belong to one project
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "project_id", nullable = false)
	@OnDelete (action = OnDeleteAction.CASCADE)
	@AMetaData (displayName = "Project", required = true, readOnly = true, description = "Project of this entity", hidden = false, order = 10)
	private CProject project;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "assigned_to_id", nullable = true)
	@AMetaData (
			displayName = "Assigned To", required = false, readOnly = false, description = "User assigned to this activity", hidden = false,
			order = 10, dataProviderBean = "CUserService"
	)
	private CUser assignedTo;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "created_by_id", nullable = true)
	@AMetaData (
			displayName = "Created By", required = false, readOnly = true, description = "User who created this activity", hidden = false, order = 11,
			dataProviderBean = "CUserService"
	)
	private CUser createdBy;

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
}
