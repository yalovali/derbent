package tech.derbent.decisions.domain;

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
 * CDecisionStatus - Domain entity representing decision status types. Layer: Domain (MVC)
 * Inherits from CStatus to provide status functionality for decisions. This entity
 * defines the possible statuses a decision can have (e.g., DRAFT, UNDER_REVIEW, APPROVED,
 * REJECTED, IMPLEMENTED).
 */
@Entity
@Table (name = "cdecisionstatus")
@AttributeOverride (name = "id", column = @Column (name = "decision_status_id"))
public class CDecisionStatus extends CStatus {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionStatus.class);

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
		description = "Indicates if this is a final status (implemented/rejected)",
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

	@Column (name = "allows_editing", nullable = false)
	@MetaData (
		displayName = "Allows Editing", required = true, readOnly = false,
		defaultValue = "true",
		description = "Whether decisions with this status can be edited", hidden = false,
		order = 6
	)
	private boolean allowsEditing = true;

	@Column (name = "requires_approval", nullable = false)
	@MetaData (
		displayName = "Requires Approval", required = true, readOnly = false,
		defaultValue = "false",
		description = "Whether decisions with this status require approval to proceed",
		hidden = false, order = 7
	)
	private boolean requiresApproval = false;

	/**
	 * Default constructor for JPA.
	 */
	public CDecisionStatus() {
		super();
	}

	/**
	 * Constructor with name.
	 * @param name the name of the decision status - must not be null or empty
	 */
	public CDecisionStatus(final String name) {
		super(name);

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("CDecisionStatus constructor - Name parameter is null or empty");
		}
	}

	/**
	 * Constructor with name and description.
	 * @param name        the name of the decision status - must not be null or empty
	 * @param description detailed description of the decision status - can be null
	 */
	public CDecisionStatus(final String name, final String description) {
		super(name, description);
	}

	/**
	 * Constructor with all main fields.
	 * @param name             the name of the decision status - must not be null or empty
	 * @param description      detailed description of the decision status - can be null
	 * @param color            the hex color code - can be null, defaults to gray
	 * @param isFinal          whether this is a final status
	 * @param allowsEditing    whether decisions with this status can be edited
	 * @param requiresApproval whether decisions with this status require approval
	 */
	public CDecisionStatus(final String name, final String description,
		final String color, final boolean isFinal, final boolean allowsEditing,
		final boolean requiresApproval) {
		super(name, description);
		LOGGER.debug(
			"CDecisionStatus constructor called with name: {}, description: {}, color: {}, isFinal: {}, allowsEditing: {}, requiresApproval: {}",
			name, description, color, isFinal, allowsEditing, requiresApproval);
		this.color = color != null ? color : "#808080";
		this.isFinal = isFinal;
		this.allowsEditing = allowsEditing;
		this.requiresApproval = requiresApproval;
	}
	// Getters and Setters

	/**
	 * Checks if decisions with this status can be modified.
	 * @return true if editing is allowed and status is not final
	 */
	public boolean canBeModified() {
		return allowsEditing && !isFinal;
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof CDecisionStatus)) {
			return false;
		}
		return super.equals(o);
	}

	public String getColor() {
		return (color != null) && !color.trim().isEmpty() ? color : "#808080";
	}

	public Integer getSortOrder() { return sortOrder != null ? sortOrder : 100; }

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public boolean isAllowsEditing() { return allowsEditing; }

	/**
	 * Checks if this status indicates completion of the decision process.
	 * @return true if this is a final status
	 */
	public boolean isCompleted() { return isFinal; }

	public boolean isFinal() { return isFinal; }

	/**
	 * Checks if decisions with this status are pending approval.
	 * @return true if approval is required and status is not final
	 */
	public boolean isPendingApproval() { return requiresApproval && !isFinal; }

	public boolean isRequiresApproval() { return requiresApproval; }
	// Business Logic Methods

	public void setAllowsEditing(final boolean allowsEditing) {
		this.allowsEditing = allowsEditing;
		updateLastModified();
	}

	public void setColor(final String color) {
		this.color = (color != null) && !color.trim().isEmpty() ? color : "#808080";
		updateLastModified();
	}

	public void setFinal(final boolean isFinal) {
		this.isFinal = isFinal;
		updateLastModified();
	}

	public void setRequiresApproval(final boolean requiresApproval) {
		this.requiresApproval = requiresApproval;
		updateLastModified();
	}

	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder != null ? sortOrder : 100;
		updateLastModified();
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}