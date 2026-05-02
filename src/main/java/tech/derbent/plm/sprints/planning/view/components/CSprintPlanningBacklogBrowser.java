package tech.derbent.plm.sprints.planning.view.components;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.api.ui.component.enhanced.CQuickAccessPanel;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

/**
 * Backlog browser used by the sprint planning board.
 *
 * <p>The backlog is intentionally split into two synchronized views:
 * <ul>
 *   <li>Left: a parent-only hierarchy browser (levels 0..n).</li>
 *   <li>Right: a flat, leaf-only backlog list that stays focused on sprint-assignable items.</li>
 * </ul>
 * This keeps the planning interaction fast while still providing parent context for large hierarchies.</p>
 */
public final class CSprintPlanningBacklogBrowser extends CVerticalLayout {

	public static final String ID_BROWSER = "custom-sprint-planning-backlog-browser";
	public static final String ID_SPLIT = "custom-sprint-planning-backlog-browser-split";
	public static final String ID_METRICS_PARENT = "custom-sprint-planning-backlog-parent-metrics";
	public static final String ID_METRICS_LEAF = "custom-sprint-planning-backlog-leaf-metrics";
	private static final long serialVersionUID = 1L;

	private final CSprintPlanningParentBrowserTreeGrid gridParents;
	private final CSprintPlanningFlatGrid gridLeaves;
	private final BiConsumer<CGnntItem, CGnntItem> parentDropListener;
	private final Span spanBacklogParentMetrics;
	private final Span spanBacklogLeafMetrics;
	private final CVerticalLayout layoutParentsPanel;
	private final SplitLayout splitLayout;

	private String selectedParentKey;
	private List<CGnntItem> allLeafItems = List.of();
	private Map<String, CProjectItem<?, ?>> entitiesByKey = Map.of();
	private CGanttTimelineRange lastRange;

	public CSprintPlanningBacklogBrowser(final CSprintPlanningDragContext dragContext, final Consumer<CGnntItem> leafSelectionListener,
			final Consumer<CSprintPlanningDropRequest> backlogDropListener,
			final BiConsumer<CGnntItem, CGnntItem> parentDropListener,
			final List<Component> parentBrowserFilters) {
		this(dragContext, leafSelectionListener, backlogDropListener, parentDropListener, parentBrowserFilters, null);
	}

	public CSprintPlanningBacklogBrowser(final CSprintPlanningDragContext dragContext, final Consumer<CGnntItem> leafSelectionListener,
			final Consumer<CSprintPlanningDropRequest> backlogDropListener,
			final BiConsumer<CGnntItem, CGnntItem> parentDropListener,
			final List<Component> parentBrowserFilters,
			final Consumer<CGnntItem> leafDragStartListener) {

		setId(ID_BROWSER);
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		setHeightFull();

		this.parentDropListener = parentDropListener;

		gridParents = new CSprintPlanningParentBrowserTreeGrid(
				CSprintPlanningParentBrowserTreeGrid.ID_TREE_GRID,
				dragContext,
				this::onParentSelected,
				this::onParentDrop);
		gridLeaves = new CSprintPlanningFlatGrid(CSprintPlanningFlatGrid.ID_GRID, dragContext, leafSelectionListener,
				backlogDropListener, leafDragStartListener);

		// Backlog metrics are shown on the backlog panels (not on the main sprint header) so sprint selection stays focused.
		spanBacklogParentMetrics = createBacklogMetricsSpan(ID_METRICS_PARENT);
		spanBacklogLeafMetrics = createBacklogMetricsSpan(ID_METRICS_LEAF);
		gridParents.getQuickAccessPanel().addCustomComponent(spanBacklogParentMetrics);
		gridLeaves.getQuickAccessPanel().addCustomComponent(spanBacklogLeafMetrics);

		layoutParentsPanel = new CVerticalLayout();
		layoutParentsPanel.setPadding(false);
		layoutParentsPanel.setSpacing(false);
		layoutParentsPanel.setWidthFull();
		layoutParentsPanel.setHeightFull();
		layoutParentsPanel.getStyle().set("gap", "8px");
		layoutParentsPanel.getStyle().set("flex", "1");
		layoutParentsPanel.getStyle().set("min-height", "0");

		// Keep the parent browser header compact: filters live in the grid header quick-access panel (no extra vertical rows).
		if (parentBrowserFilters != null && !parentBrowserFilters.isEmpty()) {
			parentBrowserFilters.stream()
					.filter((final Component filter) -> filter != null)
					.forEach((final Component filter) -> {
						if (filter instanceof HasSize) {
							// Keep header controls narrow so the parent browser doesn't steal width from the backlog leaf grid.
							((HasSize) filter).setWidth("200px");
						}
						gridParents.getQuickAccessPanel().addCustomComponent(filter);
					});
		}

		layoutParentsPanel.add(gridParents);
		layoutParentsPanel.setFlexGrow(1, gridParents);

		splitLayout = new SplitLayout(layoutParentsPanel, gridLeaves);
		splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
		splitLayout.setSplitterPosition(35.0);
		splitLayout.setWidthFull();
		splitLayout.setHeightFull();
		splitLayout.setId(ID_SPLIT);

		add(splitLayout);
		setFlexGrow(1, splitLayout);
	}

	public com.vaadin.flow.component.grid.Grid<CGnntItem> getLeavesGridComponent() {
		return gridLeaves.getGridComponent();
	}

	public CQuickAccessPanel getLeafQuickAccessPanel() {
		return gridLeaves.getQuickAccessPanel();
	}

	public CQuickAccessPanel getParentQuickAccessPanel() {
		return gridParents.getQuickAccessPanel();
	}

	public CGnntItem getSelectedLeafItem() {
		return gridLeaves.getSelectedItem();
	}

