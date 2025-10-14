package tech.derbent.api.roles.domain;

import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

public abstract class CRole<EntityType> extends CTypeEntity<EntityType> {

	protected CRole() {
		super();
	}

	protected CRole(final Class<EntityType> clazz, final String name, final CProject project) {
		super(clazz, name, project);
	}
}
