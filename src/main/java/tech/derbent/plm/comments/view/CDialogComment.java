package tech.derbent.plm.comments.view;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.ui.component.basic.CCheckbox;
import tech.derbent.api.ui.component.basic.CTextArea;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.service.CCommentService;

/** CDialogComment - Dialog for adding or editing comments.
 * <p>
 * Add mode (isNew = true): - Creates new comment with current user as author - Text area for comment input - Important checkbox
 * <p>
 * Edit mode (isNew = false): - Edits existing comment - Author is read-only (cannot change) - Can edit text and important flag */
public class CDialogComment extends CDialogDBEdit<CComment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogComment.class);
	private static final long serialVersionUID = 1L;
	private final CEnhancedBinder<CComment> binder;
	private CCheckbox checkboxImportant;
	private final CCommentService commentService;
	private final ISessionService sessionService;
	private CTextArea textAreaCommentText;

	/** Constructor for both new and edit modes.
	 * @param commentService the comment service
	 * @param sessionService the session service
	 * @param comment        the comment entity (new or existing)
	 * @param onSave         callback for save action
	 * @param isNew          true if creating new comment, false if editing */
	public CDialogComment(final CCommentService commentService, final ISessionService sessionService, final CComment comment,
			final Consumer<CComment> onSave, final boolean isNew) throws Exception {
		super(comment, onSave, isNew);
		Check.notNull(commentService, "CommentService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		Check.notNull(comment, "Comment cannot be null");
		this.commentService = commentService;
		this.sessionService = sessionService;
		binder = CBinderFactory.createEnhancedBinder(CComment.class);
		new CFormBuilder<>();
		setupDialog();
		populateForm();
	}

	private void createFormFields() throws Exception {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized");
		final CVerticalLayout formLayout = new CVerticalLayout();
		formLayout.setPadding(false);
		formLayout.setSpacing(true);
		// Comment text area
		textAreaCommentText = new CTextArea("Comment Text");
		textAreaCommentText.setWidthFull();
		textAreaCommentText.setHeight("200px");
		textAreaCommentText.setMaxLength(4000);
		textAreaCommentText.setPlaceholder("Enter your comment here...");
		textAreaCommentText.setRequired(true);
		textAreaCommentText.setHelperText("Maximum 4000 characters");
		binder.forField(textAreaCommentText).asRequired("Comment text is required").bind(CComment::getCommentText, CComment::setCommentText);
		formLayout.add(textAreaCommentText);
		// Important checkbox
		checkboxImportant = new CCheckbox("Mark as Important");
		binder.forField(checkboxImportant).bind(CComment::getImportant, CComment::setImportant);
		formLayout.add(checkboxImportant);
		// Author display (read-only)
		if (!isNew) {
			final Span authorLabel = new Span("Author: " + getEntity().getAuthorName());
			authorLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
			formLayout.add(authorLabel);
		}
		getDialogLayout().add(formLayout);
	}

	@Override
	public String getDialogTitleString() { return isNew ? "Add Comment" : "Edit Comment"; }

	@Override
	protected Icon getFormIcon() throws Exception { return isNew ? VaadinIcon.COMMENT.create() : VaadinIcon.EDIT.create(); }

	@Override
	protected String getFormTitleString() { return isNew ? "New Comment" : "Edit Comment"; }

	@Override
	protected String getSuccessCreateMessage() { return "Comment added successfully"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Comment updated successfully"; }

	@Override
	protected void populateForm() {
		try {
			createFormFields();
			binder.readBean(getEntity());
			LOGGER.debug("Form populated for comment: {}", getEntity().getId() != null ? getEntity().getId() : "new");
		} catch (final Exception e) {
			LOGGER.error("Error populating form", e);
			CNotificationService.showException("Error loading comment data", e);
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		// Width handled by CDialog base class (responsive pattern)
	}

	@Override
	protected void validateForm() {
		// Validate using binder
		if (!binder.writeBeanIfValid(getEntity())) {
			throw new IllegalStateException("Please correct validation errors");
		}
		// Set author for new comments
		if (isNew && getEntity().getAuthor() == null) {
			final CUser currentUser = sessionService.getActiveUser().orElse(null);
			if (currentUser == null) {
				LOGGER.error("Cannot create comment: no active user in session");
				throw new IllegalStateException("No active user found");
			}
			getEntity().setAuthor(currentUser);
			getEntity().setCompany(currentUser.getCompany());
		}
		// Save comment
		commentService.save(getEntity());
		LOGGER.debug("Comment validated and saved: {}", getEntity().getId());
	}
}
