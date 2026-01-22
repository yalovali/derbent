package tech.derbent.plm.providers.providertype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.companies.domain.CCompany;

@Entity
@Table (name = "cprovidertype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprovidertype_id"))
public class CProviderType extends CTypeEntity<CProviderType> {

	public static final String DEFAULT_COLOR = "#696969"; // X11 DimGray - provider types (darker)
	public static final String DEFAULT_ICON = "vaadin:handshake";
	public static final String ENTITY_TITLE_PLURAL = "Provider Types";
	public static final String ENTITY_TITLE_SINGULAR = "Provider Type";
	public static final String VIEW_NAME = "Provider Type Management";

	/** Default constructor for JPA. */
	public CProviderType() {
		super();
		initializeDefaults();
	}

	public CProviderType(final String name, final CCompany company) {
		super(CProviderType.class, name, company);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		setColor(DEFAULT_COLOR);
	}
}
