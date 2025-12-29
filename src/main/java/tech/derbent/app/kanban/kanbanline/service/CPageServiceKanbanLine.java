package tech.derbent.app.kanban.kanbanline.service;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
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
import tech.derbent.app.page.view.CDynamicPageViewWithoutGrid;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.workflow.service.CWorkflowEntityService;

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

	private void handleKanbanDrop(final CDragDropEvent event) {
		try {
			LOGGER.debug("Handling Kanban board drop event.");
			final CDragStartEvent dragStartEvent = getActiveDragStartEvent();
			Check.notNull(dragStartEvent, "Active drag start event required for Kanban drop handling");
			final Object draggedItem = dragStartEvent.getDraggedItems().isEmpty() ? null : dragStartEvent.getDraggedItems().get(0);
			Check.instanceOf(draggedItem, CSprintItem.class, "Dragged item must be a sprint item for Kanban drop");
			final CSprintItem sprintItem = (CSprintItem) draggedItem;
			final CKanbanColumn targetColumn = resolveTargetColumn(event);
			Check.notNull(targetColumn, "Target column cannot be resolved for Kanban drop");
			// set it? not here !
			sprintItem.setKanbanColumnId(targetColumn.getId());
			final List<CProjectItemStatus> targetStatuses = kanbanColumnService.resolveStatusesForColumn(targetColumn, sprintItem);
			if (targetStatuses.isEmpty()) {
				LOGGER.warn("No statuses mapped to target column {}, sprint item {} status not changed.", targetColumn.getName(), sprintItem.getId());
				CNotificationService.showWarning("The target column has no statuses mapped. Sprint item status was not changed.");
				return;
			}
			final CProjectItemStatus targetStatus = targetStatuses.get(0);
			if (targetStatuses.size() > 1) {
				// TODO ask user to choose?
				LOGGER.info("Multiple statuses mapped to target column {}, sprint item {} status set to first status" + " {}.",
						targetColumn.getName(), sprintItem.getId(), targetStatus.getName());
				CNotificationService
						.showInfo("Multiple statuses are mapped to the target column. Sprint item status set to " + targetStatus.getName() + ".");
			}
			final CProjectItemStatus newStatus = targetStatuses.get(0);
			// check if the workflow allows this transition?
			final CWorkflowEntityService workflowEntityService = CSpringContext.getBean(CWorkflowEntityService.class);
			if (!workflowEntityService.checkStatusTransitionAllowed(sprintItem.getItem(), sprintItem.getStatus(), newStatus)) {
				CNotificationService.showError(
						"Status transition to " + newStatus.getName() + " is not allowed by workflow. Sprint item status was not changed.");
				return;
			}
			sprintItem.getItem().setStatus(newStatus);
			componentKanbanBoard.refreshComponent();
			setActiveDragStartEvent(null);
		} catch (final Exception e) {
			LOGGER.error("Failed to handle Kanban board drop", e);
			throw e;
		}
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
