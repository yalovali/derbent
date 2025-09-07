package tech.derbent.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.abstracts.interfaces.CKanbanType;
import tech.derbent.activities.view.CActivityTypeView;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cactivitytype")
@AttributeOverride (name = "id", column = @Column (name = "cactivitytype_id"))
public class CActivityType extends CTypeEntity<CActivityType> implements CKanbanType {

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return "#007bff"; // Blue color for activity type entities
	}

	public static String getIconFilename() { return "vaadin:tags"; }

	public static Class<?> getViewClassStatic() { return CActivityTypeView.class; }

	/** Default constructor for JPA. */
	public CActivityType() {
		super();
	}

	public CActivityType(final String name, final CProject project) {
		super(CActivityType.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}
}
