package tech.derbent.app.kanban.kanbanline.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CComponentStoryPoint;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IHasSelectionNotification;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentPostit - A compact post-it style widget for displaying project items inside kanban columns. */
public class CComponentKanbanPostit extends CComponentWidgetEntity<CSprintItem> implements IHasSelectionNotification {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanPostit.class);
	private static final long serialVersionUID = 1L;
	private DragSource<CComponentKanbanPostit> dragSource;
	private boolean dropEnabled;
	private DropTarget<CComponentKanbanPostit> dropTarget;
	private Runnable refreshCallback;
	private final Set<ComponentEventListener<CSelectEvent>> selectListeners = new HashSet<>();

	/** Creates a post-it card for the given sprint item. */
	public CComponentKanbanPostit(final CSprintItem item) {
		super(item);
		Check.notNull(item, "Sprint item cannot be null for postit");
		addClassName("kanban-postit");
		final String height = "120px";
		getStyle().set("width", "100%").set("height", height).set("min-height", height).set("max-height", height);
		setPadding(true);
		setSpacing(false);
		getElement().setAttribute("tabindex", "0");
		addClickListener(on_component_click());
	}

	/** Builds the primary title line. */
	@Override
	protected void createFirstLine() throws Exception {
		final ISprintableItem item = resolveSprintableItem();
		layoutLineOne.setWidthFull();
		if (item instanceof CEntityDB<?>) {
			layoutLineOne.add(CLabelEntity.createH6Label((CEntityDB<?>) item));
		} else {
			layoutLineOne.add(CLabelEntity.createH6Label(item != null ? item.getName() : ""));
		}
	}

	/** Builds the status and responsible line. */
	@Override
	protected void createSecondLine() throws Exception {
		final ISprintableItem item = resolveSprintableItem();
		if (item == null) {
			return;
		}
		// Create a compact horizontal layout for status and story points
		layoutLineTwo.setWidthFull();
		layoutLineTwo.setJustifyContentMode(JustifyContentMode.BETWEEN);
		layoutLineTwo.setAlignItems(Alignment.CENTER);
		layoutLineTwo.getStyle().set("margin-top", "4px");
		// Left side: Status label
		if (item.getStatus() != null) {
			final CLabelEntity statusLabel = new CLabelEntity(item.getStatus());
			statusLabel.getStyle().set("font-size", "11px");
			layoutLineTwo.add(statusLabel);
		}
		// Right side: Editable story points (ALWAYS show, even if 0)
		final CComponentStoryPoint storyPointComponent = new CComponentStoryPoint(item, this::saveStoryPoint, this::handleStoryPointError);
		storyPointComponent.getStyle().set("font-size", "11px").set("font-weight", "600");
		layoutLineTwo.add(storyPointComponent);
	}

	/** Builds the date range line when available. */
	@Override
	protected void createThirdLine() {
		final ISprintableItem item = resolveSprintableItem();
		layoutLineThree.setWidthFull();
		layoutLineThree.setJustifyContentMode(JustifyContentMode.BETWEEN);
		layoutLineThree.setAlignItems(Alignment.CENTER);
		layoutLineThree.getStyle().set("margin-top", "4px");
		// Left side: Compact user icon
		if (item != null && item.getAssignedTo() != null) {
			final CLabelEntity userLabel = CLabelEntity.createCompactUserLabel(item.getAssignedTo());
			userLabel.getStyle().set("font-size", "10px");
			layoutLineThree.add(userLabel);
		}
		// Right side: Compact date range
		if (item != null && (item.getStartDate() != null || item.getEndDate() != null)) {
			final CLabelEntity dateLabel = CLabelEntity.createCompactDateRangeLabel(item.getStartDate(), item.getEndDate());
			layoutLineThree.add(dateLabel);
		}
	}

	@Override
	public void drag_checkEventAfterPass(final CEvent event) {
		// No-op hook for now; included for symmetry with drag_checkEventBeforePass.
	}

	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
		// LOGGER.debug("[KanbanDrag] Passing drag event {} for sprint item {}", event.getClass().getSimpleName(), entity.getId());
	}

	@Override
	public void drag_setDragEnabled(final boolean enabled) {
		if (enabled) {
			initializeDragSource();
		} else if (dragSource != null) {
			dragSource.setDraggable(false);
		}
	}

	@Override
	public void drag_setDropEnabled(final boolean enabled) {
		dropEnabled = enabled;
		if (enabled) {
			initializeDropTarget();
			return;
		}
		if (dropTarget != null) {
			dropTarget.setActive(false);
		}
	}

	/** Handles story point validation errors. */
	private void handleStoryPointError(final Exception e) {
		LOGGER.error("Story point validation error", e);
		CNotificationService.showError(e.getMessage());
	}

	@SuppressWarnings ("unused")
	private void initializeDragSource() {
		if (dragSource != null) {
			dragSource.setDraggable(true);
			return;
		}
		dragSource = DragSource.create(this);
		dragSource.addDragStartListener(event -> {
			final List<Object> draggedItems = List.of(entity);
			final CDragStartEvent dragStartEvent = new CDragStartEvent(this, draggedItems, true);
			LOGGER.debug("[KanbanDrag] Drag start for sprint item {}", entity.getId());
			notifyEvents(dragStartEvent);
		});
		dragSource.addDragEndListener(event -> {
			LOGGER.debug("[KanbanDrag] Drag end for sprint item {}", entity.getId());
			final CDragEndEvent dragEndEvent = new CDragEndEvent(this, true);
			notifyEvents(dragEndEvent);
		});
	}

	@SuppressWarnings ("unused")
	private void initializeDropTarget() {
		if (dropTarget == null) {
			dropTarget = DropTarget.create(this);
			dropTarget.addDropListener(event -> {
				final CDragDropEvent dropEvent = new CDragDropEvent(getId().orElse("None"), this, entity, GridDropLocation.ON_TOP, true);
				LOGGER.debug("[KanbanDrag] Drop on post-it for sprint item {}", entity.getId());
				notifyEvents(dropEvent);
			});
		}
		dropTarget.setDropEffect(DropEffect.MOVE);
		dropTarget.setActive(dropEnabled);
	}

	@SuppressWarnings ("unused")
	private ComponentEventListener<ClickEvent<HorizontalLayout>> on_component_click() {
		return event -> {
			LOGGER.debug("Kanban post-it clicked for sprint item {}", entity.getId());
			final CSelectEvent selectEvent = new CSelectEvent(this, true);
			select_notifyEvents(selectEvent);
		};
	}

	/** Resolves the sprintable item for display. */
	public ISprintableItem resolveSprintableItem() {
		final CSprintItem sprintItem = entity;
		return sprintItem != null ? sprintItem.getParentItem() : null;
	}

	/** Saves the story point change and notifies parent containers. */
	private void saveStoryPoint(final ISprintableItem item) {
		try {
			item.saveProjectItem();
			CNotificationService.showSaveSuccess();
			if (refreshCallback != null) {
				refreshCallback.run();
			}
			LOGGER.debug("Story point saved successfully for item {}", item.getId());
		} catch (final Exception e) {
			LOGGER.error("Error saving story point for item {}", item.getId(), e);
			throw e;
		}
	}

	@Override
	public void select_checkEventAfterPass(final CEvent event) {
		LOGGER.debug("[KanbanSelect] Selection event propagated for sprint item {}", entity.getId());
	}

	@Override
	public void select_checkEventBeforePass(final CEvent event) {
		Check.notNull(event, "Selection event cannot be null for kanban post-it");
		LOGGER.debug("[KanbanSelect] Handling selection for sprint item {}", entity.getId());
	}

	@Override
	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners() {
		return selectListeners;
	}

	/** Sets the refresh callback for notifying parent containers when story points change. */
	public void setRefreshCallback(final Runnable callback) {
		refreshCallback = callback;
	}

	/** Sets selected styles for the post-it. */
	@Override
	public void setSelected(final boolean selected) {
		this.selected = selected;
		if (selected) {
			addClassName("kanban-postit-selected");
		} else {
			removeClassName("kanban-postit-selected");
		}
	}
}
