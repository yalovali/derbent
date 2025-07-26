package tech.derbent.activities.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.base.domain.CStatus;

/**
 * CActivityStatus - Domain entity representing activity status types. Layer: Domain (MVC)
 * Inherits from CStatus to provide status functionality for activities. This entity
 * defines the possible statuses an activity can have (e.g., TODO, IN_PROGRESS, DONE).
 */
@Entity
@Table(name = "cactivitystatus")
@AttributeOverride(name = "id", column = @Column(name = "cactivitystatus_id"))
public class CActivityStatus extends CStatus {

	private static final Logger logger = LoggerFactory.getLogger(CActivityStatus.class);
	@Column(name = "color", nullable = true, length = 7)
	@Size(max = 7)
	@MetaData(displayName = "Color", required = false, readOnly = false,
		defaultValue = "#808080",
		description = "Hex color code for status visualization (e.g., #FF0000)",
		hidden = false, order = 3, maxLength = 7)
	private String color = "#808080";
	
	@Column(name = "is_final", nullable = false)
	@MetaData(displayName = "Is Final Status", required = true, readOnly = false,
		defaultValue = "false",
		description = "Indicates if this is a final status (completed/cancelled)",
		hidden = false, order = 4)
	private boolean isFinal = false;
	
	@Column(name = "sort_order", nullable = false)
	@MetaData(displayName = "Sort Order", required = true, readOnly = false,
		defaultValue = "100", description = "Display order for status sorting",
		hidden = false, order = 5)
	private Integer sortOrder = 100;

	/**
	 * Default constructor for JPA.
	 */
	public CActivityStatus() {
		super();
		logger.debug("CActivityStatus() - Creating new activity status instance");
	}

	/**
	 * Constructor with name.
	 * @param name the name of the activity status - must not be null
	 */
	public CActivityStatus(final String name) {
		super(name);
		logger.debug("CActivityStatus(name={}) - Creating activity status with name",
			name);
	}

	/**
	 * Constructor with name and description.
	 * @param name        the name of the activity status - must not be null
	 * @param description the description of the activity status - can be null
	 */
	public CActivityStatus(final String name, final String description) {
		super(name, description);
		logger.debug(
			"CActivityStatus(name={}, description={}) - Creating activity status with name and description",
			name, description);
	}

	/**
	 * Constructor with all main fields.
	 * @param name        the name of the activity status - must not be null
	 * @param description the description of the activity status - can be null
	 * @param color       the hex color code - can be null, defaults to gray
	 * @param isFinal     whether this is a final status
	 */
	public CActivityStatus(final String name, final String description,
		final String color, final boolean isFinal) {
		super(name, description);
		logger.debug(
			"CActivityStatus(name={}, description={}, color={}, isFinal={}) - Creating full activity status",
			name, description, color, isFinal);
		this.color = color != null ? color : "#808080";
		this.isFinal = isFinal;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CActivityStatus)) {
			return false;
		}
		final CActivityStatus that = (CActivityStatus) o;
		return super.equals(that);
	}

	/**
	 * Gets the color hex code for this status.
	 * @return the color hex code (e.g., "#FF0000") or default gray if null or empty
	 */
	public String getColor() {
		return ((color != null) && !color.trim().isEmpty()) ? color : "#808080";
	}

	/**
	 * Gets the sort order for this status.
	 * @return the sort order (higher numbers appear later)
	 */
	public Integer getSortOrder() { return sortOrder != null ? sortOrder : 100; }

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Checks if this is a final status (completed/cancelled).
	 * @return true if this status represents completion or cancellation
	 */
	public boolean isFinal() { return isFinal; }

	/**
	 * Sets the color hex code for this status.
	 * @param color the hex color code (e.g., "#FF0000") - if null or empty, defaults to
	 *              gray
	 */
	public void setColor(final String color) {
		logger.debug("setColor(color={}) - Setting color for status id={}", color,
			getId());
		this.color = ((color != null) && !color.trim().isEmpty()) ? color : "#808080";
	}

	/**
	 * Sets whether this is a final status.
	 * @param isFinal true if this status represents completion or cancellation
	 */
	public void setFinal(final boolean isFinal) {
		logger.debug("setFinal(isFinal={}) - Setting final flag for status id={}",
			isFinal, getId());
		this.isFinal = isFinal;
	}

	/**
	 * Sets the sort order for this status.
	 * @param sortOrder the sort order (higher numbers appear later) - if null, defaults
	 *                  to 100
	 */
	public void setSortOrder(final Integer sortOrder) {
		logger.debug("setSortOrder(sortOrder={}) - Setting sort order for status id={}",
			sortOrder, getId());
		this.sortOrder = sortOrder != null ? sortOrder : 100;
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}