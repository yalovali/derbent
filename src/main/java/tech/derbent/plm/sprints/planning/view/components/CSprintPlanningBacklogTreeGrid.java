package tech.derbent.plm.sprints.planning.view.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;

import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CAbstractGnntGridBase;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.sprints.domain.CSprintItem;

/**
 * Backlog tree grid for the sprint planning board.
 *
 * <p>We keep this separate from the generic Gnnt tree grid so sprint planning can evolve independently
 * (leaf-only validation, cross-grid drag/drop, sprint/backlog specific columns) without changing the
 * shared Gnnt board behaviour.</p>
 */
public class CSprintPlanningBacklogTreeGrid extends CAbstractGnntGridBase {

	public static final String ID_TREE_GRID = "custom-sprint-planning-backlog-tree-grid";
	private static final String KEY_STORY_POINTS = "storyPoints";
	private static final long serialVersionUID = 1L;

	private final CSprintPlanningDragContext dragContext;
	private final BiConsumer<CGnntItem, CGnntItem> dropListener;
	private final String gridId;
	private List<CGnntItem> lastRootItems = List.of();

	public CSprintPlanningBacklogTreeGrid(final String gridId, final CSprintPlanningDragContext dragContext,
			final Consumer<CGnntItem> selectionListener, final BiConsumer<CGnntItem, CGnntItem> dropListener) {
		super(new TreeGrid<>(), gridId, selectionListener);
		this.gridId = gridId;
		this.dragContext = dragContext;
		this.dropListener = dropListener;
		configureDragAndDrop();
	}

	@Override
	protected void configureColumns() {
		configureNameColumn();
		addStoryPointColumn();
		addIdColumn();
		addTrailingSharedColumns();
	}

	@Override
	protected void configureNameColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final var nameColumn = treeGrid.addComponentHierarchyColumn(this::createHierarchyComponent)
				.setAutoWidth(false)
				.setResizable(true)
				.setKey("name")
				.setHeader("Name")
				.setFlexGrow(0)
				.setWidth(NAME_COLUMN_WIDTH_PX + "px");
		CGrid.styleColumnHeader(nameColumn, "Name");
	}

	private void addStoryPointColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.addColumn(item -> {
			final Object entity = item != null ? item.getEntity() : null;
			if (!(entity instanceof ISprintableItem sprintableItem)) {
				return "";
			}
			final CSprintItem sprintItem = sprintableItem.getSprintItem();
			final Long points = sprintItem != null ? sprintItem.getStoryPoint() : null;
			return points != null ? String.valueOf(points) : "0";
		}).setWidth("70px").setFlexGrow(0).setKey(KEY_STORY_POINTS).setHeader("SP");
		CGrid.styleColumnHeader(treeGrid.getColumnByKey(KEY_STORY_POINTS), "SP");
	}

	@Override
	protected int getNonTimelineColumnWidthPx() {
		// Name + SP + ID + Start + End + Responsible + Status
		return NAME_COLUMN_WIDTH_PX + 70 + 80 + 110 + 110 + 135 + 140;
	}

	public void expandAll() {
		getTreeGrid().expand(lastRootItems);
	}

	public void collapseAll() {
		getTreeGrid().collapse(lastRootItems);
	}

	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult, final CGanttTimelineRange range) {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final CGnntItem selectedItem = treeGrid.asSingleSelect().getValue();
		final String selectedKey = selectedItem != null ? selectedItem.getEntityKey() : null;

		updateTimelineRange(range);
		final CGnntHierarchyResult safeHierarchyResult = hierarchyResult != null
				? hierarchyResult
				: new CGnntHierarchyResult(List.of(), Map.of(), List.of());
		final List<CGnntItem> flatItems = safeHierarchyResult.getFlatItems();
		final Map<String, CGnntItem> itemByKey = buildItemKeyMap(flatItems);

		lastRootItems = safeHierarchyResult.getRootItems();
		treeGrid.setItems(lastRootItems, safeHierarchyResult::getChildren);
		treeGrid.expand(lastRootItems);

		final CGnntItem restoredSelection = selectedKey != null ? itemByKey.get(selectedKey) : null;
		if (restoredSelection != null) {
			treeGrid.select(restoredSelection);
		} else if (!flatItems.isEmpty()) {
			treeGrid.select(flatItems.get(0));
		} else {
			selectionListener.accept(null);
		}

		restoreGridScrollPosition();
	}

	private void configureDragAndDrop() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.setRowsDraggable(true);
		// Backlog primarily acts as drop target (Sprint → Backlog), so ON_TOP is enough.
		treeGrid.setDropMode(GridDropMode.ON_TOP);

		treeGrid.addDragStartListener(event -> {
			final CGnntItem dragged = event.getDraggedItems().stream().findFirst().orElse(null);
			if (dragContext != null) {
				dragContext.setDraggedItem(dragged, gridId);
			}
		});
		treeGrid.addDragEndListener(event -> {
			if (dragContext != null) {
				dragContext.clear();
			}
		});
		treeGrid.addDropListener(event -> {
			final CGnntItem dropSource = dragContext != null ? dragContext.getDraggedItem() : null;
			if (dragContext != null) {
				dragContext.clear();
			}
			if (dropSource == null || dropListener == null) {
				return;
			}
			// Target can be null when dropping on empty space; that is still a valid "move to backlog" action.
			final CGnntItem targetItem = event.getDropTargetItem().orElse(null);
			dropListener.accept(dropSource, targetItem);
		});
	}

	private Map<String, CGnntItem> buildItemKeyMap(final List<CGnntItem> flatItems) {
		final Map<String, CGnntItem> itemByKey = new HashMap<>();
		for (final CGnntItem item : flatItems) {
			final String key = item != null ? item.getEntityKey() : null;
			if (key != null) {
				itemByKey.put(key, item);
			}
		}
		return itemByKey;
	}

	private TreeGrid<CGnntItem> getTreeGrid() {
		return (TreeGrid<CGnntItem>) getGrid();
	}

	private Component createHierarchyComponent(final CGnntItem item) {
		final CHorizontalLayout layout = new CHorizontalLayout();
		layout.setSpacing(true);
		layout.setPadding(false);
		layout.setAlignItems(Alignment.CENTER);
		final Component iconComponent = createIconComponent(item);
		final Span name = new Span(item.getName());
		name.getStyle().set("font-weight", item.isParentItem() ? "700" : "400").set("color", item.getColorCode());
		layout.add(iconComponent, name);
		return layout;
	}
}
