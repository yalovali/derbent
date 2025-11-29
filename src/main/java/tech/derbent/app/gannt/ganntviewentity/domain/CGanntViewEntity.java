package tech.derbent.app.gannt.ganntviewentity.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cganntview")
@AttributeOverride (name = "id", column = @Column (name = "ganntview_id"))
public class CGanntViewEntity extends CEntityOfProject<CGanntViewEntity> {

	public static final String DEFAULT_COLOR = "#fd7e14";
	public static final String DEFAULT_ICON = "vaadin:timeline";
	public static final String ENTITY_TITLE_PLURAL = "Gantt View Entities";
	public static final String ENTITY_TITLE_SINGULAR = "Gantt View Entity";
	public static final String VIEW_NAME = "GanntEntity View";

	public CGanntViewEntity() {
		super();
	}

	public CGanntViewEntity(final String name, final CProject project) {
		super(CGanntViewEntity.class, name, project);
	}
}
