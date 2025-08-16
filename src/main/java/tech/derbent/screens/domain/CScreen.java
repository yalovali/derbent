package tech.derbent.screens.domain;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;

/**
 * CScreen - Domain entity representing screen views for entities.
 * Layer: Domain (MVC)
 * Inherits from CEntityOfProject to provide project association.
 * This entity allows creating custom view definitions for various project entities.
 */
@Entity
@Table(name = "cscreen")
@AttributeOverride(name = "id", column = @Column(name = "screen_id"))
public class CScreen extends CEntityOfProject<CScreen> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CScreen.class);

    public static String getIconColorCode() {
        return "#6f42c1"; // Purple color for screen entities
    }

    public static String getIconFilename() {
        return "vaadin:viewport";
    }

    @Column(name = "entity_type", nullable = false, length = 100)
    @Size(max = 100, message = "Entity type cannot exceed 100 characters")
    @MetaData(
        displayName = "Entity Type", 
        required = true, 
        readOnly = false,
        description = "Type of entity this screen is designed for", 
        hidden = false, 
        order = 2,
        maxLength = 100
    )
    private String entityType;

    @Column(name = "screen_title", nullable = true, length = 255)
    @Size(max = 255, message = "Screen title cannot exceed 255 characters")
    @MetaData(
        displayName = "Screen Title", 
        required = false, 
        readOnly = false,
        description = "Title to display for this screen view", 
        hidden = false, 
        order = 3,
        maxLength = 255
    )
    private String screenTitle;

    @Column(name = "header_text", nullable = true, length = 500)
    @Size(max = 500, message = "Header text cannot exceed 500 characters")
    @MetaData(
        displayName = "Header Text", 
        required = false, 
        readOnly = false,
        description = "Header text to display at the top of the screen", 
        hidden = false, 
        order = 4,
        maxLength = 500
    )
    private String headerText;

    // Relations to project entities - only one can be set at a time
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_activity_id", nullable = true)
    @MetaData(
        displayName = "Related Activity", 
        required = false, 
        readOnly = false,
        description = "Activity this screen is related to", 
        hidden = false, 
        order = 10,
        dataProviderBean = "CActivityService"
    )
    private CActivity relatedActivity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_meeting_id", nullable = true)
    @MetaData(
        displayName = "Related Meeting", 
        required = false, 
        readOnly = false,
        description = "Meeting this screen is related to", 
        hidden = false, 
        order = 11,
        dataProviderBean = "CMeetingService"
    )
    private CMeeting relatedMeeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_risk_id", nullable = true)
    @MetaData(
        displayName = "Related Risk", 
        required = false, 
        readOnly = false,
        description = "Risk this screen is related to", 
        hidden = false, 
        order = 12,
        dataProviderBean = "CRiskService"
    )
    private CRisk relatedRisk;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("lineOrder ASC")
    private List<CScreenLines> screenLines = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @MetaData(
        displayName = "Active", 
        required = false, 
        readOnly = false,
        description = "Whether this screen definition is active", 
        hidden = false, 
        order = 20,
        defaultValue = "true"
    )
    private Boolean isActive = true;

    /**
     * Default constructor for JPA.
     */
    public CScreen() {
        super();
    }

    public CScreen(final String name, final CProject project) {
        super(CScreen.class, name, project);
    }

    // Getters and Setters

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getScreenTitle() {
        return screenTitle;
    }

    public void setScreenTitle(String screenTitle) {
        this.screenTitle = screenTitle;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public CActivity getRelatedActivity() {
        return relatedActivity;
    }

    public void setRelatedActivity(CActivity relatedActivity) {
        this.relatedActivity = relatedActivity;
    }

    public CMeeting getRelatedMeeting() {
        return relatedMeeting;
    }

    public void setRelatedMeeting(CMeeting relatedMeeting) {
        this.relatedMeeting = relatedMeeting;
    }

    public CRisk getRelatedRisk() {
        return relatedRisk;
    }

    public void setRelatedRisk(CRisk relatedRisk) {
        this.relatedRisk = relatedRisk;
    }

    public List<CScreenLines> getScreenLines() {
        return screenLines;
    }

    public void setScreenLines(List<CScreenLines> screenLines) {
        this.screenLines = screenLines;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Helper method to add a screen line
     */
    public void addScreenLine(CScreenLines screenLine) {
        screenLines.add(screenLine);
        screenLine.setScreen(this);
    }

    /**
     * Helper method to remove a screen line
     */
    public void removeScreenLine(CScreenLines screenLine) {
        screenLines.remove(screenLine);
        screenLine.setScreen(null);
    }

    @Override
    public String toString() {
        return String.format("CScreen{id=%d, name='%s', entityType='%s', screenTitle='%s'}", 
                getId(), getName(), entityType, screenTitle);
    }
}