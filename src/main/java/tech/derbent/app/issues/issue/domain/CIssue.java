package tech.derbent.app.issues.issue.domain;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.gannt.ganntitem.service.IGanntEntityItem;
import tech.derbent.app.issues.issuetype.domain.CIssueType;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.api.workflow.domain.CWorkflowEntity;

@Entity
@Table (name = "cissue")
@AttributeOverride (name = "id", column = @Column (name = "issue_id"))
public class CIssue extends CProjectItem<CIssue>
		implements IHasStatusAndWorkflow<CIssue>, IGanntEntityItem, ISprintableItem, IHasIcon, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#D32F2F"; // Red for issues/bugs
	public static final String DEFAULT_ICON = "vaadin:bug";
	public static final String ENTITY_TITLE_PLURAL = "Issues";
	public static final String ENTITY_TITLE_SINGULAR = "Issue";
	private static final Logger LOGGER = LoggerFactory.getLogger(CIssue.class);
	public static final String VIEW_NAME = "Issues View";
	// Expected Result
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Expected Result", required = false, readOnly = false, defaultValue = "", description = "Expected behavior or result",
			hidden = false, maxLength = 2000
	)
	private String expectedResult;
	// Actual Result
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Actual Result", required = false, readOnly = false, defaultValue = "", description = "Actual behavior or result observed",
			hidden = false, maxLength = 2000
	)
	private String actualResult;
	// Steps to Reproduce
	@Column (nullable = true, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Steps to Reproduce", required = false, readOnly = false, defaultValue = "",
			description = "Detailed steps to reproduce the issue", hidden = false, maxLength = 4000
	)
	private String stepsToReproduce;
	// Issue Type
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Issue Type", required = false, readOnly = false, description = "Type category of the issue (Bug, Improvement, Task)",
			hidden = false, dataProviderBean = "CIssueTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CIssueType entityType;
	// Severity
	@Enumerated (EnumType.STRING)
	@Column (name = "issue_severity", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Severity", required = true, readOnly = false, defaultValue = "MINOR", description = "Impact severity level of the issue",
			hidden = false, useRadioButtons = false
	)
	private EIssueSeverity issueSeverity;
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
	// Linked Activity
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "linked_activity_id", nullable = true)
	@AMetaData (
			displayName = "Linked Activity", required = false, readOnly = false, description = "Activity linked to this issue", hidden = false,
			dataProviderBean = "CActivityService"
	)
	private CActivity linkedActivity;
	// Due Date
	@Column (name = "due_date", nullable = true)
	@AMetaData (displayName = "Due Date", required = false, readOnly = false, description = "Target date for resolving the issue", hidden = false)
	private LocalDate dueDate;
	// Resolved Date
	@Column (name = "resolved_date", nullable = true)
	@AMetaData (displayName = "Resolved Date", required = false, readOnly = true, description = "Date when issue was resolved", hidden = false)
	private LocalDate resolvedDate;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "issue_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this issue", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// Sprint item relationship - CIssue owns the relationship
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "sprintitem_id", nullable = true)
	@AMetaData (displayName = "Sprint Item", required = false, readOnly = true, description = "Progress tracking for this activity", hidden = true)
	private CSprintItem sprintItem;
	// Story points for estimation
	@Column (nullable = true)
	@AMetaData (
			displayName = "Story Points", required = false, readOnly = false, defaultValue = "0",
			description = "Estimated effort or complexity in story points", hidden = false
	)
	private Long storyPoint;
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "issue_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this issue", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CIssue() {
		super();
		initializeDefaults();
	}

	public CIssue(final String name, final CProject project) {
		super(CIssue.class, name, project);
		initializeDefaults();
	}

	/** Creates a clone of this issue with the specified options. This implementation follows the recursive cloning pattern: 1. Calls parent's
	 * createClone() to handle inherited fields (CProjectItem) 2. Clones issue-specific fields based on options 3. Recursively clones collections
	 * (comments, attachments) if requested Cloning behavior: - Basic fields (strings, numbers, enums) are always cloned - Date fields are cloned only
	 * if !options.isResetDates() - User assignments (linkedActivity) are cloned only if !options.isResetAssignments() - Workflow field is cloned only
	 * if options.isCloneWorkflow() - Comments collection is recursively cloned if options.includesComments() - Attachments collection is recursively
	 * cloned if options.includesAttachments()
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the issue with cloned data
	 * @throws CloneNotSupportedException if cloning fails */
	@Override
	public CIssue createClone(final CCloneOptions options) throws Exception {
		// Get parent's clone (CProjectItem -> CEntityOfProject -> CEntityNamed -> CEntityDB)
		final CIssue clone = super.createClone(options);
		// Clone basic issue fields (always included)
		clone.expectedResult = expectedResult;
		clone.actualResult = actualResult;
		clone.stepsToReproduce = stepsToReproduce;
		// Clone enum fields
		clone.issueSeverity = issueSeverity;
		clone.issuePriority = issuePriority;
		clone.issueResolution = issueResolution;
		// Clone entity type (issue type)
		clone.entityType = entityType;
		// Clone workflow if requested
		if (options.isCloneWorkflow() && getWorkflow() != null) {
			// Workflow is obtained via entityType.getWorkflow() - already cloned via entityType
		}
		// Clone story points
		clone.storyPoint = storyPoint;
		// Handle date fields based on options
		if (!options.isResetDates()) {
			clone.dueDate = dueDate;
			clone.resolvedDate = resolvedDate;
		}
		// Clone linked activity if not resetting assignments
		if (!options.isResetAssignments() && linkedActivity != null) {
			clone.linkedActivity = linkedActivity;
		}
		// Clone comments if requested
		if (options.includesComments() && comments != null && !comments.isEmpty()) {
			clone.comments = new HashSet<>();
			for (final CComment comment : comments) {
				try {
					final CComment commentClone = comment.createClone(options);
					clone.comments.add(commentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone comment: {}", e.getMessage());
				}
			}
		}
		// Clone attachments if requested
		if (options.includesAttachments() && attachments != null && !attachments.isEmpty()) {
			clone.attachments = new HashSet<>();
			for (final CAttachment attachment : attachments) {
				try {
					final CAttachment attachmentClone = attachment.createClone(options);
					clone.attachments.add(attachmentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone attachment: {}", e.getMessage());
				}
			}
		}
		// Note: Sprint item relationship is not cloned - clone starts outside sprint
		LOGGER.debug("Successfully cloned issue '{}' with options: {}", getName(), options);
		return clone;
	}

	@jakarta.persistence.PostLoad
	protected void ensureSprintItemParent() {
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
	}

	public String getActualResult() { return actualResult; }

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public String getColor() {
		// Return color from entity type if available, else default
		if (entityType != null) {
			return entityType.getColor();
		}
		return DEFAULT_COLOR;
	}

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

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

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (issueSeverity == null) {
			issueSeverity = EIssueSeverity.MINOR;
		}
		if (issuePriority == null) {
			issuePriority = EIssuePriority.MEDIUM;
		}
		if (issueResolution == null) {
			issueResolution = EIssueResolution.NONE;
		}
		// LOGGER.debug("Issue defaults initialized: severity={}, priority={}, resolution={}", issueSeverity, issuePriority, issueResolution);
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
}
