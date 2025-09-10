package tech.derbent.screens.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.view.CGridEntityView;

@Entity
@Table (name = "cgridentity")
@AttributeOverride (name = "id", column = @Column (name = "grid_entity_id"))
public class CGridEntity extends CEntityOfProject<CGridEntity> {

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() { return "#6f42c1"; }

	public static String getIconFilename() { return "vaadin:viewport"; }

	public static Class<?> getViewClassStatic() { return CGridEntityView.class; }

	public CGridEntity() {
		super();
	}

	public CGridEntity(final String name, final CProject project) {
		super(CGridEntity.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return "GridEntity: " + getName();
	}
}
