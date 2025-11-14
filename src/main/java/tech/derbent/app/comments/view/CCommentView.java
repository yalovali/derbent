package tech.derbent.app.comments.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import tech.derbent.api.ui.component.CButton;
import tech.derbent.api.utils.Check;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.service.CCommentService;

/** CCommentView - UI component for displaying individual comments. Layer: View (MVC) Displays a comment as a div component with: - Author name and
 * event date header - Comment text content - Priority indicator - Important flag indicator - Styling based on priority and importance */
public class CCommentView extends Div {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCommentView.class);
	private static final long serialVersionUID = 1L;
	private final CComment comment;
	private final CCommentService commentService;
	private final VerticalLayout layout;
	private final HorizontalLayout headerLayout;
	private final Paragraph commentTextParagraph;
	private final TextArea editTextArea;
	private final HorizontalLayout editButtonsLayout;
	private final CButton editButton;
	private final CButton saveButton;
	private final CButton cancelButton;
	private Boolean isEditing = Boolean.FALSE;
	private String originalText;

	/** Creates a new CCommentView for the given comment (read-only).
	 * @param comment the comment to display */
	public CCommentView(final CComment comment) {
		this(comment, null);
	}

	/** Creates a new CCommentView for the given comment.
	 * @param comment        the comment to display
	 * @param commentService the comment service for edit operations (can be null for read-only) */
	public CCommentView(final CComment comment, final CCommentService commentService) {
		Check.notNull(comment, "Comment cannot be null");
		this.comment = comment;
		this.commentService = commentService;
		this.layout = new VerticalLayout();
		this.headerLayout = new HorizontalLayout();
		this.commentTextParagraph = new Paragraph();
		this.editTextArea = new TextArea();
		this.editButtonsLayout = new HorizontalLayout();
		// Create buttons
		this.editButton = CButton.createTertiary("Edit", null, e -> on_actionStartEditing());
		this.saveButton = CButton.createPrimary("Save", null, e -> on_actionSaveChanges());
		this.cancelButton = CButton.createTertiary("Cancel", null, e -> on_actionCancelEditing());
		setupLayout();
		setupEditComponents();
		updateContent();
		addClassName("comment-view");
	}

	/** Creates a new CCommentView for a comment loaded by ID from the database.
	 * @param commentId      the ID of the comment to load and display
	 * @param commentService the service to load the comment and handle edits */
	public CCommentView(final Long commentId, final CCommentService commentService) {
		LOGGER.info("CCommentView constructor called with commentId: {}", commentId);
		Check.notNull(commentId, "Comment ID cannot be null");
		Check.notNull(commentService, "Comment service cannot be null");
		final java.util.Optional<CComment> commentOptional = commentService.getById(commentId);
		if (commentOptional.isEmpty()) {
			throw new IllegalArgumentException("Comment with ID " + commentId + " not found");
		}
		this.comment = commentOptional.get();
		this.commentService = commentService;
		this.layout = new VerticalLayout();
		this.headerLayout = new HorizontalLayout();
		this.commentTextParagraph = new Paragraph();
		this.editTextArea = new TextArea();
		this.editButtonsLayout = new HorizontalLayout();
		// Create buttons
		this.editButton = CButton.createTertiary("Edit", null, e -> on_actionStartEditing());
		this.saveButton = CButton.createPrimary("Save", null, e -> on_actionSaveChanges());
		this.cancelButton = CButton.createTertiary("Cancel", null, e -> on_actionCancelEditing());
		setupLayout();
		setupEditComponents();
		updateContent();
		addClassName("comment-view");
	}

	/** Cancels editing and reverts to original text. */
	private void on_actionCancelEditing() {
		isEditing = Boolean.FALSE;
		// Revert any changes made to the comment text
		if (originalText != null) {
			comment.setCommentText(originalText);
		}
		updateContent();
	}

	/** Gets the comment associated with this view.
	 * @return the comment */
	public CComment getComment() { return comment; }

	/** Checks if the comment is currently being edited.
	 * @return true if in edit mode, false otherwise */
	public boolean isEditing() { return Boolean.TRUE.equals(isEditing); }

	/** Refreshes the view content (useful after comment updates). */
	public void refresh() {
		updateContent();
	}

	/** Saves the edited comment text. */
	private void on_actionSaveChanges() {
		final String newText = editTextArea.getValue();
		Check.notBlank(newText, "Cannot save empty comment text");
		try {
			commentService.updateCommentText(comment, newText.trim());
			isEditing = Boolean.FALSE;
			updateContent();
		} catch (final Exception e) {
			LOGGER.error("Error saving comment changes", e);
			// Could show an error notification here
		}
	}

	/** Sets up the edit components. */
	private void setupEditComponents() {
		// Configure edit text area
		editTextArea.setWidthFull();
		editTextArea.setMinHeight("100px");
		editTextArea.setMaxLength(4000);
		editTextArea.setVisible(false);
		// Configure edit buttons layout
		editButtonsLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
		editButtonsLayout.setSpacing(true);
		editButtonsLayout.add(saveButton, cancelButton);
		editButtonsLayout.setVisible(false);
		// Configure buttons
		editButton.setIcon(new Icon(VaadinIcon.EDIT));
		saveButton.setIcon(new Icon(VaadinIcon.CHECK));
		cancelButton.setIcon(new Icon(VaadinIcon.CLOSE));
	}

	/** Sets up the basic layout structure. */
	private void setupLayout() {
		layout.setPadding(true);
		layout.setSpacing(true);
		layout.setWidthFull();
		headerLayout.setWidthFull();
		headerLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
		headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
		commentTextParagraph.setWidthFull();
		layout.add(headerLayout, commentTextParagraph, editTextArea, editButtonsLayout);
		add(layout);
	}

	/** Starts editing mode for the comment. */
	private void on_actionStartEditing() {
		Check.notNull(commentService, "Cannot edit comment - no comment service available");
		isEditing = Boolean.TRUE;
		originalText = comment.getCommentText();
		updateContent();
	}

	/** Updates the comment text content. */
	private void updateCommentText() {
		if (Boolean.TRUE.equals(isEditing)) {
			commentTextParagraph.setVisible(false);
			editTextArea.setValue(comment.getCommentText());
			editTextArea.setVisible(true);
		} else {
			commentTextParagraph.setText(comment.getCommentText());
			commentTextParagraph.addClassName("comment-text");
			commentTextParagraph.getStyle().set("white-space", "pre-wrap");
			commentTextParagraph.getStyle().set("word-wrap", "break-word");
			commentTextParagraph.setVisible(true);
			editTextArea.setVisible(false);
		}
		// Update edit buttons visibility
		editButtonsLayout.setVisible(Boolean.TRUE.equals(isEditing));
	}

	/** Updates the content of the comment view. */
	private void updateContent() {
		updateHeader();
		updateCommentText();
		updateStyling();
	}

	/** Updates the header section with author, date, and indicators. */
	private void updateHeader() {
		headerLayout.removeAll();
		// Left side: Author and date
		final VerticalLayout leftSide = new VerticalLayout();
		leftSide.setPadding(false);
		leftSide.setSpacing(false);
		final H4 authorName = new H4(comment.getAuthorName());
		authorName.addClassName("comment-author");
		authorName.getStyle().set("margin", "0");
		final Span eventDate = new Span(comment.getEventDate().toString());
		eventDate.addClassName("comment-date");
		eventDate.getStyle().set("font-size", "0.875rem");
		eventDate.getStyle().set("color", "var(--lumo-secondary-text-color)");
		leftSide.add(authorName, eventDate);
		// Right side: Priority and importance indicators
		final HorizontalLayout rightSide = new HorizontalLayout();
		rightSide.setSpacing(true);
		rightSide.setAlignItems(HorizontalLayout.Alignment.CENTER);
		// Add edit button if service is available
		if ((commentService != null) && !Boolean.TRUE.equals(isEditing)) {
			rightSide.add(editButton);
		}
		// Priority indicator
		if (comment.getPriority() != null) {
			final Span prioritySpan = new Span(comment.getPriorityName());
			prioritySpan.addClassName("comment-priority");
			prioritySpan.getStyle().set("padding", "0.25rem 0.5rem");
			prioritySpan.getStyle().set("border-radius", "12px");
			prioritySpan.getStyle().set("font-size", "0.75rem");
			prioritySpan.getStyle().set("font-weight", "bold");
			prioritySpan.getStyle().set("color", "white");
			prioritySpan.getStyle().set("background-color", comment.getPriority().getColor());
			rightSide.add(prioritySpan);
		}
		// Important indicator
		if (comment.isImportant()) {
			final Icon importantIcon = new Icon(VaadinIcon.STAR);
			importantIcon.addClassName("comment-important");
			importantIcon.getStyle().set("color", "#FFD700");
			importantIcon.setSize("1.2em");
			rightSide.add(importantIcon);
		}
		headerLayout.add(leftSide, rightSide);
	}

	/** Updates the styling based on comment properties. */
	private void updateStyling() {
		// Base styling
		getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
		getStyle().set("border-radius", "8px");
		getStyle().set("background-color", "var(--lumo-base-color)");
		getStyle().set("margin-bottom", "1rem");
		// Important comments get a different styling
		if (comment.isImportant()) {
			getStyle().set("border-left", "4px solid #FFD700");
			getStyle().set("background-color", "#FFFEF7");
			addClassName("comment-important");
		}
		// Priority-based styling
		if (comment.getPriority() != null) {
			final String priorityClass = "comment-priority-" + comment.getPriority().getPriorityLevel();
			addClassName(priorityClass);
		}
	}
}
