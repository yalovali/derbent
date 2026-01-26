package tech.derbent.plm.agile.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cfeaturetype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cfeaturetype_id"))
public class CFeatureType extends CTypeEntity<CFeatureType> {

	public static final String DEFAULT_COLOR = "#28A745";
	public static final String DEFAULT_ICON = "vaadin:flash";
	public static final String ENTITY_TITLE_PLURAL = "Feature Types";
	public static final String ENTITY_TITLE_SINGULAR = "Feature Type";
	public static final String VIEW_NAME = "Feature Type Management";

	protected CFeatureType() {
		super();
	}

	public CFeatureType(final String name, final CCompany company) {
		super(CFeatureType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		setIcon(DEFAULT_ICON);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
