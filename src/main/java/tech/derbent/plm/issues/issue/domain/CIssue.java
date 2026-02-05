package tech.derbent.plm.issues.issue.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.gannt.ganntitem.service.IGanntEntityItem;
import tech.derbent.plm.issues.issuetype.domain.CIssueType;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.sprints.domain.CSprintItem;

@Entity
@Table (name = "cissue")
@AttributeOverride (name = "id", column = @Column (name = "issue_id"))
public class CIssue extends CProjectItem<CIssue>
		implements IHasStatusAndWorkflow<CIssue>, IGanntEntityItem, ISprintableItem, IHasIcon, IHasAttachments, IHasComments, IHasLinks {

	public static final String DEFAULT_COLOR = "#D32F2F"; // Red for issues/bugs
	public static final String DEFAULT_ICON = "vaadin:bug";
	public static final String ENTITY_TITLE_PLURAL = "Issues";
	public static final String ENTITY_TITLE_SINGULAR = "Issue";
	private static final Logger LOGGER = LoggerFactory.getLogger(CIssue.class);
	public static final String VIEW_NAME = "Issues View";
	// Actual Result
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Actual Result", required = false, readOnly = false, defaultValue = "", description = "Actual behavior or result observed",
			hidden = false, maxLength = 2000
	)
	private String actualResult;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "issue_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this issue", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "issue_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this issue", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	// Due Date
	@Column (name = "due_date", nullable = true)
	@AMetaData (displayName = "Due Date", required = false, readOnly = false, description = "Target date for resolving the issue", hidden = false)
	private LocalDate dueDate;
	// Issue Type
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Issue Type", required = false, readOnly = false, description = "Type category of the issue (Bug, Improvement, Task)",
			hidden = false, dataProviderBean = "CIssueTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CIssueType entityType;
	// Expected Result
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Expected Result", required = false, readOnly = false, defaultValue = "", description = "Expected behavior or result",
			hidden = false, maxLength = 2000
	)
	private String expectedResult;
	// Priority
	@Enumerated (EnumType.STRING)
	@Column (name = "issue_priority", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Priority", required = true, readOnly = false, defaultValue = "MEDIUM",
			description = "Priority level for addressing the issue", hidden = false, useRadioButtons = false
	)
	private EIssuePriority issuePriority;
	// Resolution
	@Enumerated (EnumType.STRING)
	@Column (name = "issue_resolution", nullable = true, length = 30, columnDefinition = "VARCHAR(30)")
	@AMetaData (
			displayName = "Resolution", required = false, readOnly = false, defaultValue = "NONE",
			description = "Resolution status when issue is closed", hidden = false, useRadioButtons = false
	)
	private EIssueResolution issueResolution;
	// Severity
	@Enumerated (EnumType.STRING)
	@Column (name = "issue_severity", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Severity", required = true, readOnly = false, defaultValue = "MINOR", description = "Impact severity level of the issue",
			hidden = false, useRadioButtons = false
	)
	private EIssueSeverity issueSeverity;
	// Linked Activity
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "linked_activity_id", nullable = true)
	@AMetaData (
			displayName = "Linked Activity", required = false, readOnly = false, description = "Activity linked to this issue", hidden = false,
			dataProviderBean = "CActivityService"
	)
	private CActivity linkedActivity;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "issue_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related entities linked to this cissue", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// Resolved Date
	@Column (name = "resolved_date", nullable = true)
	@AMetaData (displayName = "Resolved Date", required = false, readOnly = true, description = "Date when issue was resolved", hidden = false)
	private LocalDate resolvedDate;
	// Sprint item relationship - CIssue owns the relationship
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "sprintitem_id", nullable = true)
	@AMetaData (displayName = "Sprint Item", required = false, readOnly = true, description = "Progress tracking for this activity", hidden = true)
	private CSprintItem sprintItem;
	// Steps to Reproduce
	@Column (nullable = true, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Steps to Reproduce", required = false, readOnly = false, defaultValue = "",
			description = "Detailed steps to reproduce the issue", hidden = false, maxLength = 4000
	)
	private String stepsToReproduce;
	// Story points for estimation
	@Column (nullable = true)
	@AMetaData (
			displayName = "Story Points", required = false, readOnly = false, defaultValue = "0",
			description = "Estimated effort or complexity in story points", hidden = false
	)
	private Long storyPoint;

	/** Default constructor for JPA. */
	/** Default constructor for JPA. */
	protected CIssue() {
		
	}

	public CIssue(final String name, final CProject<?> project) {
		super(CIssue.class, name, project);
		initializeDefaults();
	}

	@PostLoad
	protected void ensureSprintItemParent() {
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
	}

	public String getActualResult() { return actualResult; }

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public String getColor() {
		return entityType != null ? entityType.getColor() : DEFAULT_COLOR;
	}

	@Override
	public Set<CComment> getComments() { return comments; }

	public LocalDate getDueDate() { return dueDate; }
	// ========================================================================
	// Gantt Chart Support - IGanntEntityItem implementation
	// ========================================================================

	@Override
	public LocalDate getEndDate() { return dueDate; }

	@Override
	public CIssueType getEntityType() { return entityType; }

	public String getExpectedResult() { return expectedResult; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	public EIssuePriority getIssuePriority() { return issuePriority; }

	public EIssueResolution getIssueResolution() { return issueResolution; }

	public EIssueSeverity getIssueSeverity() { return issueSeverity; }
	// ========================================================================
	// Sprint Support - ISprintableItem implementation
	// ========================================================================

	public CActivity getLinkedActivity() { return linkedActivity; }

	@Override
	public Set<CLink> getLinks() { return links; }

	@Override
	public Integer getProgressPercentage() {
		if (sprintItem != null) {
			return sprintItem.getProgressPercentage();
		}
		return 0;
	}

	public LocalDate getResolvedDate() { return resolvedDate; }

	@Override
	public CSprintItem getSprintItem() { return sprintItem; }

	@Override
	public Integer getSprintOrder() {
		if (sprintItem != null) {
			return sprintItem.getItemOrder();
		}
		return null;
	}

	@Override
	public LocalDate getStartDate() { return getCreatedDate() != null ? getCreatedDate().toLocalDate() : null; }

	public String getStepsToReproduce() { return stepsToReproduce; }

	@Override
	public Long getStoryPoint() {
		if (sprintItem != null) {
			return sprintItem.getStoryPoint();
		}
		return storyPoint != null ? storyPoint : 0L;
	}

	@Override
	public CWorkflowEntity getWorkflow() { return entityType != null ? entityType.getWorkflow() : null; }

	private final void initializeDefaults() {
		issueSeverity = EIssueSeverity.MINOR;
		issuePriority = EIssuePriority.MEDIUM;
		issueResolution = EIssueResolution.NONE;
		actualResult = "";
		expectedResult = "";
		stepsToReproduce = "";
		storyPoint = 0L;
		dueDate = LocalDate.now().plusDays(7); // Default due date one week from now
		entityType = null;
		linkedActivity = null;
		resolvedDate = null;
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setActualResult(final String actualResult) { this.actualResult = actualResult; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setColor(final String color) {
		// Color is managed by entity type
		// This method exists for IHasColor interface compatibility
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDueDate(final LocalDate dueDate) { this.dueDate = dueDate; }

	public void setEntityType(final CIssueType entityType) { this.entityType = entityType; }

	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		if (typeEntity instanceof CIssueType) {
			setEntityType((CIssueType) typeEntity);
		}
	}

	public void setExpectedResult(final String expectedResult) { this.expectedResult = expectedResult; }

	public void setIssuePriority(final EIssuePriority issuePriority) { this.issuePriority = issuePriority; }

	public void setIssueResolution(final EIssueResolution issueResolution) { this.issueResolution = issueResolution; }

	public void setIssueSeverity(final EIssueSeverity issueSeverity) { this.issueSeverity = issueSeverity; }

	public void setLinkedActivity(final CActivity linkedActivity) { this.linkedActivity = linkedActivity; }

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setResolvedDate(final LocalDate resolvedDate) { this.resolvedDate = resolvedDate; }

	@Override
	public void setSprintItem(final CSprintItem sprintItem) { this.sprintItem = sprintItem; }

	@Override
	public void setSprintOrder(final Integer sprintOrder) {
		// Sprint order is managed by sprint item
		// This method exists for ISprintableItem interface compatibility
		// Order is actually stored in sprint item, not on entity
		LOGGER.debug("setSprintOrder called on issue {} with value {}", getId(), sprintOrder);
	}

	public void setStepsToReproduce(final String stepsToReproduce) { this.stepsToReproduce = stepsToReproduce; }

	@Override
	public void setStoryPoint(final Long storyPoint) {
		if (sprintItem != null) {
			sprintItem.setStoryPoint(storyPoint);
		} else {
			this.storyPoint = storyPoint;
		}
	}

	/**
	 * Get the default sort field for this entity type.
	 * PERFORMANCE OPTIMIZED: Static method for issue tracking.
	 * Issues should be sorted by due date (most urgent first).
	 * 
	 * @return default order field name
	 */
	public static String getDefaultOrderByStatic() {
		return "dueDate";
	}

	/**
	 * Get the default sort field for this entity instance.
	 * LEGACY: Consider using getDefaultOrderByStatic() for better performance.
	 * 
	 * @return default order field name
	 */
	@Override
	public String getDefaultOrderBy() { 
		return getDefaultOrderByStatic(); 
	}
}
