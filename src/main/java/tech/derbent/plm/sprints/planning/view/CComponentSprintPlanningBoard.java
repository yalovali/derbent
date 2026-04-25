package tech.derbent.plm.sprints.planning.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.parentrelation.service.CParentRelationService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.plm.agile.view.CProjectHierarchyDialogSupport;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;
import tech.derbent.plm.sprints.planning.domain.CSprintPlanningViewEntity;
import tech.derbent.plm.sprints.planning.domain.ESprintPlanningScope;
import tech.derbent.plm.sprints.planning.view.components.CDialogAddBacklogItemToSprint;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningBacklogBrowser;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningDragContext;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningDropRequest;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningFilterToolbar;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningSprintMetrics;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningTreeGrid;
import tech.derbent.plm.sprints.service.CSprintItemService;
import tech.derbent.plm.sprints.service.CSprintService;

/** Sprint Planning Board (v2) - timeline + drag/drop planning without changing existing sprint pages.
 * <p>
 * Design goals:
 * <ul>
 * <li>Backlog is split: left parent browser tree (levels 0..n) + right leaf-only flat grid for sprint assignment.</li>
 * <li>Sprints are shown as a Gnnt-style tree (Sprint → Items) with timeline.</li>
 * <li>Cross-grid drag/drop is supported via a shared {@link CSprintPlanningDragContext}.</li>
 * <li>Only leaf items (level -1) can be assigned to sprints (drag/drop and dialog).</li>
 * </ul>
 * Existing sprint pages remain the source of truth for sprint definitions; this board focuses on fast assignment and review.
 * </p>
 */
public class CComponentSprintPlanningBoard extends CComponentBase<CSprintPlanningViewEntity> {

	private static final class CVisibleNode {

		private final List<CVisibleNode> children;
		private final CGnntItem item;

		private CVisibleNode(final CGnntItem item, final List<CVisibleNode> children) {
			this.item = item;
			this.children = children;
		}
	}

	private record CBacklogData(CGnntHierarchyResult parentHierarchy, CGnntHierarchyResult leafHierarchy) {}

	private record CBacklogBuildResult(CVisibleNode node, boolean hasVisibleLeafDescendant) {}

