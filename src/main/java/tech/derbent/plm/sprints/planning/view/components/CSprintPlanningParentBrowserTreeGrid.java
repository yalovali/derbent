package tech.derbent.plm.sprints.planning.view.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;

import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

/**
 * Parent browser grid for sprint planning backlog.
 *
 * <p>This grid only shows non-leaf items (hierarchy levels 0..n). Leaf items are displayed in a separate
 * flat backlog grid so planning stays focused on assignable work items while still allowing users to
 * browse the parent context (Epic → Story → ...).</p>
 */
public final class CSprintPlanningParentBrowserTreeGrid extends CAbstractSprintPlanningTreeGridBase {

	public static final String ID_TREE_GRID = "custom-sprint-planning-parent-browser-tree-grid";
	private static final long serialVersionUID = 1L;

	public CSprintPlanningParentBrowserTreeGrid(final String gridId, final Consumer<CGnntItem> selectionListener) {
		super(gridId, selectionListener, gridId);
	}

	@Override
	protected void configureColumns() {
		// Keep this grid compact: it is a hierarchy browser, not the planning timeline.
		addLevelColumn();
		configureNameColumn();
		addTypeColumn();
		addIdColumn();
	}

	private void addLevelColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.addColumn(CGnntItem::getHierarchyLevel)
				.setWidth("60px")
				.setFlexGrow(0)
				.setKey("level")
				.setHeader("Lvl");
		CGrid.styleColumnHeader(treeGrid.getColumnByKey("level"), "Lvl");
	}

	private void addTypeColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.addColumn(CGnntItem::getEntityTypeTitle)
				.setWidth("160px")
				.setFlexGrow(0)
				.setKey("type")
				.setHeader("Type");
		CGrid.styleColumnHeader(treeGrid.getColumnByKey("type"), "Type");
	}

	@Override
	protected void configureNameColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final var nameColumn = treeGrid.addComponentHierarchyColumn(this::createHierarchyComponent)
				.setAutoWidth(false)
				.setResizable(true)
				.setKey("name")
				.setHeader("Parent")
				.setFlexGrow(0)
				.setWidth(NAME_COLUMN_WIDTH_PX + "px");
		CGrid.styleColumnHeader(nameColumn, "Parent");
	}

	@Override
	protected int getNonTimelineColumnWidthPx() {
		// Level + Name + Type + ID
		return 60 + NAME_COLUMN_WIDTH_PX + 160 + 80;
	}

	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult, final CGanttTimelineRange range) {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final CGnntItem selectedItem = treeGrid.asSingleSelect().getValue();
		final String selectedKey = selectedItem != null ? selectedItem.getEntityKey() : null;

		updateTimelineRange(range);
		final CGnntHierarchyResult safeHierarchyResult =
				hierarchyResult != null ? hierarchyResult : new CGnntHierarchyResult(List.of(), Map.of(), List.of());
		final List<CGnntItem> rootItems = safeHierarchyResult.getRootItems();
		final List<CGnntItem> flatItems = safeHierarchyResult.getFlatItems();
		final Map<String, CGnntItem> itemByKey = buildItemKeyMap(flatItems);

		setRootItems(rootItems);
		treeGrid.setItems(rootItems, safeHierarchyResult::getChildren);
		treeGrid.expand(rootItems);

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

	private Component createHierarchyComponent(final CGnntItem item) {
		final CHorizontalLayout layout = new CHorizontalLayout();
		layout.setSpacing(true);
		layout.setPadding(false);
		layout.setAlignItems(Alignment.CENTER);
		final Component iconComponent = createIconComponent(item);
		final Span name = new Span(item.getName());
		name.getStyle().set("font-weight", "600").set("color", item.getColorCode());
		layout.add(iconComponent, name);
		return layout;
	}
}
