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
	@Column (name = "selected_fields", nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Selected Fields", required = false, readOnly = false,
			description = "Comma-separated list of selected fields with order (e.g., fieldName:1,otherField:2)", hidden = false, order = 3,
			maxLength = 1000
	)
	private String selectedFields;

	public CGridEntity(final String name, final CProject project) {
		super(CGridEntity.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return "GridEntity: " + getName();
	}

	public String getDataServiceBeanName() { return dataServiceBeanName; }

	public void setDataServiceBeanName(final String dataServiceBeanName) { this.dataServiceBeanName = dataServiceBeanName; }

	public String getSelectedFields() { return selectedFields; }

	public void setSelectedFields(final String selectedFields) { this.selectedFields = selectedFields; }
}
