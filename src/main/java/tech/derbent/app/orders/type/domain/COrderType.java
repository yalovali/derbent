package tech.derbent.app.orders.type.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cordertype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "order_type_id"))
public class COrderType extends CTypeEntity<COrderType> {

	public static final String DEFAULT_COLOR = "#D2B48C"; // X11 Tan - order types
	public static final String DEFAULT_ICON = "vaadin:invoice";
	public static final String VIEW_NAME = "Order Type View";

	/** Default constructor for JPA. */
	public COrderType() {
		super();
	}

	/** Constructor with name and project.
	 * @param name    the name of the order type
	 * @param project the project this type belongs to */
	public COrderType(final String name, final CProject project) {
		super(COrderType.class, name, project);
	}
}
