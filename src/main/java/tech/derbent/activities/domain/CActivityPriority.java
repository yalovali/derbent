package tech.derbent.activities.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CTypeEntity;

/**
 * CActivityPriority - Domain entity representing activity priority levels. Layer: Domain
 * (MVC) Inherits from CTypeEntity to provide type functionality for activity priorities.
 * Supports priority-based task management with levels like LOW, MEDIUM, HIGH, CRITICAL,
 * URGENT.
 */
@Entity
@Table (name = "cactivitypriority")
@AttributeOverride (name = "id", column = @Column (name = "cactivitypriority_id"))
public class CActivityPriority extends CTypeEntity {

	private static final Logger logger = LoggerFactory.getLogger(CActivityPriority.class);

	@Column (name = "priority_level", nullable = false)
	@MetaData (
		displayName = "Priority Level", required = true, readOnly = false,
		defaultValue = "3", description = "Numeric priority level (1=Highest, 5=Lowest)",
		hidden = false, order = 3
	)
	private Integer priorityLevel = 3;

	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@MetaData (
		displayName = "Color", required = false, readOnly = false,
		defaultValue = "#FFA500",
		description = "Hex color code for priority visualization (e.g., #FF0000 for critical)",
		hidden = false, order = 4, maxLength = 7
	)
	private String color = "#FFA500";

	@Column (name = "is_default", nullable = false)
	@MetaData (
		displayName = "Is Default Priority", required = true, readOnly = false,
		defaultValue = "false",
		description = "Indicates if this is the default priority for new activities",
		hidden = false, order = 5
	)
	private boolean isDefault = false;

	/**
	 * Default constructor for JPA.
	 */
	public CActivityPriority() {
		super();
		logger.debug("CActivityPriority() - Creating new activity priority instance");
	}

	/**
	 * Constructor with name and priority level.
	 * @param name          the name of the activity priority - must not be null
	 * @param priorityLevel the numeric priority level (1=Highest, 5=Lowest) - must not be
	 *                      null
	 */
	public CActivityPriority(final String name, final Integer priorityLevel) {
		super(name);
		logger.debug(
			"CActivityPriority(name={}, priorityLevel={}) - Creating activity priority",
			name, priorityLevel);

		if (priorityLevel == null) {
			logger
				.warn("CActivityPriority constructor - Priority level parameter is null");
		}
		this.priorityLevel = priorityLevel != null ? priorityLevel : 3;
	}

	/**
	 * Constructor with name, priority level, and description.
	 * @param name          the name of the activity priority - must not be null
	 * @param priorityLevel the numeric priority level (1=Highest, 5=Lowest) - must not be
	 *                      null
	 * @param description   the description of the activity priority - can be null
	 */
	public CActivityPriority(final String name, final Integer priorityLevel,
		final String description) {
		super(name, description);
		logger.debug(
			"CActivityPriority(name={}, priorityLevel={}, description={}) - Creating activity priority with description",
			name, priorityLevel, description);

		if (priorityLevel == null) {
			logger
				.warn("CActivityPriority constructor - Priority level parameter is null");
		}
		this.priorityLevel = priorityLevel != null ? priorityLevel : 3;
	}

	/**
	 * Constructor with all main fields.
	 * @param name          the name of the activity priority - must not be null
	 * @param priorityLevel the numeric priority level (1=Highest, 5=Lowest) - must not be
	 *                      null
	 * @param description   the description of the activity priority - can be null
	 * @param color         the hex color code - can be null, defaults to orange
	 * @param isDefault     whether this is the default priority
	 */
	public CActivityPriority(final String name, final Integer priorityLevel,
		final String description, final String color, final boolean isDefault) {
		super(name, description);
		logger.debug(
			"CActivityPriority(name={}, priorityLevel={}, description={}, color={}, isDefault={}) - Creating full activity priority",
			name, priorityLevel, description, color, isDefault);

		if (priorityLevel == null) {
			logger
				.warn("CActivityPriority constructor - Priority level parameter is null");
		}
		this.priorityLevel = priorityLevel != null ? priorityLevel : 3;
		this.color = color != null ? color : "#FFA500";
		this.isDefault = isDefault;
	}

	/**
	 * Gets the color hex code for this priority.
	 * @return the color hex code (e.g., "#FF0000") or default orange if null
	 */
	public String getColor() { return color != null ? color : "#FFA500"; }

	/**
	 * Gets the numeric priority level.
	 * @return the priority level (1=Highest, 5=Lowest)
	 */
	public Integer getPriorityLevel() {
		return priorityLevel != null ? priorityLevel : 3;
	}

	/**
	 * Checks if this is the default priority.
	 * @return true if this is the default priority for new activities
	 */
	public boolean isDefault() { return isDefault; }

	/**
	 * Checks if this is a high priority (level 1 or 2).
	 * @return true if priority level is 1 or 2
	 */
	public boolean isHighPriority() {
		final Integer level = getPriorityLevel();
		return (level != null) && (level <= 2);
	}

	/**
	 * Checks if this is a low priority (level 4 or 5).
	 * @return true if priority level is 4 or 5
	 */
	public boolean isLowPriority() {
		final Integer level = getPriorityLevel();
		return (level != null) && (level >= 4);
	}

	/**
	 * Sets the color hex code for this priority.
	 * @param color the hex color code (e.g., "#FF0000") - if null, defaults to orange
	 */
	public void setColor(final String color) {
		logger.debug("setColor(color={}) - Setting color for priority id={}", color,
			getId());
		this.color = color != null ? color : "#FFA500";
	}

	/**
	 * Sets whether this is the default priority.
	 * @param isDefault true if this should be the default priority for new activities
	 */
	public void setDefault(final boolean isDefault) {
		logger.debug("setDefault(isDefault={}) - Setting default flag for priority id={}",
			isDefault, getId());
		this.isDefault = isDefault;
	}

	/**
	 * Sets the numeric priority level.
	 * @param priorityLevel the priority level (1=Highest, 5=Lowest) - if null, defaults
	 *                      to 3
	 */
	public void setPriorityLevel(final Integer priorityLevel) {
		logger.debug(
			"setPriorityLevel(priorityLevel={}) - Setting priority level for priority id={}",
			priorityLevel, getId());

		if (priorityLevel == null) {
			logger.warn(
				"setPriorityLevel(priorityLevel=null) - Attempting to set null priority level for priority id={}",
				getId());
		}
		this.priorityLevel = priorityLevel != null ? priorityLevel : 3;
	}
}