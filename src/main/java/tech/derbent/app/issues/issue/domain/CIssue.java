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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.issues.issuetype.domain.CIssueType;

@Entity
@Table(name = "cissue")
@AttributeOverride(name = "id", column = @Column(name = "issue_id"))
public class CIssue extends CProjectItem<CIssue> implements IHasStatusAndWorkflow<CIssue>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#D32F2F"; // Red for issues/bugs
	public static final String DEFAULT_ICON = "vaadin:bug";
	public static final String ENTITY_TITLE_PLURAL = "Issues";
	public static final String ENTITY_TITLE_SINGULAR = "Issue";
	private static final Logger LOGGER = LoggerFactory.getLogger(CIssue.class);
	public static final String VIEW_NAME = "Issues View";

	// Expected Result
	@Column(nullable = true, length = 2000)
	@Size(max = 2000)
	@AMetaData(displayName = "Expected Result", required = false, readOnly = false, defaultValue = "", description = "Expected behavior or result", hidden = false, maxLength = 2000)
	private String expectedResult;

	// Actual Result
	@Column(nullable = true, length = 2000)
	@Size(max = 2000)
	@AMetaData(displayName = "Actual Result", required = false, readOnly = false, defaultValue = "", description = "Actual behavior or result observed", hidden = false, maxLength = 2000)
	private String actualResult;

	// Steps to Reproduce
	@Column(nullable = true, length = 4000)
	@Size(max = 4000)
	@AMetaData(displayName = "Steps to Reproduce", required = false, readOnly = false, defaultValue = "", description = "Detailed steps to reproduce the issue", hidden = false, maxLength = 4000)
	private String stepsToReproduce;

	// Issue Type
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entitytype_id", nullable = true)
	@AMetaData(displayName = "Issue Type", required = false, readOnly = false, description = "Type category of the issue (Bug, Improvement, Task)", hidden = false, dataProviderBean = "CIssueTypeService", setBackgroundFromColor = true, useIcon = true)
	private CIssueType entityType;

	// Severity
	@Enumerated(EnumType.STRING)
	@Column(name = "issue_severity", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData(displayName = "Severity", required = true, readOnly = false, defaultValue = "MINOR", description = "Impact severity level of the issue", hidden = false, useRadioButtons = false)
	private EIssueSeverity issueSeverity;

	// Priority
	@Enumerated(EnumType.STRING)
	@Column(name = "issue_priority", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData(displayName = "Priority", required = true, readOnly = false, defaultValue = "MEDIUM", description = "Priority level for addressing the issue", hidden = false, useRadioButtons = false)
	private EIssuePriority issuePriority;

	// Resolution
	@Enumerated(EnumType.STRING)
	@Column(name = "issue_resolution", nullable = true, length = 30, columnDefinition = "VARCHAR(30)")
	@AMetaData(displayName = "Resolution", required = false, readOnly = false, defaultValue = "NONE", description = "Resolution status when issue is closed", hidden = false, useRadioButtons = false)
	private EIssueResolution issueResolution;

	// Linked Activity
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "linked_activity_id", nullable = true)
	@AMetaData(displayName = "Linked Activity", required = false, readOnly = false, description = "Activity linked to this issue", hidden = false, dataProviderBean = "CActivityService")
	private CActivity linkedActivity;

	// Due Date
	@Column(name = "due_date", nullable = true)
	@AMetaData(displayName = "Due Date", required = false, readOnly = false, description = "Target date for resolving the issue", hidden = false)
	private LocalDate dueDate;

	// Resolved Date
	@Column(name = "resolved_date", nullable = true)
	@AMetaData(displayName = "Resolved Date", required = false, readOnly = true, description = "Date when issue was resolved", hidden = false)
	private LocalDate resolvedDate;

	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "issue_id")
	@AMetaData(displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this issue", hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent")
	private Set<CAttachment> attachments = new HashSet<>();

	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "issue_id")
	@AMetaData(displayName = "Comments", required = false, readOnly = false, description = "Comments for this issue", hidden = false, dataProviderBean = "CCommentService", createComponentMethod = "createComponent")
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

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	public String getActualResult() {
		return actualResult;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	@Override
	public CIssueType getEntityType() {
		return entityType;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	@Override
	public String getIconString() {
		return DEFAULT_ICON;
	}

	public EIssuePriority getIssuePriority() {
		return issuePriority;
	}

	public EIssueResolution getIssueResolution() {
		return issueResolution;
	}

	public EIssueSeverity getIssueSeverity() {
		return issueSeverity;
	}

	public CActivity getLinkedActivity() {
		return linkedActivity;
	}

	public LocalDate getResolvedDate() {
		return resolvedDate;
	}

	public String getStepsToReproduce() {
		return stepsToReproduce;
	}

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
		LOGGER.debug("Issue defaults initialized: severity={}, priority={}, resolution={}", issueSeverity, issuePriority, issueResolution);
	}

	public void setActualResult(final String actualResult) {
		this.actualResult = actualResult;
	}

	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
	}

	public void setDueDate(final LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public void setEntityType(final CIssueType entityType) {
		this.entityType = entityType;
	}

	public void setExpectedResult(final String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public void setIssuePriority(final EIssuePriority issuePriority) {
		this.issuePriority = issuePriority;
	}

	public void setIssueResolution(final EIssueResolution issueResolution) {
		this.issueResolution = issueResolution;
	}

	public void setIssueSeverity(final EIssueSeverity issueSeverity) {
		this.issueSeverity = issueSeverity;
	}

	public void setLinkedActivity(final CActivity linkedActivity) {
		this.linkedActivity = linkedActivity;
	}

	public void setResolvedDate(final LocalDate resolvedDate) {
		this.resolvedDate = resolvedDate;
	}

	public void setStepsToReproduce(final String stepsToReproduce) {
		this.stepsToReproduce = stepsToReproduce;
	}
}
