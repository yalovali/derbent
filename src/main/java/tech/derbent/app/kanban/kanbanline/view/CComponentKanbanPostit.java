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
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IHasSelectionNotification;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentPostit - A compact post-it style widget for displaying project items inside kanban columns. */
public class CComponentKanbanPostit extends CComponentWidgetEntity<CSprintItem> implements IHasSelectionNotification {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanPostit.class);
	private static final long serialVersionUID = 1L;
	private DragSource<CComponentKanbanPostit> dragSource;
	private DropTarget<CComponentKanbanPostit> dropTarget;
	private final Set<ComponentEventListener<CSelectEvent>> selectListeners = new HashSet<>();
	private boolean dropEnabled;

	/** Creates a post-it card for the given sprint item. */
	public CComponentKanbanPostit(final CSprintItem item) {
		super(item);
		Check.notNull(item, "Sprint item cannot be null for postit");
		addClassName("kanban-postit");
		getStyle().set("width", "100%");
		getElement().setAttribute("tabindex", "0");
		addClickListener(on_component_click());
	}

	/** Builds the primary title line. */
	@Override
	protected void createFirstLine() throws Exception {
		final ISprintableItem item = resolveSprintableItem();
		layoutLineOne.setWidthFull();
		if (item instanceof CEntityDB<?>) {
			layoutLineOne.add(CLabelEntity.createH3Label((CEntityDB<?>) item));
		} else {
			layoutLineOne.add(CLabelEntity.createH3Label(item != null ? item.getName() : ""));
		}
	}

	/** Builds the status and responsible line. */
	@Override
	protected void createSecondLine() throws Exception {
		final ISprintableItem item = resolveSprintableItem();
		if (item == null) {
			return;
		}
		if (item.getStatus() != null) {
			layoutLineTwo.add(new CLabelEntity(item.getStatus()));
		}
		if (item.getResponsible() != null) {
			layoutLineTwo.add(CLabelEntity.createUserLabel(item.getResponsible()));
		}
	}

	/** Builds the date range line when available. */
	@Override
	protected void createThirdLine() {
		final ISprintableItem item = resolveSprintableItem();
		if (item != null && (item.getStartDate() != null || item.getEndDate() != null)) {
			layoutLineThree.add(CLabelEntity.createDateRangeLabel(item.getStartDate(), item.getEndDate()));
		}
	}

	@Override
	public void drag_checkEventAfterPass(final tech.derbent.api.interfaces.drag.CEvent event) {
		// No-op hook for now; included for symmetry with drag_checkEventBeforePass.
	}

	@Override
	public void drag_checkEventBeforePass(final tech.derbent.api.interfaces.drag.CEvent event) {
		LOGGER.debug("[KanbanDrag] Passing drag event {} for sprint item {}", event.getClass().getSimpleName(), entity.getId());
	}

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
		return sprintItem != null ? sprintItem.getItem() : null;
	}

	@Override
	public void select_checkEventAfterPass(final tech.derbent.api.interfaces.drag.CEvent event) {
		LOGGER.debug("[KanbanSelect] Selection event propagated for sprint item {}", entity.getId());
	}

	@Override
	public void select_checkEventBeforePass(final tech.derbent.api.interfaces.drag.CEvent event) {
		Check.notNull(event, "Selection event cannot be null for kanban post-it");
		LOGGER.debug("[KanbanSelect] Handling selection for sprint item {}", entity.getId());
	}

	@Override
	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners() {
		return selectListeners;
	}

	@Override
	public void setDragEnabled(final boolean enabled) {
		if (enabled) {
			initializeDragSource();
		} else if (dragSource != null) {
			dragSource.setDraggable(false);
		}
	}

	@Override
	public void setDropEnabled(final boolean enabled) {
		dropEnabled = enabled;
		if (enabled) {
			initializeDropTarget();
			return;
		}
		if (dropTarget != null) {
			dropTarget.setActive(false);
		}
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
