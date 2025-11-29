package tech.derbent.app.components.componentversiontype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "ccomponentversiontype", uniqueConstraints = @UniqueConstraint (columnNames = {
"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "ccomponentversiontype_id"))
public class CComponentVersionType extends CTypeEntity<CComponentVersionType> {

public static final String DEFAULT_COLOR = "#F0E5C0"; // OpenWindows Menu Background - version types
public static final String DEFAULT_ICON = "vaadin:tag";
public static final String VIEW_NAME = "Component Version Type Management";

public CComponentVersionType() {
super();
}

public CComponentVersionType(final String name, final CProject project) {
super(CComponentVersionType.class, name, project);
}

@Override
public void initializeAllFields() {}
}
