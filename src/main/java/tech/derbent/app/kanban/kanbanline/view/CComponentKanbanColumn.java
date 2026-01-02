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
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
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
	private final Span defaultBadge;
	private final Set<ComponentEventListener<CDragDropEvent>> dragDropListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new HashSet<>();
	private DropTarget<CVerticalLayout> dropTarget;
	protected final CHorizontalLayout headerLayout;
	private final CVerticalLayout itemsLayout;
	private final Set<ComponentEventListener<CSelectEvent>> selectListeners = new HashSet<>();
	private List<CSprintItem> sprintItems = List.of();
	// Cache filtered items to avoid repeated filtering operations
	private List<CSprintItem> cachedFilteredItems = List.of();
	protected final CLabelEntity statusesLabel;
	private Span storyPointTotalLabel;
	protected final CH3 title;

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
		headerLayout.setAlignItems(Alignment.CENTER);
		title = new CH3("");
		title.getStyle().set("margin", "0").set("flex-grow", "1");
		defaultBadge = new Span("Default");
		defaultBadge.getStyle().set("background-color", "#E3F2FD").set("color", "#0D47A1").set("padding", "2px 6px").set("border-radius", "6px")
				.set("font-size", "10px").set("font-weight", "600");
		storyPointTotalLabel = new Span();
		storyPointTotalLabel.getStyle().set("background-color", "#E8F5E9").set("color", "#2E7D32").set("padding", "4px 8px")
				.set("border-radius", "6px").set("font-size", "12px").set("font-weight", "700").set("white-space", "nowrap");
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
		// LOGGER.debug("[KanbanDrag] Completed drag propagation for column {}", getValue() != null ? getValue().getName() : "null");
	}

	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
		Check.notNull(event, "Drag event cannot be null for kanban column");
		if (event instanceof final CDragDropEvent dropEvent && dropEvent.getTargetItem() == null) {
			dropEvent.setTargetItem(getValue());
		}
		// LOGGER.debug("[KanbanDrag] Propagating {} event for column {}", event.getClass().getSimpleName(),getValue() != null ? getValue().getName()
		// : "null");
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
		return dragDropListeners;
	}

	@Override
	public boolean drag_isDropAllowed(CDragStartEvent event) {
		final Object item = event.getDraggedItem();
		if (!(item instanceof final CSprintItem sprintItem)) {
			return false;
		}
		final CKanbanColumn column = getValue();
		if (column == null || column.getIncludedStatuses() == null) {
			return false;
		}
		// FIXED: Allow drop to any column - workflow validation is handled by CPageServiceKanbanLine.handleKanbanDrop()
		// Previous logic was: column.getIncludedStatuses().stream().anyMatch(status -> status.getId().equals(sprintItem.getStatus().getId()))
		// This incorrectly required the CURRENT status to be in the column, rather than checking for valid transitions.
		// The proper workflow transition validation happens in the drop handler, which will show appropriate warnings
		// if no valid transition exists while still allowing the visual column assignment.
		return true;
	}

	private ComponentEventListener<DropEvent<CVerticalLayout>> drag_on_column_drop() {
		return event -> {
			try {
				LOGGER.debug("Handling column drop event for column id: {}", getId());
				final CDragDropEvent dropEvent = new CDragDropEvent(getId().orElse("None"), this, null, null, true);
				notifyEvents(dropEvent);
			} catch (final Exception e) {
				LOGGER.error("Error handling grid drop event", e);
			}
		};
	}

	@Override
	public void drag_setDragEnabled(final boolean enabled) {
		// itemsLayout.getChildren().filter(CComponentKanbanPostit.class::isInstance).map(component -> (CComponentKanbanPostit) component)
		// .forEach(postit -> postit.setDragEnabled(enabled));
	}

	@Override
	public void drag_setDropEnabled(final boolean enabled) {
		if (enabled == false) {
			dropTarget = null;
			return;
		}
		dropTarget = DropTarget.create(this);
		dropTarget.setDropEffect(DropEffect.MOVE);
		dropTarget.addDropListener(drag_on_column_drop());
		
		// Note: Vaadin Flow 24.8 does not provide addDragOverListener or addDragLeaveListener in the Java API.
		// These events are managed client-side in HTML5 DnD but not exposed server-side by Vaadin.
		// For visual feedback during drag-over, we can use CSS hover states or implement custom
		// client-side JavaScript integration if needed. The main drag-drop logic uses the drop event.
		// 
		// Alternative: Use CSS for visual feedback:
		// .kanban-column:has([draggable="true"]:hover) { background-color: #e0e0e0; }
		// Or client-side JavaScript for more complex interactions.
		
		dropTarget.setActive(true);
	}

	/** Filters items that should appear in this column and caches the result. */
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
	
	/** Gets the cached filtered items for this column. Updates cache if needed. */
	private List<CSprintItem> getFilteredItems() {
		// Recompute cache if sprint items changed or column value changed
		final CKanbanColumn column = getValue();
		if (cachedFilteredItems.isEmpty() || column == null) {
			cachedFilteredItems = filterItems(sprintItems);
		}
		return cachedFilteredItems;
	}
	
	/** Invalidates the cached filtered items. Called when sprintItems or column value changes. */
	private void invalidateCache() {
		cachedFilteredItems = List.of();
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
		// Invalidate cache since column changed
		invalidateCache();
		// Only refresh if we have items - avoid refreshing during initialization
		if (!sprintItems.isEmpty()) {
			applyBackgroundColor();
			refreshHeader();
			refreshStatuses();
			refreshItems();
		}
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
		
		// Calculate and display total story points
		refreshStoryPointTotal();
	}
	
	/** Calculates and displays the total story points for items in this column. */
	protected void refreshStoryPointTotal() {
		final List<CSprintItem> columnItems = getFilteredItems(); // Use cached filtered items
		long totalStoryPoints = 0;
		for (final CSprintItem item : columnItems) {
			if (item != null && item.getItem() != null && item.getItem().getStoryPoint() != null) {
				totalStoryPoints += item.getItem().getStoryPoint();
			}
		}
		
		if (totalStoryPoints > 0) {
			storyPointTotalLabel.setText(totalStoryPoints + " SP");
			if (!headerLayout.getChildren().anyMatch(component -> component == storyPointTotalLabel)) {
				headerLayout.add(storyPointTotalLabel);
			}
		} else {
			headerLayout.remove(storyPointTotalLabel);
		}
	}

	/** Refreshes the item cards inside the column. */
	private void refreshItems() {
		LOGGER.debug("Refreshing items for kanban column {}", getValue() != null ? getValue().getName() : "null");
		itemsLayout.removeAll();
		for (final CSprintItem item : getFilteredItems()) { // Use cached filtered items
			final CComponentKanbanPostit postit = new CComponentKanbanPostit(item);
			postit.drag_setDragEnabled(true);
			postit.drag_setDropEnabled(true);
			setupSelectionNotification(postit);
			setupChildDragDropForwarding(postit);
			itemsLayout.add(postit);
		}
		// Refresh story point total after items change
		refreshStoryPointTotal();
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

	/** Sets the items displayed in this column. */
	public void setItems(final List<CSprintItem> items) {
		LOGGER.debug("Setting items for kanban column {}", getValue() != null ? getValue().getName() : "null");
		sprintItems = items == null ? List.of() : List.copyOf(items);
		// Invalidate cache when items change
		invalidateCache();
		// Only refresh if column value is set - avoid premature refresh during initialization
		if (getValue() != null) {
			refreshItems();
		}
	}
}
