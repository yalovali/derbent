package tech.derbent.screens.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.screens.domain.CScreenLines;

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

		private String fieldName = "fieldName";

		private String displayName = "displayName";

		private String description = "description";

		private String fieldType = "fieldType";

		private String javaType;

		private Class<?> fieldTypeClass;

		private boolean required = false;

		private boolean readOnly = false;

		private boolean hidden = false;

		private int order = 999;

		private int maxLength = 255;

		private String defaultValue = "";

		private String dataProviderBean = "";

		// Additional MetaData properties
		private boolean autoSelectFirst = false;

		private String placeholder = "";

		private boolean allowCustomValue = false;

		private boolean useRadioButtons = false;

		private boolean comboboxReadOnly = false;

		private boolean clearOnEmptyData = false;

		private String width = "";

		private String dataProviderMethod = "";

		private String dataProviderParamMethod = "";

		public String getDataProviderBean() { return dataProviderBean; }

		public String getDataProviderMethod() { return dataProviderMethod; }

		public String getDataProviderParamMethod() { return dataProviderParamMethod; }

		public String getDefaultValue() { return defaultValue; }

		public String getDescription() { return description; }

		public String getDisplayName() { return displayName; }

		// Getters and setters
		public String getFieldName() { return fieldName; }

		public String getFieldType() { return fieldType; }

		public Class<?> getFieldTypeClass() { return fieldTypeClass; }

		public String getJavaType() { return javaType; }

		public int getMaxLength() { return maxLength; }

		public int getOrder() { return order; }

		public String getPlaceholder() { return placeholder; }

		public String getWidth() { return width; }

		public boolean isAllowCustomValue() { return allowCustomValue; }

		public boolean isAutoSelectFirst() { return autoSelectFirst; }

		public boolean isClearOnEmptyData() { return clearOnEmptyData; }

		public boolean isComboboxReadOnly() { return comboboxReadOnly; }

		public boolean isHidden() { return hidden; }

		public boolean isReadOnly() { return readOnly; }

		public boolean isRequired() { return required; }

		public boolean isUseRadioButtons() { return useRadioButtons; }

		public void setAllowCustomValue(final boolean allowCustomValue) {
			this.allowCustomValue = allowCustomValue;
		}

		public void setAutoSelectFirst(final boolean autoSelectFirst) {
			this.autoSelectFirst = autoSelectFirst;
		}

		public void setClearOnEmptyData(final boolean clearOnEmptyData) {
			this.clearOnEmptyData = clearOnEmptyData;
		}

		public void setComboboxReadOnly(final boolean comboboxReadOnly) {
			this.comboboxReadOnly = comboboxReadOnly;
		}

		public void setDataProviderBean(final String dataProviderBean) {
			this.dataProviderBean = dataProviderBean;
		}

		public void setDataProviderMethod(final String dataProviderMethod) {
			this.dataProviderMethod = dataProviderMethod;
		}

		public void setDataProviderParamMethod(final String dataProviderParamMethod) {
			this.dataProviderParamMethod = dataProviderParamMethod;
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

		public void setFieldTypeClass(final Class<?> fieldTypeClass) {
			this.fieldTypeClass = fieldTypeClass;
		}

		public void setHidden(final boolean hidden) { this.hidden = hidden; }

		public void setJavaType(final String javaType) { this.javaType = javaType; }

		public void setMaxLength(final int maxLength) { this.maxLength = maxLength; }

		public void setOrder(final int order) { this.order = order; }

		public void setPlaceholder(final String placeholder) {
			this.placeholder = placeholder;
		}

		public void setReadOnly(final boolean readOnly) { this.readOnly = readOnly; }

		public void setRequired(final boolean required) { this.required = required; }

		public void setUseRadioButtons(final boolean useRadioButtons) {
			this.useRadioButtons = useRadioButtons;
		}

		public void setWidth(final String width) { this.width = width; }

		@Override
		public String toString() {
			return String.format("%s (%s)", displayName, fieldName);
		}
	}

	public static final String THIS_CLASS = "This Class";

	public static final String SECTION = "Section";

	public static EntityFieldInfo createFieldInfo(final Field field) {

		try {
			final EntityFieldInfo info =
				createFieldInfo(field.getAnnotation(MetaData.class));
			info.setFieldName(field.getName());
			info.setFieldType(getSimpleTypeName(field.getType())); // String
			info.setJavaType(field.getType().getSimpleName());
			info.setFieldTypeClass(field.getType()); // Class<?> ata
			return info;
		} catch (final Exception e) {
			return null;
		}
	}

	public static EntityFieldInfo createFieldInfo(final MetaData metaData) {

		try {
			final EntityFieldInfo info = new EntityFieldInfo();
			info.setFieldName("");
			info.setFieldType("");
			info.setJavaType("");
			info.setDisplayName(metaData.displayName());
			info.setDescription(metaData.description());
			info.setRequired(metaData.required());
			info.setReadOnly(metaData.readOnly());
			info.setHidden(metaData.hidden());
			info.setOrder(metaData.order());
			info.setMaxLength(metaData.maxLength());
			info.setDefaultValue(metaData.defaultValue());
			info.setDataProviderBean(metaData.dataProviderBean());
			info.setAutoSelectFirst(metaData.autoSelectFirst());
			info.setPlaceholder(metaData.placeholder());
			info.setAllowCustomValue(metaData.allowCustomValue());
			info.setUseRadioButtons(metaData.useRadioButtons());
			info.setComboboxReadOnly(metaData.comboboxReadOnly());
			info.setClearOnEmptyData(metaData.clearOnEmptyData());
			info.setWidth(metaData.width());
			info.setDataProviderMethod(metaData.dataProviderMethod());
			info.setDataProviderParamMethod(metaData.dataProviderParamMethod());
			return info;
		} catch (final Exception e) {
			return null;
		}
	}

	public static EntityFieldInfo createFieldInfo(final String screenClassName,
		final CScreenLines line) throws Exception {

		try {
			Check.notNull(line, "Line cannot be null");
			String relationFieldName = line.getRelationFieldName();
			Field field = null;

			if (relationFieldName.equals(CEntityFieldService.THIS_CLASS)) {
				relationFieldName = screenClassName;
				field = getEntityField(relationFieldName, line.getEntityProperty());
			}
			else if (relationFieldName == CEntityFieldService.SECTION) {
				final EntityFieldInfo sectionInfo = new EntityFieldInfo();
				sectionInfo.setFieldName(CEntityFieldService.SECTION);
				sectionInfo.setFieldType(CEntityFieldService.SECTION);
				sectionInfo.setJavaType(CEntityFieldService.SECTION);
				sectionInfo.setDisplayName(line.getSectionName());
				sectionInfo.setDescription(line.getSectionName());
				sectionInfo.setFieldTypeClass(CEntityFieldService.class);
				return sectionInfo;
			}
			else {
				field = getEntityField(screenClassName, relationFieldName);
				field =
					getEntityField(field.getClass().getName(), line.getEntityProperty());
			}
			// get field of class
			final EntityFieldInfo info = createFieldInfo(field);
			return info;
		} catch (final Exception e) {
			line.printLine();
			throw e;
		}
	}

	private static List<Field> getAllFields(final Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		// Get fields from current class and all superclasses
		Class<?> currentClass = clazz;

		while ((currentClass != null) && (currentClass != Object.class)) {
			fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
			currentClass = currentClass.getSuperclass();
		}
		return fields;
	}

	/**
	 * Get data provider beans for reference fields.
	 * @return list of available data provider bean names
	 */
	public static List<String> getDataProviderBeans() {
		return List.of("CActivityService", "CActivityTypeService",
			"CActivityStatusService", "CActivityPriorityService", "CMeetingService",
			"CMeetingTypeService", "CMeetingStatusService", "CRiskService",
			"CRiskTypeService", "CRiskStatusService", "CRiskPriorityService",
			"CProjectService", "CUserService", "CUserTypeService", "CCompanyService",
			"CScreenService", "CScreenLinesService");
	}

	public static Field getEntityField(final String entityType, final String fieldName)
		throws NoSuchFieldException {
		Class<?> currentClass = getEntityClass(entityType);
		Check.notNull(currentClass,
			"Entity class must not be null for type: " + entityType);

		while ((currentClass != null) && (currentClass != Object.class)) {

			try {
				final Field field = currentClass.getDeclaredField(fieldName);
				return field;
			} catch (final NoSuchFieldException e) {
				// Field not found in this class, continue to superclass
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchFieldException(
			"Field '" + fieldName + "' not found in entity type: " + entityType);
	}

	public static EntityFieldInfo getEntityFieldInfo(final String entityType,
		final String fieldName) {
		final List<EntityFieldInfo> fields = getEntityFields(entityType);
		return fields.stream().filter(field -> field.getFieldName().equals(fieldName))
			.findFirst().orElse(null);
	}

	/**
	 * Get field information for a specific entity type.
	 * @param entityType the entity type name
	 * @return list of field information
	 */
	public static List<EntityFieldInfo> getEntityFields(final String entityType) {
		final Class<?> entityClass = getEntityClass(entityType);
		Check.notNull(entityClass,
			"Entity class must not be null for type: " + entityType);
		final List<EntityFieldInfo> fields = new ArrayList<>();
		final List<Field> allFields = getAllFields(entityClass);

		for (final Field field : allFields) {

			if (Modifier.isStatic(field.getModifiers())
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

	public static List<EntityFieldInfo> getEntityRelationFields(final String entityType,
		final List<EntityFieldInfo> listOfAdditionalFields) {
		final Class<?> entityClass = getEntityClass(entityType);
		Check.notNull(entityClass,
			"Entity class must not be null for type: " + entityType);
		final List<EntityFieldInfo> fields = new ArrayList<>();

		if (listOfAdditionalFields != null) {
			fields.addAll(listOfAdditionalFields);
		}
		final List<Field> allFields = getAllFields(entityClass);

		for (final Field field : allFields) {

			if (Modifier.isStatic(field.getModifiers())
				|| field.getName().equals("serialVersionUID")
				|| field.getName().equals("LOGGER")
				|| !isFieldComplexType(field.getType())) {
				continue;
			}
			final EntityFieldInfo fieldInfo = createFieldInfo(field);

			if (fieldInfo != null) {
				fields.add(fieldInfo);
			}
		}
		return fields;
	}

	public static List<EntityFieldInfo> getEntitySimpleFields(final String entityType,
		final List<EntityFieldInfo> listOfAdditionalFields) {
		final Class<?> entityClass = getEntityClass(entityType);
		Check.notNull(entityClass,
			"Entity class must not be null for type: " + entityType);
		final List<EntityFieldInfo> fields = new ArrayList<>();

		if (listOfAdditionalFields != null) {
			fields.addAll(listOfAdditionalFields);
		}
		final List<Field> allFields = getAllFields(entityClass);

		for (final Field field : allFields) {

			if (Modifier.isStatic(field.getModifiers())
				|| field.getName().equals("serialVersionUID")
				|| field.getName().equals("LOGGER")
				|| isFieldComplexType(field.getType())) {
				continue;
			}
			final EntityFieldInfo fieldInfo = createFieldInfo(field);

			if (fieldInfo != null) {
				fields.add(fieldInfo);
			}
		}
		return fields;
	}

	private static String getSimpleTypeName(final Class<?> type) {

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

	private static boolean isFieldComplexType(final Class<?> type) {

		// Check if the field type is a complex type (not primitive or standard types)
		if (type.isPrimitive() || type.isEnum() || type == String.class
			|| Number.class.isAssignableFrom(type) || type == Boolean.class
			|| type == Date.class || type == LocalDate.class
			|| type == LocalDateTime.class) {
			return false;
		}
		return true;
		// Check if it's a domain entity (likely a reference) return
		// type.getSimpleName().startsWith("C")&&
		// Character.isUpperCase(type.getSimpleName().charAt(1));
	}

	public static void printFieldInfo(final EntityFieldInfo fieldInfo) {

		if (fieldInfo == null) {
			System.out.println("Field info is null");
			return;
		}
		System.out.println("Field Name: " + fieldInfo.getFieldName());
		System.out.println("Display Name: " + fieldInfo.getDisplayName());
		System.out.println("Description: " + fieldInfo.getDescription());
		System.out.println("Field Type: " + fieldInfo.getFieldType());
		System.out.println("Java Type: " + fieldInfo.getJavaType());
		System.out.println("Required: " + fieldInfo.isRequired());
		System.out.println("Read Only: " + fieldInfo.isReadOnly());
		System.out.println("Hidden: " + fieldInfo.isHidden());
		System.out.println("Order: " + fieldInfo.getOrder());
		System.out.println("Max Length: " + fieldInfo.getMaxLength());
		System.out.println("Default Value: " + fieldInfo.getDefaultValue());
		System.out.println("Data Provider Bean: " + fieldInfo.getDataProviderBean());
		System.out.println("Auto Select First: " + fieldInfo.isAutoSelectFirst());
		System.out.println("Placeholder: " + fieldInfo.getPlaceholder());
		System.out.println("Allow Custom Value: " + fieldInfo.isAllowCustomValue());
		System.out.println("Use Radio Buttons: " + fieldInfo.isUseRadioButtons());
		System.out.println("Combobox Read Only: " + fieldInfo.isComboboxReadOnly());
		System.out.println("Clear On Empty Data: " + fieldInfo.isClearOnEmptyData());
		System.out.println("Width: " + fieldInfo.getWidth());
	}
}