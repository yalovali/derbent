package tech.derbent.screens.domain;

import java.lang.reflect.Field;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

@Entity
@Table (name = "cgridentity")
@AttributeOverride (name = "id", column = @Column (name = "grid_entity_id"))
public class CGridEntity extends CEntityOfProject<CGridEntity> {

	/** Extended field selection that includes the actual Java Field for reflection-based operations. */
	public static class FieldConfig extends FieldSelection {

		private final Field field;

		public FieldConfig(EntityFieldInfo fieldInfo, int order, Field field) {
			super(fieldInfo, order);
			this.field = field;
		}

		public Field getField() { return field; }
	}

	/** Common data class for field selections with order, used across the project for grid field configuration. */
	public static class FieldSelection {

		private final EntityFieldInfo fieldInfo;
		private int order;

		public FieldSelection(EntityFieldInfo fieldInfo, int order) {
			this.fieldInfo = fieldInfo;
			this.order = order;
		}

		public EntityFieldInfo getFieldInfo() { return fieldInfo; }

		public int getOrder() { return order; }

		public void setOrder(int order) { this.order = order; }

		@Override
		public String toString() {
			return fieldInfo.getDisplayName() + " (" + fieldInfo.getFieldName() + ")";
		}
	}

	public static final String DEFAULT_COLOR = "#d57d00";
	public static final String DEFAULT_ICON = "vaadin:code";
	public static final String VIEW_NAME = "Grid View";
	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "false",
			description = "Whether this grid entity cannot be deleted by users", hidden = false, order = 4
	)
	private boolean attributeNonDeletable = false;
	@Column (nullable = false)
	@AMetaData (
			displayName = "None Grid", required = false, readOnly = false, defaultValue = "false",
			description = "This grid is not displayed, use for details only one pagers", hidden = false, order = 4
	)
	private boolean attributeNone = false;

	public boolean getAttributeNone() { return attributeNone; }

	public void setAttributeNone(boolean attributeNone) { this.attributeNone = attributeNone; }

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

	public CGridEntity() {
		super();
	}

	public CGridEntity(final String name, final CProject project) {
		super(CGridEntity.class, name, project);
		attributeNonDeletable = false;
	}

	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	public String getDataServiceBeanName() { return dataServiceBeanName; }

	public String getSelectedFields() { return selectedFields; }

	public void setAttributeNonDeletable(boolean attributeNonDeletable) { this.attributeNonDeletable = attributeNonDeletable; }

	public void setDataServiceBeanName(final String dataServiceBeanName) { this.dataServiceBeanName = dataServiceBeanName; }

	public void setSelectedFields(final String selectedFields) { this.selectedFields = selectedFields; }

	@Override
	public void initializeAllFields() {
		// TODO Auto-generated method stub
		
	}
}
