package tech.derbent.plm.sprints.planning.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.ProxyUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
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
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.api.ui.dialogs.CDialogClone;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.agile.view.CProjectHierarchyDialogSupport;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.kanban.kanbanline.view.CDialogKanbanStatusSelection;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;
import tech.derbent.plm.sprints.planning.domain.CSprintPlanningViewEntity;
import tech.derbent.plm.sprints.planning.domain.ESprintPlanningScope;
import tech.derbent.plm.sprints.planning.view.components.CBacklogNavigatorHierarchyBuilder;
import tech.derbent.plm.sprints.planning.view.components.CDialogAddBacklogItemToSprint;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningBacklogBrowser;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningDragContext;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningDropRequest;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningFilterToolbar;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningSprintMetrics;
import tech.derbent.plm.sprints.planning.view.components.CSprintPlanningTreeGrid;
import tech.derbent.plm.sprints.service.CSprintItemService;
import tech.derbent.plm.sprints.service.CSprintItemWorkflowStatusSupport;
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

	private static final double DEFAULT_SPLITTER_POSITION = 60.0;
	public static final String ID_BOARD = "custom-sprint-planning-board-v2";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSprintPlanningBoard.class);
	private static final long serialVersionUID = 1L;

	private static int resolveItemOrder(final CProjectItem<?, ?> item) {
		return resolveItemOrder((Object) item);
	}

	private static int resolveItemOrder(final Object entity) {
		if (!(entity instanceof final ISprintableItem sprintableItem)) {
			return entity instanceof CProjectItem<?, ?>
					? CHierarchyNavigationService.getEntityLevel((CProjectItem<?, ?>) entity) : Integer.MAX_VALUE;
		}
		final Integer order =
				sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getItemOrder() : null;
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
	private Map<String, CProjectItem<?, ?>> lastHierarchyItemsByKey = Map.of();
	private final CVerticalLayout layoutGrids;
	private final CParentRelationService parentRelationService;
	private double previousSplitterPosition = DEFAULT_SPLITTER_POSITION;
	private final CProjectItemStatusService projectItemStatusService;
	private CEntityNamed<?> selectedDetailsEntity;
	private CGnntItem selectedItem;
	private CSprint selectedSprintForMetrics;
	private final ISessionService sessionService;
	private SplitLayout splitLayout;
	private final CSprintItemService sprintItemService;
	// Sprint header metrics (items + story points) for Jira-like sprint rows.
	private Map<Long, CSprintPlanningSprintMetrics> sprintMetricsById = Map.of();
	private final CSprintService sprintService;
	private final CUserService userService;

	public CComponentSprintPlanningBoard(final ISessionService sessionService) {
		this.sessionService = sessionService;
		try {
			componentItemDetails = new CComponentItemDetails(sessionService);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to initialize sprint planning details component", e);
		}
		sprintService = CSpringContext.getBean(CSprintService.class);
		sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		hierarchyNavigationService = CSpringContext.getBean(CHierarchyNavigationService.class);
		parentRelationService = CSpringContext.getBean(CParentRelationService.class);
		projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		userService = CSpringContext.getBean(CUserService.class);
		hierarchyDialogSupport =
				new CProjectHierarchyDialogSupport(parentRelationService, hierarchyNavigationService, sessionService);
		filterToolbar = new CSprintPlanningFilterToolbar();
		filterToolbar.addChangeListener(event -> refreshComponent());
		filterToolbar.setAddToSprintHandler(this::openAddToSprintDialog);
		backlogBrowser = new CSprintPlanningBacklogBrowser(dragContext, this::onItemSelected, this::onBacklogDrop,
				this::onBacklogParentDrop, filterToolbar.getBacklogParentBrowserFilterComponents());
		// Keep search filters scoped: parent search affects only parent browser, leaf search affects only leaf grid.
		filterToolbar.getBacklogLeafFilterComponents().forEach(component -> {
			if (component instanceof HasSize) {
				((HasSize) component).setWidth("200px");
			}
			backlogBrowser.getLeafQuickAccessPanel().addCustomComponent(component);
		});
		gridSprints = new CSprintPlanningTreeGrid(CSprintPlanningTreeGrid.ID_TREE_GRID, dragContext,
				this::onItemSelected, this::onSprintDrop);
		gridSprints.setItemDoubleClickHandler(item -> {
			final Object entity = item != null ? item.getEntity() : null;
			if (entity instanceof CSprint) {
				openEditSprintDialog((CSprint) entity);
				return;
			}
			openProjectItemEditDialog(entity);
		});
		backlogBrowser.setParentItemDoubleClickHandler(
				item -> openProjectItemEditDialog(item != null ? item.getEntity() : null));
		backlogBrowser.setLeafItemDoubleClickHandler(
				item -> openProjectItemEditDialog(item != null ? item.getEntity() : null));
		layoutGrids = new CVerticalLayout();
		layoutGrids.setPadding(false);
		layoutGrids.setSpacing(false);
		layoutGrids.setWidthFull();
		layoutGrids.setHeightFull();
		initializeLayout();
	}

	private void applyStatus(final CProjectItem<?, ?> item, final CProjectItemStatus status) {
		try {
			((IHasStatusAndWorkflow<?, ?>) item).setStatus(status);
			final CEntityDB<?> saved = saveEntity(item);
			refreshComponent();
			restoreSelectionAfterRefresh(saved);
			CNotificationService.showSuccess("Set status of '%s' to '%s'".formatted(item.getName(), status.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to set status: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to set status", e);
		}
	}

	private void assignToMe(final CGnntItem context) {
		final CProjectItem<?, ?> item = resolveProjectItemContext(context);
		if (item == null) {
			CNotificationService.showWarning("Select an item first");
			return;
		}
		// Allow assigning newly created (unsaved) items; saveEntity(...) will persist and apply validation.
		final CUser currentUser = sessionService != null ? sessionService.getActiveUser().orElse(null) : null;
		if (currentUser == null) {
			CNotificationService.showWarning("No active user in session");
			return;
		}
		try {
			item.setAssignedTo(currentUser);
			final CEntityDB<?> saved = saveEntity(item);
			refreshComponent();
			restoreSelectionAfterRefresh(saved);
			CNotificationService.showSuccess("Assigned '%s' to you".formatted(item.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to assign item to current user: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to assign item", e);
		}
	}

	private CBacklogNavigatorHierarchyBuilder.CBacklogData
			buildBacklogData(final Map<String, CProjectItem<?, ?>> hierarchyItemsByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		if (scope != ESprintPlanningScope.SPRINT) {
			return CBacklogNavigatorHierarchyBuilder.buildBacklogData(hierarchyItemsByKey, scope,
					filterToolbar::shouldIncludeBacklogParentItem, filterToolbar::shouldIncludeBacklogItem,
					CComponentSprintPlanningBoard::resolveItemOrder);
		}
		// In sprint scope we keep backlog empty to focus on the sprint tree.
		final CGnntHierarchyResult empty = new CGnntHierarchyResult(List.of(), Map.of(), List.of());
		return new CBacklogNavigatorHierarchyBuilder.CBacklogData(empty, empty);
	}

	private List<CContextActionDefinition<CGnntItem>> buildLeafQuickActions() {
		return List.of(
				CContextActionDefinition.of("add-to-sprint", "Add to sprint", VaadinIcon.PLUS, context -> true,
						this::canAddLeafToSprint, this::openAddToSprintDialog),
				CContextActionDefinition.of("new-leaf-item", "New", VaadinIcon.PLUS_CIRCLE_O, context -> true,
						this::canCreateLeafItem, this::openCreateLeafItemDialog),
				CContextActionDefinition.of("copy-to", "Copy To...", VaadinIcon.COPY, context -> context != null,
						this::canCopyTo, this::openCopyToDialog),
				CContextActionDefinition.of("assign-to", "Assign To...", VaadinIcon.USER, context -> context != null,
						this::canAssignTo, this::openAssignToDialog),
				CContextActionDefinition.of("assign-to-me", "Assign To Me", VaadinIcon.USER, context -> context != null,
						this::canAssignTo, this::assignToMe),
				CContextActionDefinition.of("set-status", "Set Status...", VaadinIcon.CLIPBOARD_CHECK,
						context -> context != null, this::canSetStatus, this::openStatusDialog),
				CContextActionDefinition.of("delete-item", "Delete", VaadinIcon.TRASH, context -> context != null,
						this::canDeleteItem, this::deleteBacklogItem),
				CContextActionDefinition.of("edit-leaf-item", "Edit", VaadinIcon.EDIT, context -> context != null,
						context -> context != null,
						context -> openProjectItemEditDialog(context != null ? context.getEntity() : null)));
	}

	private List<CContextActionDefinition<CGnntItem>> buildParentQuickActions() {
		return List.of(
				CContextActionDefinition.of("new-parent-item", "New", VaadinIcon.PLUS_CIRCLE_O, context -> true,
						this::canCreateParentItem, this::openCreateParentItemDialog),
				CContextActionDefinition.of("new-leaf-item", "New child", VaadinIcon.PLUS, context -> true,
						this::canCreateLeafItem, this::openCreateLeafItemDialog),
				CContextActionDefinition.of("add-existing-child", "Add existing", VaadinIcon.LIST_SELECT,
						context -> context != null, this::canAddExistingChild, this::openAddExistingChildDialog),
				CContextActionDefinition.of("copy-to", "Copy To...", VaadinIcon.COPY, context -> context != null,
						this::canCopyTo, this::openCopyToDialog),
				CContextActionDefinition.of("assign-to", "Assign To...", VaadinIcon.USER, context -> context != null,
						this::canAssignTo, this::openAssignToDialog),
				CContextActionDefinition.of("assign-to-me", "Assign To Me", VaadinIcon.USER, context -> context != null,
						this::canAssignTo, this::assignToMe),
				CContextActionDefinition.of("set-status", "Set Status...", VaadinIcon.CLIPBOARD_CHECK,
						context -> context != null, this::canSetStatus, this::openStatusDialog),
				CContextActionDefinition.of("delete-item", "Delete", VaadinIcon.TRASH, context -> context != null,
						this::canDeleteItem, this::deleteBacklogItem),
				CContextActionDefinition.of("edit-parent-item", "Edit", VaadinIcon.EDIT, context -> context != null,
						context -> context != null,
						context -> openProjectItemEditDialog(context != null ? context.getEntity() : null)));
	}

	private List<CContextActionDefinition<CGnntItem>> buildSprintContextActions() {
		return List.of(
				CContextActionDefinition.of("move-to-backlog", "Move to backlog", VaadinIcon.ARROW_BACKWARD,
						context -> context != null && context.getEntity() instanceof ISprintableItem,
						this::canMoveSprintItemToBacklog, this::moveSprintItemToBacklog),
				CContextActionDefinition.of("edit-sprint-item", "Edit item", VaadinIcon.EDIT,
						context -> context != null && context.getEntity() instanceof CProjectItem<?, ?>,
						context -> context != null,
						context -> openProjectItemEditDialog(context != null ? context.getEntity() : null)),
				CContextActionDefinition.of("edit-sprint", "Edit sprint details", VaadinIcon.CALENDAR_CLOCK,
						context -> context != null && context.getEntity() instanceof CSprint,
						context -> context != null,
						context -> openEditSprintDialog(context != null && context.getEntity() instanceof CSprint
								? (CSprint) context.getEntity() : null)));
	}

	private CGnntHierarchyResult buildSprintHierarchy(final CSprintPlanningViewEntity view,
			final Map<String, CProjectItem<?, ?>> entitiesByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		final CSprint selectedSprint = filterToolbar.getSelectedSprint();
		final List<CSprint> sprints = new ArrayList<>(sprintService.listByProject(view.getProject()));
		sprints.sort(Comparator.comparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo))
				.thenComparing(CSprint::getName, String.CASE_INSENSITIVE_ORDER));
		// Metrics are computed on full sprint membership (not search-filtered) so the widget stays stable.
		final Map<Long, CSprintPlanningSprintMetrics> metricsBySprintId = new HashMap<>();
		for (final CProjectItem<?, ?> entity : entitiesByKey.values()) {
			final ISprintableItem sprintableItem = (ISprintableItem) entity;
			final CSprint sprint =
					sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
			if (sprint == null || sprint.getId() == null) {
				continue;
			}
			final long points =
					sprintableItem.getSprintItem() != null && sprintableItem.getSprintItem().getStoryPoint() != null
							? sprintableItem.getSprintItem().getStoryPoint() : 0L;
			final boolean done = entity.getStatus() != null && Boolean.TRUE.equals(entity.getStatus().getFinalStatus());
			final CSprintPlanningSprintMetrics current =
					metricsBySprintId.getOrDefault(sprint.getId(), new CSprintPlanningSprintMetrics(0, 0, 0, 0));
			metricsBySprintId.put(sprint.getId(),
					new CSprintPlanningSprintMetrics(current.itemDoneCount() + (done ? 1 : 0),
							current.itemTotalCount() + 1, current.storyPointsDone() + (done ? points : 0),
							current.storyPointsTotal() + points));
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
			// Sprint combobox is both a target-selector and a real timeline filter.
			if (selectedSprint != null) {
				if (selectedSprint.getId() != null && sprint.getId() != null
						&& !sprint.getId().equals(selectedSprint.getId())) {
					continue;
				}
				if (selectedSprint.getId() == null && scope == ESprintPlanningScope.SPRINT && sprint.getName() != null
						&& selectedSprint.getName() != null
						&& !sprint.getName().equalsIgnoreCase(selectedSprint.getName())) {
					continue;
				}
			}
			final CGnntItem sprintItem = new CGnntItem(sprint, uniqueId++, 0);
			rootItems.add(sprintItem);
			flatItems.add(sprintItem);
			final List<CGnntItem> sprintChildren = new ArrayList<>();
			for (final CProjectItem<?, ?> entity : entitiesByKey.values()) {
				final CSprint itemSprint = ((ISprintableItem) entity).getSprintItem() != null
						? ((ISprintableItem) entity).getSprintItem().getSprint() : null;
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

	private boolean canAddExistingChild(final CGnntItem context) {
		return resolveParentContextEntity(context) != null
				&& hierarchyDialogSupport.hasSelectableExistingChildren(resolveParentContextEntity(context), null);
	}

	private boolean canAddLeafToSprint(final CGnntItem context) {
		if (context == null || !(context.getEntity() instanceof ISprintableItem)
				|| !(context.getEntity() instanceof CProjectItem<?, ?>)) {
			return false;
		}
		return isLeafItem(context.getEntity());
	}

	private boolean canAssignTo(final CGnntItem context) {
		final CProjectItem<?, ?> item = resolveProjectItemContext(context);
		// Allow assigning newly created rows; saveEntity(...) will persist and refresh selection.
		return item != null;
	}

	private boolean canCopyTo(final CGnntItem context) {
		final CEntityDB<?> entity = resolveEntityContext(context);
		return entity != null && entity.getId() != null;
	}

	private boolean canCreateLeafItem(final CGnntItem context) {
		final CProjectItem<?, ?> parentContext = resolveLeafCreationParent(context);
		return parentContext != null && hierarchyDialogSupport.hasCreatableItems(parentContext.getProject(),
				parentContext, entityClass -> !CHierarchyNavigationService
						.canHaveChildren(createPreviewItem(entityClass, parentContext.getProject())));
	}

	private boolean canCreateParentItem(final CGnntItem context) {
		final CProjectItem<?, ?> parentContext = resolveParentContextEntity(context);
		final CSprintPlanningViewEntity view = getValue();
		return view != null && view.getProject() != null
				&& hierarchyDialogSupport.hasCreatableItems(view.getProject(), parentContext,
						entityClass -> parentContext != null || CHierarchyNavigationService
								.canHaveChildren(createPreviewItem(entityClass, view.getProject())));
	}

	private boolean canDeleteItem(final CGnntItem context) {
		final CEntityDB<?> entity = resolveEntityContext(context);
		return entity != null && entity.getId() != null;
	}

	private boolean canMoveSprintItemToBacklog(final CGnntItem context) {
		return context != null && context.getEntity() instanceof ISprintableItem;
	}

	private boolean canSetStatus(final CGnntItem context) {
		final CProjectItem<?, ?> item = resolveProjectItemContext(context);
		return item != null && item.getId() != null && item instanceof IHasStatusAndWorkflow;
	}

	private Map<String, CSprintPlanningSprintMetrics> computeBacklogParentRollups(
			final List<CGnntItem> visibleLeafItems, final Map<String, CProjectItem<?, ?>> hierarchyItemsByKey) {
		if (visibleLeafItems == null || visibleLeafItems.isEmpty() || hierarchyItemsByKey == null
				|| hierarchyItemsByKey.isEmpty()) {
			return Map.of();
		}
		final Map<String, CSprintPlanningSprintMetrics> rollups = new HashMap<>();
		for (final CGnntItem leafItem : visibleLeafItems) {
			final CProjectItem<?, ?> leaf = leafItem != null ? leafItem.getEntity() : null;
			if (!(leaf instanceof final ISprintableItem sprintableItem)) {
				continue;
			}
			final boolean done = leaf.getStatus() != null && Boolean.TRUE.equals(leaf.getStatus().getFinalStatus());
			final long points =
					sprintableItem.getSprintItem() != null && sprintableItem.getSprintItem().getStoryPoint() != null
							? sprintableItem.getSprintItem().getStoryPoint() : 0L;
			String parentKey = CHierarchyNavigationService.buildParentKey(leaf);
			while (parentKey != null) {
				final CProjectItem<?, ?> parent = hierarchyItemsByKey.get(parentKey);
				if (parent == null) {
					break;
				}
				final CSprintPlanningSprintMetrics current =
						rollups.getOrDefault(parentKey, new CSprintPlanningSprintMetrics(0, 0, 0, 0));
				rollups.put(parentKey,
						new CSprintPlanningSprintMetrics(current.itemDoneCount() + (done ? 1 : 0),
								current.itemTotalCount() + 1, current.storyPointsDone() + (done ? points : 0),
								current.storyPointsTotal() + points));
				parentKey = CHierarchyNavigationService.buildParentKey(parent);
			}
		}
		return rollups.isEmpty() ? Map.of() : Map.copyOf(rollups);
	}

	private CProjectItem<?, ?> createPreviewItem(final Class<? extends CProjectItem<?, ?>> entityClass,
			final CProject<?> project) {
		try {
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			final Object serviceBean = serviceClass != null ? CSpringContext.getBean(serviceClass) : null;
			if (!(serviceBean instanceof final CEntityOfProjectService<?> projectService)) {
				return null;
			}
			final Object previewEntity = projectService.newEntity("Preview " + entityClass.getSimpleName(), project);
			return previewEntity instanceof CProjectItem<?, ?> ? (CProjectItem<?, ?>) previewEntity : null;
		} catch (final Exception e) {
			LOGGER.debug("Could not create preview item for {} reason={}", entityClass.getSimpleName(), e.getMessage());
			return null;
		}
	}

	private void deleteBacklogItem(final CGnntItem context) {
		try {
			final CEntityDB<?> entity = resolveEntityContext(context);
			if (entity == null || entity.getId() == null) {
				CNotificationService.showWarning("Select an item to delete");
				return;
			}
			final String displayName =
					entity instanceof final CEntityNamed<?> named ? named.getName() : String.valueOf(entity.getId());
			CNotificationService.showConfirmationDialog("Delete '%s'?".formatted(displayName), () -> {
				try {
					deleteEntity(entity);
					refreshComponent();
					CNotificationService.showDeleteSuccess();
				} catch (final Exception ex) {
					LOGGER.error("Failed to delete backlog item: {}", ex.getMessage(), ex);
					CNotificationService.showException("Unable to delete item", ex);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Failed to open delete confirmation: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to delete item", e);
		}
	}

	/** Delete an entity using its registered service (avoids hard-coding entity-type services in the board). */
	@SuppressWarnings ({})
	private void deleteEntity(final CEntityDB<?> entity) {
		final Class<?> entityClass = ProxyUtils.getUserClass(entity.getClass());
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
		if (serviceClass == null) {
			throw new IllegalStateException(
					"No service registered for entity type: %s".formatted(entityClass.getSimpleName()));
		}
		final Object serviceBean = CSpringContext.getBean(serviceClass);
		if (!(serviceBean instanceof final CAbstractService service)) {
			throw new IllegalStateException(
					"Registered service is not a CAbstractService: %s".formatted(serviceClass.getSimpleName()));
		}
		service.delete(entity.getId());
	}

	private Map<String, CProjectItem<?, ?>>
			filterSprintableItems(final Map<String, CProjectItem<?, ?>> hierarchyItemsByKey) {
		final Map<String, CProjectItem<?, ?>> sprintableItemsByKey = new HashMap<>();
		for (final Map.Entry<String, CProjectItem<?, ?>> entry : hierarchyItemsByKey.entrySet()) {
			if (entry.getValue() instanceof ISprintableItem) {
				sprintableItemsByKey.put(entry.getKey(), entry.getValue());
			}
		}
		return sprintableItemsByKey;
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
		backlogBrowser.getParentQuickAccessPanel().setContextActions(buildParentQuickActions(),
				this::getSelectedParentActionContext);
		backlogBrowser.getLeafQuickAccessPanel().setOnRefresh(this::refreshComponent);
		backlogBrowser.getLeafQuickAccessPanel().setContextActions(buildLeafQuickActions(),
				this::getSelectedLeafActionContext);
		backlogBrowser.setParentContextActions(buildParentQuickActions());
		backlogBrowser.setLeafContextActions(buildLeafQuickActions());
		backlogBrowser.getParentQuickAccessPanel().setShowRefreshButton(true);
		backlogBrowser.getLeafQuickAccessPanel().setShowRefreshButton(true);
		gridSprints.setContextActions(buildSprintContextActions());
		gridSprints.getQuickAccessPanel().setContextActions(buildSprintContextActions(), gridSprints::getSelectedItem);
		// Filters/actions live in the Gnnt quick-access header; keep the board itself single-row (more vertical space for timelines).
		layoutGrids.add(gridsSplit);
		layoutGrids.setFlexGrow(1, gridsSplit);
		splitLayout = new SplitLayout(layoutGrids, componentItemDetails);
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.setSplitterPosition(detailsVisible ? DEFAULT_SPLITTER_POSITION : 100.0);
		componentItemDetails.setVisible(detailsVisible);
		splitLayout.setWidthFull();
		splitLayout.setHeightFull();
		add(splitLayout);
	}

	private boolean isClosedSprint(final CSprint sprint) {
		// Eager-fetch sprint status to avoid LazyInitialization on detached proxy during drag/drop
		if (sprint == null || sprint.getId() == null) {
			return false;
		}
		final CSprint managedSprint = sprintService.getById(sprint.getId()).orElse(null);
		return managedSprint != null && managedSprint.getStatus() != null
				&& Boolean.TRUE.equals(managedSprint.getStatus().getFinalStatus());
	}

	private boolean isLeafItem(final CProjectItem<?, ?> entity) {
		// Sprint assignment is restricted to leaf items (hierarchy level -1), regardless of entity type
		return entity != null && CHierarchyNavigationService.getEntityLevel(entity) == -1;
	}

	private Map<String, CProjectItem<?, ?>> loadHierarchyItems(final CSprintPlanningViewEntity view) {
		if (view == null || view.getProject() == null) {
			return Map.of();
		}
		final Map<String, CProjectItem<?, ?>> itemsByKey = new HashMap<>();
		for (final CProjectItem<?, ?> projectItem : hierarchyNavigationService.listHierarchyItems(view.getProject())) {
			final String entityKey = CHierarchyNavigationService.buildEntityKey(projectItem);
			if (entityKey != null) {
				itemsByKey.put(entityKey, projectItem);
			}
		}
		return itemsByKey;
	}

	private void moveSprintItemToBacklog(final CGnntItem context) {
		try {
			if (context == null || !(context.getEntity() instanceof final ISprintableItem sprintableItem)) {
				CNotificationService.showWarning("Select a sprint item first");
				return;
			}
			final CSprintItem anchorItem = resolveBacklogAnchorItem();
			sprintableItem.moveSprintItemToBacklog(anchorItem, anchorItem != null);
			final CProjectItem<?, ?> item = context != null && context.getEntity() instanceof CProjectItem<?, ?>
					? (CProjectItem<?, ?>) context.getEntity() : null;
			if (item instanceof final IHasStatusAndWorkflow<?, ?> statusItem) {
				final CProjectItemStatus resetStatus = CSprintItemWorkflowStatusSupport
						.applyWorkflowInitialStatus(statusItem, projectItemStatusService);
				if (resetStatus != null && item instanceof final ISprintableItem sprintableToSave) {
					sprintableToSave.saveProjectItem();
				}
			}
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
			final ISprintableItem anchorSprintableItem = dropRequest != null && dropRequest.targetItem() != null
					&& dropRequest.targetItem().getEntity() instanceof ISprintableItem
							? (ISprintableItem) dropRequest.targetItem().getEntity() : null;
			final CSprintItem anchorItem = anchorSprintableItem != null ? anchorSprintableItem.getSprintItem() : null;
			final boolean insertAfter =
					dropRequest != null && dropRequest.dropLocation() != null && switch (dropRequest.dropLocation()) {
					case BELOW -> true;
					default -> false;
					};
			sprintableItem.moveSprintItemToBacklog(anchorItem, insertAfter);
			if (sprintableItem instanceof final IHasStatusAndWorkflow<?, ?> statusItem) {
				final CProjectItemStatus resetStatus = CSprintItemWorkflowStatusSupport
						.applyWorkflowInitialStatus(statusItem, projectItemStatusService);
				if (resetStatus != null) {
					sprintableItem.saveProjectItem();
				}
			}
			refreshComponent();
			CNotificationService.showSuccess("Moved '%s' to backlog".formatted(draggedItem.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to move item to backlog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to move item to backlog", e);
		}
	}

	private void onBacklogParentDrop(final CGnntItem draggedItem, final CGnntItem dropTarget) {
		try {
			final CProjectItem<?, ?> child = resolveProjectItemContext(draggedItem);
			final CProjectItem<?, ?> parent = resolveProjectItemContext(dropTarget);
			if (child == null) {
				return;
			}
			// Dropping on empty space means "make root" (clear parent).
			if (parent == null) {
				parentRelationService.setParent(child, null);
				saveEntity(child);
				refreshComponent();
				CNotificationService.showSuccess("Moved '%s' to root".formatted(child.getName()));
				return;
			}
			if (!hierarchyNavigationService.isValidParentCandidate(child, parent)) {
				CNotificationService
						.showWarning("'%s' cannot be placed under '%s'".formatted(child.getName(), parent.getName()));
				return;
			}
			// Persist the relation via the centralized hierarchy service.
			parentRelationService.setParent(child, parent);
			saveEntity(child);
			refreshComponent();
			CNotificationService.showSuccess("Reparented '%s' under '%s'".formatted(child.getName(), parent.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to reparent backlog item: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to reparent backlog item", e);
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
			CNotificationService.showSuccess(
					"Assigned '%s' to sprint '%s'".formatted(draggedItem.getName(), targetSprint.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to assign item to sprint: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to assign item to sprint", e);
		}
	}

	@Override
	protected void onValueChanged(final CSprintPlanningViewEntity oldValue, final CSprintPlanningViewEntity newValue,
			final boolean fromClient) {
		LOGGER.debug("Sprint planning board changed from {} to {}", oldValue != null ? oldValue.getName() : "null",
				newValue != null ? newValue.getName() : "null");
		refreshComponent();
	}

	private void openAddExistingChildDialog(final CGnntItem context) {
		try {
			final CProjectItem<?, ?> parentContext = resolveParentContextEntity(context);
			if (parentContext == null) {
				CNotificationService.showWarning("Select a backlog parent first");
				return;
			}
			hierarchyDialogSupport.openAddExistingDialog("Add Existing Child", parentContext, null,
					this::refreshComponent);
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
			availableSprints
					.sort(Comparator.comparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo))
							.thenComparing(CSprint::getName, String.CASE_INSENSITIVE_ORDER));
			final CDialogAddBacklogItemToSprint dialog =
					new CDialogAddBacklogItemToSprint(itemToAssign.getName(), availableSprints, sprint -> {
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

	private void openAssignToDialog(final CGnntItem context) {
		try {
			final CProjectItem<?, ?> item = resolveProjectItemContext(context);
			final CSprintPlanningViewEntity view = getValue();
			if (item == null) {
				CNotificationService.showWarning("Select an item first");
				return;
			}
			if (view == null || view.getProject() == null) {
				CNotificationService.showWarning("Select a project first");
				return;
			}
			final List<EntityTypeConfig<?>> types =
					List.of(EntityTypeConfig.createWithRegistryName(CUser.class, userService));
			final CDialogEntitySelection<CUser> dialog = new CDialogEntitySelection<>("Assign To", types,
					config -> userService.listByProject(view.getProject()), selected -> {
						final CUser selectedUser = selected != null && !selected.isEmpty() ? selected.get(0) : null;
						if (selectedUser == null) {
							return;
						}
						try {
							item.setAssignedTo(selectedUser);
							final CEntityDB<?> saved = saveEntity(item);
							refreshComponent();
							restoreSelectionAfterRefresh(saved);
							CNotificationService.showSuccess(
									"Assigned '%s' to %s".formatted(item.getName(), selectedUser.getName()));
						} catch (final Exception ex) {
							LOGGER.error("Failed to assign backlog item: {}", ex.getMessage(), ex);
							CNotificationService.showException("Unable to assign item", ex);
						}
					}, false);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open assign dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to assign item", e);
		}
	}

	@SuppressWarnings ("unchecked")
	private void openCopyToDialog(final CGnntItem context) {
		try {
			final CEntityDB<?> entity = resolveEntityContext(context);
			if (entity == null || entity.getId() == null) {
				CNotificationService.showWarning("Select a saved backlog item first");
				return;
			}
			@SuppressWarnings ({
					"rawtypes"
			})
			final CEntityDB entityRaw = entity;
			openCopyToDialogForEntity(entityRaw);
		} catch (final Exception e) {
			LOGGER.error("Failed to open copy-to dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to open copy dialog", e);
		}
	}

	private <EntityClass extends CEntityDB<EntityClass>> void openCopyToDialogForEntity(final EntityClass entity)
			throws Exception {
		final CDialogClone<EntityClass> dialog = new CDialogClone<>(entity, copiedEntity -> {
			try {
				saveEntity(copiedEntity);
				refreshComponent();
				final String displayName = copiedEntity instanceof final CEntityNamed<?> named ? named.getName()
						: copiedEntity.getClass().getSimpleName();
				CNotificationService.showSuccess("Copied '%s'".formatted(displayName));
			} catch (final Exception ex) {
				LOGGER.error("Failed to save copied backlog item: {}", ex.getMessage(), ex);
				CNotificationService.showException("Unable to copy item", ex);
			}
		});
		dialog.open();
	}

	private void openCreateLeafItemDialog(final CGnntItem context) {
		final CProjectItem<?, ?> parentContext = resolveLeafCreationParent(context);
		if (parentContext == null) {
			CNotificationService.showWarning("Select a backlog parent that can own leaf items first");
			return;
		}
		hierarchyDialogSupport.openCreateDialog(parentContext.getProject(), parentContext,
				entityClass -> !CHierarchyNavigationService
						.canHaveChildren(createPreviewItem(entityClass, parentContext.getProject())),
				this::refreshComponent);
	}

	private void openCreateParentItemDialog(final CGnntItem context) {
		final CSprintPlanningViewEntity view = getValue();
		final CProjectItem<?, ?> parentContext = resolveParentContextEntity(context);
		if (view == null || view.getProject() == null) {
			CNotificationService.showWarning("Select a project first");
			return;
		}
		hierarchyDialogSupport
				.openCreateDialog(view.getProject(), parentContext,
						entityClass -> parentContext != null || CHierarchyNavigationService
								.canHaveChildren(createPreviewItem(entityClass, view.getProject())),
						this::refreshComponent);
	}

	private void openEditSprintDialog(final CSprint sprint) {
		try {
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
			if (!(entity instanceof final CProjectItem<?, ?> projectItem)) {
				return;
			}
			hierarchyDialogSupport.openEditDialog(projectItem, this::refreshComponent);
		} catch (final Exception e) {
			LOGGER.error("Failed to open project item edit dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to open item editor", e);
		}
	}

	private void openStatusDialog(final CGnntItem context) {
		final CProjectItem<?, ?> item = resolveProjectItemContext(context);
		if (item == null) {
			CNotificationService.showWarning("Select an item first");
			return;
		}
		if (item.getId() == null) {
			CNotificationService.showWarning("Save the item first");
			return;
		}
		if (!(item instanceof IHasStatusAndWorkflow)) {
			CNotificationService.showWarning("Selected item does not support workflow/status");
			return;
		}
		try {
			final List<CProjectItemStatus> statuses =
					projectItemStatusService.getValidNextStatuses((IHasStatusAndWorkflow<?, ?>) item);
			if (statuses == null || statuses.isEmpty()) {
				CNotificationService.showWarning("No valid next statuses available");
				return;
			}
			if (statuses.size() == 1) {
				applyStatus(item, statuses.get(0));
				return;
			}
			final CDialogKanbanStatusSelection dialog =
					new CDialogKanbanStatusSelection("Workflow", statuses, selectedStatus -> {
						if (selectedStatus == null) {
							return;
						}
						applyStatus(item, selectedStatus);
					});
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open status dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to set status", e);
		}
	}

	@Override
	protected void refreshComponent() {
		try {
			final CSprintPlanningViewEntity view = getValue();
			if (view == null || view.getProject() == null) {
				final CGnntHierarchyResult emptyHierarchy = new CGnntHierarchyResult(List.of(), Map.of(), List.of());
				final CGanttTimelineRange emptyRange =
						CBacklogNavigatorHierarchyBuilder.resolveTimelineRange(List.of());
				backlogBrowser.setBacklogData(emptyHierarchy, emptyHierarchy, Map.of(), emptyRange);
				backlogBrowser.setParentRollupSummaries(Map.of());
				gridSprints.setHierarchy(emptyHierarchy, emptyRange);
				lastHierarchyItemsByKey = Map.of();
				selectedDetailsEntity = null;
				selectedItem = null;
				selectedSprintForMetrics = null;
				sprintMetricsById = Map.of();
				backlogBrowser.setBacklogMetrics(new CSprintPlanningSprintMetrics(0, 0, 0, 0));
				filterToolbar.setSelectedSprintMetrics(null, null);
				if (detailsVisible) {
					componentItemDetails.clear();
				}
				return;
			}
			filterToolbar.setProject(view.getProject());
			final Map<String, CProjectItem<?, ?>> hierarchyItemsByKey = loadHierarchyItems(view);
			final Map<String, CProjectItem<?, ?>> sprintableItemsByKey = filterSprintableItems(hierarchyItemsByKey);
			lastHierarchyItemsByKey = hierarchyItemsByKey;
			final List<CGnntItem> allItems = new ArrayList<>();
			long entityTypeSequence = 1;
			for (final CProjectItem<?, ?> projectItem : hierarchyItemsByKey.values()) {
				allItems.add(new CGnntItem(projectItem, entityTypeSequence++, 0));
			}
			filterToolbar.setAvailableEntityTypes(allItems);
			final CBacklogNavigatorHierarchyBuilder.CBacklogData backlogData = buildBacklogData(hierarchyItemsByKey);
			final CGnntHierarchyResult sprints = buildSprintHierarchy(view, sprintableItemsByKey);
			updateBacklogMetrics(hierarchyItemsByKey);
			final CGanttTimelineRange range =
					resolveTimelineRange(backlogData.leafHierarchy().getFlatItems(), sprints.getFlatItems());
			backlogBrowser.setBacklogData(backlogData.parentHierarchy(), backlogData.leafHierarchy(),
					hierarchyItemsByKey, range);
			backlogBrowser.setParentRollupSummaries(
					computeBacklogParentRollups(backlogData.leafHierarchy().getFlatItems(), hierarchyItemsByKey));
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
		if (selectedLeafItem == null
				|| !(selectedLeafItem.getEntity() instanceof final ISprintableItem sprintableItem)) {
			return null;
		}
		return sprintableItem.getSprintItem();
	}

	private CGnntItem resolveEffectiveContext(final CGnntItem context) {
		// Context-menu actions supply the clicked row; quick-access actions may call without context → fall back to current selection.
		return context != null ? context : selectedItem;
	}

	private CEntityDB<?> resolveEntityContext(final CGnntItem context) {
		final CGnntItem effectiveContext = resolveEffectiveContext(context);
		return effectiveContext != null && effectiveContext.getEntity() instanceof CEntityDB<?>
				? (CEntityDB<?>) effectiveContext.getEntity() : null;
	}

	private CProjectItem<?, ?> resolveLeafCreationParent(final CGnntItem context) {
		if (context != null && context.getEntity() instanceof CProjectItem<?, ?>) {
			final CProjectItem<?, ?> projectItem = context.getEntity();
			if (CHierarchyNavigationService.canHaveChildren(projectItem)) {
				return projectItem;
			}
			return resolveParentItem(projectItem);
		}
		final CProjectItem<?, ?> selectedParent = resolveParentContextEntity(backlogBrowser.getSelectedParentItem());
		if (selectedParent != null && CHierarchyNavigationService.canHaveChildren(selectedParent)) {
			return selectedParent;
		}
		return null;
	}

	private CProjectItem<?, ?> resolveParentContextEntity(final CGnntItem context) {
		if (context == null) {
			return null;
		}
		return context.getEntity() instanceof CProjectItem<?, ?> ? (CProjectItem<?, ?>) context.getEntity() : null;
	}

	private CProjectItem<?, ?> resolveParentItem(final CProjectItem<?, ?> entity) {
		final String parentKey = CHierarchyNavigationService.buildParentKey(entity);
		if (parentKey == null) {
			return null;
		}
		return lastHierarchyItemsByKey.get(parentKey);
	}

	private CProjectItem<?, ?> resolveProjectItemContext(final CGnntItem context) {
		final CGnntItem effectiveContext = resolveEffectiveContext(context);
		return effectiveContext != null && effectiveContext.getEntity() instanceof CProjectItem<?, ?>
				? (CProjectItem<?, ?>) effectiveContext.getEntity() : null;
	}

	private CSprintItem resolveSelectedSprintAnchorItem(final CSprint targetSprint) {
		if (selectedItem == null || targetSprint == null || !(selectedItem.getEntity() instanceof ISprintableItem)) {
			return null;
		}
		final ISprintableItem sprintableItem = (ISprintableItem) selectedItem.getEntity();
		final CSprint selectedSprint =
				sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
		return selectedSprint != null && selectedSprint.getId() != null
				&& selectedSprint.getId().equals(targetSprint.getId()) ? sprintableItem.getSprintItem() : null;
	}

	private CSprintItem resolveSprintDropAnchorItem(final CSprintPlanningDropRequest dropRequest,
			final CSprint targetSprint) {
		final CGnntItem dropTarget = dropRequest != null ? dropRequest.targetItem() : null;
		if (dropTarget == null || dropTarget.getEntity() == null || targetSprint == null
				|| targetSprint.getId() == null) {
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
		if (dropRequest != null && dropRequest.dropLocation() == GridDropLocation.ABOVE) {
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

	private CGanttTimelineRange resolveTimelineRange(final List<CGnntItem> backlogItems,
			final List<CGnntItem> sprintItems) {
		return CBacklogNavigatorHierarchyBuilder.resolveTimelineRange(backlogItems, sprintItems);
	}

	private void restoreSelectionAfterRefresh(final CEntityDB<?> savedEntity) {
		if (!(savedEntity instanceof final CProjectItem<?, ?> savedItem)) {
			return;
		}
		final String entityKey = CHierarchyNavigationService.buildEntityKey(savedItem);
		if (entityKey == null) {
			return;
		}
		// Both grids cache item-by-key maps, so selection can be restored even after refreshComponent() rebuilds wrapper rows.
		backlogBrowser.selectByEntityKey(entityKey);
		gridSprints.selectByEntityKey(entityKey);
	}

	/** Persist an arbitrary planning-grid entity by resolving its owning service from the registry. We keep this local to the board because sprint
	 * planning mixes multiple project-item types in one UI. */
	@SuppressWarnings ({
			"unchecked"
	})
	private CEntityDB<?> saveEntity(final CEntityDB<?> entity) {
		final Class<?> entityClass = ProxyUtils.getUserClass(entity.getClass());
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
		if (serviceClass == null) {
			throw new IllegalStateException(
					"No service registered for entity type: %s".formatted(entityClass.getSimpleName()));
		}
		final Object serviceBean = CSpringContext.getBean(serviceClass);
		if (!(serviceBean instanceof final CAbstractService service)) {
			throw new IllegalStateException(
					"Registered service is not a CAbstractService: %s".formatted(serviceClass.getSimpleName()));
		}
		return service.save(entity);
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

	private void updateBacklogMetrics(final Map<String, CProjectItem<?, ?>> hierarchyItemsByKey) {
		final ESprintPlanningScope scope = filterToolbar.getScope();
		if (scope == ESprintPlanningScope.SPRINT) {
			backlogBrowser.setBacklogMetrics(new CSprintPlanningSprintMetrics(0, 0, 0, 0));
			return;
		}
		int itemCount = 0;
		int doneCount = 0;
		long storyPoints = 0;
		long doneStoryPoints = 0;
		for (final CProjectItem<?, ?> entity : hierarchyItemsByKey.values()) {
			if (!(entity instanceof final ISprintableItem sprintableItem)) {
				continue;
			}
			final CSprint sprint =
					sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
			final boolean backlogCandidate = scope == ESprintPlanningScope.ALL_ITEMS || sprint == null;
			if (!backlogCandidate || !filterToolbar.shouldIncludeBacklogItem(entity)) {
				continue;
			}
			// Metrics should be leaf-focused, matching the leaf-only sprint assignment rule.
			if (CHierarchyNavigationService.getEntityLevel(entity) != -1) {
				continue;
			}
			itemCount++;
			final Long points =
					sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getStoryPoint() : null;
			final long resolvedPoints = points != null ? points : 0L;
			storyPoints += resolvedPoints;
			if (entity.getStatus() != null && Boolean.TRUE.equals(entity.getStatus().getFinalStatus())) {
				doneCount++;
				doneStoryPoints += resolvedPoints;
			}
		}
		backlogBrowser.setBacklogMetrics(
				new CSprintPlanningSprintMetrics(doneCount, itemCount, doneStoryPoints, storyPoints));
	}

	private void updateSelectedSprintFromSelection(final CGnntItem item) {
		final Object entity = item != null ? item.getEntity() : null;
		if (entity instanceof CSprint) {
			selectedSprintForMetrics = (CSprint) entity;
		} else if (entity instanceof final ISprintableItem sprintableItem) {
			selectedSprintForMetrics =
					sprintableItem.getSprintItem() != null ? sprintableItem.getSprintItem().getSprint() : null;
		} else {
			selectedSprintForMetrics = filterToolbar.getSelectedSprint();
		}
		updateSelectedSprintMetrics();
	}

	private void updateSelectedSprintMetrics() {
		final CSprint sprint =
				selectedSprintForMetrics != null ? selectedSprintForMetrics : filterToolbar.getSelectedSprint();
		if (sprint == null || sprint.getId() == null) {
			filterToolbar.setSelectedSprintMetrics(null, null);
			return;
		}
		// Refresh the sprint entity before reading display fields so context-menu actions can safely reuse detached grid items.
		final CSprint managedSprint = sprintService.getById(sprint.getId()).orElse(null);
		if (managedSprint == null) {
			filterToolbar.setSelectedSprintMetrics(null, null);
			return;
		}
		final CSprintPlanningSprintMetrics metrics =
				sprintMetricsById.getOrDefault(managedSprint.getId(), new CSprintPlanningSprintMetrics(0, 0, 0, 0));
		filterToolbar.setSelectedSprintMetrics(managedSprint, metrics);
	}

	private boolean validateLeafOnly(final Object entity, final String displayName) {
		if (!(entity instanceof final CProjectItem<?, ?> projectItem)) {
			return true;
		}
		if (isLeafItem(projectItem)) {
			return true;
		}
		CNotificationService.showWarning("Only leaf items can be added to a sprint: '%s'".formatted(displayName));
		return false;
	}
}
