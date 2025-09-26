package tech.derbent.api.roles.domain;

import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

public abstract class CRole<EntityType> extends CTypeEntity<EntityType> {

	/** Default constructor for JPA. */
	protected CRole() {
		super();
	}

	/** Constructor with required fields.
	 * @param clazz   the entity class
	 * @param name    the name of the role
	 * @param project the project this role belongs to */
	protected CRole(final Class<EntityType> clazz, final String name, final CProject project) {
		super(clazz, name, project);
	}
}
