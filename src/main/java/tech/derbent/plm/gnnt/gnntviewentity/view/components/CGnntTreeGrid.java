package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;

import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.enhanced.CQuickAccessPanel;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

public class CGnntTreeGrid extends CAbstractGnntGridBase {

	public static final String ID_TREE_GRID = "custom-gnnt-tree-grid";
	private static final long serialVersionUID = 1L;
	private CGnntItem draggedItem;
	private final Set<String> expandedEntityKeys = new HashSet<>();
	private boolean hierarchyInitialized;
	private final BiConsumer<CGnntItem, CGnntItem> moveListener;

	public CGnntTreeGrid(final Consumer<CGnntItem> selectionListener, final BiConsumer<CGnntItem, CGnntItem> moveListener) {
		super(new TreeGrid<>(), ID_TREE_GRID, selectionListener);
		this.moveListener = moveListener;
		setQuickAccessPanel(new CQuickAccessPanel(ID_TREE_GRID + "-quick-access"));
		configureDragAndDrop();
		configureExpansionTracking();
	}

	@Override
	protected void configureColumns() {
		configureNameColumn();
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

	@Override
	protected int getNonTimelineColumnWidthPx() {
		return NAME_COLUMN_WIDTH_PX + 80 + 110 + 110 + 135 + 140;
	}

	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult, final CGanttTimelineRange range) {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final CGnntItem selectedItem = treeGrid.asSingleSelect().getValue();
		final String selectedKey = selectedItem != null ? selectedItem.getEntityKey() : null;
		final Set<String> expandedKeysSnapshot = hierarchyInitialized ? new HashSet<>(expandedEntityKeys) : Set.of();

		updateTimelineRange(range);
		final CGnntHierarchyResult safeHierarchyResult =
				hierarchyResult != null ? hierarchyResult : new CGnntHierarchyResult(List.of(), Map.of(), List.of());
		final List<CGnntItem> flatItems = safeHierarchyResult.getFlatItems();
		final Map<String, CGnntItem> itemByKey = buildItemKeyMap(flatItems);

		treeGrid.setItems(safeHierarchyResult.getRootItems(), safeHierarchyResult::getChildren);
		expandedEntityKeys.clear();

		if (!hierarchyInitialized) {
			// First load keeps the legacy behaviour (auto-expand) so users see the hierarchy immediately.
			treeGrid.expandRecursively(safeHierarchyResult.getRootItems(), 25);
			for (final CGnntItem item : flatItems) {
				final String itemKey = item != null ? item.getEntityKey() : null;
				if (itemKey != null && item.isParentItem()) {
					expandedEntityKeys.add(itemKey);
				}
			}
			hierarchyInitialized = true;
		} else {
			expandedEntityKeys.addAll(restoreExpandedState(treeGrid, itemByKey, expandedKeysSnapshot));
		}

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
		treeGrid.setDropMode(GridDropMode.ON_TOP);
		treeGrid.addDragStartListener(event -> draggedItem = event.getDraggedItems().stream().findFirst().orElse(null));
		treeGrid.addDragEndListener(event -> draggedItem = null);
		treeGrid.addDropListener(event -> {
			// Always clear the server-side drag state so users can retry drops after validation failures.
			final CGnntItem dropSource = draggedItem;
			draggedItem = null;
			if (dropSource == null || moveListener == null || event.getDropTargetItem().isEmpty()) {
				return;
			}
			moveListener.accept(dropSource, event.getDropTargetItem().get());
		});
	}

	private void configureExpansionTracking() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.addExpandListener(event -> event.getItems().forEach(item -> {
			final String key = item != null ? item.getEntityKey() : null;
			if (key != null) {
				expandedEntityKeys.add(key);
			}
		}));
		treeGrid.addCollapseListener(event -> event.getItems().forEach(item -> {
			final String key = item != null ? item.getEntityKey() : null;
			if (key != null) {
				expandedEntityKeys.remove(key);
			}
		}));
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

	private Set<String> restoreExpandedState(final TreeGrid<CGnntItem> treeGrid, final Map<String, CGnntItem> itemByKey,
			final Set<String> expandedKeys) {
		if (expandedKeys == null || expandedKeys.isEmpty() || itemByKey.isEmpty()) {
			return Set.of();
		}

		// Expand both the previously-expanded nodes plus their ancestors so the same branches stay visible.
		final Set<String> keysToExpand = new HashSet<>();
		for (final String expandedKey : expandedKeys) {
			CGnntItem current = itemByKey.get(expandedKey);
			while (current != null) {
				final String currentKey = current.getEntityKey();
				if (currentKey == null) {
					break;
				}
				keysToExpand.add(currentKey);
				if (!current.hasParent()) {
					break;
				}
				final String parentType = current.getParentType();
				final Long parentId = current.getParentId();
				if (parentType == null || parentId == null) {
					break;
				}
				final String parentKey = parentType + ":" + parentId;
				current = itemByKey.get(parentKey);
			}
		}

		final List<CGnntItem> itemsToExpand = new ArrayList<>();
		for (final String key : keysToExpand) {
			final CGnntItem item = itemByKey.get(key);
			if (item != null && item.isParentItem()) {
				itemsToExpand.add(item);
			}
		}
		itemsToExpand.sort(Comparator.comparingInt(CGnntItem::getHierarchyLevel));
		treeGrid.expand(itemsToExpand);
		final Set<String> keysExpanded = new HashSet<>();
		for (final CGnntItem item : itemsToExpand) {
			keysExpanded.add(item.getEntityKey());
		}
		return keysExpanded;
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
		iconComponent.getElement().getStyle().set("color", item.getColorCode());
		final Span name = new Span(item.getName());
		name.getStyle().set("font-weight", item.isParentItem() ? "700" : "400")
				.set("color", item.getColorCode());
		layout.add(iconComponent, name);
		return layout;
	}
}
