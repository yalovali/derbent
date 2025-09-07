package tech.derbent.comments.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.comments.view.CCommentPriorityView;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "ccommentpriority")
@AttributeOverride (name = "id", column = @Column (name = "ccommentpriority_id"))
public class CCommentPriority extends CTypeEntity<CCommentPriority> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCommentPriority.class);

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return "#FF9800"; // Default color for comment priority icon
	}

	public static String getIconFileName() { return "vaadin:exclamation-circle"; }

	public static Class<?> getViewClassStatic() { return CCommentPriorityView.class; }

	@Column (name = "priority_level", nullable = false, length = 20)
	@AMetaData (
			displayName = "Priority Level", required = false, readOnly = false, defaultValue = "3", description = "Priority level of the comment",
			hidden = false, order = 2, useRadioButtons = false, setBackgroundFromColor = true
	)
	private Integer priorityLevel = 3; // Default to normal priority
	@Column (name = "is_default", nullable = false)
	@AMetaData (
			displayName = "Is Default", required = false, readOnly = false, defaultValue = "false",
			description = "Indicates if this is the default priority", hidden = false, order = 7
	)
	private Boolean isDefault = false;

	/** Default constructor for JPA. */
	public CCommentPriority() {
		super();
		// Initialize with default values for JPA
		this.priorityLevel = 3;
		this.isDefault = false;
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

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
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
}
