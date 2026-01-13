package tech.derbent.app.comments.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEventEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.issues.issue.domain.CIssue;
import tech.derbent.base.users.domain.CUser;

/** CComment - Domain entity representing user comments on activities and issues. Layer: Domain (MVC) Inherits from CEvent to provide event-based functionality
 * with audit fields. Comments are linked to activities or issues and contain: - Command text (comment content) - Author information (inherited from CEvent) -
 * Date/timestamp (inherited from CEvent) - Priority level - Activity/Issue reference - Project context (inherited from CEvent) Comments are displayed in
 * historic order within activity and issue views. */
@Entity
@Table (name = "ccomment")
@AttributeOverride (name = "id", column = @Column (name = "comment_id"))
public class CComment extends CEventEntity<CComment> {

	public static final String DEFAULT_COLOR = "#B8860B"; // X11 DarkGoldenrod - discussion (darker)
	public static final String DEFAULT_ICON = "vaadin:comment";
	public static final String ENTITY_TITLE_PLURAL = "Comments";
	public static final String ENTITY_TITLE_SINGULAR = "Comment";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComment.class);
	public static final String VIEW_NAME = "Comments View";
	// Activity this comment belongs to
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "activity_id", nullable = true)
	@AMetaData (
			displayName = "Activity", required = false, readOnly = false, description = "Activity this comment belongs to", hidden = false,
			dataProviderBean = "CActivityService"
	)
	private CActivity activity;
	// Issue this comment belongs to
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "issue_id", nullable = true)
	@AMetaData (
			displayName = "Issue", required = false, readOnly = false, description = "Issue this comment belongs to", hidden = false,
			dataProviderBean = "CIssueService"
	)
	private CIssue issue;
	// Comment text content
	@Column (name = "comment_text", nullable = false, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Comment Text", required = true, readOnly = false, description = "The comment content text", hidden = false,
			maxLength = 4000
	)
	private String commentText;
	// Flag for important comments
	@Column (name = "is_important", nullable = false)
	@AMetaData (
			displayName = "Important", required = false, readOnly = false, defaultValue = "false", description = "Mark this comment as important",
			hidden = false
	)
	private Boolean important = Boolean.FALSE;
	// Priority of the comment
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "priority_id", nullable = true)
	@AMetaData (
			displayName = "Priority", required = false, readOnly = false, description = "Priority level of this comment", hidden = false,
			dataProviderBean = "CCommentPriorityService"
	)
	private CCommentPriority priority;

	/** Default constructor for JPA. */
	public CComment() {
		super();
		// Initialize with default values for JPA
		important = false;
	}

	/** Constructor with comment text, activity, and author.
	 * @param commentText the comment content text - must not be null or empty
	 * @param activity    the activity this comment belongs to - must not be null
	 * @param author      the user who created this comment - must not be null */
	public CComment(final String commentText, final CActivity activity, final CUser author) {
		super(CComment.class);
		setAuthor(author);
		this.commentText = commentText;
		this.activity = activity;
	}

	/** Constructor with comment text, activity, author, and priority.
	 * @param commentText the comment content text - must not be null or empty
	 * @param activity    the activity this comment belongs to - must not be null
	 * @param author      the user who created this comment - must not be null
	 * @param priority    the priority level of this comment */
	public CComment(final String commentText, final CActivity activity, final CUser author, final CCommentPriority priority) {
		this(commentText, activity, author);
		this.priority = priority;
	}

	/** Constructor with comment text, issue, and author.
	 * @param commentText the comment content text - must not be null or empty
	 * @param issue       the issue this comment belongs to - must not be null
	 * @param author      the user who created this comment - must not be null */
	public CComment(final String commentText, final CIssue issue, final CUser author) {
		super(CComment.class);
		setAuthor(author);
		this.commentText = commentText;
		this.issue = issue;
	}

	public CActivity getActivity() { return activity; }

	public String getActivityName() {
		if (activity == null) {
			return "Activity (unset)";
		}
		try {
			// Safe access to avoid LazyInitializationException
			return activity.getName();
		} catch (final org.hibernate.LazyInitializationException e) {
			LOGGER.debug("LazyInitializationException accessing activity name, returning safe value", e);
			return "Activity#" + (activity.getId() != null ? activity.getId() : "unknown");
		}
	}

	public CIssue getIssue() { return issue; }

	public String getIssueName() {
		if (issue == null) {
			return "Issue (unset)";
		}
		try {
			// Safe access to avoid LazyInitializationException
			return issue.getName();
		} catch (final org.hibernate.LazyInitializationException e) {
			LOGGER.debug("LazyInitializationException accessing issue name, returning safe value", e);
			return "Issue#" + (issue.getId() != null ? issue.getId() : "unknown");
		}
	}

	public String getParentName() {
		if (activity != null) {
			return getActivityName();
		} else if (issue != null) {
			return getIssueName();
		}
		return "No parent";
	}

	/** Get a short preview of the comment text (first 100 characters).
	 * @return short preview of comment text */
	public String getCommentPreview() {
		if (commentText == null) {
			return "";
		}
		return commentText.length() > 100 ? commentText.substring(0, 100) + "..." : commentText;
	}

	public String getCommentText() { return commentText; }

	public Boolean getImportant() { return important; }

	public CCommentPriority getPriority() { return priority; }

	public String getPriorityName() { return (priority != null) ? priority.getName() : "Normal"; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (commentText == null) {
			commentText = "";
		}
	}

	public Boolean isImportant() { return important; }

	public void setActivity(final CActivity activity) {
		this.activity = activity;
	}

	public void setIssue(final CIssue issue) {
		this.issue = issue;
	}

	public void setCommentText(final String commentText) {
		this.commentText = commentText == null ? "" : commentText;
	}

	public void setImportant(final Boolean important) { this.important = important; }

	public void setPriority(final CCommentPriority priority) { this.priority = priority; }

	@Override
	public String toString() {
		return String.format("CComment{id=%d, parent=%s, author=%s, preview=%s}", getId(), getParentName(), getAuthorName(), getCommentPreview());
	}
}
