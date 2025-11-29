package tech.derbent.app.projectexpenses.projectexpensetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cprojectexpensetype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprojectexpensetype_id"))
public class CProjectExpenseType extends CTypeEntity<CProjectExpenseType> {

	public static final String DEFAULT_COLOR = "#FFDEAD"; // X11 Navajowhite - expense types
	public static final String DEFAULT_ICON = "vaadin:money-withdraw";
	public static final String ENTITY_TITLE_PLURAL = "Project Expense Types";
	public static final String ENTITY_TITLE_SINGULAR = "Project Expense Type";
	public static final String VIEW_NAME = "Project Expense Type Management";

	/** Default constructor for JPA. */
	public CProjectExpenseType() {
		super();
	}

	public CProjectExpenseType(final String name, final CProject project) {
		super(CProjectExpenseType.class, name, project);
	}
}
