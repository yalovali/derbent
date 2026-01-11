package tech.derbent.app.risks.risktype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.companies.domain.CCompany;

@Entity
@Table (name = "crisktype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "crisktype_id"))
public class CRiskType extends CTypeEntity<CRiskType> {

	public static final String DEFAULT_COLOR = "#a712b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Risk Types";
	public static final String ENTITY_TITLE_SINGULAR = "Risk Type";
	public static final String VIEW_NAME = "Risk Type Management";

	/** Default constructor for JPA. */
	public CRiskType() {
		super();
	}

	public CRiskType(final String name, final CCompany company) {
		super(CRiskType.class, name, company);
	}
}
