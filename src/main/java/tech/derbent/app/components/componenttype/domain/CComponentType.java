package tech.derbent.app.components.componenttype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "ccomponenttype", uniqueConstraints = @UniqueConstraint (columnNames = {
"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "ccomponenttype_id"))
public class CComponentType extends CTypeEntity<CComponentType> {

public static final String DEFAULT_COLOR = "#F0E5C0"; // OpenWindows Menu Background - component types
public static final String DEFAULT_ICON = "vaadin:cogs";
public static final String VIEW_NAME = "Component Type Management";

public CComponentType() {
super();
}

public CComponentType(final String name, final CProject project) {
super(CComponentType.class, name, project);
}

@Override
public void initializeAllFields() {}
}
