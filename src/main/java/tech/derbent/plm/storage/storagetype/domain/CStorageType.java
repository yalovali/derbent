package tech.derbent.plm.storage.storagetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cstoragetype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cstoragetype_id"))
public class CStorageType extends CTypeEntity<CStorageType> {

	public static final String DEFAULT_COLOR = "#006699";
	public static final String DEFAULT_ICON = "vaadin:warehouse";
	public static final String ENTITY_TITLE_PLURAL = "Storage Types";
	public static final String ENTITY_TITLE_SINGULAR = "Storage Type";
	public static final String VIEW_NAME = "Storage Type Management";

	public CStorageType() {
		super();
		initializeDefaults();
	}

	public CStorageType(final String name, final CCompany company) {
		super(CStorageType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
