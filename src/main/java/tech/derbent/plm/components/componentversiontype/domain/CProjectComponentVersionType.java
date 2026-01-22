package tech.derbent.plm.components.componentversiontype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.companies.domain.CCompany;

@Entity
@Table (name = "ccomponentversiontype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "ccomponentversiontype_id"))
public class CProjectComponentVersionType extends CTypeEntity<CProjectComponentVersionType> {

	public static final String DEFAULT_COLOR = "#808000"; // X11 Olive - version types (darker)
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Component Version Types";
	public static final String ENTITY_TITLE_SINGULAR = "Component Version Type";
	public static final String VIEW_NAME = "Component Version Type Management";

	public CProjectComponentVersionType() {
		super();
		initializeDefaults();
	}

	public CProjectComponentVersionType(final String name, final CCompany company) {
		super(CProjectComponentVersionType.class, name, company);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		setColor(DEFAULT_COLOR);
	}
}
