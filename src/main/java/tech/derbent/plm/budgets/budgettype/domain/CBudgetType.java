package tech.derbent.plm.budgets.budgettype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cbudgettype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cbudgettype_id"))
public class CBudgetType extends CTypeEntity<CBudgetType> {

	public static final String DEFAULT_COLOR = "#8B4513"; // X11 SaddleBrown - budget types (darker)
	public static final String DEFAULT_ICON = "vaadin:dollar";
	public static final String ENTITY_TITLE_PLURAL = "Budget Types";
	public static final String ENTITY_TITLE_SINGULAR = "Budget Type";
	public static final String VIEW_NAME = "Budget Type Management";

	/** Default constructor for JPA. */
	/** Default constructor for JPA. */
	protected CBudgetType() {
		super();
	}

	public CBudgetType(final String name, final CCompany company) {
		super(CBudgetType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
