package tech.derbent.comments.domain;

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
 * CCommentPriority - Domain entity representing comment priority levels.
 * Layer: Domain (MVC)
 * 
 * Inherits from CTypeEntity to provide type functionality for comment priorities.
 * Supports priority-based comment management with levels like LOW, NORMAL, HIGH, URGENT.
 */
@Entity
@Table(name = "ccommentpriority")
@AttributeOverride(name = "id", column = @Column(name = "ccommentpriority_id"))
public class CCommentPriority extends CTypeEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(CCommentPriority.class);

    @Column(name = "priority_level", nullable = false)
    @MetaData(
        displayName = "Priority Level", required = true, readOnly = false,
        defaultValue = "2", description = "Numeric priority level (1=Highest, 4=Lowest)",
        hidden = false, order = 3
    )
    private Integer priorityLevel = 2; // Default to NORMAL priority

    @Column(name = "color", nullable = true, length = 7)
    @Size(max = 7)
    @MetaData(
        displayName = "Color", required = false, readOnly = false,
        defaultValue = "#0066CC",
        description = "Hex color code for priority visualization (e.g., #FF0000 for urgent)",
        hidden = false, order = 4, maxLength = 7
    )
    private String color = "#0066CC";

    @Column(name = "is_default", nullable = false)
    @MetaData(
        displayName = "Is Default Priority", required = true, readOnly = false,
        defaultValue = "false",
        description = "Indicates if this is the default priority for new comments",
        hidden = false, order = 5
    )
    private boolean isDefault = false;

    /**
     * Default constructor for JPA.
     */
    public CCommentPriority() {
        super();
    }

    /**
     * Constructor with name and priority level.
     * @param name the name of the comment priority - must not be null
     * @param priorityLevel the numeric priority level (1=Highest, 4=Lowest) - must not be null
     */
    public CCommentPriority(final String name, final Integer priorityLevel) {
        super(name);
        
        if (priorityLevel == null) {
            LOGGER.warn("CCommentPriority constructor - Priority level parameter is null");
        }
        this.priorityLevel = priorityLevel != null ? priorityLevel : 2;
    }

    /**
     * Constructor with name, priority level, and description.
     * @param name the name of the comment priority - must not be null
     * @param priorityLevel the numeric priority level (1=Highest, 4=Lowest) - must not be null
     * @param description description of the comment priority
     */
    public CCommentPriority(final String name, final Integer priorityLevel, final String description) {
        super(name, description);
        
        if (priorityLevel == null) {
            LOGGER.warn("CCommentPriority constructor - Priority level parameter is null");
        }
        this.priorityLevel = priorityLevel != null ? priorityLevel : 2;
    }

    public String getColor() {
        return color;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setColor(final String color) {
        LOGGER.info("setColor called with color: {}", color);
        this.color = color;
        updateLastModified();
    }

    public void setDefault(final boolean isDefault) {
        LOGGER.info("setDefault called with isDefault: {}", isDefault);
        this.isDefault = isDefault;
        updateLastModified();
    }

    public void setPriorityLevel(final Integer priorityLevel) {
        LOGGER.info("setPriorityLevel called with priorityLevel: {}", priorityLevel);
        
        if (priorityLevel == null) {
            LOGGER.warn("setPriorityLevel called with null priorityLevel");
            return;
        }
        
        this.priorityLevel = priorityLevel;
        updateLastModified();
    }

    @Override
    protected void initializeDefaults() {
        super.initializeDefaults();
        
        if (this.priorityLevel == null) {
            this.priorityLevel = 2; // Default to NORMAL priority
        }
        
        if (this.color == null || this.color.trim().isEmpty()) {
            this.color = "#0066CC"; // Default blue color
        }
    }

    @Override
    public String toString() {
        return String.format("%s (Level: %d, Color: %s)", getName(), priorityLevel, color);
    }
}