	private static final double DEFAULT_SPLITTER_POSITION = 60.0;
	public static final String ID_BOARD = "custom-sprint-planning-board-v2";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSprintPlanningBoard.class);
	private static final long serialVersionUID = 1L;

	private static int resolveItemOrder(final CProjectItem<?> item) {
		return resolveItemOrder((Object) item);
	}

	private static int resolveItemOrder(final Object entity) {
		if (!(entity instanceof ISprintableItem)) {
			return entity instanceof CProjectItem<?> ? CHierarchyNavigationService.getEntityLevel((CProjectItem<?>) entity) : Integer.MAX_VALUE;
		}
		final ISprintableItem sprintableItem = (ISprintableItem) entity;
		final Integer order = sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getItemOrder() : null;
		return order != null ? order : Integer.MAX_VALUE;
	}

	private final CSprintPlanningBacklogBrowser backlogBrowser;
	private final CComponentItemDetails componentItemDetails;
	private boolean detailsVisible = false;
	private final CSprintPlanningDragContext dragContext = new CSprintPlanningDragContext();
	private final CSprintPlanningFilterToolbar filterToolbar;
	private final CSprintPlanningTreeGrid gridSprints;
	private final CProjectHierarchyDialogSupport hierarchyDialogSupport;
	private final CHierarchyNavigationService hierarchyNavigationService;
	private Map<String, CProjectItem<?>> lastHierarchyItemsByKey = Map.of();
	private final CVerticalLayout layoutGrids;
	private double previousSplitterPosition = DEFAULT_SPLITTER_POSITION;
	private CEntityNamed<?> selectedDetailsEntity;
	private CGnntItem selectedItem;
	private CSprint selectedSprintForMetrics;
	private SplitLayout splitLayout;
	private final CSprintItemService sprintItemService;
	// Sprint header metrics (items + story points) for Jira-like sprint rows.
	private Map<Long, CSprintPlanningSprintMetrics> sprintMetricsById = Map.of();
	private final CSprintService sprintService;

	public CComponentSprintPlanningBoard(final ISessionService sessionService) {
		try {
			componentItemDetails = new CComponentItemDetails(sessionService);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to initialize sprint planning details component", e);
		}
		sprintService = CSpringContext.getBean(CSprintService.class);
		sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		hierarchyNavigationService = CSpringContext.getBean(CHierarchyNavigationService.class);
		final CParentRelationService parentRelationService = CSpringContext.getBean(CParentRelationService.class);
		hierarchyDialogSupport = new CProjectHierarchyDialogSupport(parentRelationService, hierarchyNavigationService, sessionService);
		filterToolbar = new CSprintPlanningFilterToolbar();
		filterToolbar.addChangeListener(event -> refreshComponent());
		filterToolbar.setAddToSprintHandler(this::openAddToSprintDialog);
		backlogBrowser = new CSprintPlanningBacklogBrowser(dragContext, this::onItemSelected, this::onBacklogDrop,
				filterToolbar.getBacklogParentBrowserFilterComponents());
		gridSprints = new CSprintPlanningTreeGrid(CSprintPlanningTreeGrid.ID_TREE_GRID, dragContext, this::onItemSelected, this::onSprintDrop);
		layoutGrids = new CVerticalLayout();
		layoutGrids.setPadding(false);
		layoutGrids.setSpacing(false);
		layoutGrids.setWidthFull();
		layoutGrids.setHeightFull();
		initializeLayout();
	}

	private CBacklogData buildBacklogData(final Map<String, CProjectItem<?>> hierarchyItemsByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		if (scope == ESprintPlanningScope.SPRINT) {
			// In sprint scope we keep backlog empty to focus on the sprint tree.
			final CGnntHierarchyResult empty = new CGnntHierarchyResult(List.of(), Map.of(), List.of());
			return new CBacklogData(empty, empty);
		}
		final Map<String, List<CProjectItem<?>>> childrenByParentKey = new HashMap<>();
		final List<CProjectItem<?>> roots = new ArrayList<>();
		for (final CProjectItem<?> item : hierarchyItemsByKey.values()) {
			final String key = CHierarchyNavigationService.buildEntityKey(item);
			if (key == null) {
				continue;
			}
			final String parentKey = CHierarchyNavigationService.buildParentKey(item);
			if (parentKey == null || !hierarchyItemsByKey.containsKey(parentKey)) {
				roots.add(item);
			} else {
				childrenByParentKey.computeIfAbsent(parentKey, ignored -> new ArrayList<>()).add(item);
			}
		}
		// Jira-like ordering: backlog is primarily rank-ordered.
		roots.sort(Comparator.comparingInt((final CProjectItem<?> item) -> resolveItemOrder(item)).thenComparing(CProjectItem::getName,
				Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
		for (final List<CProjectItem<?>> children : childrenByParentKey.values()) {
			children.sort(Comparator.comparingInt((final CProjectItem<?> item) -> resolveItemOrder(item)).thenComparing(CProjectItem::getName,
					Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
		}
		final List<CVisibleNode> visibleParentRoots = new ArrayList<>();
		final List<CGnntItem> visibleLeafItems = new ArrayList<>();
		final long[] uniqueId = new long[] {
				1
		};
		for (final CProjectItem<?> root : roots) {
			final CBacklogBuildResult result = buildVisibleBacklogParentNode(root, 0, scope, childrenByParentKey, visibleLeafItems, uniqueId);
			if (result.node() != null) {
				visibleParentRoots.add(result.node());
			}
		}
		visibleLeafItems.sort(Comparator.<CGnntItem>comparingInt(item -> resolveItemOrder(item.getEntity())).thenComparing(CGnntItem::getName,
				Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
		final CGnntHierarchyResult parentHierarchy = flattenVisibleNodes(visibleParentRoots);
		final CGnntHierarchyResult leafHierarchy = new CGnntHierarchyResult(visibleLeafItems, Map.of(), visibleLeafItems);
		return new CBacklogData(parentHierarchy, leafHierarchy);
	}

	private List<CContextActionDefinition<CGnntItem>> buildLeafQuickActions() {
		return List.of(
				CContextActionDefinition.of("add-to-sprint", "Add to sprint", VaadinIcon.PLUS, context -> true, this::canAddLeafToSprint,
						this::openAddToSprintDialog),
				CContextActionDefinition.of("new-leaf-item", "New backlog item", VaadinIcon.PLUS_CIRCLE_O, context -> true, this::canCreateLeafItem,
						this::openCreateLeafItemDialog),
				CContextActionDefinition.of("edit-leaf-item", "Edit item", VaadinIcon.EDIT, context -> context != null, context -> context != null,
						context -> openProjectItemEditDialog(context != null ? context.getEntity() : null)));
	}

	private List<CContextActionDefinition<CGnntItem>> buildParentQuickActions() {
		return List.of(
				CContextActionDefinition.of("new-parent-item", "New backlog item", VaadinIcon.PLUS_CIRCLE_O, context -> true,
						this::canCreateParentItem, context -> openCreateParentItemDialog(context)),
				CContextActionDefinition.of("add-existing-child", "Add existing", VaadinIcon.LIST_SELECT, context -> context != null,
						this::canAddExistingChild, this::openAddExistingChildDialog),
				CContextActionDefinition.of("edit-parent-item", "Edit item", VaadinIcon.EDIT, context -> context != null, context -> context != null,
						context -> openProjectItemEditDialog(context != null ? context.getEntity() : null)));
	}

	private List<CContextActionDefinition<CGnntItem>> buildSprintContextActions() {
		return List.of(
				CContextActionDefinition.of("move-to-backlog", "Move to backlog", VaadinIcon.ARROW_BACKWARD,
						context -> context != null && context.getEntity() instanceof ISprintableItem, this::canMoveSprintItemToBacklog,
						this::moveSprintItemToBacklog),
				CContextActionDefinition.of("edit-sprint-item", "Edit item", VaadinIcon.EDIT,
						context -> context != null && context.getEntity() instanceof CProjectItem<?>, context -> context != null,
						context -> openProjectItemEditDialog(context != null ? context.getEntity() : null)),
				CContextActionDefinition.of("edit-sprint", "Edit sprint", VaadinIcon.CALENDAR,
						context -> context != null && context.getEntity() instanceof CSprint, context -> context != null,
						context -> openEditSprintDialog()));
	}

	private CGnntHierarchyResult buildSprintHierarchy(final CSprintPlanningViewEntity view, final Map<String, CProjectItem<?>> entitiesByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		final CSprint selectedSprint = filterToolbar.getSelectedSprint();
		final List<CSprint> sprints = new ArrayList<>(sprintService.listByProject(view.getProject()));
		sprints.sort(Comparator.comparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo)).thenComparing(CSprint::getName,
				String.CASE_INSENSITIVE_ORDER));
		// Metrics are computed on full sprint membership (not search-filtered) so the widget stays stable.
		final Map<Long, CSprintPlanningSprintMetrics> metricsBySprintId = new HashMap<>();
		for (final CProjectItem<?> entity : entitiesByKey.values()) {
			final CSprint sprint = ((ISprintableItem) entity).getSprintItem() != null ? ((ISprintableItem) entity).getSprintItem().getSprint() : null;
			if (sprint == null || sprint.getId() == null) {
				continue;
			}
			final long points =
					((ISprintableItem) entity).getSprintItem() != null && ((ISprintableItem) entity).getSprintItem().getStoryPoint() != null
							? ((ISprintableItem) entity).getSprintItem().getStoryPoint() : 0L;
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
				final CSprint itemSprint =
						((ISprintableItem) entity).getSprintItem() != null ? ((ISprintableItem) entity).getSprintItem().getSprint() : null;
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
			sprintChildren.sort(Comparator.<CGnntItem>comparingInt(item -> resolveItemOrder(item.getEntity())).thenComparing(CGnntItem::getName,
					Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
			sprintItem.setHasChildren(!sprintChildren.isEmpty());
			childrenByParentKey.put(sprintItem.getEntityKey(), sprintChildren);
		}
		updateSelectedSprintMetrics();
		return new CGnntHierarchyResult(rootItems, childrenByParentKey, flatItems);
	}

	private CBacklogBuildResult buildVisibleBacklogParentNode(final CProjectItem<?> entity, final int hierarchyLevel,
			final ESprintPlanningScope scope, final Map<String, List<CProjectItem<?>>> childrenByParentKey, final List<CGnntItem> visibleLeafItems,
			final long[] uniqueId) {
		final String entityKey = CHierarchyNavigationService.buildEntityKey(entity);
		if (entityKey == null) {
			return new CBacklogBuildResult(null, false);
		}
		if (isLeafItem(entity)) {
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
			final CBacklogBuildResult visibleChild =
					buildVisibleBacklogParentNode(child, hierarchyLevel + 1, scope, childrenByParentKey, visibleLeafItems, uniqueId);
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

	private boolean canAddExistingChild(final CGnntItem context) {
		return resolveParentContextEntity(context) != null
				&& hierarchyDialogSupport.hasSelectableExistingChildren(resolveParentContextEntity(context), null);
	}

	private boolean canAddLeafToSprint(final CGnntItem context) {
		if (context == null || !(context.getEntity() instanceof ISprintableItem) || !(context.getEntity() instanceof CProjectItem<?>)) {
			return false;
		}
		return isLeafItem(context.getEntity());
	}

	private boolean canCreateLeafItem(final CGnntItem context) {
		final CProjectItem<?> parentContext = resolveLeafCreationParent(context);
		return parentContext != null && hierarchyDialogSupport.hasCreatableItems(parentContext.getProject(), parentContext,
				entityClass -> !CHierarchyNavigationService.canHaveChildren(createPreviewItem(entityClass, parentContext.getProject())));
	}

	private boolean canCreateParentItem(final CGnntItem context) {
		final CProjectItem<?> parentContext = resolveParentContextEntity(context);
		final CSprintPlanningViewEntity view = getValue();
		return view != null && view.getProject() != null
				&& hierarchyDialogSupport.hasCreatableItems(view.getProject(), parentContext, entityClass -> parentContext != null
						|| CHierarchyNavigationService.canHaveChildren(createPreviewItem(entityClass, view.getProject())));
	}

	private boolean canMoveSprintItemToBacklog(final CGnntItem context) {
		return context != null && context.getEntity() instanceof ISprintableItem;
	}

	private CProjectItem<?> createPreviewItem(final Class<? extends CProjectItem<?>> entityClass, final CProject<?> project) {
		try {
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			final Object serviceBean = serviceClass != null ? CSpringContext.getBean(serviceClass) : null;
			if (!(serviceBean instanceof CEntityOfProjectService<?>)) {
				return null;
			}
			final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) serviceBean;
			final Object previewEntity = projectService.newEntity("Preview " + entityClass.getSimpleName(), project);
			return previewEntity instanceof CProjectItem<?> ? (CProjectItem<?>) previewEntity : null;
		} catch (final Exception e) {
			LOGGER.debug("Could not create preview item for {} reason={}", entityClass.getSimpleName(), e.getMessage());
			return null;
		}
	}

	private Map<String, CProjectItem<?>> filterSprintableItems(final Map<String, CProjectItem<?>> hierarchyItemsByKey) {
		final Map<String, CProjectItem<?>> sprintableItemsByKey = new HashMap<>();
		for (final Map.Entry<String, CProjectItem<?>> entry : hierarchyItemsByKey.entrySet()) {
			if (entry.getValue() instanceof ISprintableItem) {
				sprintableItemsByKey.put(entry.getKey(), entry.getValue());
			}
		}
		return sprintableItemsByKey;
	}

	private void flattenVisibleNode(final CVisibleNode node, final List<CGnntItem> flatItems,
			final Map<String, List<CGnntItem>> childrenByParentKey) {
		flatItems.add(node.item);
		if (node.children == null || node.children.isEmpty()) {
			return;
		}
		final String key = node.item.getEntityKey();
		final List<CGnntItem> childItems = new ArrayList<>();
		node.children.forEach((final CVisibleNode child) -> {
			childItems.add(child.item);
			flattenVisibleNode(child, flatItems, childrenByParentKey);
		});
		childrenByParentKey.put(key, childItems);
	}

	private CGnntHierarchyResult flattenVisibleNodes(final List<CVisibleNode> rootNodes) {
		final List<CGnntItem> flatItems = new ArrayList<>();
		final List<CGnntItem> rootItems = new ArrayList<>();
		final Map<String, List<CGnntItem>> childrenByParentKey = new HashMap<>();
		rootNodes.forEach((final CVisibleNode rootNode) -> {
			rootItems.add(rootNode.item);
			flattenVisibleNode(rootNode, flatItems, childrenByParentKey);
		});
		return new CGnntHierarchyResult(rootItems, childrenByParentKey, flatItems);
	}

	private CGnntItem getSelectedLeafActionContext() { return backlogBrowser.getSelectedLeafItem(); }

	private CGnntItem getSelectedParentActionContext() { return backlogBrowser.getSelectedParentItem(); }

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
		final List<Component> quickControls = filterToolbar.extractQuickControlsForQuickAccess();
		gridSprints.getQuickAccessPanel().setOnToggleDetails(this::toggleDetailsPanel);
		gridSprints.getQuickAccessPanel().setOnRefresh(this::refreshComponent);
		gridSprints.getQuickAccessPanel().setDetailsVisible(detailsVisible);
		gridSprints.getQuickAccessPanel().addControls(quickControls);
		backlogBrowser.getParentQuickAccessPanel().setOnRefresh(this::refreshComponent);
		backlogBrowser.getParentQuickAccessPanel().setContextActions(buildParentQuickActions(), this::getSelectedParentActionContext);
		backlogBrowser.getLeafQuickAccessPanel().setOnRefresh(this::refreshComponent);
		backlogBrowser.getLeafQuickAccessPanel().setContextActions(buildLeafQuickActions(), this::getSelectedLeafActionContext);
		backlogBrowser.setParentContextActions(buildParentQuickActions());
		backlogBrowser.setLeafContextActions(buildLeafQuickActions());
		backlogBrowser.getParentQuickAccessPanel().setShowRefreshButton(true);
		backlogBrowser.getLeafQuickAccessPanel().setShowRefreshButton(true);
		gridSprints.setContextActions(buildSprintContextActions());
		gridSprints.getQuickAccessPanel().setContextActions(buildSprintContextActions(), gridSprints::getSelectedItem);
		layoutGrids.add(filterToolbar, gridsSplit);
		layoutGrids.setFlexGrow(0, filterToolbar);
		layoutGrids.setFlexGrow(1, gridsSplit);
		splitLayout = new SplitLayout(layoutGrids, componentItemDetails);
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.setSplitterPosition(detailsVisible ? DEFAULT_SPLITTER_POSITION : 100.0);
		componentItemDetails.setVisible(detailsVisible);
		splitLayout.setWidthFull();
		splitLayout.setHeightFull();
		add(splitLayout);
	}

	private boolean isBacklogCandidate(final CProjectItem<?> entity, final ESprintPlanningScope scope) {
		if (scope == ESprintPlanningScope.ALL_ITEMS) {
			return true;
		}
		if (!(entity instanceof ISprintableItem)) {
			return true;
		}
		final ISprintableItem sprintableItem = (ISprintableItem) entity;
		final CSprint sprint = sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
		return sprint == null;
	}

	private boolean isClosedSprint(final CSprint sprint) {
		return sprint != null && sprint.getStatus() != null && Boolean.TRUE.equals(sprint.getStatus().getFinalStatus());
	}

	private boolean isLeafItem(final CProjectItem<?> entity) {
		return entity != null && !CHierarchyNavigationService.canHaveChildren(entity);
	}

	private Map<String, CProjectItem<?>> loadHierarchyItems(final CSprintPlanningViewEntity view) {
		if (view == null || view.getProject() == null) {
			return Map.of();
		}
		final Map<String, CProjectItem<?>> itemsByKey = new HashMap<>();
		for (final CProjectItem<?> projectItem : hierarchyNavigationService.listHierarchyItems(view.getProject())) {
			final String entityKey = CHierarchyNavigationService.buildEntityKey(projectItem);
			if (entityKey != null) {
				itemsByKey.put(entityKey, projectItem);
			}
		}
		return itemsByKey;
	}

	private LocalDate maxDate(final LocalDate current, final LocalDate candidate) {
		if (candidate == null) {
			return current;
		}
		return current == null || candidate.isAfter(current) ? candidate : current;
	}

	private LocalDate minDate(final LocalDate current, final LocalDate candidate) {
		if (candidate == null) {
			return current;
		}
		return current == null || candidate.isBefore(current) ? candidate : current;
	}

	private void moveSprintItemToBacklog(final CGnntItem context) {
		try {
			if (context == null || !(context.getEntity() instanceof final ISprintableItem sprintableItem)) {
				CNotificationService.showWarning("Select a sprint item first");
				return;
			}
			final CSprintItem anchorItem = resolveBacklogAnchorItem();
			sprintableItem.moveSprintItemToBacklog(anchorItem, anchorItem != null);
			refreshComponent();
			CNotificationService.showSuccess("Moved '%s' to backlog".formatted(context.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to move sprint item to backlog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to move sprint item to backlog", e);
		}
	}

	private void onBacklogDrop(final CSprintPlanningDropRequest dropRequest) {
		try {
			final CGnntItem draggedItem = dropRequest != null ? dropRequest.draggedItem() : null;
			if (draggedItem == null || draggedItem.getEntity() == null) {
				return;
			}
			if (draggedItem.getEntity() instanceof CSprint) {
				return;
			}
			if (!(draggedItem.getEntity() instanceof ISprintableItem)) {
				CNotificationService.showWarning("Only sprintable items can be moved to backlog.");
				return;
			}
			final ISprintableItem sprintableItem = (ISprintableItem) draggedItem.getEntity();
			final ISprintableItem anchorSprintableItem =
					dropRequest != null && dropRequest.targetItem() != null && dropRequest.targetItem().getEntity() instanceof ISprintableItem
							? (ISprintableItem) dropRequest.targetItem().getEntity() : null;
			final CSprintItem anchorItem = anchorSprintableItem != null ? anchorSprintableItem.getSprintItem() : null;
			final boolean insertAfter = dropRequest != null && dropRequest.dropLocation() != null && switch (dropRequest.dropLocation()) {
			case BELOW -> true;
			default -> false;
			};
			sprintableItem.moveSprintItemToBacklog(anchorItem, insertAfter);
			refreshComponent();
			CNotificationService.showSuccess("Moved '%s' to backlog".formatted(draggedItem.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to move item to backlog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to move item to backlog", e);
		}
	}

	private void onItemSelected(final CGnntItem item) {
		selectedItem = item;
		selectedDetailsEntity = item != null ? item.getEntity() : null;
		updateSelectedSprintFromSelection(item);
		backlogBrowser.getParentQuickAccessPanel().refreshContextActionStates();
		backlogBrowser.getLeafQuickAccessPanel().refreshContextActionStates();
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

	private void onSprintDrop(final CSprintPlanningDropRequest dropRequest) {
		try {
			final CGnntItem draggedItem = dropRequest != null ? dropRequest.draggedItem() : null;
			final CGnntItem dropTarget = dropRequest != null ? dropRequest.targetItem() : null;
			if (draggedItem == null || draggedItem.getEntity() == null) {
				return;
			}
			if (!(draggedItem.getEntity() instanceof ISprintableItem)) {
				CNotificationService.showWarning("Only sprintable items can be assigned to sprints.");
				return;
			}
			final ISprintableItem sprintableItem = (ISprintableItem) draggedItem.getEntity();
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
			final CSprintItem anchorItem = resolveSprintDropAnchorItem(dropRequest, targetSprint);
			final boolean insertAfter = shouldInsertAfter(dropRequest, dropTarget);
			sprintableItem.moveSprintItemToSprint(targetSprint, anchorItem, insertAfter);
			refreshComponent();
			CNotificationService.showSuccess("Assigned '%s' to sprint '%s'".formatted(draggedItem.getName(), targetSprint.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to assign item to sprint: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to assign item to sprint", e);
		}
	}

	@Override
	protected void onValueChanged(final CSprintPlanningViewEntity oldValue, final CSprintPlanningViewEntity newValue, final boolean fromClient) {
		LOGGER.debug("Sprint planning board changed from {} to {}", oldValue != null ? oldValue.getName() : "null",
				newValue != null ? newValue.getName() : "null");
		refreshComponent();
	}

	private void openAddExistingChildDialog(final CGnntItem context) {
		try {
			final CProjectItem<?> parentContext = resolveParentContextEntity(context);
			if (parentContext == null) {
				CNotificationService.showWarning("Select a backlog parent first");
				return;
			}
			hierarchyDialogSupport.openAddExistingDialog("Add Existing Child", parentContext, null, this::refreshComponent);
		} catch (final Exception e) {
			LOGGER.error("Failed to open add-existing child dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to add existing child", e);
		}
	}

	private void openAddToSprintDialog() {
		openAddToSprintDialog(backlogBrowser.getSelectedLeafItem());
	}

	private void openAddToSprintDialog(final CGnntItem effectiveSelection) {
		try {
			final CSprintPlanningViewEntity view = getValue();
			if (view == null || view.getProject() == null) {
				CNotificationService.showWarning("Select a project first");
				return;
			}
			final CGnntItem itemToAssign = effectiveSelection != null ? effectiveSelection : selectedItem;
			if (itemToAssign == null || itemToAssign.getEntity() == null) {
				CNotificationService.showWarning("Select an item in backlog first");
				return;
			}
			if (itemToAssign.getEntity() instanceof CSprint) {
				CNotificationService.showWarning("Select a backlog item (not a sprint)");
				return;
			}
			if (!(itemToAssign.getEntity() instanceof ISprintableItem)) {
				CNotificationService.showWarning("Only sprintable items can be assigned to a sprint");
				return;
			}
			final ISprintableItem sprintableItem = (ISprintableItem) itemToAssign.getEntity();
			if (!validateLeafOnly(itemToAssign.getEntity(), itemToAssign.getName())) {
				return;
			}
			final List<CSprint> availableSprints = new ArrayList<>(sprintService.listByProject(view.getProject()));
			availableSprints.sort(Comparator.comparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo))
					.thenComparing(CSprint::getName, String.CASE_INSENSITIVE_ORDER));
			final CDialogAddBacklogItemToSprint dialog = new CDialogAddBacklogItemToSprint(itemToAssign.getName(), availableSprints, sprint -> {
				if (isClosedSprint(sprint)) {
					CNotificationService.showWarning("Cannot add items to a closed sprint.");
					return;
				}
				final CSprintItem anchorItem = resolveSelectedSprintAnchorItem(sprint);
				final boolean insertAfter = anchorItem != null;
				sprintableItem.moveSprintItemToSprint(sprint, anchorItem, insertAfter);
				refreshComponent();
			});
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open add-to-sprint dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to open add-to-sprint dialog", e);
		}
	}

	private void openCreateLeafItemDialog(final CGnntItem context) {
		final CProjectItem<?> parentContext = resolveLeafCreationParent(context);
		if (parentContext == null) {
			CNotificationService.showWarning("Select a backlog parent that can own leaf items first");
			return;
		}
		hierarchyDialogSupport.openCreateDialog(parentContext.getProject(), parentContext,
				entityClass -> !CHierarchyNavigationService.canHaveChildren(createPreviewItem(entityClass, parentContext.getProject())),
				this::refreshComponent);
	}

	private void openCreateParentItemDialog(final CGnntItem context) {
		final CSprintPlanningViewEntity view = getValue();
		final CProjectItem<?> parentContext = resolveParentContextEntity(context);
		if (view == null || view.getProject() == null) {
			CNotificationService.showWarning("Select a project first");
			return;
		}
		hierarchyDialogSupport
				.openCreateDialog(view.getProject(), parentContext,
						entityClass -> parentContext != null
								|| CHierarchyNavigationService.canHaveChildren(createPreviewItem(entityClass, view.getProject())),
						this::refreshComponent);
	}

	private void openEditSprintDialog() {
		try {
			final CSprint sprint = filterToolbar.getSelectedSprint();
			if (sprint == null) {
				CNotificationService.showWarning("Select a sprint first");
				return;
			}
			hierarchyDialogSupport.openEditDialog(sprint, this::refreshComponent);
		} catch (final Exception e) {
			LOGGER.error("Failed to open sprint edit dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to open sprint edit dialog", e);
		}
	}

	private void openProjectItemEditDialog(final Object entity) {
		try {
			if (!(entity instanceof CProjectItem<?>)) {
				return;
			}
			final CProjectItem<?> projectItem = (CProjectItem<?>) entity;
			hierarchyDialogSupport.openEditDialog(projectItem, this::refreshComponent);
		} catch (final Exception e) {
			LOGGER.error("Failed to open project item edit dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to open item editor", e);
		}
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
				lastHierarchyItemsByKey = Map.of();
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
			final Map<String, CProjectItem<?>> hierarchyItemsByKey = loadHierarchyItems(view);
			final Map<String, CProjectItem<?>> sprintableItemsByKey = filterSprintableItems(hierarchyItemsByKey);
			lastHierarchyItemsByKey = hierarchyItemsByKey;
			final List<CGnntItem> allItems = new ArrayList<>();
			long entityTypeSequence = 1;
			for (final CProjectItem<?> projectItem : hierarchyItemsByKey.values()) {
				allItems.add(new CGnntItem(projectItem, entityTypeSequence++, 0));
			}
			filterToolbar.setAvailableEntityTypes(allItems);
			final CBacklogData backlogData = buildBacklogData(hierarchyItemsByKey);
			final CGnntHierarchyResult sprints = buildSprintHierarchy(view, sprintableItemsByKey);
			updateBacklogMetrics(hierarchyItemsByKey);
			final CGanttTimelineRange range = resolveTimelineRange(backlogData.leafHierarchy().getFlatItems(), sprints.getFlatItems());
			backlogBrowser.setBacklogData(backlogData.parentHierarchy(), backlogData.leafHierarchy(), hierarchyItemsByKey, range);
			gridSprints.setHierarchy(sprints, range);
			backlogBrowser.getParentQuickAccessPanel().refreshContextActionStates();
			backlogBrowser.getLeafQuickAccessPanel().refreshContextActionStates();
		} catch (final Exception e) {
			LOGGER.error("Failed to refresh sprint planning board: {}", e.getMessage(), e);
			throw e;
		}
	}

	private CSprintItem resolveBacklogAnchorItem() {
		final CGnntItem selectedLeafItem = backlogBrowser.getSelectedLeafItem();
		if (selectedLeafItem == null || !(selectedLeafItem.getEntity() instanceof final ISprintableItem sprintableItem)) {
			return null;
		}
		return sprintableItem.getSprintItem();
	}

	private CProjectItem<?> resolveLeafCreationParent(final CGnntItem context) {
		if (context != null && context.getEntity() instanceof CProjectItem<?>) {
			final CProjectItem<?> projectItem = context.getEntity();
			if (CHierarchyNavigationService.canHaveChildren(projectItem)) {
				return projectItem;
			}
			return resolveParentItem(projectItem);
		}
		final CProjectItem<?> selectedParent = resolveParentContextEntity(backlogBrowser.getSelectedParentItem());
		if (selectedParent != null && CHierarchyNavigationService.canHaveChildren(selectedParent)) {
			return selectedParent;
		}
		return null;
	}

	private CProjectItem<?> resolveParentContextEntity(final CGnntItem context) {
		if (context == null) {
			return null;
		}
		return context.getEntity() instanceof CProjectItem<?> ? (CProjectItem<?>) context.getEntity() : null;
	}

	private CProjectItem<?> resolveParentItem(final CProjectItem<?> entity) {
		final String parentKey = CHierarchyNavigationService.buildParentKey(entity);
		if (parentKey == null) {
			return null;
		}
		return lastHierarchyItemsByKey.get(parentKey);
	}

	private CSprintItem resolveSelectedSprintAnchorItem(final CSprint targetSprint) {
		if (selectedItem == null || targetSprint == null || !(selectedItem.getEntity() instanceof ISprintableItem)) {
			return null;
		}
		final ISprintableItem sprintableItem = (ISprintableItem) selectedItem.getEntity();
		final CSprint selectedSprint = sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
		return selectedSprint != null && selectedSprint.getId() != null && selectedSprint.getId().equals(targetSprint.getId())
				? sprintableItem.getSprintItem() : null;
	}

	private CSprintItem resolveSprintDropAnchorItem(final CSprintPlanningDropRequest dropRequest, final CSprint targetSprint) {
		final CGnntItem dropTarget = dropRequest != null ? dropRequest.targetItem() : null;
		if (dropTarget == null || dropTarget.getEntity() == null || targetSprint == null || targetSprint.getId() == null) {
			return null;
		}
		if (dropTarget.getEntity() instanceof final ISprintableItem sprintableItem) {
			return sprintableItem.getSprintItem();
		}
		if (!(dropTarget.getEntity() instanceof CSprint)) {
			return null;
		}
		final List<CSprintItem> sprintItems = sprintItemService.findByMasterId(targetSprint.getId());
		if (sprintItems.isEmpty()) {
			return null;
		}
		if (dropRequest != null && dropRequest.dropLocation() == com.vaadin.flow.component.grid.dnd.GridDropLocation.ABOVE) {
			return sprintItems.get(0);
		}
		return sprintItems.get(sprintItems.size() - 1);
	}

	private CSprint resolveTargetSprint(final CGnntItem dropTarget) {
		if (dropTarget == null || dropTarget.getEntity() == null) {
			return filterToolbar.getSelectedSprint();
		}
		if (dropTarget.getEntity() instanceof CSprint) {
			return (CSprint) dropTarget.getEntity();
		}
		if (!(dropTarget.getEntity() instanceof ISprintableItem)) {
			return null;
		}
		final ISprintableItem sprintableItem = (ISprintableItem) dropTarget.getEntity();
		return sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
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

	private boolean shouldInsertAfter(final CSprintPlanningDropRequest dropRequest, final CGnntItem dropTarget) {
		if (dropRequest == null || dropRequest.dropLocation() == null) {
			return false;
		}
		return switch (dropRequest.dropLocation()) {
		case BELOW -> true;
		case ON_TOP -> dropTarget == null || !(dropTarget.getEntity() instanceof CSprint);
		default -> false;
		};
	}

	private void toggleDetailsPanel() {
		if (splitLayout == null) {
			return;
		}
		detailsVisible = !detailsVisible;
		// Keep the header quick-access toggle in sync with the split-layout state.
		gridSprints.getQuickAccessPanel().setDetailsVisible(detailsVisible);
		if (detailsVisible) {
			componentItemDetails.setVisible(true);
			splitLayout.setSplitterPosition(previousSplitterPosition);
			if (selectedDetailsEntity == null) {
				componentItemDetails.clear();
			} else {
				componentItemDetails.setValue(selectedDetailsEntity);
			}
		} else {
			previousSplitterPosition = splitLayout.getSplitterPosition();
			splitLayout.setSplitterPosition(100.0);
			componentItemDetails.setVisible(false);
		}
	}

	private void updateBacklogMetrics(final Map<String, CProjectItem<?>> hierarchyItemsByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		if (scope == ESprintPlanningScope.SPRINT) {
			filterToolbar.setBacklogMetrics(0, 0);
			return;
		}
		int itemCount = 0;
		long storyPoints = 0;
		for (final CProjectItem<?> entity : hierarchyItemsByKey.values()) {
			if (!(entity instanceof ISprintableItem)) {
				continue;
			}
			final ISprintableItem sprintableItem = (ISprintableItem) entity;
			final CSprint sprint = sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
			final boolean backlogCandidate = scope == ESprintPlanningScope.ALL_ITEMS || sprint == null;
			if (!backlogCandidate || !filterToolbar.shouldIncludeBacklogItem(entity)) {
				continue;
			}
			// Metrics should be leaf-focused, matching the leaf-only sprint assignment rule.
			if (CHierarchyNavigationService.getEntityLevel(entity) != -1) {
				continue;
			}
			itemCount++;
			final Long points = sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getStoryPoint() : null;
			storyPoints += points != null ? points : 0L;
		}
		filterToolbar.setBacklogMetrics(itemCount, storyPoints);
	}

	private void updateSelectedSprintFromSelection(final CGnntItem item) {
		final Object entity = item != null ? item.getEntity() : null;
		if (entity instanceof CSprint) {
			selectedSprintForMetrics = (CSprint) entity;
		} else if (entity instanceof ISprintableItem) {
			final ISprintableItem sprintableItem = (ISprintableItem) entity;
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
		// Refresh the sprint entity before reading display fields so context-menu actions can safely reuse detached grid items.
		final CSprint managedSprint = sprintService.getById(sprint.getId()).orElse(null);
		if (managedSprint == null) {
			filterToolbar.setSelectedSprintMetrics(null, 0, 0);
			return;
		}
		final CSprintPlanningSprintMetrics metrics = sprintMetricsById.getOrDefault(managedSprint.getId(), new CSprintPlanningSprintMetrics(0, 0));
		filterToolbar.setSelectedSprintMetrics(managedSprint, metrics.itemCount(), metrics.storyPoints());
	}

	private boolean validateLeafOnly(final Object entity, final String displayName) {
		if (!(entity instanceof CProjectItem<?>)) {
			return true;
		}
		final CProjectItem<?> projectItem = (CProjectItem<?>) entity;
		if (isLeafItem(projectItem)) {
			return true;
		}
		CNotificationService.showWarning("Only leaf items can be added to a sprint: '%s'".formatted(displayName));
		return false;
	}
}
