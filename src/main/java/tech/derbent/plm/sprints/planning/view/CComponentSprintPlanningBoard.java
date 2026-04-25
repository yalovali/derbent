package tech.derbent.plm.sprints.planning.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.splitlayout.SplitLayout;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.planning.domain.CSprintPlanningViewEntity;
import tech.derbent.plm.sprints.planning.domain.ESprintPlanningScope;
import tech.derbent.plm.sprints.planning.view.components.CDialogAddBacklogItemToSprint;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningBacklogBrowser;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningDragContext;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningFilterToolbar;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningQuickAccessPanel;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningSprintMetrics;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningTreeGrid;
import tech.derbent.plm.sprints.service.CSprintService;

/**
 * Sprint Planning Board (v2) - timeline + drag/drop planning without changing existing sprint pages.
 *
 * <p>Design goals:
 * <ul>
 *   <li>Backlog is split: left parent browser tree (levels 0..n) + right leaf-only flat grid for sprint assignment.</li>
 *   <li>Sprints are shown as a Gnnt-style tree (Sprint → Items) with timeline.</li>
 *   <li>Cross-grid drag/drop is supported via a shared {@link CSprintPlanningDragContext}.</li>
 *   <li>Only leaf items (level -1) can be assigned to sprints (drag/drop and dialog).</li>
 * </ul>
 *
 * Existing sprint pages remain the source of truth for sprint definitions; this board focuses on fast assignment and review.</p>
 */
public class CComponentSprintPlanningBoard extends CComponentBase<CSprintPlanningViewEntity> {

