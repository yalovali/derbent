package tech.derbent.plm.assets.assettype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cassettype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cassettype_id"))
public class CAssetType extends CTypeEntity<CAssetType> {

	public static final String DEFAULT_COLOR = "#708090"; // X11 SlateGray - asset types (darker)
	public static final String DEFAULT_ICON = "vaadin:briefcase";
	public static final String ENTITY_TITLE_PLURAL = "Asset Types";
	public static final String ENTITY_TITLE_SINGULAR = "Asset Type";
	public static final String VIEW_NAME = "Asset Type Management";

	/** Default constructor for JPA. */
	public CAssetType() {
		super();
		initializeDefaults();
	}

	public CAssetType(final String name, final CCompany company) {
		super(CAssetType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
