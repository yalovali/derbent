package tech.derbent.plm.activities.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

/** CActivityPriority - Domain entity representing activity priority levels. Provides predefined priority levels for activity categorization and
 * workflow management. Layer: Domain (MVC) Standard priority levels: CRITICAL, HIGH, MEDIUM, LOW, LOWEST
 * @author Derbent Team
 * @since 1.0 */
@Entity
@Table (name = "cactivitypriority")
@AttributeOverride (name = "id", column = @Column (name = "cactivitypriority_id"))
public class CActivityPriority extends CTypeEntity<CActivityPriority> {

	public static final String DEFAULT_COLOR = "#4966B0"; // OpenWindows Selection Blue - activity priorities (matches CActivity)
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Activity Priorities";
	public static final String ENTITY_TITLE_SINGULAR = "Activity Priority";
	public static final String VIEW_NAME = "Activity Priority Management";
	/** Indicates if this is the default priority. */
	@Column (name = "is_default", nullable = false)
	@AMetaData (
			displayName = "Is Default", required = false, readOnly = false, defaultValue = "false",
			description = "Indicates if this is the default priority", hidden = false
	)
	private Boolean isDefault = false;
	/** Priority level for the activity (1=Highest, 5=Lowest). */
	@Column (name = "priority_level", nullable = false)
	@AMetaData (
			displayName = "Priority Level", required = false, readOnly = false, defaultValue = "3",
			description = "Priority level (1=Highest, 5=Lowest)", hidden = false
	)
	private Integer priorityLevel = 3;

	/** Default constructor for JPA. */
	public CActivityPriority() {
		super();
		initializeDefaults();
	}

	/** Constructor with required fields only.
	 * @param name    the name of the activity priority (e.g., "HIGH", "MEDIUM")
	 * @param company the company this priority belongs to */
	public CActivityPriority(final String name, final CCompany company) {
		super(CActivityPriority.class, name, company);
		initializeDefaults();
	}

	/** Constructor with all common fields.
	 * @param name      the name of the activity priority
	 * @param company   the company this priority belongs to
	 * @param color     the hex color code for UI display
	 * @param sortOrder the display order */
	public CActivityPriority(final String name, final CCompany company, final String color, final Integer sortOrder) {
		super(CActivityPriority.class, name, company);
		initializeDefaults();
	}

	/** Gets the default status of this priority.
	 * @return true if this is the default priority, false otherwise */
	public Boolean getIsDefault() { return isDefault; }

	public Integer getPriorityLevel() { return priorityLevel; }

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		priorityLevel = 3;
		isDefault = false;
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Convenience method to check if this is the default priority.
	 * @return true if this is the default priority, false otherwise */
	public boolean isDefault() { return Boolean.TRUE.equals(isDefault); }

	/** Sets the default status of this priority.
	 * @param isDefault true to set as default, false otherwise */
	public void setIsDefault(final Boolean isDefault) { this.isDefault = isDefault; }

	public void setPriorityLevel(final Integer priorityLevel) { this.priorityLevel = priorityLevel; }

	@Override
	public String toString() {
		return String.format("CActivityPriority{id=%d, name='%s', color='%s', sortOrder=%d, active=%s, company=%s, priorityLevel=%d, isDefault=%s}",
				getId(), getName(), getColor(), getSortOrder(), getActive(), getCompany() != null ? getCompany().getName() : "null", priorityLevel,
				isDefault);
	}
}
