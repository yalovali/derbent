package tech.derbent.api.screens.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.api.screens.domain.CDetailLines;

/** Service to provide entity field information for screen line configuration. This service uses reflection to extract field information from domain
 * entities. */
@Service
public class CEntityFieldService {

	/** Data class to hold entity field information. */
	public static class EntityFieldInfo {

		private boolean allowCustomValue = false;
		// Additional AMetaData properties
		private boolean autoSelectFirst = false;
		private boolean clearOnEmptyData = false;
		private boolean colorField = false;
		private boolean comboboxReadOnly = false;
		private String createComponentMethod = "";
		private String dataProviderBean = "";
		private String dataProviderMethod = "";
		private String dataProviderParamBean = "";
		private String dataProviderParamMethod = "";
		private String dataUpdateMethod = "";
		private String defaultValue = "";
		private String description = "description";
		private String displayName = "displayName";
		private String fieldName = "fieldName";
		private String fieldType = "fieldType";
		private Class<?> fieldTypeClass;
		private boolean hidden = false;
		private boolean imageData = false;
		private Boolean isCaptionVisible = true;
		private String javaType;
		private int maxLength = 255;
		private int order = 999;
		private boolean passwordField;
		private boolean passwordRevealButton;
		private String placeholder = "";
		private boolean readOnly = false;
		private boolean required = false;
		private boolean setBackgroundFromColor = false;
		private boolean useDualListSelector = false;
		private boolean useGridSelection = false;
		private boolean useIcon = false;
		private boolean useRadioButtons = false;
		private String width = "";

		public String getCreateComponentMethod() { return createComponentMethod; }

		public String getDataProviderBean() { return dataProviderBean; }

		public String getDataProviderMethod() { return dataProviderMethod; }

		public String getDataProviderParamBean() { return dataProviderParamBean; }

		public String getDataProviderParamMethod() { return dataProviderParamMethod; }

		public String getDataUpdateMethod() { return dataUpdateMethod; }

		public String getDefaultValue() { return defaultValue; }

		public String getDescription() { return description; }

		public String getDisplayName() { return displayName; }

		// Getters and setters
		public String getFieldName() { return fieldName; }

		public String getFieldType() { return fieldType; }

		public Class<?> getFieldTypeClass() { return fieldTypeClass; }

		public Boolean getIsCaptionVisible() { return isCaptionVisible; }

		public String getJavaType() { return javaType; }

		public int getMaxLength() { return maxLength; }

		public int getOrder() { return order; }

		public String getPlaceholder() { return placeholder; }

		public String getWidth() { return width; }

		public boolean isAllowCustomValue() { return allowCustomValue; }

		public boolean isAutoSelectFirst() { return autoSelectFirst; }

		public boolean isClearOnEmptyData() { return clearOnEmptyData; }

		public boolean isColorField() { return colorField; }

		public boolean isComboboxReadOnly() { return comboboxReadOnly; }

		public boolean isHidden() { return hidden; }

		public boolean isImageData() { return imageData; }

		public boolean isPasswordField() { return passwordField; }

		public boolean isPasswordRevealButton() { return passwordRevealButton; }

		public boolean isReadOnly() { return readOnly; }

		public boolean isRequired() { return required; }

		public boolean isSetBackgroundFromColor() { return setBackgroundFromColor; }

		public boolean isUseDualListSelector() { return useDualListSelector; }

		public boolean isUseGridSelection() { return useGridSelection; }

		public boolean isUseIcon() { return useIcon; }

		public boolean isUseRadioButtons() { return useRadioButtons; }

		public void setAllowCustomValue(final boolean allowCustomValue) { this.allowCustomValue = allowCustomValue; }

		public void setAutoSelectFirst(final boolean autoSelectFirst) { this.autoSelectFirst = autoSelectFirst; }

		public void setClearOnEmptyData(final boolean clearOnEmptyData) { this.clearOnEmptyData = clearOnEmptyData; }

		public void setColorField(final boolean colorField) { this.colorField = colorField; }

		public void setComboboxReadOnly(final boolean comboboxReadOnly) { this.comboboxReadOnly = comboboxReadOnly; }

		public void setCreateComponentMethod(final String createComponentMethod) { this.createComponentMethod = createComponentMethod; }

		public void setDataProviderBean(final String dataProviderBean) { this.dataProviderBean = dataProviderBean; }

		public void setDataProviderMethod(final String dataProviderMethod) { this.dataProviderMethod = dataProviderMethod; }

