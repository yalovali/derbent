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

/**
 * Service to provide entity field information for screen line configuration. This service uses reflection to extract
 * field information from domain entities.
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

        private boolean required = false;

        private boolean readOnly = false;

        private boolean hidden = false;

        private int order = 999;

        private int maxLength = 255;

        private String defaultValue = "";

        private String dataProviderBean = "";

        public String getDataProviderBean() {
            return dataProviderBean;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getDescription() {
            return description;
        }

        public String getDisplayName() {
            return displayName;
        }

        // Getters and setters
        public String getFieldName() {
            return fieldName;
        }

        public String getFieldType() {
            return fieldType;
        }

        public String getJavaType() {
            return javaType;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public int getOrder() {
            return order;
        }

        public boolean isHidden() {
            return hidden;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public boolean isRequired() {
            return required;
        }

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

        public void setFieldName(final String fieldName) {
            this.fieldName = fieldName;
        }

        public void setFieldType(final String fieldType) {
            this.fieldType = fieldType;
        }

        public void setHidden(final boolean hidden) {
            this.hidden = hidden;
        }

        public void setJavaType(final String javaType) {
            this.javaType = javaType;
        }

        public void setMaxLength(final int maxLength) {
            this.maxLength = maxLength;
        }

        public void setOrder(final int order) {
            this.order = order;
        }

        public void setReadOnly(final boolean readOnly) {
            this.readOnly = readOnly;
        }

        public void setRequired(final boolean required) {
            this.required = required;
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", displayName, fieldName);
        }
    }

    public static final String THIS_CLASS = "This Class";

    public static final String SECTION = "Section";

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
            } else {
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
                .replaceAll("^.", String.valueOf(Character.toUpperCase(fieldName.charAt(0)))).trim();
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

    /**
     * Get data provider beans for reference fields.
     * 
     * @return list of available data provider bean names
     */
    public List<String> getDataProviderBeans() {
        return List.of("CActivityService", "CActivityTypeService", "CActivityStatusService", "CActivityPriorityService",
                "CMeetingService", "CMeetingTypeService", "CMeetingStatusService", "CRiskService", "CRiskTypeService",
                "CRiskStatusService", "CRiskPriorityService", "CProjectService", "CUserService", "CUserTypeService",
                "CCompanyService", "CScreenService", "CScreenLinesService");
    }

    public EntityFieldInfo getEntityFieldInfo(final String entityType, final String fieldName) {
        final List<EntityFieldInfo> fields = getEntityFields(entityType);
        return fields.stream().filter(field -> field.getFieldName().equals(fieldName)).findFirst().orElse(null);
    }

    /**
     * Get field information for a specific entity type.
     * 
     * @param entityType
     *            the entity type name
     * @return list of field information
     */
    public List<EntityFieldInfo> getEntityFields(final String entityType) {
        final Class<?> entityClass = getEntityClass(entityType);
        Check.notNull(entityClass, "Entity class must not be null for type: " + entityType);
        final List<EntityFieldInfo> fields = new ArrayList<>();
        final List<Field> allFields = getAllFields(entityClass);

        for (final Field field : allFields) {

            if (Modifier.isStatic(field.getModifiers()) || field.getName().equals("serialVersionUID")
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

    public List<EntityFieldInfo> getEntityRelationFields(final String entityType,
            final List<EntityFieldInfo> listOfAdditionalFields) {
        final Class<?> entityClass = getEntityClass(entityType);
        Check.notNull(entityClass, "Entity class must not be null for type: " + entityType);
        final List<EntityFieldInfo> fields = new ArrayList<>();

        if (listOfAdditionalFields != null) {
            fields.addAll(listOfAdditionalFields);
        }
        final List<Field> allFields = getAllFields(entityClass);

        for (final Field field : allFields) {

            if (Modifier.isStatic(field.getModifiers()) || field.getName().equals("serialVersionUID")
                    || field.getName().equals("LOGGER") || !isFieldComplexType(field.getType())) {
                continue;
            }
            final EntityFieldInfo fieldInfo = createFieldInfo(field);

            if (fieldInfo != null) {
                fields.add(fieldInfo);
            }
        }
        return fields;
    }

    public List<EntityFieldInfo> getEntitySimpleFields(final String entityType,
            final List<EntityFieldInfo> listOfAdditionalFields) {
        final Class<?> entityClass = getEntityClass(entityType);
        Check.notNull(entityClass, "Entity class must not be null for type: " + entityType);
        final List<EntityFieldInfo> fields = new ArrayList<>();

        if (listOfAdditionalFields != null) {
            fields.addAll(listOfAdditionalFields);
        }
        final List<Field> allFields = getAllFields(entityClass);

        for (final Field field : allFields) {

            if (Modifier.isStatic(field.getModifiers()) || field.getName().equals("serialVersionUID")
                    || field.getName().equals("LOGGER") || isFieldComplexType(field.getType())) {
                continue;
            }
            final EntityFieldInfo fieldInfo = createFieldInfo(field);

            if (fieldInfo != null) {
                fields.add(fieldInfo);
            }
        }
        return fields;
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

    private boolean isFieldComplexType(final Class<?> type) {

        // Check if the field type is a complex type (not primitive or standard types)
        if (type.isPrimitive() || type.isEnum() || type == String.class || Number.class.isAssignableFrom(type)
                || type == Boolean.class || type == Date.class || type == LocalDate.class
                || type == LocalDateTime.class) {
            return false;
        }
        return true;
        // Check if it's a domain entity (likely a reference) return
        // type.getSimpleName().startsWith("C")&&
        // Character.isUpperCase(type.getSimpleName().charAt(1));
    }
}