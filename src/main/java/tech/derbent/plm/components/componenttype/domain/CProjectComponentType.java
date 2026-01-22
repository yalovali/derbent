package tech.derbent.plm.components.componenttype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.companies.domain.CCompany;

@Entity
@Table (name = "cprojectcomponenttype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprojectcomponenttype_id"))
public class CProjectComponentType extends CTypeEntity<CProjectComponentType> {

	public static final String DEFAULT_COLOR = "#808000"; // X11 Olive - component types (darker)
	public static final String DEFAULT_ICON = "vaadin:cogs";
	public static final String ENTITY_TITLE_PLURAL = "Component Types";
	public static final String ENTITY_TITLE_SINGULAR = "Component Type";
	public static final String VIEW_NAME = "Component Type Management";

	public CProjectComponentType() {
		super();
		initializeDefaults();
	}

	public CProjectComponentType(final String name, final CCompany company) {
		super(CProjectComponentType.class, name, company);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		setColor(DEFAULT_COLOR);
	}
}
