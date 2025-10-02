package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

/** CUserType - Domain entity representing user types. Layer: Domain (MVC) Inherits from CTypeEntity to provide project-aware type functionality for
 * users with color support. */
@Entity
@Table (name = "cusertype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cusertype_id"))
public class CUserType extends CTypeEntity<CUserType> {

	public static final String DEFAULT_COLOR = "#005e7b";
	public static final String DEFAULT_ICON = "vaadin:bell";
	public static final String VIEW_NAME = "User Type View";

	public CUserType() {
		super();
	}

	public CUserType(final String name, final CProject project) {
		super(CUserType.class, name, project);
	}

	@Override
	public void initializeAllFields() {
		// TODO Auto-generated method stub
	}
}
