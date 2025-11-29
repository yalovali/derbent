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
import tech.derbent.api.domains.CEvent;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.base.users.domain.CUser;

/** CComment - Domain entity representing user comments on activities. Layer: Domain (MVC) Inherits from CEvent to provide event-based functionality
 * with audit fields. Comments are linked to activities and contain: - Command text (comment content) - Author information (inherited from CEvent) -
 * Date/timestamp (inherited from CEvent) - Priority level - Activity reference - Project context (inherited from CEvent) Comments are displayed in
 * historic order within activity views. */
@Entity
@Table (name = "ccomment")
@AttributeOverride (name = "id", column = @Column (name = "comment_id"))
public class CComment extends CEvent<CComment> {

	public static final String DEFAULT_COLOR = "#F5E8A2"; // OpenWindows Highlight Yellow - discussion
	public static final String DEFAULT_ICON = "vaadin:comment";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComment.class);
	public static final String VIEW_NAME = "Comments View";
	// Activity this comment belongs to
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "activity_id", nullable = false)
	@AMetaData (
			displayName = "Activity", required = true, readOnly = false, description = "Activity this comment belongs to", hidden = false,
			dataProviderBean = "CActivityService"
	)
	private CActivity activity;
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

	public CActivity getActivity() { return activity; }

	public String getActivityName() {
		Check.notNull(activity, "Activity cannot be null");
		try {
			// Safe access to avoid LazyInitializationException
			return activity.getName();
		} catch (final org.hibernate.LazyInitializationException e) {
			LOGGER.debug("LazyInitializationException accessing activity name, returning safe value", e);
			return "Activity#" + (activity.getId() != null ? activity.getId() : "unknown");
		}
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
		Check.notNull(activity, "Activity cannot be null");
		this.activity = activity;
	}

	public void setCommentText(final String commentText) {
		Check.notBlank(commentText, "Comment text cannot be null or empty");
		this.commentText = commentText;
	}

	public void setImportant(final Boolean important) { this.important = important; }

	public void setPriority(final CCommentPriority priority) { this.priority = priority; }

	@Override
	public String toString() {
		return String.format("CComment{id=%d, activity=%s, author=%s, preview=%s}", getId(), getActivityName(), getAuthorName(), getCommentPreview());
	}
}
