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
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

/** Parent browser grid for sprint planning backlog.
 * <p>
 * This grid only shows non-leaf items (hierarchy levels 0..n). Leaf items are displayed in a separate flat backlog grid so planning stays focused on
 * assignable work items while still allowing users to browse the parent context (Epic → Story → ...).
 * </p>
 */
public final class CSprintPlanningParentBrowserTreeGrid
		extends CAbstractSprintPlanningTreeGridBase {

	private Map<String, CSprintPlanningSprintMetrics> rollupMetricsByEntityKey =
			Map.of();

	private final CSprintPlanningDragContext dragContext;
	private final BiConsumer<CGnntItem, CGnntItem> dropListener;

	public static final String ID_TREE_GRID =
			"custom-sprint-planning-parent-browser-tree-grid";
	private static final long serialVersionUID = 1L;

	public CSprintPlanningParentBrowserTreeGrid(final String gridId,
			final CSprintPlanningDragContext dragContext,
			final Consumer<CGnntItem> selectionListener,
			final BiConsumer<CGnntItem, CGnntItem> dropListener) {
		super(gridId, selectionListener, gridId);
		this.dragContext = dragContext;
		this.dropListener = dropListener;
		configureDragAndDrop();
	}

	private Map<String, CGnntItem>
			buildItemKeyMap(final List<CGnntItem> flatItems) {
		final Map<String, CGnntItem> itemByKey = new HashMap<>();
		flatItems.forEach((final CGnntItem item) -> {
			final String key = item != null ? item.getEntityKey() : null;
			if (key != null) {
				itemByKey.put(key, item);
			}
		});
		return itemByKey;
	}

	@Override
	protected void configureColumns() {
		// Keep this grid minimal: it is only a parent context browser (leaf work items are shown in the right-side grid).
		configureNameColumn();
	}

	@Override
	protected void configureNameColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final var nameColumn = treeGrid
				.addComponentHierarchyColumn(this::createHierarchyComponent)
				.setAutoWidth(false).setResizable(true).setKey("name")
				.setFlexGrow(1);
		// .setWidth(NAME_COLUMN_WIDTH_PX + "px");
		decorateNameColumnHeader(nameColumn, "Parent");
	}

	private Component createHierarchyComponent(final CGnntItem item) {
		final CHorizontalLayout layout = new CHorizontalLayout();
		layout.setSpacing(true);
		layout.setPadding(false);
		layout.setAlignItems(Alignment.CENTER);
		final Component iconComponent = createIconComponent(item);
		final Span name = new Span(item.getName());
		name.getStyle().set("font-weight", "600").set("color",
				item.getColorCode());
		layout.add(iconComponent, name);

		final CSprintPlanningSprintMetrics rollup = item != null
				? rollupMetricsByEntityKey.get(item.getEntityKey()) : null;
		if (rollup != null) {
			final Span summary = new Span("  " + rollup.formatRollup());
			summary.getStyle().set("font-size", "var(--lumo-font-size-xs)")
					.set("color", "var(--lumo-secondary-text-color)")
					.set("white-space", "nowrap");
			layout.add(summary);
		}
		return decorateHierarchyComponent(layout, item);
	}

	@Override
	protected int getNonTimelineColumnWidthPx() {
		// Parent only.
		return NAME_COLUMN_WIDTH_PX;
	}

	@Override
	public CGnntItem getSelectedItem() {
		return getTreeGrid().asSingleSelect().getValue();
	}

	private void configureDragAndDrop() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		// Parent browser is a drop target only: leaf items can be dropped onto a parent to reparent.
		treeGrid.setDropMode(GridDropMode.ON_TOP);
		treeGrid.addDropListener(event -> {
			final CGnntItem dragged = dragContext != null ? dragContext.getDraggedItem() : null;
			if (dragContext != null) {
				dragContext.clear();
			}
			if (dragged == null || dropListener == null) {
				return;
			}
			final CGnntItem target = event.getDropTargetItem().orElse(null);
			dropListener.accept(dragged, target);
		});
	}

	public void setContextActions(
			final List<CContextActionDefinition<CGnntItem>> actions) {
		setItemContextActions(actions);
		setHierarchyContextActions(actions);
	}

	public void setRollupMetricsByEntityKey(
			final Map<String, CSprintPlanningSprintMetrics> rollupMetricsByEntityKey) {
		this.rollupMetricsByEntityKey =
				rollupMetricsByEntityKey != null ? rollupMetricsByEntityKey : Map.of();
		// Metrics are rendered in component columns, so a provider refresh is enough.
		getTreeGrid().getDataProvider().refreshAll();
	}

	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult,
			final CGanttTimelineRange range) {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final CGnntItem selectedItem = treeGrid.asSingleSelect().getValue();
		final String selectedKey =
				selectedItem != null ? selectedItem.getEntityKey() : null;
		updateTimelineRange(range);
		final CGnntHierarchyResult safeHierarchyResult = hierarchyResult != null
				? hierarchyResult
				: new CGnntHierarchyResult(List.of(), Map.of(), List.of());
		final List<CGnntItem> rootItems = safeHierarchyResult.getRootItems();
		final List<CGnntItem> flatItems = safeHierarchyResult.getFlatItems();
		final Map<String, CGnntItem> itemByKey = buildItemKeyMap(flatItems);
		setRootItems(rootItems);
		treeGrid.setItems(rootItems, safeHierarchyResult::getChildren);
		restoreExpandedState(itemByKey);
		final CGnntItem restoredSelection =
				selectedKey != null ? itemByKey.get(selectedKey) : null;
		if (restoredSelection != null) {
			treeGrid.select(restoredSelection);
		} else {
			// Keep the parent browser passive on first load so the leaf pane shows the full backlog until the
			// user explicitly focuses a parent branch.
			treeGrid.deselectAll();
			selectionListener.accept(null);
		}
		refreshHeaderActionStates();
		restoreGridScrollPosition();
	}
}
