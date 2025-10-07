package tech.derbent.comments.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "ccommentpriority")
@AttributeOverride (name = "id", column = @Column (name = "ccommentpriority_id"))
public class CCommentPriority extends CTypeEntity<CCommentPriority> {

	public static final String DEFAULT_COLOR = "#ffc107";
	public static final String DEFAULT_ICON = "vaadin:star";
	private static final Logger LOGGER = LoggerFactory.getLogger(CCommentPriority.class);
	public static final String VIEW_NAME = "Comment Priority View";
	@Column (name = "is_default", nullable = false)
	@AMetaData (
			displayName = "Is Default", required = false, readOnly = false, defaultValue = "false",
			description = "Indicates if this is the default priority", hidden = false, order = 7
	)
	private Boolean isDefault = false;
	@Column (name = "priority_level", nullable = false, length = 20)
	@AMetaData (
			displayName = "Priority Level", required = false, readOnly = false, defaultValue = "3", description = "Priority level of the comment",
			hidden = false, order = 2, useRadioButtons = false, setBackgroundFromColor = true
	)
	private Integer priorityLevel = 3; // Default to normal priority

	/** Default constructor for JPA. */
	public CCommentPriority() {
		super();
		// Initialize with default values for JPA
		priorityLevel = 3;
		isDefault = false;
	}

	public CCommentPriority(final String name, final CProject project) {
		super(CCommentPriority.class, name, project);
		LOGGER.debug("CCommentPriority constructor called with name: {} and project: {}", name, project);
	}

	public CCommentPriority(final String name, final CProject project, final String color, final Integer sortOrder) {
		super(CCommentPriority.class, name, project);
		setColor(color);
		setSortOrder(sortOrder);
	}

	public Boolean getIsDefault() { return isDefault; }

	public Integer getPriorityLevel() { return priorityLevel; }

	public boolean isDefault() { return Boolean.TRUE.equals(isDefault); }

	public void setDefault(final Boolean isDefault) { this.isDefault = isDefault; }

	public void setPriorityLevel(final Integer priorityLevel) { this.priorityLevel = priorityLevel; }

	@Override
	public String toString() {
		return String.format("CCommentPriority{id=%d, name='%s', color='%s', sortOrder=%d, isActive=%s, project=%s, priorityLevel=%d, isDefault=%s}",
				getId(), getName(), getColor(), getSortOrder(), getIsActive(), getProject() != null ? getProject().getName() : "null", priorityLevel,
				isDefault);
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships from parent class (CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}
}
