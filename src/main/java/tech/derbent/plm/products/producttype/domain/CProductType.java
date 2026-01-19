package tech.derbent.plm.products.producttype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.companies.domain.CCompany;

@Entity
@Table (name = "cproducttype", uniqueConstraints = @UniqueConstraint (columnNames = {
"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cproducttype_id"))
public class CProductType extends CTypeEntity<CProductType> {

public static final String DEFAULT_COLOR = "#6B8E23"; // X11 OliveDrab - product types (darker)
public static final String DEFAULT_ICON = "vaadin:package";
public static final String ENTITY_TITLE_PLURAL = "Product Types";
public static final String ENTITY_TITLE_SINGULAR = "Product Type";
public static final String VIEW_NAME = "Product Type Management";

public CProductType() {
super();
}

public CProductType(final String name, final CCompany company) {
super(CProductType.class, name, company);
}
}
