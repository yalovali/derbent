package tech.derbent.app.deliverables.deliverabletype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cdeliverabletype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cdeliverabletype_id"))
public class CDeliverableType extends CTypeEntity<CDeliverableType> {

	public static final String DEFAULT_COLOR = "#BC8F8F"; // X11 RosyBrown - deliverable types (darker)
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String ENTITY_TITLE_PLURAL = "Deliverable Types";
	public static final String ENTITY_TITLE_SINGULAR = "Deliverable Type";
	public static final String VIEW_NAME = "Deliverable Type Management";

	/** Default constructor for JPA. */
	public CDeliverableType() {
		super();
	}

	public CDeliverableType(final String name, final CProject project) {
		super(CDeliverableType.class, name, project);
	}
}
