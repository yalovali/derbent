package tech.derbent.app.comments.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextArea;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.service.CCommentService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CDialogComment - Dialog for adding or editing comments.
 * <p>
 * Add mode (isNew = true):
 * - Creates new comment with current user as author
 * - Text area for comment input
 * - Important checkbox
 * <p>
 * Edit mode (isNew = false):
 * - Edits existing comment
 * - Author is read-only (cannot change)
 * - Can edit text and important flag
 */
public class CDialogComment extends CDialogDBEdit<CComment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogComment.class);
	private static final long serialVersionUID = 1L;

	private final CCommentService commentService;
	private final ISessionService sessionService;
	private final CEnhancedBinder<CComment> binder;
	private final CFormBuilder<CComment> formBuilder;

	private TextArea textAreaCommentText;
	private Checkbox checkboxImportant;

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
		this.binder = CBinderFactory.createEnhancedBinder(CComment.class);
		this.formBuilder = new CFormBuilder<>();

		setupDialog();
		populateForm();
	}

	private void createFormFields() throws Exception {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized");

		final CVerticalLayout formLayout = new CVerticalLayout();
		formLayout.setPadding(false);
		formLayout.setSpacing(true);

		// Comment text area
		textAreaCommentText = new TextArea("Comment Text");
		textAreaCommentText.setWidthFull();
		textAreaCommentText.setHeight("200px");
		textAreaCommentText.setMaxLength(4000);
		textAreaCommentText.setPlaceholder("Enter your comment here...");
		textAreaCommentText.setRequired(true);
		textAreaCommentText.setHelperText("Maximum 4000 characters");
		binder.forField(textAreaCommentText).asRequired("Comment text is required").bind(CComment::getCommentText, CComment::setCommentText);
		formLayout.add(textAreaCommentText);

		// Important checkbox
		checkboxImportant = new Checkbox("Mark as Important");
		binder.forField(checkboxImportant).bind(CComment::getImportant, CComment::setImportant);
		formLayout.add(checkboxImportant);

		// Author display (read-only)
		if (!isNew) {
			final com.vaadin.flow.component.html.Span authorLabel = new com.vaadin.flow.component.html.Span(
					"Author: " + getEntity().getAuthorName());
			authorLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
			formLayout.add(authorLabel);
		}

		getDialogLayout().add(formLayout);
	}

	@Override
	protected void on_buttonCancel_clicked() {
		close();
	}

	@Override
	protected void on_buttonSave_clicked() {
		try {
			// Validate
			if (!binder.writeBeanIfValid(getEntity())) {
				CNotificationService.showWarning("Please correct validation errors");
				return;
			}

			// Set author for new comments
			if (isNew && getEntity().getAuthor() == null) {
				final CUser currentUser = sessionService.getActiveUser().orElse(null);
				if (currentUser == null) {
					CNotificationService.showError("No active user found");
					LOGGER.error("Cannot create comment: no active user in session");
					return;
				}
				getEntity().setAuthor(currentUser);
				getEntity().setCompany(currentUser.getCompany());
			}

			// Save comment
			commentService.save(getEntity());

			// Invoke callback
			if (getSaveCallback() != null) {
				getSaveCallback().accept(getEntity());
			}

			CNotificationService.showSaveSuccess();
			close();
		} catch (final Exception e) {
			LOGGER.error("Error saving comment", e);
			CNotificationService.showException("Error saving comment", e);
		}
	}

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
	protected void setupDialog() {
		setTitle(isNew ? "Add Comment" : "Edit Comment");
		setWidth("600px");
		LOGGER.debug("Comment dialog initialized: isNew={}", isNew);
	}
}
