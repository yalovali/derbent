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

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
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
	// Cached by-key map so the board can restore selection after saving entities.
	private Map<String, CGnntItem> itemByKey = Map.of();

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
		addProgressColumn();
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

	private void addProgressColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.addColumn(item -> {
			final Object entity = item != null ? item.getEntity() : null;
			if (entity instanceof final CSprint sprint && sprint.getId() != null) {
				final CSprintPlanningSprintMetrics metrics =
					sprintMetricsById.get(sprint.getId());
				return metrics != null ? metrics.formatRollup()
						: "0/0 tasks, 0/0 SP";
			}
			if (!(entity instanceof final ISprintableItem sprintableItem)) {
				return "";
			}
			final CSprintItem sprintItem = sprintableItem.getSprintItem();
			final long points = sprintItem != null && sprintItem.getStoryPoint() != null
					? sprintItem.getStoryPoint()
					: 0L;
			final boolean done = entity instanceof final CProjectItem<?> projectItem
					&& projectItem.getStatus() != null
					&& Boolean.TRUE.equals(projectItem.getStatus().getFinalStatus());
			final CSprintPlanningSprintMetrics metrics =
					new CSprintPlanningSprintMetrics(done ? 1 : 0, 1,
						done ? points : 0, points);
			return metrics.formatRollup();
		}).setWidth("180px").setFlexGrow(0).setKey("progress")
				.setHeader("Progress");
		CGrid.styleColumnHeader(treeGrid.getColumnByKey("progress"),
				"Progress");
	}

	@Override
	protected void configureNameColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final var nameColumn = treeGrid
				.addComponentHierarchyColumn(this::createHierarchyComponent)
				.setAutoWidth(false)
				.setResizable(true)
				.setKey("name")
				.setFlexGrow(0)
				.setWidth(NAME_COLUMN_WIDTH_PX + "px");
		decorateNameColumnHeader(nameColumn, "Name");
	}

	@Override
	protected int getNonTimelineColumnWidthPx() {
		// Name + SP + Progress + ID + Start + End + Responsible + Status
		return NAME_COLUMN_WIDTH_PX + 70 + 180 + 80 + 110 + 110 + 135 + 140;
	}

	public void setSprintMetrics(final Map<Long, CSprintPlanningSprintMetrics> sprintMetricsById) {
		this.sprintMetricsById = sprintMetricsById != null ? sprintMetricsById : Map.of();
	}

	public CGnntItem getSelectedItem() {
		return getTreeGrid().asSingleSelect().getValue();
	}

	public void setContextActions(
			final List<CContextActionDefinition<CGnntItem>> actions) {
		setItemContextActions(actions);
		setHierarchyContextActions(actions);
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
		itemByKey = buildItemKeyMap(flatItems);

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

		refreshHeaderActionStates();
		restoreGridScrollPosition();
	}

	private void configureDragAndDrop(final String gridId) {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.setRowsDraggable(true);
		// Allow both sprint assignment (drop on sprint row) and ordered insertion relative to sprint items.
		treeGrid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);
		treeGrid.addDragStartListener(event -> {
			final CGnntItem dragged = event.getDraggedItems().stream().findFirst().orElse(null);
			if (dragContext != null) {
				dragContext.setDraggedItem(dragged, gridId);
			}
		});
		treeGrid.addDropListener(event -> {
			final CGnntItem dropSource = dragContext != null ? dragContext.getDraggedItem() : null;
			if (dragContext != null) {
				// Always clear after the target resolves the dragged item so cross-grid drops do not lose their payload.
				dragContext.clear();
			}
			if (dropSource == null || dropListener == null) {
				return;
			}
			final GridDropLocation dropLocation = event.getDropLocation() != null ? event.getDropLocation() : GridDropLocation.ON_TOP;
			// TreeGrid can report an empty target when the pointer lands between rendered rows, so keep the drop request and let the board resolve a fallback sprint.
			dropListener.accept(new CSprintPlanningDropRequest(dropSource, event.getDropTargetItem().orElse(null), dropLocation, gridId));
		});
	}

	public boolean selectByEntityKey(final String entityKey) {
		if (entityKey == null || entityKey.isBlank()) {
			return false;
		}
		final CGnntItem item = itemByKey.get(entityKey);
		if (item == null) {
			return false;
		}
		getTreeGrid().select(item);
		return true;
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

		return decorateHierarchyComponent(layout, item);
	}
}
