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

import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.enhanced.CQuickAccessPanel;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

public class CGnntTreeGrid extends CAbstractGnntGridBase {

	public static final String ID_TREE_GRID = "custom-gnnt-tree-grid";
	private static final long serialVersionUID = 1L;
	private record CHierarchySummary(int tasksDone, int tasksTotal, long storyPointsDone, long storyPointsTotal) {
		private String formatForHeader() {
			return "%d/%d tasks, %d/%d SP".formatted(tasksDone, tasksTotal, storyPointsDone, storyPointsTotal);
		}
	}

	private CGnntItem draggedItem;
	private final Set<String> expandedEntityKeys = new HashSet<>();
	private boolean hierarchyInitialized;
	private Map<String, CHierarchySummary> hierarchySummaryByKey = Map.of();
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

		// Pre-compute rollups so parent rows (L0..N) can show done/total task + story point summaries.
		hierarchySummaryByKey = computeHierarchySummaries(safeHierarchyResult);

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
		treeGrid.addDragStartListener(event -> {
			final CGnntItem candidate = event.getDraggedItems().stream().findFirst().orElse(null);
			// Prevent hierarchy moves for read-only planning rows.
			draggedItem = candidate != null && candidate.isEditable() ? candidate : null;
		});
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
		final boolean editable = item != null && item.isEditable();
		final String displayColor = editable ? item.getColorCode() : "var(--lumo-secondary-text-color)";
		final Component iconComponent = createIconComponent(item);
		iconComponent.getElement().getStyle().set("color", displayColor);
		final Span name = new Span(item.getName());
		name.getStyle().set("font-weight", item.isParentItem() ? "700" : "400")
				.set("color", displayColor);
		if (!editable) {
			// Keep non-editable rows visibly muted so users understand why inline actions are disabled.
			layout.getStyle().set("opacity", "0.75");
		}
		layout.add(iconComponent, name);

		// Display rollups only for non-leaf nodes so hierarchy headers stay readable (similar to Jira's epic/user story summaries).
		final CHierarchySummary summary = item != null ? hierarchySummaryByKey.get(item.getEntityKey()) : null;
		if (summary != null && item != null && item.isParentItem()) {
			final Span summarySpan = new Span("  " + summary.formatForHeader());
			summarySpan.getStyle().set("font-size", "var(--lumo-font-size-xs)")
					.set("color", "var(--lumo-secondary-text-color)")
					.set("white-space", "nowrap");
			layout.add(summarySpan);
		}
		return layout;
	}

	private Map<String, CHierarchySummary> computeHierarchySummaries(final CGnntHierarchyResult hierarchyResult) {
		if (hierarchyResult == null || hierarchyResult.isEmpty()) {
			return Map.of();
		}
		final Map<String, CHierarchySummary> result = new HashMap<>();
		for (final CGnntItem root : hierarchyResult.getRootItems()) {
			computeSummaryRecursively(root, hierarchyResult, result);
		}
		return Map.copyOf(result);
	}

	private CHierarchySummary computeSummaryRecursively(final CGnntItem item, final CGnntHierarchyResult hierarchyResult,
			final Map<String, CHierarchySummary> summariesByKey) {
		if (item == null || item.getEntityKey() == null) {
			return new CHierarchySummary(0, 0, 0, 0);
		}
		final CHierarchySummary cached = summariesByKey.get(item.getEntityKey());
		if (cached != null) {
			return cached;
		}
		final List<CGnntItem> children = hierarchyResult != null ? hierarchyResult.getChildren(item) : List.of();
		if (children == null || children.isEmpty()) {
			final Object entity = item.getEntity();
			final boolean done = isDone(entity);
			final long storyPoints = resolveStoryPoints(entity);
			final CHierarchySummary leafSummary = new CHierarchySummary(done ? 1 : 0, 1, done ? storyPoints : 0, storyPoints);
			summariesByKey.put(item.getEntityKey(), leafSummary);
			return leafSummary;
		}
		int tasksDone = 0;
		int tasksTotal = 0;
		long storyPointsDone = 0;
		long storyPointsTotal = 0;
		for (final CGnntItem child : children) {
			final CHierarchySummary childSummary = computeSummaryRecursively(child, hierarchyResult, summariesByKey);
			tasksDone += childSummary.tasksDone();
			tasksTotal += childSummary.tasksTotal();
			storyPointsDone += childSummary.storyPointsDone();
			storyPointsTotal += childSummary.storyPointsTotal();
		}
		final CHierarchySummary parentSummary = new CHierarchySummary(tasksDone, tasksTotal, storyPointsDone, storyPointsTotal);
		summariesByKey.put(item.getEntityKey(), parentSummary);
		return parentSummary;
	}

	private boolean isDone(final Object entity) {
		if (!(entity instanceof IHasStatusAndWorkflow<?, ?> itemWithStatus)) {
			return false;
		}
		final CProjectItemStatus status = itemWithStatus.getStatus();
		return status != null && Boolean.TRUE.equals(status.getFinalStatus());
	}

	private long resolveStoryPoints(final Object entity) {
		if (!(entity instanceof ISprintableItem sprintableItem)) {
			return 0L;
		}
		final Long points = sprintableItem.getStoryPoint();
		return points != null ? points : 0L;
	}
}

