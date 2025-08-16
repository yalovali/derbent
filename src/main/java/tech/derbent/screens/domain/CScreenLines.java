package tech.derbent.screens.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CScreenLines - Domain entity representing individual lines/fields in a screen definition.
 * Layer: Domain (MVC)
 * Each line represents a field that should be displayed in the screen view.
 */
@Entity
@Table(name = "cscreen_lines")
@AttributeOverride(name = "id", column = @Column(name = "screen_line_id"))
public class CScreenLines extends CEntityDB<CScreenLines> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CScreenLines.class);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    @NotNull(message = "Screen reference is required")
    private CScreen screen;

    @Column(name = "line_order", nullable = false)
    @Min(value = 1, message = "Line order must be at least 1")
    @Max(value = 999, message = "Line order cannot exceed 999")
    @MetaData(
        displayName = "Line Order", 
        required = true, 
        readOnly = false,
        description = "Order of this line in the screen (1-999)", 
        hidden = false, 
        order = 1,
        defaultValue = "1"
    )
    private Integer lineOrder = 1;

    @Column(name = "field_caption", nullable = false, length = 255)
    @Size(max = 255, message = "Field caption cannot exceed 255 characters")
    @NotNull(message = "Field caption is required")
    @MetaData(
        displayName = "Field Caption", 
        required = true, 
        readOnly = false,
        description = "Caption/label to display for this field", 
        hidden = false, 
        order = 2,
        maxLength = 255
    )
    private String fieldCaption;

    @Column(name = "entity_field_name", nullable = false, length = 100)
    @Size(max = 100, message = "Entity field name cannot exceed 100 characters")
    @NotNull(message = "Entity field name is required")
    @MetaData(
        displayName = "Entity Field Name", 
        required = true, 
        readOnly = false,
        description = "Name of the field in the entity", 
        hidden = false, 
        order = 3,
        maxLength = 100
    )
    private String entityFieldName;

    @Column(name = "field_description", nullable = true, length = 500)
    @Size(max = 500, message = "Field description cannot exceed 500 characters")
    @MetaData(
        displayName = "Field Description", 
        required = false, 
        readOnly = false,
        description = "Description or help text for this field", 
        hidden = false, 
        order = 4,
        maxLength = 500
    )
    private String fieldDescription;

    @Column(name = "field_type", nullable = false, length = 50)
    @Size(max = 50, message = "Field type cannot exceed 50 characters")
    @NotNull(message = "Field type is required")
    @MetaData(
        displayName = "Field Type", 
        required = true, 
        readOnly = false,
        description = "Type of field (TEXT, NUMBER, DATE, BOOLEAN, REFERENCE)", 
        hidden = false, 
        order = 5,
        maxLength = 50
    )
    private String fieldType;

    @Column(name = "is_required", nullable = false)
    @MetaData(
        displayName = "Required", 
        required = false, 
        readOnly = false,
        description = "Whether this field is required", 
        hidden = false, 
        order = 6,
        defaultValue = "false"
    )
    private Boolean isRequired = false;

    @Column(name = "is_readonly", nullable = false)
    @MetaData(
        displayName = "Read Only", 
        required = false, 
        readOnly = false,
        description = "Whether this field is read-only", 
        hidden = false, 
        order = 7,
        defaultValue = "false"
    )
    private Boolean isReadonly = false;

    @Column(name = "is_hidden", nullable = false)
    @MetaData(
        displayName = "Hidden", 
        required = false, 
        readOnly = false,
        description = "Whether this field is hidden", 
        hidden = false, 
        order = 8,
        defaultValue = "false"
    )
    private Boolean isHidden = false;

    @Column(name = "default_value", nullable = true, length = 255)
    @Size(max = 255, message = "Default value cannot exceed 255 characters")
    @MetaData(
        displayName = "Default Value", 
        required = false, 
        readOnly = false,
        description = "Default value for this field", 
        hidden = false, 
        order = 9,
        maxLength = 255
    )
    private String defaultValue;

    @Column(name = "related_entity_type", nullable = true, length = 100)
    @Size(max = 100, message = "Related entity type cannot exceed 100 characters")
    @MetaData(
        displayName = "Related Entity Type", 
        required = false, 
        readOnly = false,
        description = "Type of related entity for reference fields", 
        hidden = false, 
        order = 10,
        maxLength = 100
    )
    private String relatedEntityType;

    @Column(name = "data_provider_bean", nullable = true, length = 100)
    @Size(max = 100, message = "Data provider bean cannot exceed 100 characters")
    @MetaData(
        displayName = "Data Provider Bean", 
        required = false, 
        readOnly = false,
        description = "Spring bean name for data provider (for comboboxes)", 
        hidden = false, 
        order = 11,
        maxLength = 100
    )
    private String dataProviderBean;

    @Column(name = "max_length", nullable = true)
    @Min(value = 1, message = "Max length must be at least 1")
    @Max(value = 10000, message = "Max length cannot exceed 10000")
    @MetaData(
        displayName = "Max Length", 
        required = false, 
        readOnly = false,
        description = "Maximum length for text fields", 
        hidden = false, 
        order = 12
    )
    private Integer maxLength;

    @Column(name = "is_active", nullable = false)
    @MetaData(
        displayName = "Active", 
        required = false, 
        readOnly = false,
        description = "Whether this line is active", 
        hidden = false, 
        order = 20,
        defaultValue = "true"
    )
    private Boolean isActive = true;

    /**
     * Default constructor for JPA.
     */
    public CScreenLines() {
        super(CScreenLines.class);
    }

    public CScreenLines(CScreen screen, String fieldCaption, String entityFieldName) {
        super(CScreenLines.class);
        this.screen = screen;
        this.fieldCaption = fieldCaption;
        this.entityFieldName = entityFieldName;
    }

    // Getters and Setters

    public CScreen getScreen() {
        return screen;
    }

    public void setScreen(CScreen screen) {
        this.screen = screen;
    }

    public Integer getLineOrder() {
        return lineOrder;
    }

    public void setLineOrder(Integer lineOrder) {
        this.lineOrder = lineOrder;
    }

    public String getFieldCaption() {
        return fieldCaption;
    }

    public void setFieldCaption(String fieldCaption) {
        this.fieldCaption = fieldCaption;
    }

    public String getEntityFieldName() {
        return entityFieldName;
    }

    public void setEntityFieldName(String entityFieldName) {
        this.entityFieldName = entityFieldName;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }

    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Boolean getIsReadonly() {
        return isReadonly;
    }

    public void setIsReadonly(Boolean isReadonly) {
        this.isReadonly = isReadonly;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public String getDataProviderBean() {
        return dataProviderBean;
    }

    public void setDataProviderBean(String dataProviderBean) {
        this.dataProviderBean = dataProviderBean;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return String.format("CScreenLines{id=%d, lineOrder=%d, fieldCaption='%s', entityFieldName='%s'}", 
                getId(), lineOrder, fieldCaption, entityFieldName);
    }
}