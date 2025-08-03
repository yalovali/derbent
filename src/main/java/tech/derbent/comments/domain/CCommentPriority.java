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
 * CCommentPriority - Domain entity representing comment priority levels. Provides priority classification for comments
 * to support workflow and escalation management. Layer: Domain (MVC) Standard priority levels: URGENT, HIGH, NORMAL,
 * LOW, INFO
 * 
 * @author Derbent Team
 * @since 1.0
 */
@Entity
@Table(name = "ccommentpriority")
@AttributeOverride(name = "id", column = @Column(name = "ccommentpriority_id"))
public class CCommentPriority extends CTypeEntity<CCommentPriority> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCommentPriority.class);

    @Column(name = "priority_level", nullable = false, length = 20)
    @MetaData(displayName = "Priority Level", required = false, readOnly = false, defaultValue = "3", description = "Priority level of the comment", hidden = false, order = 2, useRadioButtons = false)
    private Integer priorityLevel = 3; // Default to normal priority

    @Column(name = "is_default", nullable = false)
    @MetaData(displayName = "Is Default", required = false, readOnly = false, defaultValue = "false", description = "Indicates if this is the default priority", hidden = false, order = 7)
    private Boolean isDefault = false;

    /**
     * Default constructor for JPA.
     */
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
        LOGGER.debug("CCommentPriority constructor called with name: {}, project: {}, color: {}, sortOrder: {}", name,
                project, color, sortOrder);
    }

    /**
     * Gets the default status of this priority.
     * 
     * @return true if this is the default priority, false otherwise
     */
    public Boolean getIsDefault() {
        return isDefault;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    /**
     * Convenience method to check if this is the default priority.
     * 
     * @return true if this is the default priority, false otherwise
     */
    public boolean isDefault() {
        return Boolean.TRUE.equals(isDefault);
    }

    /**
     * Sets the default status of this priority.
     * 
     * @param isDefault
     *            true to set as default, false otherwise
     */
    public void setDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setPriorityLevel(final Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    @Override
    public String toString() {
        return String.format(
                "CCommentPriority{id=%d, name='%s', color='%s', sortOrder=%d, isActive=%s, project=%s, priorityLevel=%d, isDefault=%s}",
                getId(), getName(), getColor(), getSortOrder(), getIsActive(),
                getProject() != null ? getProject().getName() : "null", priorityLevel, isDefault);
    }
}