	public static final String ID_BOARD = "custom-sprint-planning-board-v2";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSprintPlanningBoard.class);
	private static final long serialVersionUID = 1L;

	private static final double DEFAULT_SPLITTER_POSITION = 60.0;

	// Sprint header metrics (items + story points) for Jira-like sprint rows.
	private Map<Long, CSprintPlanningSprintMetrics> sprintMetricsById = Map.of();

	private final CSprintPlanningDragContext dragContext = new CSprintPlanningDragContext();
	private final CComponentItemDetails componentItemDetails;
	private final CSprintPlanningFilterToolbar filterToolbar;
	private final CSprintPlanningBacklogBrowser backlogBrowser;
	private final CSprintPlanningTreeGrid gridSprints;
	private final CVerticalLayout layoutGrids;
	private final CSprintService sprintService;
	private CSprintPlanningQuickAccessPanel quickAccessPanel;
	private boolean detailsVisible = false;
	private SplitLayout splitLayout;
	private double previousSplitterPosition = DEFAULT_SPLITTER_POSITION;
	private CEntityNamed<?> selectedDetailsEntity;
	private CGnntItem selectedItem;
	private CSprint selectedSprintForMetrics;

	public CComponentSprintPlanningBoard(final ISessionService sessionService) {
		try {
			componentItemDetails = new CComponentItemDetails(sessionService);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to initialize sprint planning details component", e);
		}

		sprintService = CSpringContext.getBean(CSprintService.class);

		filterToolbar = new CSprintPlanningFilterToolbar();
		filterToolbar.addChangeListener(event -> refreshComponent());
		filterToolbar.setAddToSprintHandler(this::openAddToSprintDialog);

		backlogBrowser = new CSprintPlanningBacklogBrowser(
				dragContext,
				this::onItemSelected,
				this::onBacklogDrop,
				filterToolbar.getBacklogParentBrowserFilterComponents());
		gridSprints = new CSprintPlanningTreeGrid(CSprintPlanningTreeGrid.ID_TREE_GRID, dragContext, this::onItemSelected, this::onSprintDrop);

		layoutGrids = new CVerticalLayout();
		layoutGrids.setPadding(false);
		layoutGrids.setSpacing(false);
		layoutGrids.setWidthFull();
		layoutGrids.setHeightFull();

		initializeLayout();
	}

	private void initializeLayout() {
		setId(ID_BOARD);
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		setHeightFull();

		// Layout requested: sprints on top, backlog at bottom.
		final SplitLayout gridsSplit = new SplitLayout(gridSprints, backlogBrowser);
		gridsSplit.setOrientation(SplitLayout.Orientation.VERTICAL);
		gridsSplit.setSplitterPosition(55.0);
		gridsSplit.setWidthFull();
		gridsSplit.setHeightFull();

		final List<com.vaadin.flow.component.Component> quickControls = filterToolbar.extractQuickControlsForSidebar();
		quickAccessPanel = new CSprintPlanningQuickAccessPanel(
				this::toggleDetailsPanel,
				this::refreshComponent,
				quickControls);

		final SplitLayout horizontalSplit = new SplitLayout(quickAccessPanel, gridsSplit);
		horizontalSplit.setOrientation(SplitLayout.Orientation.HORIZONTAL);
		horizontalSplit.setSplitterPosition(20.0);
		horizontalSplit.setWidthFull();
		horizontalSplit.setHeightFull();

		layoutGrids.add(filterToolbar, horizontalSplit);
		layoutGrids.setFlexGrow(0, filterToolbar);
		layoutGrids.setFlexGrow(1, horizontalSplit);

		splitLayout = new SplitLayout(layoutGrids, componentItemDetails);
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.setSplitterPosition(detailsVisible ? DEFAULT_SPLITTER_POSITION : 100.0);
		componentItemDetails.setVisible(detailsVisible);
		splitLayout.setWidthFull();
		splitLayout.setHeightFull();

		add(splitLayout);
	}

	private void toggleDetailsPanel() {
		if (splitLayout == null) {
			return;
		}
		detailsVisible = !detailsVisible;
		if (detailsVisible) {
			componentItemDetails.setVisible(true);
			splitLayout.setSplitterPosition(previousSplitterPosition);
			if (quickAccessPanel != null) {
				quickAccessPanel.setDetailsVisible(true);
			}
			if (selectedDetailsEntity == null) {
				componentItemDetails.clear();
			} else {
				componentItemDetails.setValue(selectedDetailsEntity);
			}
		} else {
			previousSplitterPosition = splitLayout.getSplitterPosition();
			splitLayout.setSplitterPosition(100.0);
			componentItemDetails.setVisible(false);
			if (quickAccessPanel != null) {
				quickAccessPanel.setDetailsVisible(false);
			}
		}
	}

	private void onItemSelected(final CGnntItem item) {
		selectedItem = item;
		selectedDetailsEntity = item != null ? item.getEntity() : null;
		updateSelectedSprintFromSelection(item);

		if (!detailsVisible) {
			// Avoid triggering expensive detail layouts while in planning mode.
			return;
		}
		if (selectedDetailsEntity == null) {
			componentItemDetails.clear();
			return;
		}
		componentItemDetails.setValue(selectedDetailsEntity);
	}

	private void onBacklogDrop(final CGnntItem draggedItem, final CGnntItem dropTarget) {
		try {
			if (draggedItem == null || draggedItem.getEntity() == null) {
				return;
			}
			if (draggedItem.getEntity() instanceof CSprint) {
				return;
			}
			if (!(draggedItem.getEntity() instanceof ISprintableItem sprintableItem)) {
				CNotificationService.showWarning("Only sprintable items can be moved to backlog.");
				return;
			}

			sprintableItem.moveSprintItemToBacklog();
			refreshComponent();
			CNotificationService.showSuccess("Moved '%s' to backlog".formatted(draggedItem.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to move item to backlog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to move item to backlog", e);
		}
	}

	private void onSprintDrop(final CGnntItem draggedItem, final CGnntItem dropTarget) {
		try {
			Check.notNull(dropTarget, "Drop target cannot be null");
			if (draggedItem == null || draggedItem.getEntity() == null) {
				return;
			}
			if (!(draggedItem.getEntity() instanceof ISprintableItem sprintableItem)) {
				CNotificationService.showWarning("Only sprintable items can be assigned to sprints.");
				return;
			}
			if (!validateLeafOnly(draggedItem.getEntity(), draggedItem.getName())) {
				return;
			}

			final CSprint targetSprint = resolveTargetSprint(dropTarget);
			if (targetSprint == null) {
				CNotificationService.showWarning("Drop on a sprint row to assign the item.");
				return;
			}
			if (isClosedSprint(targetSprint)) {
				CNotificationService.showWarning("Cannot add items to a closed sprint.");
				return;
			}

			sprintableItem.moveSprintItemToSprint(targetSprint);
			refreshComponent();
			CNotificationService.showSuccess("Assigned '%s' to sprint '%s'".formatted(draggedItem.getName(), targetSprint.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to assign item to sprint: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to assign item to sprint", e);
		}
	}

	private CSprint resolveTargetSprint(final CGnntItem dropTarget) {
		if (dropTarget == null || dropTarget.getEntity() == null) {
			return null;
		}
		if (dropTarget.getEntity() instanceof CSprint sprint) {
			return sprint;
		}
		if (dropTarget.getEntity() instanceof ISprintableItem sprintableItem) {
			return sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
		}
		return null;
	}

	private void updateSelectedSprintFromSelection(final CGnntItem item) {
		final Object entity = item != null ? item.getEntity() : null;
		if (entity instanceof CSprint sprint) {
			selectedSprintForMetrics = sprint;
		} else if (entity instanceof ISprintableItem sprintableItem) {
			selectedSprintForMetrics = sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
		} else {
			selectedSprintForMetrics = filterToolbar.getSelectedSprint();
		}
		updateSelectedSprintMetrics();
	}

	private void updateSelectedSprintMetrics() {
		final CSprint sprint = selectedSprintForMetrics != null ? selectedSprintForMetrics : filterToolbar.getSelectedSprint();
		if (sprint == null || sprint.getId() == null) {
			filterToolbar.setSelectedSprintMetrics(null, 0, 0);
			return;
		}
		final CSprintPlanningSprintMetrics metrics = sprintMetricsById.getOrDefault(sprint.getId(), new CSprintPlanningSprintMetrics(0, 0));
		filterToolbar.setSelectedSprintMetrics(sprint, metrics.itemCount(), metrics.storyPoints());
	}

	private boolean isClosedSprint(final CSprint sprint) {
		return sprint != null && sprint.getStatus() != null && Boolean.TRUE.equals(sprint.getStatus().getFinalStatus());
	}

	private boolean validateLeafOnly(final Object entity, final String displayName) {
		if (!(entity instanceof CProjectItem<?> projectItem)) {
			return true;
		}
		// Level -1 indicates leaf items; only those can be committed into a sprint.
		final int level = CHierarchyNavigationService.getEntityLevel(projectItem);
		if (level == -1) {
			return true;
		}
		CNotificationService.showWarning("Only leaf items can be added to a sprint: '%s'".formatted(displayName));
		return false;
	}

	private void openAddToSprintDialog() {
		try {
			final CSprintPlanningViewEntity view = getValue();
			if (view == null || view.getProject() == null) {
				CNotificationService.showWarning("Select a project first");
				return;
			}
			if (selectedItem == null || selectedItem.getEntity() == null) {
				CNotificationService.showWarning("Select an item in backlog first");
				return;
			}
			if (selectedItem.getEntity() instanceof CSprint) {
				CNotificationService.showWarning("Select a backlog item (not a sprint)");
				return;
			}
			if (!(selectedItem.getEntity() instanceof ISprintableItem sprintableItem)) {
				CNotificationService.showWarning("Only sprintable items can be assigned to a sprint");
				return;
			}
			if (!validateLeafOnly(selectedItem.getEntity(), selectedItem.getName())) {
				return;
			}

			final List<CSprint> availableSprints = new ArrayList<>(sprintService.listByProject(view.getProject()));
			availableSprints.sort(Comparator.comparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo))
					.thenComparing(CSprint::getName, String.CASE_INSENSITIVE_ORDER));

			final CDialogAddBacklogItemToSprint dialog = new CDialogAddBacklogItemToSprint(selectedItem.getName(), availableSprints, sprint -> {
				if (isClosedSprint(sprint)) {
					CNotificationService.showWarning("Cannot add items to a closed sprint.");
					return;
				}
				sprintableItem.moveSprintItemToSprint(sprint);
				refreshComponent();
			});
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open add-to-sprint dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to open add-to-sprint dialog", e);
		}
	}

	@Override
	protected void onValueChanged(final CSprintPlanningViewEntity oldValue, final CSprintPlanningViewEntity newValue, final boolean fromClient) {
		LOGGER.debug("Sprint planning board changed from {} to {}", oldValue != null ? oldValue.getName() : "null",
				newValue != null ? newValue.getName() : "null");
		refreshComponent();
	}

	@Override
	protected void refreshComponent() {
		try {
			final CSprintPlanningViewEntity view = getValue();
			if (view == null || view.getProject() == null) {
				final CGnntHierarchyResult emptyHierarchy = new CGnntHierarchyResult(List.of(), Map.of(), List.of());
				final CGanttTimelineRange emptyRange = new CGanttTimelineRange(LocalDate.now(), LocalDate.now());
				backlogBrowser.setBacklogData(emptyHierarchy, emptyHierarchy, Map.of(), emptyRange);
				gridSprints.setHierarchy(emptyHierarchy, emptyRange);
				selectedDetailsEntity = null;
				selectedItem = null;
				selectedSprintForMetrics = null;
				sprintMetricsById = Map.of();
				filterToolbar.setBacklogMetrics(0, 0);
				filterToolbar.setSelectedSprintMetrics(null, 0, 0);
				if (detailsVisible) {
					componentItemDetails.clear();
				}
				return;
			}

			filterToolbar.setProject(view.getProject());

			final Map<String, CProjectItem<?>> entitiesByKey = loadSprintableItems(view);
			final List<CGnntItem> allItems = new ArrayList<>();
			long entityTypeSequence = 1;
			for (final CProjectItem<?> projectItem : entitiesByKey.values()) {
				allItems.add(new CGnntItem(projectItem, entityTypeSequence++, 0));
			}
			filterToolbar.setAvailableEntityTypes(allItems);

			final CBacklogData backlogData = buildBacklogData(view, entitiesByKey);
			final CGnntHierarchyResult sprints = buildSprintHierarchy(view, entitiesByKey);
			updateBacklogMetrics(view, entitiesByKey);

			final CGanttTimelineRange range = resolveTimelineRange(backlogData.leafHierarchy().getFlatItems(), sprints.getFlatItems());
			backlogBrowser.setBacklogData(backlogData.parentHierarchy(), backlogData.leafHierarchy(), entitiesByKey, range);
			gridSprints.setHierarchy(sprints, range);
		} catch (final Exception e) {
			LOGGER.error("Failed to refresh sprint planning board: {}", e.getMessage(), e);
			throw e;
		}
	}

	private static final class CVisibleNode {
		private final CGnntItem item;
		private final List<CVisibleNode> children;

		private CVisibleNode(final CGnntItem item, final List<CVisibleNode> children) {
			this.item = item;
			this.children = children;
		}
	}

	private Map<String, CProjectItem<?>> loadSprintableItems(final CSprintPlanningViewEntity view) {
		final Map<String, CProjectItem<?>> itemsByKey = new HashMap<>();
		for (final Object rawKey : CEntityRegistry.getAllRegisteredEntityKeys()) {
			// Conservative list: only entities that already implement ISprintableItem are shown.
			final String entityKey = String.valueOf(rawKey);
			final Class<?> entityClass = CEntityRegistry.getEntityClass(entityKey);
			if (entityClass == null || !CProjectItem.class.isAssignableFrom(entityClass) || !ISprintableItem.class.isAssignableFrom(entityClass)) {
				continue;
			}
			try {
				final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
				final Object serviceBean = CSpringContext.getBean(serviceClass);
				if (!(serviceBean instanceof CEntityOfProjectService<?> projectService)) {
					continue;
				}
				for (final Object rawEntity : projectService.listByProject(view.getProject())) {
					if (!(rawEntity instanceof CProjectItem<?> projectItem) || !(projectItem instanceof ISprintableItem)) {
						continue;
					}
					final String key = CHierarchyNavigationService.buildEntityKey(projectItem);
					if (key != null) {
						itemsByKey.put(key, projectItem);
					}
				}
			} catch (final Exception e) {
				LOGGER.debug("Skipping sprint planning items for {}: {}", entityKey, e.getMessage());
			}
		}
		return itemsByKey;
	}

	private void updateBacklogMetrics(final CSprintPlanningViewEntity view, final Map<String, CProjectItem<?>> entitiesByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		if (scope == ESprintPlanningScope.SPRINT) {
			filterToolbar.setBacklogMetrics(0, 0);
			return;
		}
		int itemCount = 0;
		long storyPoints = 0;
		for (final CProjectItem<?> entity : entitiesByKey.values()) {
			final CSprint sprint = ((ISprintableItem) entity).getSprintItem() != null ? ((ISprintableItem) entity).getSprintItem().getSprint() : null;
			final boolean backlogCandidate = scope == ESprintPlanningScope.ALL_ITEMS || sprint == null;
			if (!backlogCandidate || !filterToolbar.shouldIncludeBacklogItem(entity)) {
				continue;
			}
			// Metrics should be leaf-focused, matching the leaf-only sprint assignment rule.
			if (CHierarchyNavigationService.getEntityLevel(entity) != -1) {
				continue;
			}
			itemCount++;
			final Long points = ((ISprintableItem) entity).getSprintItem() != null ? ((ISprintableItem) entity).getSprintItem().getStoryPoint() : null;
			storyPoints += points != null ? points : 0L;
		}
		filterToolbar.setBacklogMetrics(itemCount, storyPoints);
	}

	private record CBacklogData(CGnntHierarchyResult parentHierarchy, CGnntHierarchyResult leafHierarchy) {
	}

	private record CBacklogBuildResult(CVisibleNode node, boolean hasVisibleLeafDescendant) {
	}

	private CBacklogData buildBacklogData(final CSprintPlanningViewEntity view, final Map<String, CProjectItem<?>> entitiesByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		if (scope == ESprintPlanningScope.SPRINT) {
			// In sprint scope we keep backlog empty to focus on the sprint tree.
			final CGnntHierarchyResult empty = new CGnntHierarchyResult(List.of(), Map.of(), List.of());
			return new CBacklogData(empty, empty);
		}

		final Map<String, List<CProjectItem<?>>> childrenByParentKey = new HashMap<>();
		final List<CProjectItem<?>> roots = new ArrayList<>();
		for (final CProjectItem<?> item : entitiesByKey.values()) {
			final String key = CHierarchyNavigationService.buildEntityKey(item);
			if (key == null) {
				continue;
			}
			final String parentKey = CHierarchyNavigationService.buildParentKey(item);
			if (parentKey == null || !entitiesByKey.containsKey(parentKey)) {
				roots.add(item);
			} else {
				childrenByParentKey.computeIfAbsent(parentKey, ignored -> new ArrayList<>()).add(item);
			}
		}
		// Jira-like ordering: backlog is primarily rank-ordered.
		roots.sort(Comparator.comparingInt((final CProjectItem<?> item) -> resolveItemOrder(item))
				.thenComparing(CProjectItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
		for (final List<CProjectItem<?>> children : childrenByParentKey.values()) {
			children.sort(Comparator.comparingInt((final CProjectItem<?> item) -> resolveItemOrder(item))
					.thenComparing(CProjectItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
		}

		final List<CVisibleNode> visibleParentRoots = new ArrayList<>();
		final List<CGnntItem> visibleLeafItems = new ArrayList<>();
		final long[] uniqueId = new long[] { 1 };
		for (final CProjectItem<?> root : roots) {
			final CBacklogBuildResult result = buildVisibleBacklogParentNode(root, 0, scope, childrenByParentKey, visibleLeafItems, uniqueId);
			if (result.node() != null) {
				visibleParentRoots.add(result.node());
			}
		}

		visibleLeafItems.sort(Comparator.<CGnntItem>comparingInt(item -> resolveItemOrder(item.getEntity()))
				.thenComparing(CGnntItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

		final CGnntHierarchyResult parentHierarchy = flattenVisibleNodes(visibleParentRoots);
		final CGnntHierarchyResult leafHierarchy = new CGnntHierarchyResult(visibleLeafItems, Map.of(), visibleLeafItems);
		return new CBacklogData(parentHierarchy, leafHierarchy);
	}

	private static int resolveItemOrder(final CProjectItem<?> item) {
		return resolveItemOrder((Object) item);
	}

	private static int resolveItemOrder(final Object entity) {
		if (!(entity instanceof ISprintableItem sprintableItem)) {
			return Integer.MAX_VALUE;
		}
		final Integer order = sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getItemOrder() : null;
		return order != null ? order : Integer.MAX_VALUE;
	}

	private CBacklogBuildResult buildVisibleBacklogParentNode(final CProjectItem<?> entity, final int hierarchyLevel, final ESprintPlanningScope scope,
			final Map<String, List<CProjectItem<?>>> childrenByParentKey, final List<CGnntItem> visibleLeafItems, final long[] uniqueId) {
		final String entityKey = CHierarchyNavigationService.buildEntityKey(entity);
		if (entityKey == null) {
			return new CBacklogBuildResult(null, false);
		}

		final int entityLevel = CHierarchyNavigationService.getEntityLevel(entity);
		if (entityLevel == -1) {
			final boolean leafVisible = isBacklogCandidate(entity, scope) && filterToolbar.shouldIncludeBacklogItem(entity);
			if (leafVisible) {
				// Leaf items are rendered in a separate flat grid, so they don't need hierarchy indentation here.
				visibleLeafItems.add(new CGnntItem(entity, uniqueId[0]++, 0));
			}
			return new CBacklogBuildResult(null, leafVisible);
		}

		final List<CVisibleNode> visibleChildren = new ArrayList<>();
		boolean hasVisibleLeaf = false;
		for (final CProjectItem<?> child : childrenByParentKey.getOrDefault(entityKey, List.of())) {
			final CBacklogBuildResult visibleChild = buildVisibleBacklogParentNode(child, hierarchyLevel + 1, scope, childrenByParentKey,
					visibleLeafItems, uniqueId);
			if (visibleChild.node() != null) {
				visibleChildren.add(visibleChild.node());
			}
			hasVisibleLeaf = hasVisibleLeaf || visibleChild.hasVisibleLeafDescendant();
		}

		final boolean matchesFilters = isBacklogCandidate(entity, scope) && filterToolbar.shouldIncludeBacklogItem(entity);
		if (!matchesFilters && visibleChildren.isEmpty() && !hasVisibleLeaf) {
			return new CBacklogBuildResult(null, false);
		}

		final CGnntItem item = new CGnntItem(entity, uniqueId[0]++, hierarchyLevel);
		item.setHasChildren(!visibleChildren.isEmpty());
		return new CBacklogBuildResult(new CVisibleNode(item, visibleChildren), hasVisibleLeaf);
	}

	private boolean isBacklogCandidate(final CProjectItem<?> entity, final ESprintPlanningScope scope) {
		if (scope == ESprintPlanningScope.ALL_ITEMS) {
			return true;
		}
		final CSprint sprint = ((ISprintableItem) entity).getSprintItem() != null ? ((ISprintableItem) entity).getSprintItem().getSprint() : null;
		return sprint == null;
	}

	private CGnntHierarchyResult flattenVisibleNodes(final List<CVisibleNode> rootNodes) {
		final List<CGnntItem> flatItems = new ArrayList<>();
		final List<CGnntItem> rootItems = new ArrayList<>();
		final Map<String, List<CGnntItem>> childrenByParentKey = new HashMap<>();
		for (final CVisibleNode rootNode : rootNodes) {
			rootItems.add(rootNode.item);
			flattenVisibleNode(rootNode, flatItems, childrenByParentKey);
		}
		return new CGnntHierarchyResult(rootItems, childrenByParentKey, flatItems);
	}

	private void flattenVisibleNode(final CVisibleNode node, final List<CGnntItem> flatItems, final Map<String, List<CGnntItem>> childrenByParentKey) {
		flatItems.add(node.item);
		if (node.children == null || node.children.isEmpty()) {
			return;
		}
		final String key = node.item.getEntityKey();
		final List<CGnntItem> childItems = new ArrayList<>();
		for (final CVisibleNode child : node.children) {
			childItems.add(child.item);
			flattenVisibleNode(child, flatItems, childrenByParentKey);
		}
		childrenByParentKey.put(key, childItems);
	}

	private CGnntHierarchyResult buildSprintHierarchy(final CSprintPlanningViewEntity view, final Map<String, CProjectItem<?>> entitiesByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		final CSprint selectedSprint = filterToolbar.getSelectedSprint();

		final List<CSprint> sprints = new ArrayList<>(sprintService.listByProject(view.getProject()));
		sprints.sort(Comparator.comparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo))
				.thenComparing(CSprint::getName, String.CASE_INSENSITIVE_ORDER));

		// Metrics are computed on full sprint membership (not search-filtered) so the widget stays stable.
		final Map<Long, CSprintPlanningSprintMetrics> metricsBySprintId = new HashMap<>();
		for (final CProjectItem<?> entity : entitiesByKey.values()) {
			final CSprint sprint = ((ISprintableItem) entity).getSprintItem() != null ? ((ISprintableItem) entity).getSprintItem().getSprint() : null;
			if (sprint == null || sprint.getId() == null) {
				continue;
			}
			final long points = ((ISprintableItem) entity).getSprintItem() != null && ((ISprintableItem) entity).getSprintItem().getStoryPoint() != null
					? ((ISprintableItem) entity).getSprintItem().getStoryPoint()
					: 0L;
			final CSprintPlanningSprintMetrics current = metricsBySprintId.getOrDefault(sprint.getId(), new CSprintPlanningSprintMetrics(0, 0));
			metricsBySprintId.put(sprint.getId(), new CSprintPlanningSprintMetrics(current.itemCount() + 1, current.storyPoints() + points));
		}
		sprintMetricsById = Map.copyOf(metricsBySprintId);
		gridSprints.setSprintMetrics(sprintMetricsById);

		final List<CGnntItem> rootItems = new ArrayList<>();
		final Map<String, List<CGnntItem>> childrenByParentKey = new HashMap<>();
		final List<CGnntItem> flatItems = new ArrayList<>();
		long uniqueId = 10_000;

		for (final CSprint sprint : sprints) {
			if (!filterToolbar.shouldIncludeSprint(sprint)) {
				continue;
			}
			if (scope == ESprintPlanningScope.SPRINT && selectedSprint != null && sprint.getId() != null
					&& !sprint.getId().equals(selectedSprint.getId())) {
				continue;
			}
			final CGnntItem sprintItem = new CGnntItem(sprint, uniqueId++, 0);
			rootItems.add(sprintItem);
			flatItems.add(sprintItem);

			final List<CGnntItem> sprintChildren = new ArrayList<>();
			for (final CProjectItem<?> entity : entitiesByKey.values()) {
				final CSprint itemSprint = ((ISprintableItem) entity).getSprintItem() != null ? ((ISprintableItem) entity).getSprintItem().getSprint() : null;
				if (itemSprint == null || itemSprint.getId() == null || sprint.getId() == null) {
					continue;
				}
				if (!itemSprint.getId().equals(sprint.getId())) {
					continue;
				}
				if (!filterToolbar.shouldIncludeItem(entity)) {
					continue;
				}
				final CGnntItem child = new CGnntItem(entity, uniqueId++, 1);
				sprintChildren.add(child);
				flatItems.add(child);
			}
			// Jira-like ordering: keep sprint items ranked by CSprintItem.itemOrder.
			sprintChildren.sort(Comparator.<CGnntItem>comparingInt(item -> resolveItemOrder(item.getEntity()))
					.thenComparing(CGnntItem::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

			sprintItem.setHasChildren(!sprintChildren.isEmpty());
			childrenByParentKey.put(sprintItem.getEntityKey(), sprintChildren);
		}

		updateSelectedSprintMetrics();
		return new CGnntHierarchyResult(rootItems, childrenByParentKey, flatItems);
	}

	private CGanttTimelineRange resolveTimelineRange(final List<CGnntItem> backlogItems, final List<CGnntItem> sprintItems) {
		LocalDate min = null;
		LocalDate max = null;
		for (final CGnntItem item : backlogItems) {
			min = minDate(min, item.getStartDate());
			max = maxDate(max, item.getEndDate());
		}
		for (final CGnntItem item : sprintItems) {
			min = minDate(min, item.getStartDate());
			max = maxDate(max, item.getEndDate());
		}
		if (min == null) {
			min = LocalDate.now().minusDays(7);
		}
		if (max == null) {
			max = LocalDate.now().plusDays(14);
		}
		return new CGanttTimelineRange(min, max);
	}

	private LocalDate minDate(final LocalDate current, final LocalDate candidate) {
		if (candidate == null) {
			return current;
		}
		return current == null || candidate.isBefore(current) ? candidate : current;
	}

	private LocalDate maxDate(final LocalDate current, final LocalDate candidate) {
		if (candidate == null) {
			return current;
		}
		return current == null || candidate.isAfter(current) ? candidate : current;
	}
}
