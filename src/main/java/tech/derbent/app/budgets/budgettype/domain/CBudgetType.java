package tech.derbent.app.budgets.budgettype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cbudgettype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cbudgettype_id"))
public class CBudgetType extends CTypeEntity<CBudgetType> {

	public static final String DEFAULT_COLOR = "#4CAF50";
	public static final String DEFAULT_ICON = "vaadin:dollar";
	public static final String VIEW_NAME = "Budget Type Management";

	/** Default constructor for JPA. */
	public CBudgetType() {
		super();
	}

	public CBudgetType(final String name, final CProject project) {
		super(CBudgetType.class, name, project);
	}
}
