package tech.derbent.activities.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

/**
 * CActivityPriority - Domain entity representing activity priority levels. Provides
 * predefined priority levels for activity categorization and workflow management. Layer:
 * Domain (MVC) Standard priority levels: CRITICAL, HIGH, MEDIUM, LOW, LOWEST
 * @author Derbent Team
 * @since 1.0
 */
@Entity
@Table (name = "cactivitypriority")
@AttributeOverride (name = "id", column = @Column (name = "cactivitypriority_id"))
public class CActivityPriority extends CTypeEntity<CActivityPriority> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CActivityPriority.class);

	/**
	 * Priority level for the activity (1=Highest, 5=Lowest).
	 */
	@Column (name = "priority_level", nullable = false)
	@MetaData (
		displayName = "Priority Level", required = false, readOnly = false,
		defaultValue = "3", description = "Priority level (1=Highest, 5=Lowest)",
		hidden = false, order = 2
	)
	private Integer priorityLevel = 3;

	/**
	 * Indicates if this is the default priority.
	 */
	@Column (name = "is_default", nullable = false)
	@MetaData (
		displayName = "Is Default", required = false, readOnly = false,
		defaultValue = "false", description = "Indicates if this is the default priority",
		hidden = false, order = 7
	)
	private Boolean isDefault = false;

	/**
	 * Constructor with required fields only.
	 * @param name    the name of the activity priority (e.g., "HIGH", "MEDIUM")
	 * @param project the project this priority belongs to
	 */
	public CActivityPriority(final String name, final CProject project) {
		super(CActivityPriority.class, name, project);
		LOGGER.debug("CActivityPriority constructor called with name: {} and project: {}",
			name, project);
	}

	/**
	 * Constructor with all common fields.
	 * @param name      the name of the activity priority
	 * @param project   the project this priority belongs to
	 * @param color     the hex color code for UI display
	 * @param sortOrder the display order
	 */
	public CActivityPriority(final String name, final CProject project,
		final String color, final Integer sortOrder) {
		super(CActivityPriority.class, name, project);
		setColor(color);
		setSortOrder(sortOrder);
		LOGGER.debug(
			"CActivityPriority constructor called with name: {}, project: {}, color: {}, sortOrder: {}",
			name, project, color, sortOrder);
	}

	/**
	 * Gets the default status of this priority.
	 * @return true if this is the default priority, false otherwise
	 */
	public Boolean getIsDefault() { return isDefault; }

	public Integer getPriorityLevel() { return priorityLevel; }

	/**
	 * Convenience method to check if this is the default priority.
	 * @return true if this is the default priority, false otherwise
	 */
	public boolean isDefault() { return Boolean.TRUE.equals(isDefault); }

	/**
	 * Sets the default status of this priority.
	 * @param isDefault true to set as default, false otherwise
	 */
	public void setIsDefault(final Boolean isDefault) { this.isDefault = isDefault; }

	public void setPriorityLevel(final Integer priorityLevel) {
		this.priorityLevel = priorityLevel;
	}

	@Override
	public String toString() {
		return String.format(
			"CActivityPriority{id=%d, name='%s', color='%s', sortOrder=%d, isActive=%s, project=%s, priorityLevel=%d, isDefault=%s}",
			getId(), getName(), getColor(), getSortOrder(), getIsActive(),
			getProject() != null ? getProject().getName() : "null", priorityLevel,
			isDefault);
	}
}