	public CGnntItem getSelectedParentItem() {
		return gridParents.getSelectedItem();
	}

	public boolean selectByEntityKey(final String entityKey) {
		if (entityKey == null || entityKey.isBlank()) {
			return false;
		}
		// Selection must win over the parent-based leaf filter: if the entity is currently hidden by a pinned parent,
		// clear the parent and retry so quick actions work after refreshes.
		if (gridLeaves.selectByEntityKey(entityKey)) {
			return true;
		}
		if (selectedParentKey != null) {
			selectedParentKey = null;
			updateLeafGrid();
			if (gridLeaves.selectByEntityKey(entityKey)) {
				return true;
			}
		}
		return gridParents.selectByEntityKey(entityKey);
	}

	public void setLeafContextActions(final List<CContextActionDefinition<CGnntItem>> actions) {
		gridLeaves.setContextActions(actions);
	}

	public void setParentContextActions(final List<CContextActionDefinition<CGnntItem>> actions) {
		gridParents.setContextActions(actions);
	}

	public void setShowParentTaskRollup(final boolean show) {
		gridParents.setShowTaskRollup(show);
	}

	public void setBacklogMetrics(final CSprintPlanningSprintMetrics metrics) {
		final CSprintPlanningSprintMetrics safeMetrics = metrics != null
				? metrics
				: new CSprintPlanningSprintMetrics(0, 0, 0, 0);
		final String text = "Backlog: " + safeMetrics.formatRollup();
		spanBacklogParentMetrics.setText(text);
		spanBacklogLeafMetrics.setText(text);
	}

	public void setParentRollupSummaries(
			final Map<String, CSprintPlanningSprintMetrics> rollupSummariesByKey) {
		gridParents.setRollupMetricsByEntityKey(rollupSummariesByKey);
	}

	public void setParentItemDoubleClickHandler(
			final Consumer<CGnntItem> itemDoubleClickHandler) {
		gridParents.setItemDoubleClickHandler(itemDoubleClickHandler);
	}

	public void setLeafItemDoubleClickHandler(
			final Consumer<CGnntItem> itemDoubleClickHandler) {
		gridLeaves.setItemDoubleClickHandler(itemDoubleClickHandler);
	}

	private Span createBacklogMetricsSpan(final String id) {
		final Span span = new Span("Backlog: 0/0 tasks, 0/0 SP");
		span.setId(id);
		span.getStyle().set("font-size", "var(--lumo-font-size-s)")
				.set("color", "var(--lumo-secondary-text-color)")
				.set("padding", "0 6px")
				.set("white-space", "nowrap");
		return span;
	}

	public void setBacklogData(final CGnntHierarchyResult parentHierarchy, final CGnntHierarchyResult leafHierarchy,
			final Map<String, CProjectItem<?, ?>> entitiesByKey, final CGanttTimelineRange range) {

		// Keep these cached so parent selection can filter leaf items without reloading from the database.
		this.entitiesByKey = entitiesByKey != null ? entitiesByKey : Map.of();
		allLeafItems = leafHierarchy != null ? leafHierarchy.getFlatItems() : List.of();
		lastRange = range;
		if (selectedParentKey != null && !this.entitiesByKey.containsKey(selectedParentKey)) {
			selectedParentKey = null;
		} else if (selectedParentKey != null && parentHierarchy != null
				&& parentHierarchy.getFlatItems().stream().noneMatch(item -> selectedParentKey.equals(item.getEntityKey()))) {
			// Search/filter refresh can hide the previously selected parent; fall back to the full leaf result set
			// instead of leaving the detail grid pinned to a now-hidden branch.
			selectedParentKey = null;
		}

		gridParents.setHierarchy(parentHierarchy, range);
		updateLeafGrid();
	}

	private void onParentSelected(final CGnntItem selectedParent) {
		selectedParentKey = selectedParent != null ? selectedParent.getEntityKey() : null;
		updateLeafGrid();
	}

	private void onParentDrop(final CGnntItem draggedItem,
			final CGnntItem targetItem) {
		// Delegate to the board so the parent browser remains a passive view component.
		if (parentDropListener != null) {
			parentDropListener.accept(draggedItem, targetItem);
		}
	}

	private void updateLeafGrid() {
		// Fallback range keeps timeline components stable when the board has no active project.
		final CGanttTimelineRange safeRange = lastRange != null ? lastRange : new CGanttTimelineRange(LocalDate.now(), LocalDate.now());
		final List<CGnntItem> filtered = filterLeafItems(allLeafItems);
		gridLeaves.setHierarchy(new CGnntHierarchyResult(filtered, Map.of(), filtered), safeRange);
	}

	private List<CGnntItem> filterLeafItems(final List<CGnntItem> items) {
		if (selectedParentKey == null || selectedParentKey.isBlank()) {
			return items != null ? items : List.of();
		}
		if (items == null || items.isEmpty()) {
			return List.of();
		}

		final List<CGnntItem> filtered = new ArrayList<>();
		items.forEach((final CGnntItem item) -> {
			final CProjectItem<?, ?> entity = item != null ? item.getEntity() : null;
			if (entity != null && isDescendantOfSelectedParent(entity)) {
				filtered.add(item);
			}
		});
		return filtered;
	}

	private boolean isDescendantOfSelectedParent(final CProjectItem<?, ?> leaf) {
		String parentKey = CHierarchyNavigationService.buildParentKey(leaf);
		while (parentKey != null) {
			if (parentKey.equals(selectedParentKey)) {
				return true;
			}
			final CProjectItem<?, ?> parent = entitiesByKey.get(parentKey);
			if (parent == null) {
				break;
			}
			parentKey = CHierarchyNavigationService.buildParentKey(parent);
		}
		return false;
	}
}
