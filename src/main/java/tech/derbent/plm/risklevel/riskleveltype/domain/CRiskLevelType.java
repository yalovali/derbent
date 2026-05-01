package tech.derbent.plm.risklevel.riskleveltype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "criskleveltype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "criskleveltype_id"))
public class CRiskLevelType extends CTypeEntity<CRiskLevelType> {

	public static final String DEFAULT_COLOR = "#7A6E58"; // Risk level palette
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Risk Level Types";
	public static final String ENTITY_TITLE_SINGULAR = "Risk Level Type";
	public static final String VIEW_NAME = "Risk Level Types View";

	protected CRiskLevelType() {}

	public CRiskLevelType(final String name, final CCompany company) {
		super(CRiskLevelType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		setLevel(-1);
		setCanHaveChildren(false);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
