package tech.derbent.app.kanban.kanbanline.view;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.binder.Binder;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.IHasSelectionNotification;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentKanbanColumn - Renders a single kanban column with its header and post-it items. */
public class CComponentKanbanColumn extends CComponentBase<CKanbanColumn> implements IHasSelectionNotification, IHasDragControl {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanColumn.class);
	private static final long serialVersionUID = 1L;
	private final Binder<CKanbanColumn> binder;
	private DropTarget<CVerticalLayout> columnDropTarget;
	private final Span defaultBadge;
	private boolean dragEnabled;
	private final Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new HashSet<>();
	private boolean dropEnabled;
	private final Set<ComponentEventListener<CDragDropEvent>> dropListeners = new HashSet<>();
	private final CHorizontalLayout headerLayout;
	private final CVerticalLayout itemsLayout;
	private final Set<ComponentEventListener<CSelectEvent>> selectListeners = new HashSet<>();
	private List<CSprintItem> sprintItems = List.of();
	private final CLabelEntity statusesLabel;
	private final CH3 title;

	/** Creates the kanban column component and its layout. */
	public CComponentKanbanColumn() {
		setPadding(true);
		setSpacing(true);
		setWidth("280px");
		setMinHeight("500px");
		setHeight(null);
		setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
		addClassName("kanban-column");
		getStyle().set("border-radius", "10px").set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");
		headerLayout = new CHorizontalLayout();
		headerLayout.setWidthFull();
		headerLayout.setSpacing(true);
		title = new CH3("");
		title.getStyle().set("margin", "0");
		defaultBadge = new Span("Default");
		defaultBadge.getStyle().set("background-color", "#E3F2FD").set("color", "#0D47A1").set("padding", "2px 6px").set("border-radius", "6px")
				.set("font-size", "10px").set("font-weight", "600");
		headerLayout.add(title);
		add(headerLayout);
		statusesLabel = new CLabelEntity();
		statusesLabel.getStyle().set("font-size", "11px").set("color", "#666");
		add(statusesLabel);
		itemsLayout = new CVerticalLayout(false, true, false);
		itemsLayout.setPadding(false);
		itemsLayout.setSpacing(true);
		itemsLayout.setWidthFull();
		itemsLayout.setHeight(null);
		itemsLayout.addClassName("kanban-column-items");
		add(itemsLayout);
		binder = new Binder<>(CKanbanColumn.class);
		binder.forField(this).bind(value -> value, (bean, value) -> {/**/});
		initializeColumnDropTarget();
	}

	/** Applies the configured background color for the column. */
	private void applyBackgroundColor() {
		final CKanbanColumn column = getValue();
		final String backgroundColor =
				column != null && column.getColor() != null && !column.getColor().isBlank() ? column.getColor() : CKanbanColumn.DEFAULT_COLOR;
		getStyle().set("background-color", backgroundColor);
	}

	@Override
	public void drag_checkEventAfterPass(final CEvent event) {
		LOGGER.debug("[KanbanDrag] Completed drag propagation for column {}", getValue() != null ? getValue().getName() : "null");
	}

	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
		Check.notNull(event, "Drag event cannot be null for kanban column");
		if (event instanceof final CDragDropEvent dropEvent && dropEvent.getTargetItem() == null) {
			dropEvent.setTargetItem(getValue());
		}
		LOGGER.debug("[KanbanDrag] Propagating {} event for column {}", event.getClass().getSimpleName(),
				getValue() != null ? getValue().getName() : "null");
	}

	@Override
	public Set<ComponentEventListener<CDragEndEvent>> drag_getDragEndListeners() {
		return dragEndListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragStartEvent>> drag_getDragStartListeners() {
		return dragStartListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragDropEvent>> drag_getDropListeners() {
		return dropListeners;
	}

	/** Filters items that should appear in this column. */
	private List<CSprintItem> filterItems(final List<CSprintItem> items) {
		LOGGER.debug("Filtering items for kanban column {}", getValue() != null ? getValue().getName() : "null");
		if (items == null || items.isEmpty()) {
			return List.of();
		}
		final CKanbanColumn column = getValue();
		if (column == null || column.getId() == null) {
			return List.of();
		}
		final Long columnId = column.getId();
		return items.stream().filter(Objects::nonNull).filter(item -> {
			final Long itemColumnId = item.getKanbanColumnId();
			return itemColumnId != null && itemColumnId.equals(columnId);
		}).toList();
	}

	private void initializeColumnDropTarget() {
		columnDropTarget = DropTarget.create(itemsLayout);
		columnDropTarget.setDropEffect(DropEffect.MOVE);
		columnDropTarget.addDropListener(event -> {
			if (!dropEnabled) {
				LOGGER.debug("[KanbanDrag] Drop ignored because column drop is disabled");
				return;
			}
			final CDragDropEvent dropEvent = new CDragDropEvent(getId().orElse("None"), this, getValue(), GridDropLocation.EMPTY, true);
			LOGGER.debug("[KanbanDrag] Drop on column {}", getValue() != null ? getValue().getName() : "null");
			notifyEvents(dropEvent);
		});
		columnDropTarget.setActive(dropEnabled);
	}

	/** Updates the column UI when its value changes. */
	@Override
	protected void onValueChanged(final CKanbanColumn oldValue, final CKanbanColumn newValue, final boolean fromClient) {
		LOGGER.debug("Kanban column value changed from {} to {}", oldValue != null ? oldValue.getName() : "null",
				newValue != null ? newValue.getName() : "null");
		if (binder.getBean() == newValue) {
			return;
		}
		binder.setBean(newValue);
		applyBackgroundColor();
		refreshHeader();
		refreshStatuses();
		refreshItems();
	}

	/** Refreshes the column UI components. */
	@Override
	protected void refreshComponent() {
		applyBackgroundColor();
		refreshHeader();
		refreshStatuses();
		refreshItems();
	}

	/** Refreshes the header title and default badge. */
	private void refreshHeader() {
		final CKanbanColumn column = getValue();
		title.setText(column != null ? column.getName() : "");
		final boolean isDefault = column != null && Boolean.TRUE.equals(column.getDefaultColumn());
		if (isDefault) {
			if (!headerLayout.getChildren().anyMatch(component -> component == defaultBadge)) {
				headerLayout.add(defaultBadge);
			}
		} else {
			headerLayout.remove(defaultBadge);
		}
	}

	/** Refreshes the item cards inside the column. */
	private void refreshItems() {
		LOGGER.debug("Refreshing items for kanban column {}", getValue() != null ? getValue().getName() : "null");
		itemsLayout.removeAll();
		for (final CSprintItem item : filterItems(sprintItems)) {
			final CComponentKanbanPostit postit = new CComponentKanbanPostit(item);
			postit.setDragEnabled(dragEnabled);
			postit.setDropEnabled(dropEnabled);
			setupSelectionNotification(postit);
			setupChildDragDropForwarding(postit);
			itemsLayout.add(postit);
		}
	}

	/** Refreshes the status summary label. */
	private void refreshStatuses() {
		LOGGER.debug("Refreshing statuses label for kanban column {}", getValue() != null ? getValue().getName() : "null");
		final CKanbanColumn column = getValue();
		if (column == null || column.getIncludedStatuses() == null || column.getIncludedStatuses().isEmpty()) {
			statusesLabel.setText("");
			return;
		}
		final String statuses = column.getIncludedStatuses().stream().filter(Objects::nonNull).map(status -> status.getName())
				.filter(name -> name != null && !name.isBlank()).sorted(String::compareToIgnoreCase).collect(Collectors.joining(", "));
		statusesLabel.setText(statuses);
	}

	@Override
	public void select_checkEventAfterPass(final CEvent event) {
		LOGGER.debug("[KanbanSelect] Selection propagated for column {}", getValue() != null ? getValue().getName() : "null");
	}

	@Override
	public void select_checkEventBeforePass(final CEvent event) {
		Check.notNull(event, "Selection event cannot be null for kanban column");
		LOGGER.debug("[KanbanSelect] Column {} received selection", getValue() != null ? getValue().getName() : "null");
	}

	@Override
	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners() {
		return selectListeners;
	}

	@Override
	public void setDragEnabled(final boolean enabled) {
		dragEnabled = enabled;
		itemsLayout.getChildren().filter(CComponentKanbanPostit.class::isInstance).map(component -> (CComponentKanbanPostit) component)
				.forEach(postit -> postit.setDragEnabled(enabled));
	}

	@Override
	public void setDropEnabled(final boolean enabled) {
		dropEnabled = enabled;
		itemsLayout.getChildren().filter(CComponentKanbanPostit.class::isInstance).map(component -> (CComponentKanbanPostit) component)
				.forEach(postit -> postit.setDropEnabled(enabled));
		if (columnDropTarget != null) {
			columnDropTarget.setActive(enabled);
		}
	}

	/** Sets the items displayed in this column. */
	public void setItems(final List<CSprintItem> items) {
		LOGGER.debug("Setting items for kanban column {}", getValue() != null ? getValue().getName() : "null");
		sprintItems = items == null ? List.of() : List.copyOf(items);
		refreshItems();
	}
}
