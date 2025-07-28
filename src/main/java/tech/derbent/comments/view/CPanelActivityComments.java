package tech.derbent.comments.view;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.view.CPanelActivityBase;
import tech.derbent.comments.domain.CComment;
import tech.derbent.comments.service.CCommentService;
import tech.derbent.session.service.SessionService;
import tech.derbent.users.domain.CUser;

/**
 * CPanelActivityComments - Accordion panel for managing comments on activities. Layer:
 * View (MVC) Provides UI for: - Viewing existing comments in chronological order - Adding
 * new comments - Comment count display - Integration with activity details view
 */
public class CPanelActivityComments extends CPanelActivityBase {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CPanelActivityComments.class);

	private static final long serialVersionUID = 1L;

	private final CCommentService commentService;

	private final SessionService sessionService;

	private VerticalLayout commentsContainer;

	private TextArea newCommentArea;

	private CButton addCommentButton;

	private H3 commentsTitle;

	/**
	 * Constructor for CPanelActivityComments.
	 * @param currentEntity   the current activity entity
	 * @param binder          the validation binder
	 * @param activityService the activity service
	 * @param commentService  the comment service
	 * @param sessionService  the session service
	 */
	public CPanelActivityComments(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> binder,
		final CActivityService activityService, final CCommentService commentService,
		final SessionService sessionService) {
		super("Comments", currentEntity, binder, activityService, sessionService);
		LOGGER.info("CPanelActivityComments constructor called with activity: {}",
			currentEntity);
		this.commentService = commentService;
		this.sessionService = sessionService;
	}

	/**
	 * Handles adding a new comment.
	 */
	private void addComment() {
		LOGGER.info("Adding new comment for activity: {}", getCurrentEntity());
		final String commentText = newCommentArea.getValue();

		if ((commentText == null) || commentText.trim().isEmpty()) {
			LOGGER.warn("Cannot add empty comment");
			return;
		}

		if (getCurrentEntity() == null) {
			LOGGER.warn("Cannot add comment - current entity is null");
			return;
		}

		try {
			final Optional<CUser> currentUserOpt = sessionService.getActiveUser();

			if (currentUserOpt.isEmpty()) {
				LOGGER.warn("Cannot add comment - no current user");
				return;
			}
			final CUser currentUser = currentUserOpt.get();
			final CComment newComment = commentService.createComment(commentText.trim(),
				getCurrentEntity(), currentUser);
			LOGGER.info("Created new comment: {}", newComment);
			// Clear the text area
			newCommentArea.clear();
			// Reload comments to show the new one
			loadComments();
		} catch (final Exception e) {
			LOGGER.error("Error adding comment", e);
			// Could show an error notification here
		}
	}

	@Override
	protected ComboBoxDataProvider createComboBoxDataProvider() {
		return null;
	}

	/**
	 * Creates the new comment section.
	 * @return the new comment section layout
	 */
	private VerticalLayout createNewCommentSection() {
		final VerticalLayout section = new VerticalLayout();
		section.setPadding(false);
		section.setSpacing(true);
		section.setWidthFull();
		section.addClassName("new-comment-section");
		// Style the section
		section.getStyle().set("border-top", "1px solid var(--lumo-contrast-20pct)");
		section.getStyle().set("padding-top", "1rem");
		section.getStyle().set("margin-top", "1rem");
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
		buttonLayout.add(addCommentButton);
		section.add(newCommentArea, buttonLayout);
		return section;
	}

	@Override
	protected void createPanelContent() {
		// Initialize UI components
		this.commentsContainer = new VerticalLayout();
		this.newCommentArea = new TextArea();
		this.addCommentButton =
			CButton.createPrimary("Add Comment", event -> addComment());
		this.commentsTitle = new H3("H3 Comments");
		setupComponents();
		// New comment section
		getBaseLayout().add(createNewCommentSection());
		getBaseLayout().add(commentsTitle);
		setupCommentsContainer();
		updateCommentsTitle();
		loadComments();
	}

	/**
	 * Gets the comment service.
	 * @return the comment service
	 */
	public CCommentService getCommentService() { return commentService; }

	/**
	 * Loads and displays comments for the current activity.
	 */
	private void loadComments() {
		commentsContainer.removeAll();

		if (getCurrentEntity() == null) {
			LOGGER.warn("Cannot load comments - current entity is null");
			return;
		}

		try {
			final List<CComment> comments =
				commentService.findByActivityWithRelationships(getCurrentEntity());
			LOGGER.debug("Found {} comments for activity", comments.size());

			if (comments.isEmpty()) {
				final Div noCommentsDiv = new Div();
				noCommentsDiv.setText("No comments yet. Be the first to add a comment!");
				noCommentsDiv.addClassName("no-comments-message");
				noCommentsDiv.getStyle().set("text-align", "center");
				noCommentsDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");
				noCommentsDiv.getStyle().set("font-style", "italic");
				noCommentsDiv.getStyle().set("padding", "2rem");
				commentsContainer.add(noCommentsDiv);
			}
			else {

				// Add comments in chronological order
				for (final CComment comment : comments) {
					final CCommentView commentView =
						new CCommentView(comment, commentService);
					commentsContainer.add(commentView);
				}
			}
			updateCommentsTitle();
		} catch (final Exception e) {
			LOGGER.error("Error loading comments for activity: {}", getCurrentEntity(),
				e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading comments. Please try again.");
			errorDiv.addClassName("error-message");
			errorDiv.getStyle().set("color", "var(--lumo-error-text-color)");
			commentsContainer.add(errorDiv);
		}
	}

	@Override
	public void populateForm(final CActivity entity) {
		super.populateForm(entity);

		// Refresh comments when activity changes
		if (entity != null) {
			loadComments();
		}
		else {
			// Clear comments if no activity selected
			commentsContainer.removeAll();
			updateCommentsTitle();
		}
	}

	/**
	 * Refreshes the comments panel (useful after external changes).
	 */
	public void refreshComments() {
		loadComments();
	}

	private void setupCommentsContainer() {
		commentsContainer.setPadding(false);
		commentsContainer.setSpacing(true);
		commentsContainer.setWidthFull();
		commentsContainer.addClassName("comments-container");
		getBaseLayout().add(commentsContainer);
	}

	/**
	 * Sets up the UI components.
	 */
	private void setupComponents() {
		// New comment text area
		newCommentArea.setLabel("New Comment");
		newCommentArea.setPlaceholder("Enter your comment here...");
		newCommentArea.setWidthFull();
		newCommentArea.setMinHeight("100px");
		newCommentArea.setMaxLength(4000);
		// Add comment button
		addCommentButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		final Icon addIcon = new Icon(VaadinIcon.PLUS);
		addCommentButton.setIcon(addIcon);
		addCommentButton.setEnabled(false); // Disabled until text is entered
		// Enable/disable button based on text area content Use value change listener for
		// immediate response
		newCommentArea.addValueChangeListener(event -> {
			final String text = event.getValue();
			addCommentButton.setEnabled((text != null) && !text.trim().isEmpty());
		});
		// Additional listener for immediate response during typing
		newCommentArea.getElement().addEventListener("input", e -> {
			final String currentValue = newCommentArea.getValue();
			addCommentButton
				.setEnabled((currentValue != null) && !currentValue.trim().isEmpty());
		});
	}

	/**
	 * Updates the comments title with count.
	 */
	private void updateCommentsTitle() {

		if (commentsTitle == null) {
			LOGGER.warn("Comments title is null, cannot update");
			return;
		}

		if (getCurrentEntity() == null) {
			commentsTitle.setText("Comments");
			return;
		}

		try {
			final long commentCount = commentService.countByActivity(getCurrentEntity());
			commentsTitle.setText(String.format("Comments (%d)", commentCount));
		} catch (final Exception e) {
			LOGGER.warn("Error getting comment count", e);
			commentsTitle.setText("Comments");
		}
	}

	@Override
	protected void updatePanelEntityFields() {
		// No entity fields needed for comments panel
		setEntityFields(List.of());
	}
}