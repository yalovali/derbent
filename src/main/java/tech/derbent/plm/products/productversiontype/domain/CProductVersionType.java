package tech.derbent.plm.products.productversiontype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.companies.domain.CCompany;

@Entity
@Table (name = "cproductversiontype", uniqueConstraints = @UniqueConstraint (columnNames = {
"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cproductversiontype_id"))
public class CProductVersionType extends CTypeEntity<CProductVersionType> {

public static final String DEFAULT_COLOR = "#6B8E23"; // X11 OliveDrab - version types (darker)
public static final String DEFAULT_ICON = "vaadin:tag";
public static final String ENTITY_TITLE_PLURAL = "Product Version Types";
public static final String ENTITY_TITLE_SINGULAR = "Product Version Type";
public static final String VIEW_NAME = "Product Version Type Management";

public CProductVersionType() {
super();
initializeDefaults();
}

public CProductVersionType(final String name, final CCompany company) {
super(CProductVersionType.class, name, company);
initializeDefaults();
}

@Override
protected void initializeDefaults() {
super.initializeDefaults();
setColor(DEFAULT_COLOR);
}
}
