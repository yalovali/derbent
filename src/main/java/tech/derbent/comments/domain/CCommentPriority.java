package tech.derbent.comments.domain;

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
 * CCommentPriority - Domain entity representing comment priority levels. Provides
 * priority classification for comments to support workflow and escalation management.
 * Layer: Domain (MVC) Standard priority levels: URGENT, HIGH, NORMAL, LOW, INFO
 * @author Derbent Team
 * @since 1.0
 */
@Entity
@Table (name = "ccommentpriority")
@AttributeOverride (name = "id", column = @Column (name = "ccommentpriority_id"))
public class CCommentPriority extends CTypeEntity {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCommentPriority.class);

	/**
	 * Priority level for the comment (1=Highest, 4=Lowest).
	 */
	@Column (name = "priority_level", nullable = false, length = 20)
	@MetaData (
		displayName = "Priority Level", required = false, readOnly = false,
		defaultValue = "3", description = "Priority level of the comment", hidden = false,
		order = 2, useRadioButtons = false
	)
	private Integer priorityLevel = 3; // Default to normal priority

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
	 * Default constructor for JPA.
	 */
	public CCommentPriority() {
		super();
		LOGGER.debug("CCommentPriority default constructor called");
	}

	/**
	 * Constructor with required fields only.
	 * @param name    the name of the comment priority (e.g., "URGENT", "NORMAL")
	 * @param project the project this priority belongs to
	 */
	public CCommentPriority(final String name, final CProject project) {
		super(name, project);
		LOGGER.debug("CCommentPriority constructor called with name: {} and project: {}",
			name, project);
	}

	/**
	 * Constructor with name and priority level.
	 * @param name the name of the comment priority
	 * @param priorityLevel the priority level (1=Highest, 4=Lowest)
	 */
	public CCommentPriority(final String name, final Integer priorityLevel) {
		super();
		setName(name);
		this.priorityLevel = priorityLevel;
		LOGGER.debug("CCommentPriority constructor called with name: {} and priorityLevel: {}",
			name, priorityLevel);
	}

	/**
	 * Constructor with all common fields.
	 * @param name      the name of the comment priority
	 * @param project   the project this priority belongs to
	 * @param color     the hex color code for UI display
	 * @param sortOrder the display order
	 */
	public CCommentPriority(final String name, final CProject project, final String color,
		final Integer sortOrder) {
		super(name, project);
		setColor(color);
		setSortOrder(sortOrder);
		LOGGER.debug(
			"CCommentPriority constructor called with name: {}, project: {}, color: {}, sortOrder: {}",
			name, project, color, sortOrder);
	}

	public Integer getPriorityLevel() { return priorityLevel; }

	public void setPriorityLevel(final Integer priorityLevel) {
		this.priorityLevel = priorityLevel;
	}

	/**
	 * Gets the default status of this priority.
	 * 
	 * @return true if this is the default priority, false otherwise
	 */
	public Boolean getIsDefault() {
		return isDefault;
	}

	/**
	 * Sets the default status of this priority.
	 * 
	 * @param isDefault true to set as default, false otherwise
	 */
	public void setDefault(final Boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * Convenience method to check if this is the default priority.
	 * 
	 * @return true if this is the default priority, false otherwise
	 */
	public boolean isDefault() {
		return Boolean.TRUE.equals(isDefault);
	}

	@Override
	public String toString() {
		return String.format(
			"CCommentPriority{id=%d, name='%s', color='%s', sortOrder=%d, isActive=%s, project=%s, priorityLevel=%d, isDefault=%s}",
			getId(), getName(), getColor(), getSortOrder(), getIsActive(),
			getProject() != null ? getProject().getName() : "null", priorityLevel, isDefault);
	}
}