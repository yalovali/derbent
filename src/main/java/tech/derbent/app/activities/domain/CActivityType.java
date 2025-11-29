package tech.derbent.app.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cactivitytype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cactivitytype_id"))
public class CActivityType extends CTypeEntity<CActivityType> {

	public static final String DEFAULT_COLOR = "#17a2b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Activity Types";
	public static final String ENTITY_TITLE_SINGULAR = "Activity Type";
	public static final String VIEW_NAME = "Activity Type Management";

	/** Default constructor for JPA. */
	public CActivityType() {
		super();
	}

	public CActivityType(final String name, final CProject project) {
		super(CActivityType.class, name, project);
	}
}
