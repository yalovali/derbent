package tech.derbent.abstracts.domains;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.projects.domain.CProject;

@MappedSuperclass
public class CEntityOfProject extends CEntityNamed {

	// Many risks belong to one project
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "project_id", nullable = false)
	private CProject project;

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

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	public void setProject(final CProject project) { this.project = project; }
}
