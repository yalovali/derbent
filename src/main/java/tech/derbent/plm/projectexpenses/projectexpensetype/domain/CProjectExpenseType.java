package tech.derbent.plm.projectexpenses.projectexpensetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cprojectexpensetype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprojectexpensetype_id"))
public class CProjectExpenseType extends CTypeEntity<CProjectExpenseType> {

	public static final String DEFAULT_COLOR = "#A0522D"; // X11 Sienna - expense types (darker)
	public static final String DEFAULT_ICON = "vaadin:money-withdraw";
	public static final String ENTITY_TITLE_PLURAL = "Project Expense Types";
	public static final String ENTITY_TITLE_SINGULAR = "Project Expense Type";
	public static final String VIEW_NAME = "Project Expense Type Management";

	/** Default constructor for JPA. */
	protected CProjectExpenseType() {}

	public CProjectExpenseType(final String name, final CCompany company) {
		super(CProjectExpenseType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