		public void setDataProviderParamBean(final String dataProviderParamBean) { this.dataProviderParamBean = dataProviderParamBean; }

		public void setDataProviderParamMethod(final String dataProviderParamMethod) { this.dataProviderParamMethod = dataProviderParamMethod; }

		public void setDataUpdateMethod(final String dataUpdateMethod) { this.dataUpdateMethod = dataUpdateMethod; }

		public void setDefaultValue(final String defaultValue) { this.defaultValue = defaultValue; }

		public void setDescription(final String description) { this.description = description; }

		public void setDisplayName(final String displayName) { this.displayName = displayName; }

		public void setFieldName(final String fieldName) { this.fieldName = fieldName; }

		public void setFieldType(final String fieldType) { this.fieldType = fieldType; }

		public void setFieldTypeClass(final Class<?> fieldTypeClass) { this.fieldTypeClass = fieldTypeClass; }

		public void setHidden(final boolean hidden) { this.hidden = hidden; }

		public void setImageData(final boolean imageData) { this.imageData = imageData; }

		public void setIsCaptionVisible(Boolean isCaptionVisible) { this.isCaptionVisible = isCaptionVisible; }

		public void setJavaType(final String javaType) { this.javaType = javaType; }

		public void setMaxLength(final int maxLength) { this.maxLength = maxLength; }

		public void setOrder(final int order) { this.order = order; }

		public void setPasswordField(final boolean passwordField) { this.passwordField = passwordField; }

		public void setPasswordRevealButton(final boolean passwordRevealButton) { this.passwordRevealButton = passwordRevealButton; }

		public void setPlaceholder(final String placeholder) { this.placeholder = placeholder; }

		public void setReadOnly(final boolean readOnly) { this.readOnly = readOnly; }

		public void setRequired(final boolean required) { this.required = required; }

		public void setSetBackgroundFromColor(final boolean setBackgroundFromColor) { this.setBackgroundFromColor = setBackgroundFromColor; }

		public void setUseDualListSelector(final boolean useDualListSelector) { this.useDualListSelector = useDualListSelector; }

		public void setUseGridSelection(final boolean useGridSelection) { this.useGridSelection = useGridSelection; }

		public void setUseIcon(final boolean useIcon) { this.useIcon = useIcon; }

		public void setUseRadioButtons(final boolean useRadioButtons) { this.useRadioButtons = useRadioButtons; }

		public void setWidth(final String width) { this.width = width; }

		// toString method for easy debugging
		@Override
		public String toString() {
			return String.format("%s (%s)", displayName, fieldName);
		}
	}

