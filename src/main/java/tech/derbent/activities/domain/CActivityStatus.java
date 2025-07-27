package tech.derbent.activities.domain;

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
@Table (name = "cactivitystatus")
@AttributeOverride (name = "id", column = @Column (name = "cactivitystatus_id"))
public class CActivityStatus extends CStatus {

	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@MetaData (
		displayName = "Color", required = false, readOnly = false,
		defaultValue = "#808080",
		description = "Hex color code for status visualization (e.g., #FF0000)",
		hidden = false, order = 3, maxLength = 7
	)
	private String color = "#808080";

	@Column (name = "is_final", nullable = false)
	@MetaData (
		displayName = "Is Final Status", required = true, readOnly = false,
		defaultValue = "false",
		description = "Indicates if this is a final status (completed/cancelled)",
		hidden = false, order = 4
	)
	private boolean isFinal = false;

	@Column (name = "sort_order", nullable = false)
	@MetaData (
		displayName = "Sort Order", required = true, readOnly = false,
		defaultValue = "100", description = "Display order for status sorting",
		hidden = false, order = 5
	)
	private Integer sortOrder = 100;

	/**
	 * Default constructor for JPA.
	 */
	public CActivityStatus() {
		super();
		// logger.debug("CActivityStatus() - Creating new activity status instance");
	}

	public CActivityStatus(final String name) {
		super(name);
	}

	public CActivityStatus(final String name, final String description) {
		super(name, description);
	}

	public CActivityStatus(final String name, final String description,
		final String color, final boolean isFinal) {
		super(name, description);
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

	public String getColor() {
		return ((color != null) && !color.trim().isEmpty()) ? color : "#808080";
	}

	public Integer getSortOrder() { return sortOrder != null ? sortOrder : 100; }

	public boolean isFinal() { return isFinal; }

	public void setColor(final String color) {
		this.color = ((color != null) && !color.trim().isEmpty()) ? color : "#808080";
	}

	public void setFinal(final boolean isFinal) { this.isFinal = isFinal; }

	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder != null ? sortOrder : 100;
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}