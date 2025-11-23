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

	public static final String DEFAULT_COLOR = "#9C27B0";
	public static final String DEFAULT_ICON = "vaadin:clipboard-check";
	public static final String VIEW_NAME = "Deliverable Type Management";

	/** Default constructor for JPA. */
	public CDeliverableType() {
		super();
	}

	public CDeliverableType(final String name, final CProject project) {
		super(CDeliverableType.class, name, project);
	}
}
