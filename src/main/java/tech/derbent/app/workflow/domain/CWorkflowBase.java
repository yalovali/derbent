package tech.derbent.app.workflow.domain;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
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
}
