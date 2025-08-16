package tech.derbent.screens.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Service to provide entity field information for screen line configuration.
 * This service uses reflection to extract field information from domain entities.
 */
@Service
public class CEntityFieldService {

    /**
     * Get available entity types for screen configuration.
     * @return list of entity types
     */
    public List<String> getAvailableEntityTypes() {
        return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
    }

    /**
     * Get field information for a specific entity type.
     * @param entityType the entity type name
     * @return list of field information
     */
    public List<EntityFieldInfo> getEntityFields(String entityType) {
        final Class<?> entityClass = getEntityClass(entityType);
        if (entityClass == null) {
            return List.of();
        }

        final List<EntityFieldInfo> fields = new ArrayList<>();
        
        // Get all fields including inherited ones
        final List<Field> allFields = getAllFields(entityClass);
        
        for (Field field : allFields) {
            // Skip static fields and certain system fields
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
                field.getName().equals("serialVersionUID") ||
                field.getName().equals("LOGGER")) {
                continue;
            }

            final EntityFieldInfo fieldInfo = createFieldInfo(field);
            if (fieldInfo != null) {
                fields.add(fieldInfo);
            }
        }

        return fields;
    }

    /**
     * Get data provider beans for reference fields.
     * @return list of available data provider bean names
     */
    public List<String> getDataProviderBeans() {
        return List.of(
            "CActivityService", "CActivityTypeService", "CActivityStatusService", "CActivityPriorityService",
            "CMeetingService", "CMeetingTypeService", "CMeetingStatusService",
            "CRiskService", "CRiskTypeService", "CRiskStatusService", "CRiskPriorityService",
            "CProjectService", "CUserService", "CUserTypeService",
            "CCompanyService", "CScreenService", "CScreenLinesService"
        );
    }

    private Class<?> getEntityClass(String entityType) {
        try {
            switch (entityType) {
                case "CActivity":
                    return CActivity.class;
                case "CMeeting":
                    return CMeeting.class;
                case "CRisk":
                    return CRisk.class;
                case "CProject":
                    return CProject.class;
                case "CUser":
                    return CUser.class;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        final List<Field> fields = new ArrayList<>();
        
        // Get fields from current class and all superclasses
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }

    private EntityFieldInfo createFieldInfo(Field field) {
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
        } catch (Exception e) {
            return null;
        }
    }

    private String getSimpleTypeName(Class<?> type) {
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

    private String formatFieldName(String fieldName) {
        // Convert camelCase to Title Case
        return fieldName.replaceAll("([A-Z])", " $1")
                       .replaceAll("^.", String.valueOf(Character.toUpperCase(fieldName.charAt(0))))
                       .trim();
    }

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

        // Getters and setters
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }
        
        public String getJavaType() { return javaType; }
        public void setJavaType(String javaType) { this.javaType = javaType; }
        
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public boolean isReadOnly() { return readOnly; }
        public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
        
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
        
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
        
        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
        
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        
        public String getDataProviderBean() { return dataProviderBean; }
        public void setDataProviderBean(String dataProviderBean) { this.dataProviderBean = dataProviderBean; }

        @Override
        public String toString() {
            return String.format("%s (%s)", displayName, fieldName);
        }
    }
}