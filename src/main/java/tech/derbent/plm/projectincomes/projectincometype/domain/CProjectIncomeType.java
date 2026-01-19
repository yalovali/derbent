package tech.derbent.plm.projectincomes.projectincometype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.companies.domain.CCompany;

@Entity
@Table (name = "cprojectincometype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprojectincometype_id"))
public class CProjectIncomeType extends CTypeEntity<CProjectIncomeType> {

	public static final String DEFAULT_COLOR = "#B8860B"; // X11 DarkGoldenrod - income types (darker)
	public static final String DEFAULT_ICON = "vaadin:money-deposit";
	public static final String ENTITY_TITLE_PLURAL = "Project Income Types";
	public static final String ENTITY_TITLE_SINGULAR = "Project Income Type";
	public static final String VIEW_NAME = "Project Income Type Management";

	/** Default constructor for JPA. */
	public CProjectIncomeType() {
		super();
	}

	public CProjectIncomeType(final String name, final CCompany company) {
		super(CProjectIncomeType.class, name, company);
	}
}
