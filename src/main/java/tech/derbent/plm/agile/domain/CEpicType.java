package tech.derbent.plm.agile.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cepictype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cepictype_id"))
public class CEpicType extends CTypeEntity<CEpicType> {

	public static final String DEFAULT_COLOR = "#6F42C1";
	public static final String DEFAULT_ICON = "vaadin:records";
	public static final String ENTITY_TITLE_PLURAL = "Epic Types";
	public static final String ENTITY_TITLE_SINGULAR = "Epic Type";
	public static final String VIEW_NAME = "Epic Type Management";

	protected CEpicType() {}

	public CEpicType(final String name, final CCompany company) {
		super(CEpicType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
