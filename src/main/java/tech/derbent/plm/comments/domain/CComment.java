package tech.derbent.plm.comments.domain;

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
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.base.users.domain.CUser;

/** CComment - Company-scoped domain entity representing user comments. Stores comment text and metadata about comments. Company-scoped for
 * multi-tenant support and universal usage across all entities. Pattern: Unidirectional @OneToMany from parent entities. Parent entities (Activity,
 * Risk, Meeting, Sprint, Project, Issue, etc.) have:
 * @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
 * @JoinColumn(name = "activity_id") // or risk_id, issue_id, etc. private Set<CComment> comments = new HashSet<>(); CComment has NO back-reference to
 *                  parents (clean unidirectional pattern). Layer: Domain (MVC) */
@Entity
@Table (name = "ccomment")
@AttributeOverride (name = "id", column = @Column (name = "comment_id"))
public class CComment extends CEntityOfCompany<CComment> {

	public static final String DEFAULT_COLOR = "#B8860B"; // X11 DarkGoldenrod - discussion
	public static final String DEFAULT_ICON = "vaadin:comment";
	public static final String ENTITY_TITLE_PLURAL = "Comments";
	public static final String ENTITY_TITLE_SINGULAR = "Comment";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComment.class);
	public static final String VIEW_NAME = "Comments View";
	// Author of the comment
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "author_id", nullable = true)
	@AMetaData (
			displayName = "Author", required = false, readOnly = true, description = "User who created this comment", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser author;
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

	/** Default constructor for JPA. */
	public CComment() {
		super();
		initializeDefaults();
	}

	/** Constructor with comment text and author.
	 * @param commentText the comment content text - must not be null or empty
	 * @param author      the user who created this comment */
	public CComment(final String commentText, final CUser author) {
		super(CComment.class, "comment name", author != null ? author.getCompany() : null);
		this.commentText = commentText;
		this.author = author;
		initializeDefaults();
	}

	public CUser getAuthor() { return author; }

	public String getAuthorName() {
		if (author == null) {
			return "Unknown";
		}
		try {
			return author.getName();
		} catch (final org.hibernate.LazyInitializationException e) {
			LOGGER.debug("LazyInitializationException accessing author name, returning safe value", e);
			return "User#" + (author.getId() != null ? author.getId() : "unknown");
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

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (commentText == null) {
			commentText = "";
		}
		if (important == null) {
			important = Boolean.FALSE;
		}
	}

	public Boolean isImportant() { return important; }

	public void setAuthor(final CUser author) { this.author = author; }

	public void setCommentText(final String commentText) { this.commentText = commentText == null ? "" : commentText; }

	public void setImportant(final Boolean important) { this.important = important; }

	@Override
	public String toString() {
		return String.format("CComment{id=%d, author=%s, preview=%s}", getId(), getAuthorName(), getCommentPreview());
	}
}
