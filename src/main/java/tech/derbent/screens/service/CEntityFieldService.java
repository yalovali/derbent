package tech.derbent.screens.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import tech.derbent.abstracts.annotations.MetaData;

/**
 * Service to provide entity field information for screen line configuration. This service
 * uses reflection to extract field information from domain entities.
 */
@Service
public class CEntityFieldService extends CFieldServiceBase {

	/**
	 * Data class to hold entity field information.
	 */
	public static class EntityFieldInfo {

		private String fieldName;

		private String displayName;

		private String description;

		private String fieldType;

		private String javaType;

		private boolean required;

		private boolean readOnly;

		private boolean hidden;

		private int order;

		private int maxLength;

		private String defaultValue;

		private String dataProviderBean;

		public String getDataProviderBean() { return dataProviderBean; }

		public String getDefaultValue() { return defaultValue; }

		public String getDescription() { return description; }

		public String getDisplayName() { return displayName; }

		// Getters and setters
		public String getFieldName() { return fieldName; }

		public String getFieldType() { return fieldType; }

		public String getJavaType() { return javaType; }

		public int getMaxLength() { return maxLength; }

		public int getOrder() { return order; }

		public boolean isHidden() { return hidden; }

		public boolean isReadOnly() { return readOnly; }

		public boolean isRequired() { return required; }

		public void setDataProviderBean(final String dataProviderBean) {
			this.dataProviderBean = dataProviderBean;
		}

		public void setDefaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		public void setDisplayName(final String displayName) {
			this.displayName = displayName;
		}

		public void setFieldName(final String fieldName) { this.fieldName = fieldName; }

		public void setFieldType(final String fieldType) { this.fieldType = fieldType; }

		public void setHidden(final boolean hidden) { this.hidden = hidden; }

		public void setJavaType(final String javaType) { this.javaType = javaType; }

		public void setMaxLength(final int maxLength) { this.maxLength = maxLength; }

		public void setOrder(final int order) { this.order = order; }

		public void setReadOnly(final boolean readOnly) { this.readOnly = readOnly; }

		public void setRequired(final boolean required) { this.required = required; }

		@Override
		public String toString() {
			return String.format("%s (%s)", displayName, fieldName);
		}
	}

	private EntityFieldInfo createFieldInfo(final Field field) {

		try {
			final EntityFieldInfo info = new EntityFieldInfo();
			info.setFieldName(field.getName());
			info.setFieldType(getSimpleTypeName(field.getType()));
			info.setJavaType(field.getType().getSimpleName());
			// Check for MetaData annotation
			final MetaData metaData = field.getAnnotation(MetaData.class);

			if (metaData != null) {
				info.setDisplayName(metaData.displayName());
				info.setDescription(metaData.description());
				info.setRequired(metaData.required());
				info.setReadOnly(metaData.readOnly());
				info.setHidden(metaData.hidden());
				info.setOrder(metaData.order());
				info.setMaxLength(metaData.maxLength());
				info.setDefaultValue(metaData.defaultValue());
				info.setDataProviderBean(metaData.dataProviderBean());
			}
			else {
				// Set defaults if no MetaData annotation
				info.setDisplayName(formatFieldName(field.getName()));
				info.setDescription("");
				info.setRequired(false);
				info.setReadOnly(false);
				info.setHidden(false);
				info.setOrder(999);
				info.setMaxLength(255);
				info.setDefaultValue("");
				info.setDataProviderBean("");
			}
			return info;
		} catch (final Exception e) {
			return null;
		}
	}

	private String formatFieldName(final String fieldName) {
		// Convert camelCase to Title Case
		return fieldName.replaceAll("([A-Z])", " $1")
			.replaceAll("^.", String.valueOf(Character.toUpperCase(fieldName.charAt(0))))
			.trim();
	}

	private List<Field> getAllFields(final Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		// Get fields from current class and all superclasses
		Class<?> currentClass = clazz;

		while ((currentClass != null) && (currentClass != Object.class)) {
			fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
			currentClass = currentClass.getSuperclass();
		}
		return fields;
	}

	// return available field types for a given entity type
	public List<Field> getAvailableFieldClassesOfType(final Class<?> entityType) {
		final List<Field> fieldTypes = new ArrayList<Field>();
		final List<Field> fields = getAllFields(entityType);

		for (final Field field : fields) {

			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
				|| field.getName().equals("serialVersionUID")
				|| field.getName().equals("LOGGER")) {
				continue;
			}
			fieldTypes.add(field);
		}
		return fieldTypes;
	}

	/**
	 * Get data provider beans for reference fields.
	 * @return list of available data provider bean names
	 */
	public List<String> getDataProviderBeans() {
		return List.of("CActivityService", "CActivityTypeService",
			"CActivityStatusService", "CActivityPriorityService", "CMeetingService",
			"CMeetingTypeService", "CMeetingStatusService", "CRiskService",
			"CRiskTypeService", "CRiskStatusService", "CRiskPriorityService",
			"CProjectService", "CUserService", "CUserTypeService", "CCompanyService",
			"CScreenService", "CScreenLinesService");
	}

	/**
	 * Get field information for a specific entity type.
	 * @param entityType the entity type name
	 * @return list of field information
	 */
	public List<EntityFieldInfo> getEntityFields(final String entityType) {
		final Class<?> entityClass = getEntityClass(entityType);

		if (entityClass == null) {
			return List.of();
		}
		final List<EntityFieldInfo> fields = new ArrayList<>();
		// Get all fields including inherited ones
		final List<Field> allFields = getAllFields(entityClass);

		for (final Field field : allFields) {

			// Skip static fields and certain system fields
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
				|| field.getName().equals("serialVersionUID")
				|| field.getName().equals("LOGGER")) {
				continue;
			}
			final EntityFieldInfo fieldInfo = createFieldInfo(field);

			if (fieldInfo != null) {
				fields.add(fieldInfo);
			}
		}
		return fields;
	}

	public List<Field> getFields(final String entityType) {
		final Class<?> entityClass = getEntityClass(entityType);

		if (entityClass == null) {
			return List.of();
		}
		// Get all fields including inherited ones
		return getAllFields(entityClass);
	}

	private String getSimpleTypeName(final Class<?> type) {

		if (type.isPrimitive()) {
			return type.getSimpleName().toUpperCase();
		}
		final String typeName = type.getSimpleName();

		switch (typeName) {
		case "String":
			return "TEXT";
		case "Integer":
		case "Long":
		case "BigDecimal":
		case "Double":
		case "Float":
			return "NUMBER";
		case "LocalDate":
		case "LocalDateTime":
		case "Date":
			return "DATE";
		case "Boolean":
			return "BOOLEAN";
		default:
			// Check if it's a domain entity (likely a reference)
			if (typeName.startsWith("C") && Character.isUpperCase(typeName.charAt(1))) {
				return "REFERENCE";
			}
			return "TEXT";
		}
	}
}