	public static final String COMPONENT = "Component";
	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityFieldService.class);
	public static final String SECTION = "Section";
	public static final String THIS_CLASS = "This Class";

	public static EntityFieldInfo createFieldInfo(final AMetaData metaData) {
		try {
			Check.notNull(metaData, "AMetaData annotation cannot be null");
			final EntityFieldInfo info = new EntityFieldInfo();
			info.setFieldName("");
			info.setFieldType("");
			info.setJavaType("");
			info.setDisplayName(metaData.displayName());
			info.setDescription(metaData.description());
			info.setRequired(metaData.required());
			info.setReadOnly(metaData.readOnly());
			info.setHidden(metaData.hidden());
			info.setImageData(metaData.imageData());
			info.setOrder(metaData.order());
			info.setMaxLength(metaData.maxLength());
			info.setDefaultValue(metaData.defaultValue());
			info.setDataProviderBean(metaData.dataProviderBean());
			info.setDataProviderMethod(metaData.dataProviderMethod());
			info.setDataProviderParamMethod(metaData.dataProviderParamMethod());
			info.setDataProviderParamBean(metaData.dataProviderParamBean());
			info.setAutoSelectFirst(metaData.autoSelectFirst());
			info.setPlaceholder(metaData.placeholder());
			info.setAllowCustomValue(metaData.allowCustomValue());
			info.setUseRadioButtons(metaData.useRadioButtons());
			info.setComboboxReadOnly(metaData.comboboxReadOnly());
			info.setClearOnEmptyData(metaData.clearOnEmptyData());
			info.setColorField(metaData.colorField());
			info.setCreateComponentMethod(metaData.createComponentMethod());
			info.setSetBackgroundFromColor(metaData.setBackgroundFromColor());
			info.setWidth(metaData.width());
			info.setPasswordField(metaData.passwordField());
			info.setDataUpdateMethod(metaData.dataUpdateMethod());
			info.setPasswordRevealButton(metaData.passwordRevealButton());
			info.setUseIcon(metaData.useIcon());
			info.setUseDualListSelector(metaData.useDualListSelector());
			info.setUseGridSelection(metaData.useGridSelection());
			return info;
		} catch (final Exception e) {
			throw e;
		}
	}

	public static EntityFieldInfo createFieldInfo(final Field field) {
		try {
			final EntityFieldInfo info = createFieldInfo(field.getAnnotation(AMetaData.class));
			info.setFieldName(field.getName());
			info.setFieldType(getSimpleTypeName(field.getType())); // String
			info.setJavaType(field.getType().getSimpleName());
			info.setFieldTypeClass(field.getType()); // Class<?> ata
			return info;
		} catch (final Exception e) {
			throw e;
		}
	}

	public static EntityFieldInfo createFieldInfo(final String screenClassName, final CDetailLines line) throws Exception {
		try {
			Check.notNull(line, "Line cannot be null");
			String relationFieldName = line.getRelationFieldName();
			Field field = null;
			if (relationFieldName.equals(CEntityFieldService.THIS_CLASS)) {
				relationFieldName = screenClassName;
				field = getEntityField(relationFieldName, line.getEntityProperty());
			} else if (relationFieldName == CEntityFieldService.SECTION) {
				final EntityFieldInfo sectionInfo = new EntityFieldInfo();
				sectionInfo.setFieldName(CEntityFieldService.SECTION);
				sectionInfo.setFieldType(CEntityFieldService.SECTION);
				sectionInfo.setJavaType(CEntityFieldService.SECTION);
				sectionInfo.setDisplayName(line.getSectionName());
				sectionInfo.setDescription(line.getSectionName());
				sectionInfo.setFieldTypeClass(CEntityFieldService.class);
				sectionInfo.setIsCaptionVisible(line.getIsCaptionVisible());
				return sectionInfo;
			} else if (line.getEntityProperty().startsWith(CEntityFieldService.COMPONENT + ":")) {
				final EntityFieldInfo sectionInfo = new EntityFieldInfo();
				sectionInfo.setDataProviderBean(line.getDataProviderBean());
				sectionInfo.setCreateComponentMethod(line.getEntityProperty().split(CEntityFieldService.COMPONENT + ":")[1]);
				sectionInfo.setFieldName(line.getRelationFieldName());
				sectionInfo.setFieldType(CEntityFieldService.COMPONENT);
				sectionInfo.setJavaType(CEntityFieldService.COMPONENT);
				sectionInfo.setDisplayName("");
				sectionInfo.setDescription("");
				sectionInfo.setFieldTypeClass(CEntityFieldService.class);
				sectionInfo.setIsCaptionVisible(line.getIsCaptionVisible());
				return sectionInfo;
			} else {
				field = getEntityField(screenClassName, relationFieldName);
				Check.notNull(field, "Relation field not found: " + relationFieldName + " in class " + screenClassName);
				field = getEntityField(field.getType().getSimpleName(), line.getEntityProperty());
				Check.notNull(field, "Field not found: " + line.getEntityProperty() + " in class " + field.getType().getSimpleName());
			}
			// get field of class
			final EntityFieldInfo info = createFieldInfo(field);
			// copy non problematic properties from line to info
			info.setIsCaptionVisible(line.getIsCaptionVisible());
			info.setDisplayName(line.getFieldCaption());
			info.setDescription(line.getFieldCaption());
			info.setRequired(line.getIsRequired());
			info.setReadOnly(line.getIsReadonly());
			info.setHidden(line.getIsHidden());
			Check.notNull(info, "Field info not found for field: " + line.getEntityProperty() + " in class " + field.getType().getSimpleName());
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

	/** Get available custom component methods for a given entity type.
	 * @param entityType the entity type to analyze
	 * @return list of available custom component method names */
	public static List<String> getCustomComponentMethods(final String entityType) {
		Check.notBlank(entityType, "Entity type must not be empty");
		final Class<?> entityClass = CAuxillaries.getEntityClass(entityType);
		if (entityClass == null) {
			return List.of();
		}
		final List<String> customMethods = new ArrayList<>();
		final List<Field> allFields = getAllFields(entityClass);
		for (final Field field : allFields) {
			if (Modifier.isStatic(field.getModifiers()) || field.getName().equals("serialVersionUID") || field.getName().equals("LOGGER")) {
				continue;
			}
			final AMetaData metaData = field.getAnnotation(AMetaData.class);
			if ((metaData != null) && (metaData.createComponentMethod() != null) && !metaData.createComponentMethod().trim().isEmpty()) {
				final String methodNames = metaData.createComponentMethod().trim();
				// Split by comma and add each method
				final String[] methods = methodNames.split(",");
				for (String methodName : methods) {
					methodName = methodName.trim();
					if (!methodName.isEmpty() && !customMethods.contains(methodName)) {
						customMethods.add(methodName);
					}
				}
			}
		}
		return customMethods;
	}

	public static String extractEntityTypeFromBeanName(String beanName) {
		Check.notNull(beanName, "Bean name cannot be null");
		Check.notBlank(beanName, "Bean name cannot be empty");
		// Convert service bean name to entity class name
		// E.g., CActivityService -> CActivity
		Check.isTrue(beanName.length() > "Service".length(), "Bean name is too short to extract entity type");
		Check.isTrue(beanName.endsWith("Service"), "Bean name must end with 'Service'");
		return beanName.substring(0, beanName.length() - "Service".length());
	}

	/** Get data provider beans for reference fields.
	 * @return list of available data provider bean names */
	public static List<String> getDataProviderBeans() {
		return List.of("CActivityService", "CActivityTypeService", "CProjectItemStatusService", "CActivityPriorityService", "CMeetingService",
				"CMeetingTypeService", "CMeetingStatusService", "CRiskService", "CRiskTypeService", "CRiskStatusService", "CRiskPriorityService",
				"CProjectService", "CUserService", "CCompanyService", "CDetailSectionService", "CDetailLinesService");
	}

	public static Field getEntityField(Class<?> type, final String fieldName) throws NoSuchFieldException {
		Check.notNull(type, "Entity class must not be null");
		Check.notBlank(fieldName, "Field name must not be empty");
		final Class<?> clazz = type;
		while ((type != null) && (type != Object.class)) {
			try {
				return type.getDeclaredField(fieldName);
			} catch (final NoSuchFieldException e) {
				type = type.getSuperclass(); // bir üst sınıfa bak
			}
		}
		throw new NoSuchFieldException("Field '" + fieldName + "' not found in entity type: " + clazz.getSimpleName());
	}

	public static Field getEntityField(final String entityType, final String fieldName) throws NoSuchFieldException {
		Check.notBlank(entityType, "Entity type must not be empty");
		final Class<?> currentClass = CAuxillaries.getEntityClass(entityType);
		Check.notNull(currentClass, "Entity class must not be null for type: " + entityType);
		return getEntityField(currentClass, fieldName);
	}

	public static EntityFieldInfo getEntityFieldInfo(final String entityType, final String fieldName) {
		Check.notBlank(entityType, "Entity type must not be empty");
		final List<EntityFieldInfo> fields = getEntityFields(entityType);
		Check.notNull(fields, "Fields list must not be null for type: " + entityType);
		return fields.stream().filter(field -> field.getFieldName().equals(fieldName)).findFirst().orElse(null);
	}

	/** Get field information for a specific entity type.
	 * @param entityType the entity type name
	 * @return list of field information */
	public static List<EntityFieldInfo> getEntityFields(final String entityType) {
		Check.notBlank(entityType, "Entity type must not be empty");
		final Class<?> entityClass = CAuxillaries.getEntityClass(entityType);
		Check.notNull(entityClass, "Entity class must not be null for type: " + entityType);
		final List<EntityFieldInfo> fields = new ArrayList<>();
		final List<Field> allFields = getAllFields(entityClass);
		for (final Field field : allFields) {
			if (Modifier.isStatic(field.getModifiers()) || field.getName().equals("serialVersionUID") || field.getName().equals("LOGGER")) {
				continue;
			}
			if (field.getAnnotation(AMetaData.class) == null) {
				continue;
			}
			final EntityFieldInfo fieldInfo = createFieldInfo(field);
			if (fieldInfo != null) {
				fields.add(fieldInfo);
			}
		}
		return fields;
	}

	public static List<EntityFieldInfo> getEntityRelationFields(final String entityType, final List<EntityFieldInfo> listOfAdditionalFields) {
		Check.notBlank(entityType, "Entity type must not be empty");
		final Class<?> entityClass = CAuxillaries.getEntityClass(entityType);
		Check.notNull(entityClass, "Entity class must not be null for type: " + entityType);
		final List<EntityFieldInfo> fields = new ArrayList<>();
		if (listOfAdditionalFields != null) {
			fields.addAll(listOfAdditionalFields);
		}
		final List<Field> allFields = getAllFields(entityClass);
		for (final Field field : allFields) {
			if ((field.getAnnotation(AMetaData.class) == null) || Modifier.isStatic(field.getModifiers())
					|| field.getName().equals("serialVersionUID") || field.getName().equals("LOGGER") || !isFieldComplexType(field.getType())) {
				continue;
			}
			final EntityFieldInfo fieldInfo = createFieldInfo(field);
			if (fieldInfo != null) {
				fields.add(fieldInfo);
			}
		}
		return fields;
	}

	public static List<EntityFieldInfo> getEntitySimpleFields(final String entityType, final List<EntityFieldInfo> listOfAdditionalFields) {
		Check.notBlank(entityType, "Entity type must not be empty");
		final Class<?> entityClass = CAuxillaries.getEntityClass(entityType);
		Check.notNull(entityClass, "Entity class must not be null for type: " + entityType);
		final List<EntityFieldInfo> fields = new ArrayList<>();
		if (listOfAdditionalFields != null) {
			fields.addAll(listOfAdditionalFields);
		}
		final List<Field> allFields = getAllFields(entityClass);
		for (final Field field : allFields) {
			if (Modifier.isStatic(field.getModifiers()) || field.getName().equals("serialVersionUID") || field.getName().equals("LOGGER")
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
			return "UNKNOWN";
		}
	}

	private static boolean isFieldComplexType(final Class<?> type) {
		// Check if the field type is a complex type (not primitive or standard types)
		if (type.isPrimitive() || type.isEnum() || (type == String.class) || Number.class.isAssignableFrom(type) || (type == Boolean.class)
				|| (type == Date.class) || (type == LocalDate.class) || (type == LocalDateTime.class)) {
			return false;
		}
		return true;
		// Check if it's a domain entity (likely a reference) return
		// type.getSimpleName().startsWith("C")&&
		// Character.isUpperCase(type.getSimpleName().charAt(1));
	}

	public static boolean isRelationType(final Class<?> type) {
		if (type.isPrimitive()) {
			return false;
		}
		// final String typeName = type.getSimpleName();
		if ((type == String.class) || (type == Integer.class) || (type == Long.class) || (type == BigDecimal.class) || (type == Double.class)
				|| (type == Float.class) || (type == LocalDate.class) || (type == LocalDateTime.class) || (type == Date.class)
				|| (type == Boolean.class)) {
			return false;
		} else {
			if (type.getSimpleName().startsWith("E")) {
				// enum
				return false;
			}
			return true;
		}
	}

	/** Prints detailed field information for debugging purposes.
	 * @param fieldInfo the field information to print */
	public static void printFieldInfo(final EntityFieldInfo fieldInfo) {
		if (fieldInfo == null) {
			LOGGER.debug("Field info is null");
			return;
		}
		LOGGER.debug("Field Name: {}", fieldInfo.getFieldName());
		LOGGER.debug("Display Name: {}", fieldInfo.getDisplayName());
		LOGGER.debug("Description: {}", fieldInfo.getDescription());
		LOGGER.debug("Field Type: {}", fieldInfo.getFieldType());
		LOGGER.debug("Java Type: {}", fieldInfo.getJavaType());
		LOGGER.debug("Required: {}", fieldInfo.isRequired());
		LOGGER.debug("Read Only: {}", fieldInfo.isReadOnly());
		LOGGER.debug("Hidden: {}", fieldInfo.isHidden());
		LOGGER.debug("Order: {}", fieldInfo.getOrder());
		LOGGER.debug("Max Length: {}", fieldInfo.getMaxLength());
		LOGGER.debug("Default Value: {}", fieldInfo.getDefaultValue());
		LOGGER.debug("Data Provider Bean: {}", fieldInfo.getDataProviderBean());
		LOGGER.debug("Auto Select First: {}", fieldInfo.isAutoSelectFirst());
		LOGGER.debug("Placeholder: {}", fieldInfo.getPlaceholder());
		LOGGER.debug("Allow Custom Value: {}", fieldInfo.isAllowCustomValue());
		LOGGER.debug("Use Radio Buttons: {}", fieldInfo.isUseRadioButtons());
		LOGGER.debug("Combobox Read Only: {}", fieldInfo.isComboboxReadOnly());
		LOGGER.debug("Clear On Empty Data: {}", fieldInfo.isClearOnEmptyData());
		LOGGER.debug("Width: {}", fieldInfo.getWidth());
	}
}
