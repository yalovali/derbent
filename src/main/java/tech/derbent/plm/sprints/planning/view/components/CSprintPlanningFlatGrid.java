package tech.derbent.plm.sprints.planning.view.components;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Span;

import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.enhanced.CQuickAccessPanel;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CAbstractGnntGridBase;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.sprints.domain.CSprintItem;

/**
 * Flat timeline grid for sprint planning backlog.
 *
 * <p>This is intentionally a new component so we can share drag state via
 * {@link CSprintPlanningDragContext} (cross-grid backlog ↔ sprint moves) without
 * touching the existing Gnnt board implementation.</p>
 */
public class CSprintPlanningFlatGrid extends CAbstractGnntGridBase {

	public static final String ID_GRID = "custom-sprint-planning-backlog-grid";
	private static final String KEY_STORY_POINTS = "storyPoints";
	private static final long serialVersionUID = 1L;

	private final CSprintPlanningDragContext dragContext;
	private final Consumer<CSprintPlanningDropRequest> dropListener;
	private final String gridId;

	public CSprintPlanningFlatGrid(final String gridId, final CSprintPlanningDragContext dragContext, final Consumer<CGnntItem> selectionListener,
			final Consumer<CSprintPlanningDropRequest> dropListener) {
		super(new CGrid<>(CGnntItem.class), gridId, selectionListener);
		this.gridId = gridId;
		this.dragContext = dragContext;
		this.dropListener = dropListener;

		// Backlog leaf actions belong into the shared header quick-access slot (keeps the split layout compact).
		setQuickAccessPanel(new CQuickAccessPanel(gridId + "-quick-access"));

		configureDragAndDrop();
	}

	@Override
	protected void configureColumns() {
		// Keep the same baseline columns as tree grids, but render the backlog as a flat list (leaf items only).
		addIdColumn();
		grid.addComponentColumn(this::createIconComponent)
				.setWidth(CGrid.WIDTH_IMAGE)
				.setFlexGrow(0)
				.setResizable(true)
				.setKey("icon")
				.setHeader("");
		configureNameColumn();
		addStoryPointColumn();
		addTrailingSharedColumns();
	}

	@Override
	protected void configureNameColumn() {
		grid.addComponentColumn(item -> {
			final CHorizontalLayout layout = new CHorizontalLayout();
			layout.setPadding(false);
			layout.setSpacing(true);
			layout.setAlignItems(Alignment.CENTER);

			// Icon is rendered in its own column; keep the name column text-only so it stays compact.
			final Span name = new Span(item.getName());
			name.getStyle().set("font-weight", "500");
			layout.add(name);
			return layout;
		}).setKey("name").setHeader("Name").setResizable(true).setWidth(NAME_COLUMN_WIDTH_PX + "px").setFlexGrow(0);
		CGrid.styleColumnHeader(grid.getColumnByKey("name"), "Name");
	}

	@Override
	protected int getNonTimelineColumnWidthPx() {
		// ID + icon + name + SP + Start + End + Responsible + Status
		return 80 + 60 + NAME_COLUMN_WIDTH_PX + 70 + 110 + 110 + 135 + 140;
	}

	private void addStoryPointColumn() {
		grid.addColumn(item -> {
			final Object entity = item != null ? item.getEntity() : null;
			if (!(entity instanceof ISprintableItem sprintableItem)) {
				return "";
			}
			final CSprintItem sprintItem = sprintableItem.getSprintItem();
			final Long points = sprintItem != null ? sprintItem.getStoryPoint() : null;
			return points != null ? String.valueOf(points) : "0";
		}).setWidth("70px").setFlexGrow(0).setKey(KEY_STORY_POINTS).setHeader("SP");
		CGrid.styleColumnHeader(grid.getColumnByKey(KEY_STORY_POINTS), "SP");
	}

	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult, final CGanttTimelineRange range) {
		final CGnntItem selectedItem = grid.asSingleSelect().getValue();
		final String selectedKey = selectedItem != null ? selectedItem.getEntityKey() : null;
		updateTimelineRange(range);
		final List<CGnntItem> items = hierarchyResult != null ? hierarchyResult.getFlatItems() : List.of();
		grid.setItems(items);
		if (selectedKey != null) {
			items.stream().filter(item -> selectedKey.equals(item.getEntityKey())).findFirst().ifPresent(grid::select);
		}
		if (!items.isEmpty() && grid.asSingleSelect().getValue() == null) {
			grid.select(items.get(0));
		}
		if (items.isEmpty()) {
			selectionListener.accept(null);
		}
		refreshHeaderActionStates();
		restoreGridScrollPosition();
	}

	public CGnntItem getSelectedItem() {
		// Used by sprint-planning actions (e.g., "Add to sprint") so the UI can prefer leaf selection over sprint selection.
		return grid.asSingleSelect().getValue();
	}

	public void setContextActions(final List<CContextActionDefinition<CGnntItem>> actions) {
		setItemContextActions(actions);
	}

	private void configureDragAndDrop() {
		grid.setRowsDraggable(true);
		grid.setDropMode(GridDropMode.BETWEEN);

		grid.addDragStartListener(event -> {
			final CGnntItem dragged = event.getDraggedItems().stream().findFirst().orElse(null);
			if (dragContext != null) {
				dragContext.setDraggedItem(dragged, gridId);
			}
		});
		grid.addDragEndListener(event -> {
			if (dragContext != null) {
				dragContext.clear();
			}
		});
		grid.addDropListener(event -> {
			final CGnntItem dropSource = dragContext != null ? dragContext.getDraggedItem() : null;
			if (dragContext != null) {
				dragContext.clear();
			}
			if (dropSource == null || dropListener == null) {
				return;
			}
			final CGnntItem targetItem = event.getDropTargetItem().orElse(null);
			// Target can be null when dropping on empty space; we still treat it as "drop on backlog".
			final GridDropLocation dropLocation = event.getDropLocation() != null ? event.getDropLocation() : GridDropLocation.EMPTY;
			dropListener.accept(new CSprintPlanningDropRequest(dropSource, targetItem, dropLocation, gridId));
		});
	}
}
