package tech.derbent.plm.kanban.kanbanline.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.IHasSelectionNotification;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.page.view.CDynamicPageRouter;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CTabSheet;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.basic.IHasMultiValuePersistence;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.dialogs.CDialogEntitySelection;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.sprints.planning.domain.ESprintPlanningScope;
import tech.derbent.plm.sprints.planning.view.components.CComponentBacklogNavigator;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.api.ui.component.filter.CAbstractFilterToolbar;
import tech.derbent.api.ui.component.filter.CKanbanSearchFilter;
import tech.derbent.api.ui.component.filter.CKanbanSprintMembershipFilter;
import tech.derbent.api.ui.component.filter.CKanbanSprintScopeFilter;
import tech.derbent.api.ui.component.filter.CResponsibleUserFilter;
import tech.derbent.api.ui.component.filter.CShowClosedFilter;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.plm.kanban.kanbanline.domain.EKanbanViewMode;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.plm.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;
import tech.derbent.plm.sprints.service.CSprintItemService;
import tech.derbent.plm.sprints.service.CSprintService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;

/** CComponentKanbanBoard - Displays a kanban line as a board with vertical columns and post-it style project items. */
public class CComponentKanbanBoard extends CComponentBase<CKanbanLine>
		implements IContentOwner, IHasSelectionNotification, IHasDragControl, IPageServiceAutoRegistrable, IHasMultiValuePersistence {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanBoard.class);
	private static final long serialVersionUID = 1L;

	/** Returns true when the sprint item is owned by the target user. */
	private static boolean matchesResponsibleUser(final CSprintItem sprintItem, final CUser targetUser) {
		final ISprintableItem item = sprintItem.getParentItem();
		if (item == null || item.getAssignedTo() == null || item.getAssignedTo().getId() == null || targetUser.getId() == null) {
			return false;
		}
		return item.getAssignedTo().getId().equals(targetUser.getId());
	}

	/** Returns true when the sprint item matches the selected type filter. */
	private static boolean matchesTypeFilter(final CSprintItem sprintItem, final Class<?> entityClass) {
		if (entityClass == null) {
			return true;
		}
		final ISprintableItem item = sprintItem.getParentItem();
		return item != null && entityClass.isAssignableFrom(item.getClass());
	}

	private List<CSprintItem> allSprintItems;
	private List<CSprint> availableSprints;
	private CComponentKanbanColumnBacklog backlogColumn;
	private final CDynamicPageRouter currentEntityPageRouter;
	private final CTabSheet detailTabs;
	private final CVerticalLayout tabBacklogLayout;
	private final CVerticalLayout tabSprintFeaturesLayout;
	private final CVerticalLayout tabSprintSummaryLayout;
	private final com.vaadin.flow.component.tabs.Tab tabDetails;
	private CComponentBacklogNavigator backlogNavigator;
	private final EKanbanViewMode currentMode = EKanbanViewMode.SPRINT_BOARD;
	private CSprint currentSprint;
	private boolean statusOnlyMode;
	private boolean allSprintsMode = true;
	private final Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragDropEvent>> dropListeners = new HashSet<>();
	private final CComponentKanbanBoardFilterToolbar filterToolbar;
	private boolean isRestoring = false;
	private boolean suppressFilterEvents = false;
	private final CKanbanLineService kanbanLineService;
	private final CHorizontalLayout layoutColumns;
	final CVerticalLayout layoutDetails = new CVerticalLayout();
	protected final CPageEntityService pageEntityService;
	private boolean persistenceEnabled;
	// Persistence fields
	private String persistenceNamespace;
	private CComponentKanbanPostit selectedPostit;
	private final Set<ComponentEventListener<CSelectEvent>> selectListeners = new HashSet<>();
	private final ISessionService sessionService;
	protected SplitLayout splitLayout = new SplitLayout();
	private List<CSprintItem> sprintItems;
	private final CSprintItemService sprintItemService;
	private final Comparator<CSprint> sprintRecencyComparator;
	private final CSprintService sprintService;
	private final CUserService userService;
	private final CProjectItemStatusService projectItemStatusService;

	/** Creates the kanban board and initializes filters and layout. */
	
	public CComponentKanbanBoard() {
		LOGGER.debug("Initializing Kanban board component");
		sessionService = CSpringContext.getBean(ISessionService.class);
		kanbanLineService = CSpringContext.getBean(CKanbanLineService.class);
		sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		sprintService = CSpringContext.getBean(CSprintService.class);
		userService = CSpringContext.getBean(CUserService.class);
		projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		Check.notNull(sessionService, "Session service cannot be null for Kanban board");
		Check.notNull(kanbanLineService, "Kanban line service cannot be null for Kanban board");
		Check.notNull(sprintItemService, "Sprint item service cannot be null for Kanban board");
		Check.notNull(sprintService, "Sprint service cannot be null for Kanban board");
		allSprintItems = new ArrayList<>();
		availableSprints = new ArrayList<>();
		sprintItems = new ArrayList<>();
		layoutColumns = new CHorizontalLayout();
		layoutColumns.setWidthFull();
		layoutColumns.setHeight(null);
		layoutColumns.setSpacing(true);
		layoutColumns.setAlignItems(Alignment.STRETCH);
		layoutColumns.addClassName("kanban-board-columns");
		sprintRecencyComparator =
				Comparator.<CSprint, LocalDateTime>comparing(CSprint::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo))
						.thenComparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo))
						.thenComparing(CSprint::getCreatedDate, Comparator.nullsLast(LocalDateTime::compareTo))
						.thenComparing(CSprint::getId, Comparator.nullsLast(Long::compareTo));
		filterToolbar = new CComponentKanbanBoardFilterToolbar();
		filterToolbar.addFilterChangeListener(event -> { if (!suppressFilterEvents) applyFilters(); });
		// Value persistence is automatically enabled in build() method
		setSizeFull();
		setPadding(false);
		setSpacing(false);
		add(splitLayout);
		splitLayout.setSizeFull();
		splitLayout.getStyle().set("padding", "0px");
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.addToPrimary(layoutColumns);
		splitLayout.addToSecondary(layoutDetails);
		splitLayout.setSplitterPosition(70);
		final CDetailSectionService detailSectionService = CSpringContext.getBean(CDetailSectionService.class);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		currentEntityPageRouter = new CDynamicPageRouter(pageEntityService, sessionService, detailSectionService, null);

		detailTabs = new CTabSheet();
		tabBacklogLayout = new CVerticalLayout();
		tabBacklogLayout.setPadding(false);
		tabBacklogLayout.setSpacing(false);
		tabBacklogLayout.setSizeFull();

		final CVerticalLayout tabDetailsLayout = new CVerticalLayout();
		tabDetailsLayout.setPadding(false);
		tabDetailsLayout.setSpacing(false);
		tabDetailsLayout.setSizeFull();
		tabDetailsLayout.add(currentEntityPageRouter);

		tabSprintFeaturesLayout = new CVerticalLayout();
		tabSprintFeaturesLayout.setPadding(true);
		tabSprintFeaturesLayout.setSpacing(true);
		tabSprintFeaturesLayout.setSizeFull();
		tabSprintFeaturesLayout.add(new Span("Sprint features (due dates, status, etc.) — TODO"));

		tabSprintSummaryLayout = new CVerticalLayout();
		tabSprintSummaryLayout.setPadding(true);
		tabSprintSummaryLayout.setSpacing(true);
		tabSprintSummaryLayout.setSizeFull();
		tabSprintSummaryLayout.add(new Span("Sprint summary (distribution, totals, etc.) — TODO"));

		detailTabs.add("Backlog", tabBacklogLayout);
		tabDetails = detailTabs.add("Details", tabDetailsLayout);
		detailTabs.add("Sprint features", tabSprintFeaturesLayout);
		detailTabs.add("Sprint summary", tabSprintSummaryLayout);

		layoutDetails.removeAll();
		layoutDetails.add(detailTabs);
		// splitLayout.setFlexGrow(1, layoutColumns);
		add(filterToolbar, splitLayout);
		expand(splitLayout);
		// setDropEnabled(true);
		// addDropListener(on_grid_dragDrop());
	}

	/** Applies current filters and refreshes the board. */
	private void applyFilters() {
		sessionService.setSessionValue("counter_applyFilters_called",
				(Integer) sessionService.getSessionValue("counter_applyFilters_called").orElse(0) + 1);
		LOGGER.info("[Performance] applyFilters() called count:{}",
				sessionService.getSessionValue("counter_applyFilters_called").orElse(0));
		final CKanbanLine currentLine = getValue();
		Check.notNull(currentLine, "Kanban line must be set before applying filters");
		final CAbstractFilterToolbar.FilterCriteria<CSprintItem> criteria = filterToolbar.getCurrentCriteria();
		final CKanbanSprintScopeFilter.CSprintScope scope = criteria.getValue(CKanbanSprintScopeFilter.FILTER_KEY);
		final boolean newStatusOnlyMode = scope != null && scope.mode() == CKanbanSprintScopeFilter.EScopeMode.STATUS_ONLY;
		final boolean newAllSprintsMode = scope == null || scope.mode() == CKanbanSprintScopeFilter.EScopeMode.ALL_SPRINTS;
		final CSprint selectedSprint = scope != null && scope.mode() == CKanbanSprintScopeFilter.EScopeMode.SPRINT ? scope.sprint() : null;
		final boolean sprintChanged = !isSameSprint(selectedSprint);
		final boolean modeChanged = newStatusOnlyMode != statusOnlyMode || newAllSprintsMode != allSprintsMode;
		boolean dataReloaded = false;
		if (modeChanged || sprintChanged) {
			statusOnlyMode = newStatusOnlyMode;
			allSprintsMode = newAllSprintsMode;
			currentSprint = selectedSprint;
			final CProject<?> project = sessionService.getActiveProject().orElse(null);
			if (currentSprint != null && currentSprint.getId() != null) {
				loadSprintItemsForSprint(currentSprint);
			} else if (project != null) {
				loadAllProjectItems(project);
			} else {
				allSprintItems = new ArrayList<>();
				filterToolbar.setAvailableItems(allSprintItems);
			}
			dataReloaded = true;
		}
		// Propagate showClosed to the backlog navigator if it exists
		final boolean showClosed = Boolean.TRUE.equals(criteria.getValue(CShowClosedFilter.FILTER_KEY));
		if (backlogNavigator != null) {
			backlogNavigator.setShowClosed(showClosed);
		}
		List<CSprintItem> newFilteredItems = filterSprintItems(criteria);
		// In "All sprints" mode the backlog column already shows sprint=null items.
		// Exclude them from status columns to avoid double-counting (and mismatched totals vs per-sprint view).
		if (allSprintsMode && !statusOnlyMode) {
			newFilteredItems = newFilteredItems.stream().filter(item -> item != null && item.getSprint() != null).toList();
		}
		final boolean itemsChanged = !newFilteredItems.equals(sprintItems);
		if (modeChanged || dataReloaded || itemsChanged) {
			LOGGER.info("[Performance] Items changed, refreshing component");
			sprintItems = newFilteredItems;
			refreshComponent();
		} else {
			LOGGER.info("[Performance] No changes detected, skipping refresh");
		}
	}

	/** Returns the current board view mode. */
	public EKanbanViewMode getCurrentMode() {
		return EKanbanViewMode.SPRINT_BOARD;
	}

	/** Loads all project items for Status Board mode. */
	private void loadAllProjectItems(final CProject<?> project) {
		LOGGER.info("[StatusBoard] Loading all project items for project: {}", project.getId());
		try {
			allSprintItems = new ArrayList<>(sprintItemService.findAllByProjectWithItems(project));
			sprintItems = new ArrayList<>(allSprintItems);
			filterToolbar.setAvailableItems(allSprintItems);
		} catch (final Exception e) {
			LOGGER.error("[StatusBoard] Failed to load all project items reason={}", e.getMessage());
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			filterToolbar.setAvailableItems(allSprintItems);
		}
	}

	/** Assigns each sprint item to a kanban column id before rendering. This is the core status-to-column mapping algorithm for kanban board display.
	 * Mapping Logic: 1. Build a status ID -> column ID map from all kanban columns 2. For each sprint item: a. If item already has kanbanColumnId
	 * set: skip auto-mapping (manual override) b. Otherwise: look up item's status ID in the map c. If status found in map: assign corresponding
	 * column ID d. If status not found: assign default column ID (if exists), otherwise -1 Status Uniqueness Assumption: This algorithm assumes each
	 * status is mapped to AT MOST ONE column. If a status is mapped to multiple columns (status overlap), the first mapping wins due to
	 * Map.putIfAbsent() behavior. However, such overlap is prevented by validation in CKanbanColumnService.validateStatusUniqueness(). Default Column
	 * Handling: The default column (marked with defaultColumn=true) serves as a fallback for: - Items whose status is not explicitly mapped to any
	 * column - Items with null or unmapped statuses It's stored in the map with key=-1L for easy lookup. */
	private void assignKanbanColumns(final List<CSprintItem> items, final List<CKanbanColumn> columns) {
		LOGGER.debug("Assigning Kanban columns to sprint items for board display");
		if (items == null || items.isEmpty() || columns == null || columns.isEmpty()) {
			return;
		}
		// Build status ID -> column ID mapping from all columns
		final Map<Long, Long> statusToColumnId = prepareStatusToColumnIdMap(columns);
		for (final CSprintItem sprintItem : items) {
			if (sprintItem == null) {
				continue;
			}
			// Respect manual column assignment: skip auto-mapping if column already set
			if (sprintItem.getKanbanColumnId() != null) {
				LOGGER.debug("Kanban column already assigned for item {}, skipping auto-mapping", sprintItem.getId());
				continue;
			}
			// Get item's current status and map to column
			final ISprintableItem sprintableItem = sprintItem.getParentItem();
			if (sprintableItem == null || sprintableItem.getStatus() == null || sprintableItem.getStatus().getId() == null) {
				LOGGER.warn("Skipping sprint item {} during kanban mapping - missing parent/status", sprintItem.getId());
				continue;
			}
			final Long statusId = sprintableItem.getStatus().getId();
			// Lookup: try explicit status mapping first, fall back to default column
			
			final Long columnId = statusToColumnId.computeIfAbsent(statusId, event -> statusToColumnId.getOrDefault(-1L, -1L));
			// Debug logging for troubleshooting status-to-column mappings
			// LOGGER.debug("Mapping status id {}:{} -> column id {} result to: {} company
			// id:{}", statusId, sprintableItem.getStatus().getName(),
			// statusToColumnId.get(statusId), columnId,
			// sprintableItem.getStatus().getCompany().getId());
			// No column found: log warning (indicates configuration issue)
			if (columnId == -1L) {
				LOGGER.warn("No kanban column found for status id {} in line {}", statusId, getValue() != null ? getValue().getName() : "null");
				continue;
			}
			// Assign the resolved column ID to the sprint item
			sprintItem.setKanbanColumnId(columnId);
		}
	}

	/** Creates and configures a backlog column for the kanban board. The backlog column is automatically created as the first column in the kanban
	 * board and displays items from the project that are not assigned to any sprint.
	 * @param project The project whose backlog items should be displayed
	 * @return Configured backlog column component */
	private void ensureBacklogNavigator() {
		if (backlogNavigator != null) {
			return;
		}
		final CProject<?> project = sessionService.getActiveProject().orElse(null);
		if (project == null) {
			return;
		}
		try {
			backlogNavigator = new CComponentBacklogNavigator();
			backlogNavigator.setId("kanban-backlog-navigator");
			backlogNavigator.setShowParentTaskRollup(true);
			backlogNavigator.setProject(project);
			backlogNavigator.setScope(ESprintPlanningScope.BACKLOG);
			backlogNavigator.drag_setDropEnabled(true);
			setupSelectionNotification(backlogNavigator);
			setupChildDragDropForwarding(backlogNavigator);
			tabBacklogLayout.removeAll();
			tabBacklogLayout.add(backlogNavigator);
			tabBacklogLayout.expand(backlogNavigator);
			backlogNavigator.setVisible(!statusOnlyMode);
		} catch (final Exception e) {
			LOGGER.error("Failed to create backlog navigator for kanban board reason={}", e.getMessage());
		}
	}

	private List<CContextActionDefinition<CComponentKanbanPostit>> buildPostitContextActions() {
		return List.of(
				CContextActionDefinition.of("show-details", "Show details", VaadinIcon.SEARCH,
						postit -> postit != null && postit.resolveSprintableItem() != null,
						postit -> postit != null && postit.resolveSprintableItem() != null, this::showPostitDetails),
				CContextActionDefinition.of("open-page", "Open page", VaadinIcon.EDIT,
						postit -> postit != null && postit.resolveSprintableItem() instanceof CEntityDB<?>,
						postit -> postit != null && postit.resolveSprintableItem() instanceof CEntityDB<?>, this::openPostitPage),
				CContextActionDefinition.of("set-status", "Set status...", VaadinIcon.CLIPBOARD_CHECK,
						postit -> postit != null && postit.resolveSprintableItem() instanceof IHasStatusAndWorkflow,
						postit -> postit != null && postit.resolveSprintableItem() instanceof IHasStatusAndWorkflow,
						this::openPostitStatusDialog),
				CContextActionDefinition.of("assign-to-me", "Assign to me", VaadinIcon.USER_CHECK,
						postit -> postit != null && postit.resolveSprintableItem() instanceof CProjectItem<?, ?>,
						postit -> postit != null && postit.resolveSprintableItem() instanceof CProjectItem<?, ?>,
						this::assignPostitToMe),
				CContextActionDefinition.of("assign-to", "Assign to...", VaadinIcon.USER,
						postit -> postit != null && postit.resolveSprintableItem() instanceof CProjectItem<?, ?>,
						postit -> postit != null && postit.resolveSprintableItem() instanceof CProjectItem<?, ?>,
						this::openPostitAssignToDialog),
				CContextActionDefinition.of("remove-from-sprint", "Remove from sprint", VaadinIcon.MINUS_CIRCLE,
						postit -> postit != null && postit.getEntity() != null && postit.getEntity().getSprint() != null,
						postit -> postit != null && postit.getEntity() != null && postit.getEntity().getSprint() != null,
						this::removePostitFromSprint),
				CContextActionDefinition.of("refresh-board", "Refresh board", VaadinIcon.REFRESH, postit -> true, postit -> true,
						postit -> refreshGridSafely()));
	}

	private void openPostitStatusDialog(final CComponentKanbanPostit postit) {
		final ISprintableItem sprintable = postit != null ? postit.resolveSprintableItem() : null;
		if (!(sprintable instanceof final CProjectItem<?, ?> item)) {
			CNotificationService.showWarning("Select an item first");
			return;
		}
		if (item.getId() == null) {
			CNotificationService.showWarning("Save the item first");
			return;
		}
		if (!(item instanceof IHasStatusAndWorkflow)) {
			CNotificationService.showWarning("Item does not support workflow/status");
			return;
		}
		try {
			final List<CProjectItemStatus> statuses = projectItemStatusService.getValidNextStatuses((IHasStatusAndWorkflow<?, ?>) item);
			if (statuses == null || statuses.isEmpty()) {
				CNotificationService.showWarning("No valid next statuses available");
				return;
			}
			if (statuses.size() == 1) {
				applyPostitStatus(item, statuses.get(0));
				return;
			}
			final CDialogKanbanStatusSelection dialog = new CDialogKanbanStatusSelection("Set Status", statuses,
					selectedStatus -> {
						if (selectedStatus == null) {
							return;
						}
						applyPostitStatus(item, selectedStatus);
					});
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open status dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to set status", e);
		}
	}

	@SuppressWarnings ("unchecked")
	private void applyPostitStatus(final CProjectItem<?, ?> item, final CProjectItemStatus status) {
		try {
			((IHasStatusAndWorkflow<?, ?>) item).setStatus(status);
			saveSprintableItem(item);
			refreshGridSafely();
			CNotificationService.showSuccess("Set status of '%s' to '%s'".formatted(item.getName(), status.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to set status: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to set status", e);
		}
	}

	private void assignPostitToMe(final CComponentKanbanPostit postit) {
		final ISprintableItem sprintable = postit != null ? postit.resolveSprintableItem() : null;
		if (!(sprintable instanceof final CProjectItem<?, ?> item)) {
			CNotificationService.showWarning("Select an item first");
			return;
		}
		final CUser currentUser = sessionService.getActiveUser().orElse(null);
		if (currentUser == null) {
			CNotificationService.showWarning("No active user in session");
			return;
		}
		try {
			item.setAssignedTo(currentUser);
			saveSprintableItem(item);
			refreshGridSafely();
			CNotificationService.showSuccess("Assigned '%s' to you".formatted(item.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to assign item to current user: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to assign item", e);
		}
	}

	private void openPostitAssignToDialog(final CComponentKanbanPostit postit) {
		final ISprintableItem sprintable = postit != null ? postit.resolveSprintableItem() : null;
		if (!(sprintable instanceof final CProjectItem<?, ?> item)) {
			CNotificationService.showWarning("Select an item first");
			return;
		}
		final CProject<?> project = sessionService.getActiveProject().orElse(null);
		if (project == null) {
			CNotificationService.showWarning("No active project");
			return;
		}
		try {
			final List<EntityTypeConfig<?>> types = List.of(EntityTypeConfig.createWithRegistryName(CUser.class, userService));
			final CDialogEntitySelection<CUser> dialog = new CDialogEntitySelection<>("Assign To", types,
					config -> userService.listByProject(project), selected -> {
						final CUser selectedUser = selected != null && !selected.isEmpty() ? selected.get(0) : null;
						if (selectedUser == null) {
							return;
						}
						try {
							item.setAssignedTo(selectedUser);
							saveSprintableItem(item);
							refreshGridSafely();
							CNotificationService.showSuccess("Assigned '%s' to %s".formatted(item.getName(), selectedUser.getName()));
						} catch (final Exception ex) {
							LOGGER.error("Failed to assign item: {}", ex.getMessage(), ex);
							CNotificationService.showException("Unable to assign item", ex);
						}
					}, false);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Failed to open assign dialog: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to open assign dialog", e);
		}
	}

	private void removePostitFromSprint(final CComponentKanbanPostit postit) {
		final CSprintItem sprintItem = postit != null ? postit.getEntity() : null;
		if (sprintItem == null || sprintItem.getSprint() == null) {
			CNotificationService.showWarning("Item is not in a sprint");
			return;
		}
		try {
			sprintItem.setSprint(null);
			sprintItemService.save(sprintItem);
			refreshGridSafely();
			CNotificationService.showSuccess("Moved item back to backlog");
		} catch (final Exception e) {
			LOGGER.error("Failed to remove item from sprint: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to remove from sprint", e);
		}
	}

	@SuppressWarnings ("unchecked")
	private void saveSprintableItem(final CProjectItem<?, ?> item) {
		final Class<?> entityClass = org.springframework.data.util.ProxyUtils.getUserClass(item.getClass());
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
		if (serviceClass == null) {
			throw new IllegalStateException("No service registered for: " + entityClass.getSimpleName());
		}
		final Object serviceBean = CSpringContext.getBean(serviceClass);
		if (!(serviceBean instanceof final CAbstractService service)) {
			throw new IllegalStateException("Registered service is not CAbstractService: " + serviceClass.getSimpleName());
		}
		service.save(item);
	}

	/** Kanban board does not support creating entities here. */
	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		LOGGER.debug("Creating new entity instance is not supported for Kanban board component");
		return null;
	}

	@Override
	public void drag_checkEventAfterPass(final CEvent event) {
		LOGGER.debug("[KanbanDrag] Completed drag event {}", event.getClass().getSimpleName());
	}

	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
		Check.notNull(event, "Drag event cannot be null for Kanban board");
		LOGGER.debug("[KanbanDrag] Board propagating {}", event.getClass().getSimpleName());
	}

	@Override
	public Set<ComponentEventListener<CDragEndEvent>> drag_getDragEndListeners() {
		return dragEndListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragStartEvent>> drag_getDragStartListeners() {
		return dragStartListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragDropEvent>> drag_getDropListeners() {
		return dropListeners;
	}

	@Override
	public boolean drag_isDropAllowed(CDragStartEvent event) {
		return false;
	}

	@Override
	public void drag_setDragEnabled(final boolean enabled) {
		// children are set at construction time
		return;
	}

	@Override
	public void drag_setDropEnabled(final boolean enabled) {
		// children are set at construction time
		return;
	}

	/** Enables persistence for this kanban board with project context. Should be called after project is set.
	 * @param project The project context for persistence namespace */
	public void enablePersistenceForProject(final CProject<?> project) {
		if (!(project != null && project.getId() != null)) {
			return;
		}
		final String namespace = "kanbanBoard_project" + project.getId();
		persist_enableMultiValue(namespace);
		LOGGER.info("[Persistence] Enabled for namespace: {}", namespace);
	}

	/** Filters sprint items based on the provided criteria. */
	private List<CSprintItem> filterSprintItems(final CAbstractFilterToolbar.FilterCriteria<CSprintItem> criteria) {
		final List<CSprintItem> filtered = new ArrayList<>();
		final Class<?> entityType = null;
		final CResponsibleUserFilter.ResponsibleFilterMode responsibleMode = criteria.getValue(CResponsibleUserFilter.FILTER_KEY);
		final String searchQuery = criteria.getValue(CKanbanSearchFilter.FILTER_KEY);
		final CKanbanSprintMembershipFilter.MembershipMode membershipMode = CKanbanSprintMembershipFilter.MembershipMode.ALL;
		final boolean showClosed = Boolean.TRUE.equals(criteria.getValue(CShowClosedFilter.FILTER_KEY));
		for (final CSprintItem sprintItem : allSprintItems) {
			if (sprintItem == null || sprintItem.getParentItem() == null) {
				continue;
			}
			if (!showClosed) {
				final ISprintableItem parentItem = sprintItem.getParentItem();
				if (parentItem instanceof final CProjectItem<?, ?> pi && pi.getStatus() != null
						&& Boolean.TRUE.equals(pi.getStatus().getFinalStatus())) {
					continue;
				}
			}
			if (!matchesTypeFilter(sprintItem, entityType)) {
				continue;
			}
			if (!matchesResponsibleFilter(sprintItem, responsibleMode)) {
				continue;
			}
			if (!matchesSearchFilter(sprintItem, searchQuery)) {
				continue;
			}
			if (!matchesMembershipFilter(sprintItem, membershipMode)) {
				continue;
			}
			filtered.add(sprintItem);
		}
		return filtered;
	}

	/** Returns true when the sprint item name contains the search query (case-insensitive). */
	private static boolean matchesSearchFilter(final CSprintItem sprintItem, final String searchQuery) {
		if (searchQuery == null || searchQuery.isBlank()) {
			return true;
		}
		final ISprintableItem item = sprintItem.getParentItem();
		if (item == null) {
			return false;
		}
		final String lower = searchQuery.toLowerCase();
		final String name = item.getName() != null ? item.getName().toLowerCase() : "";
		return name.contains(lower);
	}

	/** Returns true when the sprint item matches the sprint membership filter (Status Board mode). */
	private static boolean matchesMembershipFilter(final CSprintItem sprintItem, final CKanbanSprintMembershipFilter.MembershipMode mode) {
		if (mode == null || mode == CKanbanSprintMembershipFilter.MembershipMode.ALL) {
			return true;
		}
		final boolean hasSprint = sprintItem.getSprint() != null;
		return switch (mode) {
		case IN_SPRINT -> hasSprint;
		case BACKLOG_ONLY -> !hasSprint;
		default -> true;
		};
	}

	/** Finds a kanban postit component by its sprint item ID. Searches through all columns in the kanban board.
	 * @param sprintItemId The ID of the sprint item to find
	 * @return Optional containing the postit if found */
	// java
	private Optional<CComponentKanbanPostit> findPostitBySprintItemId(final Long sprintItemId) {
		if (sprintItemId == null) {
			return Optional.empty();
		}
		for (final CComponentKanbanColumn column : getKanbanColumns()) {
			for (final CComponentKanbanPostit postit : column.getPostits()) {
				if (postit.getEntity() != null && sprintItemId.equals(postit.getEntity().getId())) {
					return Optional.of(postit);
				}
			}
		}
		return Optional.empty();
	}

	/** Gets the backlog column component if it exists.
	 * @return The backlog column or null if not yet created */
	public CComponentKanbanColumnBacklog getBacklogColumn() { return backlogColumn; }

	public CComponentBacklogNavigator getBacklogNavigator() { return backlogNavigator; }

	@Override
	public String getComponentName() { return "kanbanBoard"; }

	/** Returns the current line id as string. */
	@Override
	public String getCurrentEntityIdString() {
		final CKanbanLine currentLine = getValue();
		if (currentLine == null || currentLine.getId() == null) {
			return null;
		}
		return currentLine.getId().toString();
	}

	/** Gets the current sprint selected in the filter toolbar.
	 * @return The current sprint or null if no sprint selected */
	public CSprint getCurrentSprint() { return currentSprint; }

	/** Returns true when the board is in status-only mode (no sprint context, backlog hidden). */
	public boolean isStatusOnlyMode() { return statusOnlyMode; }

	/** Kanban board does not expose a direct entity service. */
	@Override
	public CAbstractService<?> getEntityService() { return null; }

	List<CComponentKanbanColumn> getKanbanColumns() {
		final List<CComponentKanbanColumn> columns = new ArrayList<>();
		final List<Component> columnComponents = layoutColumns.getChildren().toList();
		columnComponents.stream().filter((final Component component) -> component instanceof CComponentKanbanColumn)
				.forEach((final Component component) -> columns.add((CComponentKanbanColumn) component));
		return columns;
	}

	@Override
	public Logger getLogger() { return LOGGER; }

	/** Checks whether the sprint selection has changed. */
	private boolean isSameSprint(final CSprint candidate) {
		if (candidate == null && currentSprint == null) {
			return true;
		}
		if (candidate == null || currentSprint == null) {
			return false;
		}
		if (candidate.getId() != null && currentSprint.getId() != null) {
			return candidate.getId().equals(currentSprint.getId());
		}
		return Objects.equals(candidate, currentSprint);
	}

	/** Loads items bound to the selected sprint. */
	private void loadSprintItemsForSprint(final CSprint sprint) {
		LOGGER.info("[DragDrop] loadSprintItemsForSprint called for sprint: {}", sprint != null ? sprint.getId() : "null");
		if (sprint == null || sprint.getId() == null) {
			LOGGER.info("[DragDrop] Sprint is null or has no ID, clearing items");
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			filterToolbar.setAvailableItems(allSprintItems);
			return;
		}
		try {
			// Sprint items already encode the project scope; use them as the single source
			// of truth
			// for the board cards to keep project selection aligned with the sprint filter.
			final List<CSprintItem> sprintItemsRaw = sprintItemService.findByMasterIdWithItems(sprint.getId());
			LOGGER.info("[DragDrop] Loaded {} sprint items from database for sprint {}", sprintItemsRaw != null ? sprintItemsRaw.size() : 0,
					sprint.getId());
			allSprintItems = new ArrayList<>(sprintItemsRaw);
			sprintItems = new ArrayList<>(allSprintItems);
			LOGGER.info("[DragDrop] Set sprintItems list to {} items", sprintItems.size());
			filterToolbar.setAvailableItems(allSprintItems);
		} catch (final Exception e) {
			LOGGER.error("[DragDrop] Failed to load sprint items for Kanban board reason={}", e.getMessage());
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			filterToolbar.setAvailableItems(allSprintItems);
		}
	}

	/** Loads available sprints for the active project. */
	private void loadSprintsForActiveProject() {
		availableSprints = new ArrayList<>();
		final CProject<?> project = sessionService.getActiveProject().orElse(null);
		if (project == null) {
			filterToolbar.setAvailableSprints(List.of(), null);
			filterToolbar.setAvailableItems(List.of());
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			currentSprint = null;
			return;
		}
		// Enable persistence for this project (only once)
		if (!persist_isEnabled()) {
			enablePersistenceForProject(project);
		}
		try {
			availableSprints = sprintService.listByProject(project);
			availableSprints.sort(sprintRecencyComparator.reversed());
			final CSprint defaultSprint = resolveDefaultSprint(availableSprints);
			// Suppress filter change events during programmatic sprint list update to
			// prevent triggering applyFilters() prematurely
			suppressFilterEvents = true;
			filterToolbar.setAvailableSprints(availableSprints, defaultSprint);
			suppressFilterEvents = false;
			// Reset mode state so applyFilters() detects a "change" and loads data on first load
			statusOnlyMode = false;
			allSprintsMode = false;
			currentSprint = null;
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			applyFilters();
		} catch (final Exception e) {
			suppressFilterEvents = false;
			LOGGER.error("Failed to load sprints for Kanban board reason={}", e.getMessage());
			filterToolbar.setAvailableSprints(List.of(), null);
			filterToolbar.setAvailableItems(List.of());
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			currentSprint = null;
		}
	}

	/** Filters items by responsible mode. */
	private boolean matchesResponsibleFilter(final CSprintItem sprintItem, final CResponsibleUserFilter.ResponsibleFilterMode mode) {
		// LOGGER.debug("Checking responsible filter for Kanban board item {}",
		// sprintItem != null ? sprintItem.getId() : "null");
		if (mode == null || mode == CResponsibleUserFilter.ResponsibleFilterMode.ALL) {
			return true;
		}
		if (mode != CResponsibleUserFilter.ResponsibleFilterMode.CURRENT_USER) {
			return true;
		}
		final CUser activeUser = sessionService.getActiveUser().orElse(null);
		Check.notNull(activeUser, "Active user not available for Kanban board filtering");
		return matchesResponsibleUser(sprintItem, activeUser);
	}

	/** Handles selection of backlog items. Does NOT switch to the details tab or load the detail view, intentionally — loading full entity details on
	 * every backlog click is expensive and the user did not request it. Details are shown only when a kanban postit is explicitly selected. */
	private void on_backlog_item_selected(final CSelectEvent selectEvent) {
		if (!(selectEvent.getSource() instanceof CComponentBacklogNavigator)) {
			return;
		}
		// Clear postit selection when backlog item is selected
		if (selectedPostit != null) {
			selectedPostit.setSelected(false);
			selectedPostit = null;
		}
		// Intentionally not loading entity details or switching tabs here
	}

	/** Updates selection state and details area. */
	private void on_postit_selected(final CComponentKanbanPostit postit) {
		LOGGER.debug("Kanban board post-it selection changed to {}", postit != null ? postit.getEntity().getId() : "null");
		if (selectedPostit != null && selectedPostit != postit) {
			selectedPostit.setSelected(false);
		}
		selectedPostit = postit;
		if (postit == null) {
			CDynamicPageRouter.displayEntityInDynamicOnepager(null, currentEntityPageRouter, sessionService, this);
			if (!isRestoring && persist_isEnabled()) {
				persist_clearValue(persistenceNamespace);
			}
			return;
		}
		selectedPostit.setSelected(true);
		detailTabs.setSelectedTab(tabDetails);
		final ISprintableItem sprintableEntity = postit.resolveSprintableItem();
		Check.instanceOf(sprintableEntity, CProjectItem.class, "Sprintable item must be a CEntityDB for Kanban board details display");
		CDynamicPageRouter.displayEntityInDynamicOnepager((CProjectItem<?, ?>) sprintableEntity, currentEntityPageRouter, sessionService, this);
		// Persist selected item ID (only if not restoring)
		if (!(!isRestoring && persist_isEnabled())) {
			return;
		}
		persistValue("selectedSprintItemId", postit.getEntity().getId());
		LOGGER.debug("Persisted selected sprint item ID: {}", postit.getEntity().getId());
	}

	private void openPostitPage(final CComponentKanbanPostit postit) {
		try {
			showPostitDetails(postit);
			final ISprintableItem sprintableEntity = postit != null ? postit.resolveSprintableItem() : null;
			Check.instanceOf(sprintableEntity, CEntityDB.class, "Kanban post-it must resolve to a navigable entity");
			CDynamicPageRouter.navigateToEntity((CEntityDB<?>) sprintableEntity);
		} catch (final Exception e) {
			CNotificationService.showException("Unable to open Kanban item page", e);
		}
	}

	/** Reacts to kanban line changes by reloading sprints. */
	@Override
	protected void onValueChanged(final CKanbanLine oldValue, final CKanbanLine newValue, final boolean fromClient) {
		LOGGER.debug("Kanban board value changed from {} to {}", oldValue, newValue);
		if (newValue == null) {
			layoutColumns.removeAll();
			return;
		}
		loadSprintsForActiveProject();
	}

	@Override
	public String persist_getNamespace() {
		return persistenceNamespace;
	}

	@Override
	public boolean persist_isEnabled() {
		return persistenceEnabled;
	}

	/** Restores the selected sprint item from persisted state. Called automatically when component attaches to UI. */
	@Override
	public void persist_onRestore() {
		if (!persist_isEnabled()) {
			return;
		}
		persist_getValue("selectedSprintItemId").ifPresent(sprintItemIdStr -> {
			try {
				isRestoring = true; // Prevent saving during restoration
				final Long sprintItemId = Long.parseLong(sprintItemIdStr);
				LOGGER.info("[Persistence] Restoring selected sprint item: {}", sprintItemId);
				// Find the postit with this sprint item ID across all columns
				findPostitBySprintItemId(sprintItemId).ifPresentOrElse(postit -> {
					LOGGER.info("[Persistence] Found postit for sprint item {}, selecting it", sprintItemId);
					on_postit_selected(postit);
				}, () -> LOGGER.warn("[Persistence] No postit found for persisted sprint item ID: {}", sprintItemId));
			} catch (final Exception e) {
				LOGGER.error("[Persistence] Error restoring selected sprint item reason={}", e.getMessage());
			} finally {
				isRestoring = false;
			}
		});
	}

	@Override
	public void persist_setEnabled(final boolean enabled) {
		persistenceEnabled = enabled;
	}

	@Override
	public void persist_setNamespace(final String namespace) {
		persistenceNamespace = namespace;
	}

	/** Populates and refreshes the board view. */
	@Override
	public void populateForm() {
		LOGGER.debug("Populating Kanban board component");
		refreshComponent();
	}

	/** Prepares a status ID -> column ID mapping for efficient kanban column assignment. This method builds a lookup map used by
	 * assignKanbanColumns() to determine which column should display each sprint item based on its status. Map Structure: - Key: Status ID (Long) -
	 * the ID of a CProjectItemStatus - Value: Column ID (Long) - the ID of the CKanbanColumn that should display this status - Special key -1L:
	 * Default column ID (fallback for unmapped statuses) Mapping Rules: 1. Default column (if exists): maps to key=-1L for fallback lookup 2. For
	 * each column's included statuses: maps status ID -> column ID 3. Uses Map.putIfAbsent() to respect first mapping when status overlap exists
	 * Status Overlap Handling: If a status appears in multiple columns (should not happen due to validation), putIfAbsent() ensures the FIRST column
	 * mapping wins. However, this is a safeguard; the primary defense against overlap is CKanbanColumnService.validateStatusUniqueness(). Debug
	 * Logging: Logs each status -> column mapping for troubleshooting kanban board display issues.
	 * @param columns The kanban columns to build mappings from
	 * @return Map of status ID -> column ID (includes special key -1L for default column) */
	
	Map<Long, Long> prepareStatusToColumnIdMap(final List<CKanbanColumn> columns) {
		final Map<Long, Long> statusToColumnId = new LinkedHashMap<>();
		for (final CKanbanColumn column : columns) {
			if (column == null || column.getId() == null) {
				continue;
			}
			// Register default column with special key -1L for fallback lookup
			if (Boolean.TRUE.equals(column.getDefaultColumn())) {
				statusToColumnId.putIfAbsent(-1L, column.getId());
			}
			// Map each included status to this column
			if (column.getIncludedStatuses() == null) {
				continue;
			}
			for (final var status : column.getIncludedStatuses()) {
				if (status == null || status.getId() == null) {
					continue;
				}
				// putIfAbsent: first mapping wins if status appears in multiple columns
				// This should not happen due to validateStatusUniqueness(), but acts as
				// safeguard
				statusToColumnId.putIfAbsent(status.getId(), column.getId());
				// Debug logging for troubleshooting status-to-column mappings
				// LOGGER.debug("Mapping status id {}:{} to column id {} company id:{}",
				// status.getId(), status.getName(), column.getId(),
				// status.getCompany().getId());
			}
		}
		return statusToColumnId;
	}

	/** Rebuilds the column layout with current items. */
	@Override
	public void refreshComponent() {
		LOGGER.info("[DragDrop] refreshComponent called - sprintItems size: {}", sprintItems != null ? sprintItems.size() : "null");
		// Save compact states before destroying existing columns
		final Map<Long, Boolean> compactStates = new HashMap<>();
		for (final CComponentKanbanColumn col : getKanbanColumns()) {
			if (col.getValue() != null && col.getValue().getId() != null && col.isCompactView()) {
				compactStates.put(col.getValue().getId(), Boolean.TRUE);
			}
		}
		layoutColumns.removeAll();
		if (backlogNavigator != null) {
			backlogNavigator.refreshData();
		}
		selectedPostit = null;
		final CKanbanLine currentLine = resolveLineForDisplay(getValue());
		if (currentLine == null) {
			final CDiv div = new CDiv("Select a Kanban line to display its board.");
			div.addClassName("kanban-board-placeholder");
			layoutColumns.add(div);
			return;
		}
		layoutColumns.removeClassName("kanban-status-board-mode");
		ensureBacklogNavigator();
		final List<CKanbanColumn> columns = new ArrayList<>(currentLine.getKanbanColumns());
		columns.sort(Comparator.comparing(CKanbanColumn::getItemOrder, Comparator.nullsLast(Integer::compareTo)));
		assignKanbanColumns(sprintItems, columns);
		columns.forEach((final CKanbanColumn column) -> {
			final CComponentKanbanColumn columnComponent = new CComponentKanbanColumn();
			columnComponent.drag_setDragEnabled(true);
			columnComponent.drag_setDropEnabled(true);
			columnComponent.setPostitContextActions(buildPostitContextActions());
			columnComponent.setStatusBoardMode(false);
			setupSelectionNotification(columnComponent);
			setupChildDragDropForwarding(columnComponent);
			columnComponent.setValue(column);
			// Restore compact state BEFORE setItems so postits are skipped when compact
			if (Boolean.TRUE.equals(compactStates.get(column.getId()))) {
				columnComponent.setCompactView(true);
			}
			LOGGER.info("[DragDrop] Setting {} items to column {}", sprintItems != null ? sprintItems.size() : "null", column.getName());
			columnComponent.setItems(sprintItems);
			layoutColumns.add(columnComponent);
		});
	}

	/** Implements IContentOwner.refreshGrid() to refresh the kanban board when entity changes occur. This method is called by child components (e.g.,
	 * detail views) when entities are saved or deleted. It reloads sprint items from the database and refreshes the kanban board UI to reflect
	 * changes. This enables automatic updates when, for example, an activity's status is changed via the CRUD toolbar in the detail view - the kanban
	 * board will automatically reflect the new status/column assignment. */
	@Override
	public void refreshGrid() throws Exception {
		LOGGER.debug("Refreshing kanban board grid after entity change notification");
		reloadSprintItems();
		refreshComponent();
	}

	private void refreshGridSafely() {
		try {
			refreshGrid();
		} catch (final Exception e) {
			CNotificationService.showException("Unable to refresh Kanban board", e);
		}
	}

	// ==================== IHasMultiValuePersistence Implementation ====================

	/** Reloads sprint items from database to reflect persisted changes. Mode-aware: reloads from sprint or all project items depending on view mode. */
	public void reloadSprintItems() {
		LOGGER.info("[DragDrop] reloadSprintItems called");
		final CAbstractFilterToolbar.FilterCriteria<CSprintItem> criteria = filterToolbar.getCurrentCriteria();
		final CKanbanSprintScopeFilter.CSprintScope scope = criteria.getValue(CKanbanSprintScopeFilter.FILTER_KEY);
		statusOnlyMode = scope != null && scope.mode() == CKanbanSprintScopeFilter.EScopeMode.STATUS_ONLY;
		allSprintsMode = scope == null || scope.mode() == CKanbanSprintScopeFilter.EScopeMode.ALL_SPRINTS;
		currentSprint = scope != null && scope.mode() == CKanbanSprintScopeFilter.EScopeMode.SPRINT ? scope.sprint() : null;

		final CProject<?> project = sessionService.getActiveProject().orElse(null);
		if (currentSprint != null && currentSprint.getId() != null) {
			loadSprintItemsForSprint(currentSprint);
		} else if (project != null) {
			loadAllProjectItems(project);
		} else {
			allSprintItems = new ArrayList<>();
			filterToolbar.setAvailableItems(allSprintItems);
		}
		List<CSprintItem> filtered = filterSprintItems(criteria);
		if (allSprintsMode && !statusOnlyMode) {
			filtered = filtered.stream().filter(item -> item != null && item.getSprint() != null).toList();
		}
		sprintItems = filtered;
		LOGGER.info("[DragDrop] After filterSprintItems - sprintItems size: {}", sprintItems != null ? sprintItems.size() : "null");
		ensureBacklogNavigator();
		if (backlogNavigator != null) {
			backlogNavigator.setVisible(!statusOnlyMode);
		}
	}

	/** Picks the newest sprint as default. */
	private CSprint resolveDefaultSprint(final List<CSprint> sprints) {
		return sprints.stream().max(sprintRecencyComparator).orElse(null);
	}

	/** Reloads the line with columns and statuses for accurate filtering. */
	private CKanbanLine resolveLineForDisplay(final CKanbanLine line) {
		if (line == null) {
			return null;
		}
		final Long lineId = line.getId();
		if (lineId == null) {
			return line;
		}
		return kanbanLineService.getById(lineId).orElse(line);
	}

	@Override
	public void select_checkEventAfterPass(final CEvent event) {
		LOGGER.debug("[KanbanSelect] Selection propagated to board");
	}

	@Override
	public void select_checkEventBeforePass(final CEvent event) {
		Check.notNull(event, "Selection event cannot be null for Kanban board");
		if (event instanceof final CSelectEvent selectEvent) {
			if (selectEvent.getSource() instanceof final CComponentKanbanPostit postit) {
				// Selection from kanban postit
				on_postit_selected(postit);
			} else if (selectEvent.getSource() instanceof CComponentBacklogNavigator) {
				// Selection from backlog navigator
				on_backlog_item_selected(selectEvent);
			}
		}
	}

	@Override
	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners() {
		return selectListeners;
	}

	private void showPostitDetails(final CComponentKanbanPostit postit) {
		on_postit_selected(postit);
	}

	/** Sets items and reapplies filters for display. */
	public void setSprintItems(final List<CSprintItem> sprintItems) {
		LOGGER.debug("Setting sprint items for Kanban board component");
		Check.notNull(getValue(), "Kanban line must be set before setting sprint items");
		Check.notNull(sprintItems, "Sprint items cannot be null for kanban board");
		allSprintItems = new ArrayList<>(sprintItems);
		filterToolbar.setAvailableItems(allSprintItems);
		applyFilters();
	}

	/** Sets the current kanban line value. */
	@Override
	public void setValue(CEntityDB<?> entity) {
		super.setValue((CKanbanLine) entity);
	}
}
