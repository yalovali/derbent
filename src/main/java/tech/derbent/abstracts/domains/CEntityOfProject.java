package tech.derbent.abstracts.domains;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

@MappedSuperclass
public abstract class CEntityOfProject extends CEntityNamed {

	// Many risks belong to one project
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "project_id", nullable = false)
	private CProject project;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "assigned_to_id", nullable = true)
	@MetaData (
		displayName = "Assigned To", required = false, readOnly = false,
		description = "User assigned to this activity", hidden = false, order = 10,
		dataProviderBean = "CUserService"
	)
	CUser assignedTo;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "created_by_id", nullable = true)
	@MetaData (
		displayName = "Created By", required = false, readOnly = true,
		description = "User who created this activity", hidden = false, order = 11,
		dataProviderBean = "CUserService"
	)
	CUser createdBy;

	// Default constructor for JPA
	public CEntityOfProject() {
		super();
		this.project = null; // This should be set later
	}

	public CEntityOfProject(final CProject project) {
		this.project = project;
	}

	public CEntityOfProject(final String name, final CProject project) {
		super(name);
		this.project = project;
	}

	public CProject getProject() { return project; }

	public String getProjectName() {
		return (project != null) ? project.getName() : "No Project";
	}

	public CUser getAssignedTo() { return assignedTo; }

	public void setAssignedTo(final CUser assignedTo) { this.assignedTo = assignedTo; }

	public CUser getCreatedBy() { return createdBy; }

	public void setCreatedBy(final CUser createdBy) { this.createdBy = createdBy; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	public void setProject(final CProject project) { this.project = project; }
}
