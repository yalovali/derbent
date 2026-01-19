package tech.derbent.api.screens.domain;

import java.lang.reflect.Field;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.projects.domain.CProject;

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
	public static final String ENTITY_TITLE_PLURAL = "Grid Entities";
	public static final String ENTITY_TITLE_SINGULAR = "Grid Entity";
	public static final String VIEW_NAME = "Grid View";
	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "false",
			description = "Whether this grid entity cannot be deleted by users", hidden = false
	)
	private boolean attributeNonDeletable = false;
	@Column (nullable = false)
	@AMetaData (
			displayName = "None Grid", required = false, readOnly = false, defaultValue = "false",
			description = "This grid is not displayed, use for details only one pagers", hidden = false
	)
	private boolean attributeNone = false;
	@Column (name = "column_fields", nullable = true, length = 1000)
	@Size (max = 100)
	@AMetaData (
			displayName = "Column Fields", required = false, readOnly = false, description = "List of fields with order", hidden = false,
			maxLength = 100, useDualListSelector = true, dataProviderBean = "CGridEntityService", dataProviderMethod = "getFieldNames",
			dataProviderParamBean = "context", dataProviderParamMethod = "getValue"
	)
	private List<String> columnFields;
	@Column (name = "data_service_bean_name", nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Data Service Bean", required = true, readOnly = false, description = "Data Service Bean", hidden = false, maxLength = 100,
			dataProviderBean = "CViewsService", dataProviderMethod = "getAvailableBeans"
	)
	private String dataServiceBeanName;

	public CGridEntity() {
		super();
	}

	public CGridEntity(final String name, final CProject<?> project) {
		super(CGridEntity.class, name, project);
		attributeNonDeletable = false;
	}

	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	public boolean getAttributeNone() { return attributeNone; }

	public List<String> getColumnFields() { return columnFields; }

	public String getDataServiceBeanName() { return dataServiceBeanName; }

	public void setAttributeNonDeletable(boolean attributeNonDeletable) { this.attributeNonDeletable = attributeNonDeletable; }

	public void setAttributeNone(boolean attributeNone) { this.attributeNone = attributeNone; }

	public void setColumnFields(List<String> columnFields) {
		// make sure the string are unique and filter out empty strings
		this.columnFields = columnFields.stream().distinct().filter(s -> s != null && !s.trim().isEmpty()).toList();
	}

	public void setDataServiceBeanName(final String dataServiceBeanName) { this.dataServiceBeanName = dataServiceBeanName; }
}
