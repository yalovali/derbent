package tech.derbent.app.products.productversiontype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cproductversiontype", uniqueConstraints = @UniqueConstraint (columnNames = {
"name", "project_id"
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
}

public CProductVersionType(final String name, final CProject project) {
super(CProductVersionType.class, name, project);
}

@Override
public void initializeAllFields() { /*****/ }
}
