package tech.derbent.plm.comments.view;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.comments.service.CCommentService;

/** CComponentListComments - Component for managing comments on entities.
 * <p>
 * Displays a list of comments with author, date, preview and important flag. Supports CRUD operations (Create, Read, Update, Delete) with expandable
 * details. Comments can be clicked to expand and show full text.
 * <p>
 * This component uses the IHasComments interface for clean, type-safe integration with any entity that can have comments.
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentListComments component = new CComponentListComments(service, sessionService);
 * component.setMasterEntity(activity); // activity implements IHasComments
 * </pre>
 */
public class CComponentListComments extends CVerticalLayout
		implements IContentOwner, IGridComponent<CComment>, IGridRefreshListener<CComment>, IPageServiceAutoRegistrable {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	public static final String ID_GRID = "custom-comments-grid";
	public static final String ID_HEADER = "custom-comments-header";
	public static final String ID_ROOT = "custom-comments-component";
	public static final String ID_TOOLBAR = "custom-comments-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListComments.class);
	private static final long serialVersionUID = 1L;
	private CButton buttonAdd;
	private CButton buttonDelete;
	private CButton buttonEdit;
	private final CCommentService commentService;
	private CGrid<CComment> grid;
	private CHorizontalLayout layoutToolbar;
	private IHasComments masterEntity;
	private final List<Consumer<CComment>> refreshListeners = new ArrayList<>();
	private final ISessionService sessionService;

	/** Constructor for comment list component.
	 * @param commentService the comment service
	 * @param sessionService the session service */
	public CComponentListComments(final CCommentService commentService, final ISessionService sessionService) {
		Check.notNull(commentService, "CommentService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.commentService = commentService;
		this.sessionService = sessionService;
		initializeComponent();
	}

	@Override
	public void addRefreshListener(final Consumer<CComment> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
	}

	@Override
	public void clearGrid() {
		Check.notNull(grid, "Grid cannot be null when clearing comments");
		grid.setItems(List.of());
		grid.asSingleSelect().clear();
		buttonEdit.setEnabled(false);
		buttonDelete.setEnabled(false);
		updateCompactMode(true);
	}

	/** Configure grid columns with expandable details. */
	@Override
	public void configureGrid(final CGrid<CComment> grid1) {
		try {
			Check.notNull(grid1, "Grid cannot be null");
			// Author column
			grid1.addCustomColumn(CComment::getAuthorName, "Author", "150px", "author", 0);
			// Created date column
			grid1.addCustomColumn(comment -> {
				if (comment.getCreatedDate() != null) {
					return comment.getCreatedDate().format(DATE_TIME_FORMATTER);
				}
				return "";
			}, "Date", "150px", "createdDate", 0);
			// Comment preview column (expanding)
			grid1.addExpandingShortTextColumn(CComment::getCommentPreview, "Comment", "commentPreview");
			// Important flag column
			grid1.addCustomColumn(comment -> {
				if (Boolean.TRUE.equals(comment.getImportant())) {
					return "⚠";
				}
				return "";
			}, "!", "50px", "important", 0).setTooltipGenerator(comment -> Boolean.TRUE.equals(comment.getImportant()) ? "Important" : "");
			// Add expandable details renderer for full comment text
			grid1.setItemDetailsRenderer(new ComponentRenderer<>(comment -> {
				final CVerticalLayout detailsLayout = new CVerticalLayout();
				detailsLayout.setPadding(true);
				detailsLayout.setSpacing(true);
				detailsLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-left",
						"3px solid var(--lumo-primary-color)");
				// Full comment text
				final CSpan commentText = new CSpan(comment.getCommentText());
				commentText.getStyle().set("white-space", "pre-wrap").set("word-wrap", "break-word");
				detailsLayout.add(commentText);
				// Metadata footer
				final CSpan metadata = new CSpan(String.format("By %s on %s%s", comment.getAuthorName(),
						comment.getCreatedDate() != null ? comment.getCreatedDate().format(DATE_TIME_FORMATTER) : "unknown",
						Boolean.TRUE.equals(comment.getImportant()) ? " [IMPORTANT]" : ""));
				metadata.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
				detailsLayout.add(metadata);
				return detailsLayout;
			}));
			// Enable single-click to expand/collapse
			grid1.addItemClickListener(event -> {
				final CComment comment = event.getItem();
				if (grid1.isDetailsVisible(comment)) {
					grid1.setDetailsVisible(comment, false);
				} else {
					grid1.setDetailsVisible(comment, true);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Error configuring comments grid", e);
			CNotificationService.showException("Error configuring comments grid", e);
		}
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		throw new UnsupportedOperationException("Comments are managed via comment dialog.");
	}

	/** Create toolbar buttons. */
	private void createToolbarButtons() {
		// Add button
		buttonAdd = new CButton(VaadinIcon.PLUS.create());
		buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonAdd.setTooltipText("Add comment");
		buttonAdd.addClickListener(event -> on_buttonAdd_clicked());
		layoutToolbar.add(buttonAdd);
		// Edit button
		buttonEdit = new CButton(VaadinIcon.EDIT.create());
		buttonEdit.setTooltipText("Edit comment");
		buttonEdit.addClickListener(event -> on_buttonEdit_clicked());
		buttonEdit.setEnabled(false);
		layoutToolbar.add(buttonEdit);
		// Delete button
		buttonDelete = new CButton(VaadinIcon.TRASH.create());
		buttonDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		buttonDelete.setTooltipText("Delete comment");
		buttonDelete.addClickListener(event -> on_buttonDelete_clicked());
		buttonDelete.setEnabled(false);
		layoutToolbar.add(buttonDelete);
	}

	@Override
	public String getComponentName() { return "comments"; }

	@Override
	public String getCurrentEntityIdString() {
		if (masterEntity instanceof CEntityDB<?>) {
			final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
			return entity.getId() != null ? entity.getId().toString() : null;
		}
		return null;
	}

	@Override
	public CAbstractService<?> getEntityService() { return commentService; }

	@Override
	public CGrid<CComment> getGrid() { return grid; }

	@Override
	public CEntityDB<?> getValue() {
		if (masterEntity instanceof CEntityDB<?>) {
			return (CEntityDB<?>) masterEntity;
		}
		return null;
	}

	/** Initialize the component layout and grid. */
	private void initializeComponent() {
		setId(ID_ROOT);
		setPadding(false);
		setSpacing(true);
		// Header
		final CH3 header = new CH3("Comments");
		header.setId(ID_HEADER);
		add(header);
		// Toolbar
		layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		createToolbarButtons();
		add(layoutToolbar);
		// Grid
		grid = new CGrid<>(CComment.class);
		grid.setId(ID_GRID);
		CGrid.setupGrid(grid);
		grid.setRefreshConsumer(event -> refreshGrid());
		configureGrid(grid);
		grid.setHeight("300px"); // Default height
		grid.asSingleSelect().addValueChangeListener(e -> on_grid_selectionChanged(e.getValue()));
		// Add double-click to edit
		grid.addItemDoubleClickListener(e -> on_grid_doubleClicked(e.getItem()));
		add(grid);
		// Set initial compact mode (will adjust when data loaded)
		updateCompactMode(true);
	}

	private void linkCommentToMaster(final CComment comment) {
		Check.notNull(comment, "Comment cannot be null");
		Check.notNull(masterEntity, "Master entity cannot be null");
		Set<CComment> items = masterEntity.getComments();
		if (items == null) {
			items = new HashSet<>();
			masterEntity.setComments(items);
		}
		// Check if comment already exists (by ID for persisted, by reference for new)
		final Long commentId = comment.getId();
		final boolean exists = items.stream().anyMatch(existing -> {
			if (commentId != null && existing != null && existing.getId() != null) {
				return commentId.equals(existing.getId());
			}
			// For new comments (null ID), check by reference to avoid duplicates
			return existing == comment;
		});
		if (!exists) {
			items.add(comment);
		}
		if (masterEntity instanceof CEntityDB<?>) {
			final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
			if (entity.getId() != null) {
				saveMasterEntity(entity);
			} else {
				LOGGER.warn("Master entity has no ID; comment will persist when the parent entity is saved");
			}
		}
	}

	@Override
	public void notifyRefreshListeners(final CComment changedItem) {
		if (!refreshListeners.isEmpty()) {
			for (final Consumer<CComment> listener : refreshListeners) {
				try {
					listener.accept(changedItem);
				} catch (final Exception e) {
					LOGGER.error("Error notifying refresh listener", e);
				}
			}
		}
	}

	/** Handle add button click. */
	protected void on_buttonAdd_clicked() {
		try {
			if (masterEntity == null) {
				CNotificationService.showWarning("Please select an entity first");
				return;
			}
			final Object parentEntity = masterEntity;
			if (!(parentEntity instanceof CEntityDB)) {
				CNotificationService.showError("Entity does not support comments");
				LOGGER.error("Master entity does not extend CEntityDB: {}", parentEntity.getClass().getSimpleName());
				return;
			}
			// Create new comment with current user as author
			final CUser currentUser = sessionService.getActiveUser().orElse(null);
			if (currentUser == null) {
				CNotificationService.showError("No active user found");
				LOGGER.error("Cannot create comment: no active user in session");
				return;
			}
			final CComment newComment = new CComment("", currentUser);
			final CDialogComment dialog = new CDialogComment(commentService, sessionService, newComment, comment -> {
				try {
					linkCommentToMaster(comment);
					refreshGrid();
					notifyRefreshListeners(comment);
				} catch (final Exception e) {
					LOGGER.error("Error refreshing grid after adding comment", e);
				}
			}, true);
			dialog.open();
		} catch (final Exception e) {
			CNotificationService.showException("Error opening comment dialog", e);
		}
	}

	/** Handle delete button click. */
	protected void on_buttonDelete_clicked() {
		try {
			final CComment selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No comment selected");
			CNotificationService.showConfirmationDialog("Delete this comment?", () -> {
				try {
					unlinkCommentFromMaster(selected);
					commentService.delete(selected);
					refreshGrid();
					notifyRefreshListeners(selected);
					CNotificationService.showDeleteSuccess();
				} catch (final Exception e) {
					CNotificationService.showException("Error deleting comment", e);
				}
			});
		} catch (final Exception e) {
			CNotificationService.showException("Failed to delete comment", e);
		}
	}

	/** Handle edit button click. */
	protected void on_buttonEdit_clicked() {
		try {
			final CComment selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No comment selected");
			final CDialogComment dialog = new CDialogComment(commentService, sessionService, selected, comment -> {
				try {
					commentService.save(comment);
					refreshGrid();
					notifyRefreshListeners(comment);
				} catch (final Exception e) {
					LOGGER.error("Error saving comment", e);
					CNotificationService.showException("Error saving comment", e);
				}
			}, false);
			dialog.open();
		} catch (final Exception e) {
			CNotificationService.showException("Error opening edit dialog", e);
		}
	}

	/** Handle grid double-click to edit. */
	protected void on_grid_doubleClicked(final CComment comment) {
		if (comment != null) {
			on_buttonEdit_clicked();
		}
	}

	/** Handle grid selection changes. */
	private void on_grid_selectionChanged(final CComment selected) {
		buttonEdit.setEnabled(selected != null);
		buttonDelete.setEnabled(selected != null);
	}

	@Override
	public void populateForm() {
		refreshGrid();
	}

	@Override
	public void refreshGrid() {
		Check.notNull(grid, "Grid cannot be null when refreshing comments");
		if (masterEntity == null) {
			LOGGER.debug("Master entity is null, clearing grid");
			clearGrid();
			return;
		}
		// Load comments from parent entity's collection
		final List<CComment> items = new ArrayList<>(masterEntity.getComments());
		// Sort by date descending (newest first)
		items.sort((c1, c2) -> {
			if (c1.getCreatedDate() == null && c2.getCreatedDate() == null) {
				return 0;
			}
			if (c1.getCreatedDate() == null) {
				return 1;
			}
			if (c2.getCreatedDate() == null) {
				return -1;
			}
			return c2.getCreatedDate().compareTo(c1.getCreatedDate());
		});
		grid.setItems(items);
		grid.asSingleSelect().clear();
		updateCompactMode(items.isEmpty());
		LOGGER.debug("Loaded {} comments for entity", items.size());
	}

	@Override
	public void removeRefreshListener(final Consumer<CComment> listener) {
		if (listener != null) {
			refreshListeners.remove(listener);
		}
	}

	private void saveMasterEntity(final CEntityDB<?> entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			saveMasterEntityTyped(entity);
		} catch (final Exception e) {
			LOGGER.error("Failed to save master entity after comment update", e);
			CNotificationService.showException("Failed to save comment to parent entity", e);
		}
	}

	@SuppressWarnings ("unchecked")
	private <T extends CEntityDB<T>> void saveMasterEntityTyped(final CEntityDB<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService<T> service = (CAbstractService<T>) CSpringContext.getBean(serviceClass);
		service.save((T) entity);
	}

	public void setEntity(final Object entity) {
		if (entity == null) {
			setValue(null);
			return;
		}
		if (entity instanceof CEntityDB<?>) {
			setValue((CEntityDB<?>) entity);
			return;
		}
		if (entity instanceof IHasComments) {
			masterEntity = (IHasComments) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("Entity does not implement IHasComments: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	/** Set the master entity for this component.
	 * @param masterEntity the master entity that owns the comments */
	public void setMasterEntity(final IHasComments masterEntity) {
		this.masterEntity = masterEntity;
		refreshGrid();
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		if (entity == null) {
			masterEntity = null;
			clearGrid();
			return;
		}
		if (entity instanceof IHasComments) {
			masterEntity = (IHasComments) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("setValue called with unexpected entity type: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	private void unlinkCommentFromMaster(final CComment comment) {
		Check.notNull(comment, "Comment cannot be null");
		Check.notNull(masterEntity, "Master entity cannot be null");
		Check.instanceOf(masterEntity, CEntityDB.class, "Master entity must support database persistence");
		final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
		Check.notNull(entity.getId(), "Master entity must be saved before deleting comments");
		final Set<CComment> items = masterEntity.getComments();
		Check.notNull(items, "Comments list cannot be null");
		final Long commentId = comment.getId();
		final boolean removed = items.removeIf(existing -> {
			if (commentId != null && existing != null) {
				return commentId.equals(existing.getId());
			}
			return existing == comment;
		});
		Check.isTrue(removed, "Comment not found in master entity");
		saveMasterEntity(entity);
	}

	/** Update component height based on content.
	 * @param isEmpty true if no comments exist */
	private void updateCompactMode(final boolean isEmpty) {
		if (isEmpty) {
			// Compact mode: narrow height when empty
			grid.setHeight("150px");
			setHeight("200px"); // Component total height
			// LOGGER.debug("Compact mode: No comments");
		} else {
			// Normal mode: full height when has content
			grid.setHeight("300px");
			setHeight("auto"); // Component auto-adjusts
			// LOGGER.debug("Normal mode: Has comments");
		}
	}
}
