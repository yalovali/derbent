package tech.derbent.api.screens.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.view.CAbstractEntityDBPage;
import tech.derbent.api.screens.service.IOrderedEntity;
import tech.derbent.api.validation.ValidationMessages;

/** CDetailLines - Domain entity representing individual lines/fields in a screen definition. Layer: Domain (MVC) Each line represents a field that
 * should be displayed in the screen view. */
@Entity
@Table (name = "cdetaillines")
@AttributeOverride (name = "id", column = @Column (name = "detaillines_id"))
public class CDetailLines extends CEntityDB<CDetailLines> implements IOrderedEntity {

	public static final String DEFAULT_COLOR = "#7A6E58"; // OpenWindows Border Darker - detail lines (darker)
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Detail Lines";
	public static final String ENTITY_TITLE_SINGULAR = "Detail Line";
	public static final String VIEW_NAME = "Detail Lines View";

	public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { return null; }

	@Column (name = "data_provider_bean", nullable = true, length = 100)
	@Size (max = 100, message = ValidationMessages.DATA_PROVIDER_MAX_LENGTH)
	@AMetaData (
			displayName = "Data Provider Bean", required = false, readOnly = false,
			description = "Spring bean name for data provider (for comboboxes)", hidden = false, maxLength = 100
	)
	private String dataProviderBean;
	@Column (name = "default_value", nullable = true, length = 255)
	@Size (max = 255, message = "Default value cannot exceed 255 characters")
	@AMetaData (
			displayName = "Default Value", required = false, readOnly = false, description = "Default value for this field", hidden = false,
			maxLength = 255
	)
	private String defaultValue;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "detailsection_id", nullable = false)
	@NotNull (message = ValidationMessages.SCREEN_REFERENCE_REQUIRED)
	@AMetaData (
			displayName = "Screen Reference", required = true, readOnly = false, description = "Screen Reference", hidden = false, defaultValue = "1"
	)
	private CDetailSection detailSection;
	@Column (name = "entity_property", nullable = false, length = 100)
	@Size (max = 100, message = "Field property name cannot exceed 100 characters")
	@NotNull (message = "Field propery name is required")
	@AMetaData (
			displayName = "Field Property", required = true, readOnly = false, description = "Name of the property in the entity", hidden = false,
			maxLength = 100, dataProviderBean = "none"
	)
	private String entityProperty = "";
	@Column (name = "field_caption", nullable = false, length = 255)
	@Size (max = 255, message = "Field caption cannot exceed 255 characters")
	@NotNull (message = "Field caption is required")
	@AMetaData (
			displayName = "Field Caption", required = true, readOnly = false, description = "Caption/label to display for this field", hidden = false,
			maxLength = 255
	)
	private String fieldCaption = "";
	@Column (name = "field_description", nullable = true, length = 500)
	@Size (max = 500, message = ValidationMessages.FIELD_DESCRIPTION_MAX_LENGTH)
	@AMetaData (
			displayName = "Field Description", required = false, readOnly = false, description = "Description or help text for this field",
			hidden = false, maxLength = 500
	)
	private String fieldDescription;
	@Column (name = "haveNextOneOnSameLine", nullable = false)
	@AMetaData (
			displayName = "Have Next One On Same Line", required = false, readOnly = false,
			description = "Whether the next field is on the same line", hidden = false, defaultValue = "false"
	)
	private Boolean haveNextOneOnSameLine = false;
	@Column (name = "is_caption_visible", nullable = false)
	@AMetaData (
			displayName = "Caption Visible", required = false, readOnly = false, description = "Whether the caption is visible", hidden = false,
			defaultValue = "false"
	)
	private Boolean isCaptionVisible = true;
	@Column (name = "is_hidden", nullable = false)
	@AMetaData (
			displayName = "Hidden", required = false, readOnly = false, description = "Whether this field is hidden", hidden = false,
			defaultValue = "false"
	)
	private Boolean isHidden = false;
	@Column (name = "is_readonly", nullable = false)
	@AMetaData (
			displayName = "Read Only", required = false, readOnly = false, description = "Whether this field is read-only", hidden = false,
			defaultValue = "false"
	)
	private Boolean isReadonly = false;
	@Column (name = "is_required", nullable = false)
	@AMetaData (
			displayName = "Required", required = false, readOnly = false, description = "Whether this field is required", hidden = false,
			defaultValue = "false"
	)
	private Boolean isRequired = false;
	@Column (name = "itemOrder", nullable = false)
	@Min (value = 1, message = "Line order must be at least 1")
	@Max (value = 999, message = "Line order cannot exceed 999")
	@AMetaData (
			displayName = "Line Order", required = true, readOnly = false, description = "Order of this line in the screen (1-999)", hidden = false,
			defaultValue = "0"
	)
	private Integer itemOrder = 0;
	@Column (name = "max_length", nullable = true)
	@Min (value = -1, message = "Min length must be at least 1")
	@Max (value = 10000, message = "Max length cannot exceed 10000")
	@AMetaData (displayName = "Max Length", required = false, readOnly = false, description = "Maximum length for text fields", hidden = false)
	private Integer maxLength = 0;
	@Column (name = "related_entity_type", nullable = true, length = 100)
	@Size (max = 100, message = "Related entity type cannot exceed 100 characters")
	@AMetaData (
			displayName = "Related Entity Type", required = false, readOnly = false, description = "Type of related entity for reference fields",
			hidden = false, maxLength = 100
	)
	private String relatedEntityType;
	@Column (name = "relationFieldName", nullable = false, length = 100)
	@Size (max = 100, message = ValidationMessages.RELATION_FIELD_NAME_MAX_LENGTH)
	@AMetaData (
			displayName = "Relation Field", required = true, readOnly = false, description = "Relation Field is designed for", hidden = false,
			maxLength = 100, dataProviderBean = "none"
	)
	private String relationFieldName = "";
	@Column (name = "isSectionAsTab", nullable = false)
	@AMetaData (
			displayName = "A Tab Section", required = false, readOnly = false, description = "Whether this section is a tab", hidden = false,
			defaultValue = "false"
	)
	private Boolean sectionAsTab = true;
	@Column (name = "sectionName", nullable = true, length = 100)
	@Size (max = 100, message = "Name cannot exceed 100 characters")
	@AMetaData (
			displayName = "Field Property", required = false, readOnly = false, description = "Section of entries below", hidden = false,
			maxLength = 100
	)
	private String sectionName;

	/** Default constructor for JPA. */
	protected CDetailLines() {}

	public CDetailLines(final CDetailSection detail, final String relationFieldName, final String entityProperty) {
		super(CDetailLines.class);
		initializeDefaults();
		detailSection = detail;
		this.relationFieldName = relationFieldName;
		this.entityProperty = entityProperty;
	}

	public String getDataProviderBean() { return dataProviderBean; }

	public String getDefaultValue() { return defaultValue; }

	public CDetailSection getDetailSection() { return detailSection; }

	public String getEntityProperty() { return entityProperty; }

	public String getFieldCaption() { return fieldCaption; }

	public String getFieldDescription() { return fieldDescription; }

	public Boolean getHaveNextOneOnSameLine() { return haveNextOneOnSameLine; }

	public Boolean getIsCaptionVisible() { return isCaptionVisible; }

	public Boolean getIsHidden() { return isHidden; }

	public Boolean getIsReadonly() { return isReadonly; }

	public Boolean getIsRequired() { return isRequired; }

	/** {@inheritDoc} */
	@Override
	public Integer getItemOrder() { return itemOrder; }

	public Integer getMaxLength() { return maxLength; }

	public String getRelatedEntityType() { return relatedEntityType; }

	public String getRelationFieldName() { return relationFieldName; }

	public Boolean getSectionAsTab() { return sectionAsTab; }

	public String getSectionName() { return sectionName; }

	private final void initializeDefaults() {
		// Note: Screen entities are internal framework classes, not registered in entity registry
	}
	// Getters and Setters

	public void setDataProviderBean(final String dataProviderBean) { this.dataProviderBean = dataProviderBean; }

	public void setDefaultValue(final String defaultValue) { this.defaultValue = defaultValue; }

	public void setDescription(final String fieldDescription) { this.fieldDescription = fieldDescription; }

	public void setDetailSection(final CDetailSection screen) { detailSection = screen; }

	public void setEntityProperty(final String entityProperty) { this.entityProperty = entityProperty; }

	public void setFieldCaption(final String fieldCaption) { this.fieldCaption = fieldCaption; }

	public void setHaveNextOneOnSameLine(Boolean haveNextOneOnSameLine) { this.haveNextOneOnSameLine = haveNextOneOnSameLine; }

	public void setIsCaptionVisible(Boolean isCaptionVisible) { this.isCaptionVisible = isCaptionVisible; }

	public void setIsHidden(final Boolean isHidden) { this.isHidden = isHidden; }

	public void setIsReadonly(final Boolean isReadonly) { this.isReadonly = isReadonly; }

	public void setIsRequired(final Boolean isRequired) { this.isRequired = isRequired; }

	/** {@inheritDoc} */
	@Override
	public void setItemOrder(final Integer itemOrder) { this.itemOrder = itemOrder; }

	public void setMaxLength(final Integer maxLength) { this.maxLength = maxLength; }

	public void setRelatedEntityType(final String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

	public void setRelationFieldName(final String relationFieldName) { this.relationFieldName = relationFieldName; }

	public void setSectionAsTab(Boolean sectionAsTab) { this.sectionAsTab = sectionAsTab; }

	public void setSectionName(final String sectionName) {
		this.sectionName = sectionName;
		fieldCaption = sectionName;
	}

	@Override
	public String toString() {
		return "CDetailLines{id=%d, itemOrder=%d, fieldCaption='%s', entityProperty='%s'}".formatted(getId(), itemOrder, fieldCaption,
				entityProperty);
	}
}
