package tech.derbent.plm.agile.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cagiletype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cagiletype_id"))
public class CAgileType extends CTypeEntity<CAgileType> {

	public static final String DEFAULT_COLOR = "#4966B0";
	public static final String DEFAULT_ICON = "vaadin:cluster";
	public static final String ENTITY_TITLE_PLURAL = "Agile Types";
	public static final String ENTITY_TITLE_SINGULAR = "Agile Type";
	public static final String VIEW_NAME = "Agile Type Management";

	protected CAgileType() {}

	public CAgileType(final String name, final CCompany company) {
		super(CAgileType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
