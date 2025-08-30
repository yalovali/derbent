package tech.derbent.gannt.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.gannt.view.CGanntViewEntityView;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cganntview")
@AttributeOverride (name = "id", column = @Column (name = "ganntview_id"))
public class CGanntViewEntity extends CEntityOfProject<CGanntViewEntity> {
	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() { return "#1f3221"; }

	public static String getIconFilename() { return "vaadin:viewport"; }

	public static Class<?> getViewClass() { return CGanntViewEntityView.class; }

	public CGanntViewEntity() {
		super();
	}

	public CGanntViewEntity(final String name, final CProject project) {
		super(CGanntViewEntity.class, name, project);
	}
}
