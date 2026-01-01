package tech.derbent.app.kanban.kanbanline.service;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.kanban.kanbanline.view.CComponentKanbanBoard;
import tech.derbent.app.kanban.kanbanline.view.CComponentKanbanColumn;
import tech.derbent.app.kanban.kanbanline.view.CComponentKanbanPostit;
import tech.derbent.app.kanban.kanbanline.view.CComponentListKanbanColumns;
import tech.derbent.app.kanban.kanbanline.view.CDialogKanbanStatusSelection;
import tech.derbent.app.page.view.CDynamicPageViewWithoutGrid;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

public class CPageServiceKanbanLine extends CPageServiceDynamicPage<CKanbanLine> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceKanbanLine.class);
	private CComponentKanbanBoard componentKanbanBoard;
	private CComponentListKanbanColumns componentKanbanColumns;
	private CKanbanColumnService kanbanColumnService;
	private CKanbanLineService kanbanLineService;

	/** Creates the page service and resolves kanban dependencies. */
	public CPageServiceKanbanLine(final IPageServiceImplementer<CKanbanLine> view) {
		super(view);
		try {
			kanbanColumnService = CSpringContext.getBean(CKanbanColumnService.class);
			kanbanLineService = CSpringContext.getBean(CKanbanLineService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize Kanban services", e);
		}
	}

	/** Binds the kanban line page and adjusts layout sizing. */
	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CKanbanLine.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
			if (getView() instanceof CDynamicPageViewWithoutGrid) {
				// after form creation
				final CHorizontalLayout layout = getView().getDetailsBuilder().getFormBuilder().getHorizontalLayout("kanbanBoard");
				Objects.requireNonNull(layout, "Kanban board layout must not be null");
				layout.setHeightFull();
			}
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CKanbanLine.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	/** Builds or returns the cached kanban board component. */
	public CComponentKanbanBoard createKanbanBoardComponent() {
		// it is null when ui is created
		// Check.notNull(currentLine, "Kanban line must be available to create board component");
		if (componentKanbanBoard == null) {
			componentKanbanBoard = new CComponentKanbanBoard();
			componentKanbanBoard.registerWithPageService(this);
		}
		// this is always null here, no problem
		// componentKanbanBoard.setValue(currentLine); let the binder handle this
		return componentKanbanBoard;
	}

	/** Builds or returns the cached kanban columns list component. */
	public CComponentListKanbanColumns createKanbanColumnsComponent() {
		LOGGER.debug("Creating Kanban columns component for Kanban line page service.");
		if (componentKanbanColumns == null) {
			componentKanbanColumns = new CComponentListKanbanColumns(kanbanLineService, kanbanColumnService);
			componentKanbanColumns.registerWithPageService(this);
		}
		return componentKanbanColumns;
	}

	/** Handles kanban board drop events by updating sprint item status and kanban column assignment.
	 * 
	 * This is the core drag-drop workflow for the kanban board:
	 * 1. Extract dragged sprint item from active drag start event
	 * 2. Resolve target kanban column from drop event
	 * 3. Update sprint item's kanban column ID (visual assignment)
	 * 4. Resolve valid status(es) for target column (workflow-aware status mapping)
	 * 5a. If ONE valid status: automatically set it and save
	 * 5b. If MULTIPLE valid statuses: show selection dialog for user choice
	 * 5c. If NO valid statuses: save kanban column assignment without status change
	 * 6. Save changes to database
	 * 7. Refresh kanban board to reflect changes
	 * 
	 * Status Resolution Logic:
	 * - Gets statuses mapped to target column (column.getIncludedStatuses())
	 * - Intersects with workflow-valid transitions (getValidNextStatuses())
	 * - Single status: automatic transition
	 * - Multiple statuses: user selection via dialog
	 * - No statuses: warning notification, but still saves column assignment
	 * 
	 * CRITICAL BUG FIX: Always save sprint item to persist kanbanColumnId changes,
	 * even when no valid status transition exists. Previously, returning early
	 * without save caused drag-drop to appear broken (item visually moved but 
	 * position not persisted to database).
	 * 
	 * This ensures drag-drop respects both kanban column mappings AND workflow transition rules.
	 */
	private void handleKanbanDrop(final CDragDropEvent event) {
		try {
			LOGGER.debug("Handling Kanban board drop event.");
			
			// Step 1: Extract dragged sprint item from active drag start event
			final CDragStartEvent dragStartEvent = getActiveDragStartEvent();
			Check.notNull(dragStartEvent, "Active drag start event required for Kanban drop handling");
			final Object draggedItem = dragStartEvent.getDraggedItems().isEmpty() ? null : dragStartEvent.getDraggedItems().get(0);
			Check.instanceOf(draggedItem, CSprintItem.class, "Dragged item must be a sprint item for Kanban drop");
			final CSprintItem sprintItem = (CSprintItem) draggedItem;
			final CProjectItem<?> item = (CProjectItem<?>) sprintItem.getItem();
			
			// Step 2: Resolve target kanban column from drop event (column, post-it, or component)
			final CKanbanColumn targetColumn = resolveTargetColumn(event);
			Check.notNull(targetColumn, "Target column cannot be resolved for Kanban drop");
			
			// Step 3: Update sprint item's kanban column ID (visual assignment for board display)
			sprintItem.setKanbanColumnId(targetColumn.getId());
			
			// Step 4: Resolve valid status(es) for target column
			// This intersects column's included statuses with workflow-valid transitions
			final CProjectItemStatusService projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final List<CProjectItemStatus> targetStatuses =
					projectItemStatusService.resolveStatusesForColumn(targetColumn, (IHasStatusAndWorkflow<?>) item);
			
			// Step 5: Handle status transition based on number of valid statuses
			if (targetStatuses.isEmpty()) {
				// Case 1: No valid status for this column - update column assignment but not status
				LOGGER.warn("No valid workflow transitions to target column {}, sprint item {} status not changed.", 
					targetColumn.getName(), sprintItem.getId());
				
				// CRITICAL: Still save the sprint item to persist kanbanColumnId change
				// This was a bug - returning early without save caused drag-drop to appear broken
				saveSprintItemOnly(sprintItem);
				
				// Reload sprint items from database to get updated data, then refresh board
				componentKanbanBoard.reloadSprintItems();
				componentKanbanBoard.refreshComponent();
				
				// Warn user that status couldn't be changed
				CNotificationService.showWarning(
					"Item moved to '" + targetColumn.getName() + "' column, but status remains '" + 
					((ISprintableItem) item).getStatus().getName() + "' (no valid workflow transition available).");
			} else if (targetStatuses.size() == 1) {
				// Case 2: Exactly one valid status - automatically apply it
				final CProjectItemStatus newStatus = targetStatuses.get(0);
				LOGGER.info("Single status available for column {}, automatically setting status to {} for sprint item {}",
						targetColumn.getName(), newStatus.getName(), sprintItem.getId());
				applyStatusAndSave(item, sprintItem, newStatus);
			} else {
				// Case 3: Multiple valid statuses - show selection dialog for user to choose
				LOGGER.info("Multiple statuses ({}) available for column {}, showing selection dialog for sprint item {}",
						targetStatuses.size(), targetColumn.getName(), sprintItem.getId());
				showStatusSelectionDialog(item, sprintItem, targetColumn, targetStatuses);
			}
			
			// Clear active drag state (if not showing dialog, otherwise cleared after dialog closes)
			if (targetStatuses.size() == 1 || targetStatuses.isEmpty()) {
				setActiveDragStartEvent(null);
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to handle Kanban board drop", e);
			setActiveDragStartEvent(null);
			throw e;
		}
	}
	
	/** Applies the selected status to the project item and saves it.
	 * 
	 * This method encapsulates the save logic so it can be called both from
	 * automatic single-status transitions and from dialog-based multi-status selections.
	 * 
	 * @param item The project item to update
	 * @param sprintItem The sprint item wrapping the project item
	 * @param newStatus The status to apply
	 */
	private void applyStatusAndSave(final CProjectItem<?> item, final CSprintItem sprintItem, final CProjectItemStatus newStatus) {
		try {
			Check.notNull(item, "Project item cannot be null");
			Check.notNull(sprintItem, "Sprint item cannot be null");
			Check.notNull(newStatus, "New status cannot be null");
			
			// Update project item status
			((ISprintableItem) item).setStatus(newStatus);
			
			// Save project item using its specific service (dynamic service lookup by entity class)
			final Class<?> projectItemServiceClass = CEntityRegistry.getServiceClassForEntity(item.getClass());
			final CProjectItemService<?> projectItemService = (CProjectItemService<?>) CSpringContext.getBean(projectItemServiceClass);
			projectItemService.revokeSave(item);  // revokeSave = save bypassing some validations for system updates
			
			// Reload sprint items from database to get updated data, then refresh board
			componentKanbanBoard.reloadSprintItems();
			componentKanbanBoard.refreshComponent();
			
			// Show success notification
			CNotificationService.showSuccess("Status updated to '" + newStatus.getName() + "'");
			
			LOGGER.info("Successfully updated sprint item {} status to {}", sprintItem.getId(), newStatus.getName());
		} catch (final Exception e) {
			LOGGER.error("Failed to apply status and save project item", e);
			CNotificationService.showError("Failed to update status: " + e.getMessage());
			// Refresh board anyway to reset visual state
			componentKanbanBoard.reloadSprintItems();
			componentKanbanBoard.refreshComponent();
			throw e;
		}
	}
	
	/** Saves sprint item to persist kanbanColumnId without changing the project item status.
	 * 
	 * This method is used when drag-drop updates the visual column assignment but there
	 * is no valid workflow transition available. The sprint item must still be saved to
	 * persist the kanbanColumnId change, otherwise the drag-drop appears broken (item
	 * visually moves but position is lost on refresh).
	 * 
	 * CRITICAL: This fixes a bug where returning early without save caused inconsistent
	 * behavior - sometimes drag-drop worked (when status transition was valid), sometimes
	 * it didn't (when no valid transition).
	 * 
	 * @param sprintItem The sprint item to save (only kanbanColumnId is modified)
	 */
	private void saveSprintItemOnly(final CSprintItem sprintItem) {
		try {
			Check.notNull(sprintItem, "Sprint item cannot be null");
			Check.notNull(sprintItem.getId(), "Sprint item must have ID to save");
			
			// Get sprint item service and save
			final tech.derbent.app.sprints.service.CSprintItemService sprintItemService = 
				CSpringContext.getBean(tech.derbent.app.sprints.service.CSprintItemService.class);
			sprintItemService.save(sprintItem);
			
			LOGGER.info("Saved sprint item {} with kanbanColumnId {} (no status change)", 
				sprintItem.getId(), sprintItem.getKanbanColumnId());
		} catch (final Exception e) {
			LOGGER.error("Failed to save sprint item kanban column assignment", e);
			CNotificationService.showError("Failed to save column assignment: " + e.getMessage());
			throw e;
		}
	}
	
	/** Shows a dialog for the user to select which status to apply when multiple valid statuses exist.
	 * 
	 * This dialog displays all valid statuses with their colors and icons, allowing the user
	 * to choose the appropriate status for the transition. If the user cancels, the status
	 * is not changed but the kanban column assignment remains (visual-only change).
	 * 
	 * @param item The project item being moved
	 * @param sprintItem The sprint item wrapping the project item
	 * @param targetColumn The column being dropped onto
	 * @param targetStatuses List of valid statuses the user can choose from (must have at least 2 items)
	 */
	private void showStatusSelectionDialog(final CProjectItem<?> item, final CSprintItem sprintItem,
			final CKanbanColumn targetColumn, final List<CProjectItemStatus> targetStatuses) {
		Check.notNull(item, "Project item cannot be null");
		Check.notNull(sprintItem, "Sprint item cannot be null");
		Check.notNull(targetColumn, "Target column cannot be null");
		Check.notEmpty(targetStatuses, "Target statuses list cannot be empty");
		Check.isTrue(targetStatuses.size() >= 2, "Status selection dialog requires at least 2 statuses");
		
		// Create and open the status selection dialog
		final CDialogKanbanStatusSelection dialog = new CDialogKanbanStatusSelection(
			targetColumn.getName(),
			targetStatuses,
			selectedStatus -> {
				// This callback is invoked when user selects a status or cancels
				if (selectedStatus != null) {
					// User selected a status: apply it and save
					LOGGER.info("User selected status {} for sprint item {}", selectedStatus.getName(), sprintItem.getId());
					applyStatusAndSave(item, sprintItem, selectedStatus);
				} else {
					// User cancelled: keep kanban column assignment but don't change status
					LOGGER.info("User cancelled status selection for sprint item {}, saving column change only", sprintItem.getId());
					
					// Still save sprint item to persist kanbanColumnId change
					saveSprintItemOnly(sprintItem);
					
					// Reload sprint items from database to get updated data, then refresh board
					componentKanbanBoard.reloadSprintItems();
					componentKanbanBoard.refreshComponent();
					
					CNotificationService.showInfo("Item moved to '" + targetColumn.getName() + 
						"' column, status remained '" + ((ISprintableItem) item).getStatus().getName() + "'.");
				}
				// Clear active drag state after dialog closes
				setActiveDragStartEvent(null);
			}
		);
		
		dialog.open();
	}

	@SuppressWarnings ("unused")
	public void on_kanbanBoard_dragEnd(final Component component, final Object value) {
		LOGGER.debug("Kanban board drag end event received. Active drag item name is {}.",
				getActiveDragStartEvent() != null && !getActiveDragStartEvent().getDraggedItems().isEmpty()
						? getActiveDragStartEvent().getDraggedItems().get(0).toString() : "None");
		setActiveDragStartEvent(null);
	}

	public void on_kanbanBoard_dragStart(@SuppressWarnings ("unused") final Component component, final Object value) {
		LOGGER.debug("Kanban board drag start event received.");
		Check.instanceOf(value, CDragStartEvent.class, "Drag value must be CDragStartEvent");
		setActiveDragStartEvent((CDragStartEvent) value);
	}

	public void on_kanbanBoard_drop(@SuppressWarnings ("unused") final Component component, final Object value) {
		LOGGER.debug("Kanban board drop event received.");
		Check.instanceOf(value, CDragDropEvent.class, "Drop value must be CDragDropEvent");
		final CDragDropEvent event = (CDragDropEvent) value;
		handleKanbanDrop(event);
	}

	@SuppressWarnings ("static-method")
	public void on_kanbanBoard_selected(@SuppressWarnings ("unused") final Component component, final Object value) {
		LOGGER.debug("Kanban board selection event received.");
		Check.instanceOf(value, CSelectEvent.class, "Selection value must be CSelectEvent");
		final CSelectEvent event = (CSelectEvent) value;
		if (event.getSource() instanceof final CComponentKanbanPostit postit) {
			LOGGER.info("[KanbanSelect] Post-it selected for sprint item {}", postit.getEntity().getId());
		} else {
			LOGGER.debug("[KanbanSelect] Kanban board selection event from {}", event.getSource().getClass().getSimpleName());
		}
	}

	/** Hook executed after binding for optional post-load work. */
	public void on_load_after_bind() throws Exception {
		// todo: implement if needed
	}

	private CKanbanColumn resolveTargetColumn(final CDragDropEvent event) {
		final Object targetItem = event.getTargetItem();
		if (targetItem instanceof final CKanbanColumn column) {
			return column;
		}
		if (targetItem instanceof final CSprintItem targetSprintItem && targetSprintItem.getKanbanColumnId() != null && componentKanbanBoard != null
				&& componentKanbanBoard.getValue() != null) {
			return componentKanbanBoard.getValue().getKanbanColumns().stream().filter(col -> targetSprintItem.getKanbanColumnId().equals(col.getId()))
					.findFirst().orElse(null);
		}
		if (event.getDropTarget() instanceof final CComponentKanbanColumn columnComponent) {
			return columnComponent.getValue();
		}
		return null;
	}
}
