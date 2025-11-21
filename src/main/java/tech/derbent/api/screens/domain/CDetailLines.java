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

/** CDetailLines - Domain entity representing individual lines/fields in a screen definition. Layer: Domain (MVC) Each line represents a field that
 * should be displayed in the screen view. */
@Entity
@Table (name = "cdetaillines")
@AttributeOverride (name = "id", column = @Column (name = "detaillines_id"))
public class CDetailLines extends CEntityDB<CDetailLines> {

	public static final String DEFAULT_COLOR = "#a76100";
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String VIEW_NAME = "Detail Lines View";

	public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { return null; }

	@Column (name = "data_provider_bean", nullable = true, length = 100)
	@Size (max = 100, message = "Data provider bean cannot exceed 100 characters")
	@AMetaData (
			displayName = "Data Provider Bean", required = false, readOnly = false,
			description = "Spring bean name for data provider (for comboboxes)", hidden = false, order = 11, maxLength = 100
	)
	private String dataProviderBean;
	@Column (name = "default_value", nullable = true, length = 255)
	@Size (max = 255, message = "Default value cannot exceed 255 characters")
	@AMetaData (
			displayName = "Default Value", required = false, readOnly = false, description = "Default value for this field", hidden = false,
			order = 9, maxLength = 255
	)
	private String defaultValue;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "detailsection_id", nullable = false)
	@NotNull (message = "Screen reference is required")
	@AMetaData (
			displayName = "Screen Reference", required = true, readOnly = false, description = "Screen Reference", hidden = false, order = 1,
			defaultValue = "1"
	)
	private CDetailSection detailSection;
	@Column (name = "entity_property", nullable = false, length = 100)
	@Size (max = 100, message = "Field property name cannot exceed 100 characters")
	@NotNull (message = "Field propery name is required")
	@AMetaData (
			displayName = "Field Property", required = true, readOnly = false, description = "Name of the property in the entity", hidden = false,
			order = 3, maxLength = 100, dataProviderBean = "none"
	)
	private String entityProperty;
	@Column (name = "field_caption", nullable = false, length = 255)
	@Size (max = 255, message = "Field caption cannot exceed 255 characters")
	@NotNull (message = "Field caption is required")
	@AMetaData (
			displayName = "Field Caption", required = true, readOnly = false, description = "Caption/label to display for this field", hidden = false,
			order = 2, maxLength = 255
	)
	private String fieldCaption;
	@Column (name = "field_description", nullable = true, length = 500)
	@Size (max = 500, message = "Field description cannot exceed 500 characters")
	@AMetaData (
			displayName = "Field Description", required = false, readOnly = false, description = "Description or help text for this field",
			hidden = false, order = 4, maxLength = 500
	)
	private String fieldDescription;
	@Column (name = "is_caption_visible", nullable = false)
	@AMetaData (
			displayName = "Caption Visible", required = false, readOnly = false, description = "Whether the caption is visible", hidden = false,
			order = 10, defaultValue = "false"
	)
	private Boolean isCaptionVisible = true;
	@Column (name = "is_hidden", nullable = false)
	@AMetaData (
			displayName = "Hidden", required = false, readOnly = false, description = "Whether this field is hidden", hidden = false, order = 8,
			defaultValue = "false"
	)
	private Boolean isHidden = false;
	@Column (name = "is_readonly", nullable = false)
	@AMetaData (
			displayName = "Read Only", required = false, readOnly = false, description = "Whether this field is read-only", hidden = false, order = 9,
			defaultValue = "false"
	)
	private Boolean isReadonly = false;
	@Column (name = "is_required", nullable = false)
	@AMetaData (
			displayName = "Required", required = false, readOnly = false, description = "Whether this field is required", hidden = false, order = 6,
			defaultValue = "false"
	)
	private Boolean isRequired = false;
	@Column (name = "lineOrder", nullable = false)
	@Min (value = 1, message = "Line order must be at least 1")
	@Max (value = 999, message = "Line order cannot exceed 999")
	@AMetaData (
			displayName = "Line Order", required = true, readOnly = false, description = "Order of this line in the screen (1-999)", hidden = false,
			order = 1, defaultValue = "0"
	)
	private Integer lineOrder = 0;
	@Column (name = "max_length", nullable = true)
	@Min (value = 0, message = "Max length must be at least 1")
	@Max (value = 10000, message = "Max length cannot exceed 10000")
	@AMetaData (
			displayName = "Max Length", required = false, readOnly = false, description = "Maximum length for text fields", hidden = false, order = 12
	)
	private Integer maxLength = 0;
	@Column (name = "related_entity_type", nullable = true, length = 100)
	@Size (max = 100, message = "Related entity type cannot exceed 100 characters")
	@AMetaData (
			displayName = "Related Entity Type", required = false, readOnly = false, description = "Type of related entity for reference fields",
			hidden = false, order = 10, maxLength = 100
	)
	private String relatedEntityType;
	@Column (name = "relationFieldName", nullable = false, length = 100)
	@Size (max = 100, message = "Relation Field Name cannot exceed 100 characters")
	@AMetaData (
			displayName = "Relation Field", required = true, readOnly = false, description = "Relation Field is designed for", hidden = false,
			order = 2, maxLength = 100, dataProviderBean = "none"
	)
	private String relationFieldName;
	@Column (name = "isSectionAsTab", nullable = false)
	@AMetaData (
			displayName = "A Tab Section", required = false, readOnly = false, description = "Whether this section is a tab", hidden = false,
			order = 6, defaultValue = "false"
	)
	private Boolean sectionAsTab;
	@Column (name = "sectionName", nullable = true, length = 100)
	@Size (max = 100, message = "Name cannot exceed 100 characters")
	@AMetaData (
			displayName = "Field Property", required = false, readOnly = false, description = "Section of entries below", hidden = false, order = 3,
			maxLength = 100
	)
	private String sectionName;

	/** Default constructor for JPA. */
	public CDetailLines() {
		super(CDetailLines.class);
		sectionAsTab = false;
	}

	public CDetailLines(final CDetailSection detail, final String relationFieldName, final String entityProperty) {
		super(CDetailLines.class);
		sectionAsTab = false;
		detailSection = detail;
		this.relationFieldName = relationFieldName;
		this.entityProperty = entityProperty;
	}
	// Getters and Setters

	public String getDataProviderBean() { return dataProviderBean; }

	public String getDefaultValue() { return defaultValue; }

	public CDetailSection getDetailSection() { return detailSection; }

	public String getEntityProperty() { return entityProperty; }

	public String getFieldCaption() { return fieldCaption; }

	public String getFieldDescription() { return fieldDescription; }

	public Boolean getIsCaptionVisible() { return isCaptionVisible; }

	public Boolean getIsHidden() { return isHidden; }

	public Boolean getIsReadonly() { return isReadonly; }

	public Boolean getIsRequired() { return isRequired; }

	public Integer getLineOrder() { return lineOrder; }

	public Integer getMaxLength() { return maxLength; }

	public String getRelatedEntityType() { return relatedEntityType; }

	public String getRelationFieldName() { return relationFieldName; }

	public Boolean getSectionAsTab() { return sectionAsTab; }

	public String getSectionName() { return sectionName; }

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships
		if (detailSection != null) {
			detailSection.getName(); // Trigger detail section loading
		}
	}

	public void printLine() {
		System.out.println(
				"CDetailLines{" + "id=" + getId() + ", lineOrder=" + lineOrder + ", fieldCaption='" + fieldCaption + '\'' + ", entityProperty='"
						+ entityProperty + '\'' + ", relationFieldName='" + relationFieldName + '\'' + ", sectionName='" + sectionName + '\'' + '}');
	}

	public void setDataProviderBean(final String dataProviderBean) { this.dataProviderBean = dataProviderBean; }

	public void setDefaultValue(final String defaultValue) { this.defaultValue = defaultValue; }

	public void setDescription(final String fieldDescription) { this.fieldDescription = fieldDescription; }

	public void setDetailSection(final CDetailSection screen) { detailSection = screen; }

	public void setFieldCaption(final String fieldCaption) { this.fieldCaption = fieldCaption; }

	public void setIsCaptionVisible(Boolean isCaptionVisible) { this.isCaptionVisible = isCaptionVisible; }

	public void setIsHidden(final Boolean isHidden) { this.isHidden = isHidden; }

	public void setIsReadonly(final Boolean isReadonly) { this.isReadonly = isReadonly; }

	public void setIsRequired(final Boolean isRequired) { this.isRequired = isRequired; }

	public void setLineOrder(final Integer lineOrder) { this.lineOrder = lineOrder; }

	public void setMaxLength(final Integer maxLength) { this.maxLength = maxLength; }

	public void setEntityProperty(final String entityProperty) { this.entityProperty = entityProperty; }

	public void setRelatedEntityType(final String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

	public void setRelationFieldName(final String relationFieldName) { this.relationFieldName = relationFieldName; }

	public void setSectionAsTab(Boolean sectionAsTab) { this.sectionAsTab = sectionAsTab; }

	public void setSectionName(final String sectionName) {
		this.sectionName = sectionName;
		fieldCaption = sectionName;
	}

	@Override
	public String toString() {
		return String.format("CDetailLines{id=%d, lineOrder=%d, fieldCaption='%s', entityProperty='%s'}", getId(), lineOrder, fieldCaption,
				entityProperty);
	}
}
