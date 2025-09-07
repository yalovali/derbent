package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.view.CUserTypeView;

/** CUserType - Domain entity representing user types. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project-aware type functionality
 * for users. */
@Entity
@Table (name = "cusertype")
@AttributeOverride (name = "id", column = @Column (name = "cusertype_id"))
public class CUserType extends CEntityOfProject<CUserType> {

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return "#6f42c1"; // Purple color for user type entities
	}

	public static String getIconFilename() { return "vaadin:group"; }

	public static Class<?> getViewClassStatic() { return CUserTypeView.class; }

	public CUserType() {
		super();
	}

	public CUserType(final String name, final CProject project) {
		super(CUserType.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}
}
