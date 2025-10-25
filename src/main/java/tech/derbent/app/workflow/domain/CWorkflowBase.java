package tech.derbent.app.workflow.domain;

import tech.derbent.api.domains.CProjectItem;
import tech.derbent.app.projects.domain.CProject;

public abstract class CWorkflowBase<EntityClass extends CWorkflowBase<?>> extends CProjectItem<EntityClass> {

	/** Default constructor for JPA. */
	protected CWorkflowBase() {
		super();
	}

	/** Constructor with required fields.
	 * @param name    the name of the workflow
	 * @param project the project this workflow belongs to */
	protected CWorkflowBase(final Class<EntityClass> clazz, final String name, final CProject project) {
		super(clazz, name, project);
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships from parent class (CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}
}
