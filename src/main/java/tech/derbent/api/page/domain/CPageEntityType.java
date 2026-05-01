package tech.derbent.api.page.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cpageentitytype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cpageentitytype_id"))
public class CPageEntityType extends CTypeEntity<CPageEntityType> {

	public static final String DEFAULT_COLOR = "#BDB76B"; // DarkKhaki - page categories
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Page Entity Types";
	public static final String ENTITY_TITLE_SINGULAR = "Page Entity Type";
	public static final String VIEW_NAME = "Page Entity Types View";

	protected CPageEntityType() {}

	public CPageEntityType(final String name, final CCompany company) {
		super(CPageEntityType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		setLevel(-1);
		setCanHaveChildren(false);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
