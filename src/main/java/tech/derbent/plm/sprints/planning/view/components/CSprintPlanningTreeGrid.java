package tech.derbent.plm.sprints.planning.view.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;

import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;

import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;

/**
 * Tree grid variant for sprint planning.
 *
 * <p>We intentionally keep this as a separate class from {@code CGnntTreeGrid} so the
 * existing Gnnt board behaviour stays unchanged while we experiment with cross-grid
 * drag/drop (backlog ↔ sprint) in the new planning UI.</p>
 */
public final class CSprintPlanningTreeGrid extends CAbstractSprintPlanningTreeGridBase {

	public static final String ID_TREE_GRID = "custom-sprint-planning-tree-grid";
	private static final String KEY_STORY_POINTS = "storyPoints";
	private static final long serialVersionUID = 1L;

	private final CSprintPlanningDragContext dragContext;
	private final Consumer<CSprintPlanningDropRequest> dropListener;
	private Map<Long, CSprintPlanningSprintMetrics> sprintMetricsById = Map.of();

	public CSprintPlanningTreeGrid(final String gridId, final CSprintPlanningDragContext dragContext, final Consumer<CGnntItem> selectionListener,
			final Consumer<CSprintPlanningDropRequest> dropListener) {
		super(gridId, selectionListener, gridId);
		this.dragContext = dragContext;
		this.dropListener = dropListener;
		configureDragAndDrop(gridId);
	}

	@Override
	protected void configureColumns() {
		configureNameColumn();
		addStoryPointColumn();
		addIdColumn();
		addTrailingSharedColumns();
	}

	private void addStoryPointColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		// Story points live on the owned CSprintItem, so we surface them directly in planning grids.
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
		// Name + SP + ID + Start + End + Responsible + Status
		return NAME_COLUMN_WIDTH_PX + 70 + 80 + 110 + 110 + 135 + 140;
	}

	public void setSprintMetrics(final Map<Long, CSprintPlanningSprintMetrics> sprintMetricsById) {
		this.sprintMetricsById = sprintMetricsById != null ? sprintMetricsById : Map.of();
	}


	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult, final CGanttTimelineRange range) {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final CGnntItem selectedItem = treeGrid.asSingleSelect().getValue();
		final String selectedKey = selectedItem != null ? selectedItem.getEntityKey() : null;

		updateTimelineRange(range);
		final CGnntHierarchyResult safeHierarchyResult =
				hierarchyResult != null ? hierarchyResult : new CGnntHierarchyResult(List.of(), Map.of(), List.of());
		final List<CGnntItem> flatItems = safeHierarchyResult.getFlatItems();
		final Map<String, CGnntItem> itemByKey = buildItemKeyMap(flatItems);

		final List<CGnntItem> rootItems = safeHierarchyResult.getRootItems();
		setRootItems(rootItems);
		treeGrid.setItems(rootItems, safeHierarchyResult::getChildren);
		restoreExpandedState(itemByKey);

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

	private void configureDragAndDrop(final String gridId) {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.setRowsDraggable(true);
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
				// Always clear the server-side drag state so users can retry drops after validation failures.
				dragContext.clear();
			}
			if (dropSource == null || dropListener == null || event.getDropTargetItem().isEmpty()) {
				return;
			}
			final GridDropLocation dropLocation = event.getDropLocation() != null ? event.getDropLocation() : GridDropLocation.ON_TOP;
			dropListener.accept(new CSprintPlanningDropRequest(dropSource, event.getDropTargetItem().get(), dropLocation, gridId));
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

		// Jira-like sprint header: show sprint totals directly on the sprint row.
		if (item.getEntity() instanceof CSprint sprint && sprint.getId() != null) {
			final CSprintPlanningSprintMetrics metrics = sprintMetricsById.get(sprint.getId());
			if (metrics != null) {
				final Integer velocity = sprint.getVelocity();
				final String suffix = velocity != null && velocity > 0
						? "Items: %d | SP: %d/%d".formatted(metrics.itemCount(), metrics.storyPoints(), velocity)
						: "Items: %d | SP: %d".formatted(metrics.itemCount(), metrics.storyPoints());
				final Span summary = new Span("  " + suffix);
				summary.getStyle().set("font-size", "var(--lumo-font-size-xs)")
						.set("color", "var(--lumo-secondary-text-color)");
				layout.add(summary);
			}
		}
		return layout;
	}
}
