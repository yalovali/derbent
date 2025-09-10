package tech.derbent.screens.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.view.CGridEntityView;

@Entity
@Table (name = "cgridentity")
@AttributeOverride (name = "id", column = @Column (name = "grid_entity_id"))
public class CGridEntity extends CEntityOfProject<CGridEntity> {

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() { return "#6f42c1"; }

	public static String getIconFilename() { return "vaadin:viewport"; }

	public static Class<?> getViewClassStatic() { return CGridEntityView.class; }

	public CGridEntity() {
		super();
	}

	@Column (name = "data_service_bean_name", nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Data Service Bean", required = true, readOnly = false, description = "Data Service Bean", hidden = false, order = 2,
			maxLength = 100, dataProviderBean = "CViewsService", dataProviderMethod = "getAvailableBeans"
	)
	private String dataServiceBeanName;

	public CGridEntity(final String name, final CProject project) {
		super(CGridEntity.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return "GridEntity: " + getName();
	}
}
