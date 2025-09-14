package tech.derbent.gannt.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.gannt.view.CGanntViewEntityView;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cganntview")
@AttributeOverride (name = "id", column = @Column (name = "ganntview_id"))
public class CGanntViewEntity extends CEntityOfProject<CGanntViewEntity> {

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() { return "#1f3221"; }

	public static String getStaticIconFilename() { return "vaadin:viewport"; }

	public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { return CGanntViewEntityView.class; }

	public CGanntViewEntity() {
		super();
	}

	public CGanntViewEntity(final String name, final CProject project) {
		super(CGanntViewEntity.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends CAbstractEntityDBPage<?>> getViewClass() { // TODO Auto-generated method stub
		return CGanntViewEntity.getViewClassStatic();
	}
}
