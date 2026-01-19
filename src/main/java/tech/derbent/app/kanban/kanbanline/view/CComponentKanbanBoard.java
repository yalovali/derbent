package tech.derbent.app.kanban.kanbanline.view;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.IHasSelectionNotification;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.ui.component.filter.CAbstractFilterToolbar;
import tech.derbent.api.ui.component.filter.CEntityTypeFilter;
import tech.derbent.api.ui.component.filter.CResponsibleUserFilter;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.ui.component.filter.CSprintFilter;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.basic.IHasMultiValuePersistence;
import tech.derbent.api.ui.component.enhanced.CComponentBacklog;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.page.view.CDynamicPageRouter;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;
import tech.derbent.app.sprints.service.CSprintService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

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

	// Persistence fields
	private String persistenceNamespace;
	private boolean persistenceEnabled;
	private boolean isRestoring = false; // Flag to prevent save during restore
	private List<CSprintItem> allSprintItems;
	private List<CSprint> availableSprints;
	private CComponentKanbanColumnBacklog backlogColumn;
	private final CDynamicPageRouter currentEntityPageRouter;
	private CSprint currentSprint;
	private final Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragDropEvent>> dropListeners = new HashSet<>();
	private final CComponentKanbanBoardFilterToolbar filterToolbar;
	private final CKanbanLineService kanbanLineService;
	private final CHorizontalLayout layoutColumns;
	final CVerticalLayout layoutDetails = new CVerticalLayout();
	protected final CPageEntityService pageEntityService;
	private CComponentKanbanPostit selectedPostit;
	private final Set<ComponentEventListener<CSelectEvent>> selectListeners = new HashSet<>();
	private final ISessionService sessionService;
	protected SplitLayout splitLayout = new SplitLayout();
	private List<CSprintItem> sprintItems;
	private final CSprintItemService sprintItemService;
	private final Comparator<CSprint> sprintRecencyComparator;
	private final CSprintService sprintService;

	/** Creates the kanban board and initializes filters and layout. */
	public CComponentKanbanBoard() {
		LOGGER.debug("Initializing Kanban board component");
		sessionService = CSpringContext.getBean(ISessionService.class);
		kanbanLineService = CSpringContext.getBean(CKanbanLineService.class);
		sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		sprintService = CSpringContext.getBean(CSprintService.class);
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
		layoutColumns.setAlignItems(Alignment.START);
		layoutColumns.addClassName("kanban-board-columns");
		sprintRecencyComparator =
				Comparator.<CSprint, LocalDateTime>comparing(CSprint::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo))
						.thenComparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo))
						.thenComparing(CSprint::getCreatedDate, Comparator.nullsLast(LocalDateTime::compareTo))
						.thenComparing(CSprint::getId, Comparator.nullsLast(Long::compareTo));
		filterToolbar = new CComponentKanbanBoardFilterToolbar();
		filterToolbar.addFilterChangeListener( event -> applyFilters());
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
		layoutDetails.add(currentEntityPageRouter);
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
		LOGGER.info("[Performance] applyFilters() called - stack trace to identify caller, called:{}",
				sessionService.getSessionValue("counter_applyFilters_called").orElse(0));
		LOGGER.debug("Applying filters to Kanban board component");
		final CKanbanLine currentLine = getValue();
		Check.notNull(currentLine, "Kanban line must be set before applying filters");
		final CAbstractFilterToolbar.FilterCriteria<CSprintItem> criteria = filterToolbar.getCurrentCriteria();
		final CSprint sprint = criteria.getValue(CSprintFilter.FILTER_KEY);
		// PERFORMANCE OPTIMIZATION: Check if sprint changed before reloading from
		// database
		final boolean sprintChanged = !isSameSprint(sprint);
		if (sprintChanged) {
			LOGGER.info("[Performance] Sprint changed, reloading items from database");
			currentSprint = sprint;
			loadSprintItemsForSprint(currentSprint);
		}
		// PERFORMANCE OPTIMIZATION: Only filter and refresh if something actually
		// changed
		final List<CSprintItem> newFilteredItems = filterSprintItems(criteria);
		final boolean itemsChanged = !newFilteredItems.equals(sprintItems);
		if (sprintChanged || itemsChanged) {
			LOGGER.info("[Performance] Items changed, refreshing component");
			sprintItems = newFilteredItems;
			refreshComponent();
		} else {
			LOGGER.info("[Performance] No changes detected, skipping refresh");
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
	private CComponentKanbanColumnBacklog createBacklogColumn(final CProject project) {
		LOGGER.debug("Creating backlog column for project: {}", project.getName());
		final CComponentKanbanColumnBacklog column = new CComponentKanbanColumnBacklog(project);
		// Enable drag-drop for backlog items
		column.drag_setDragEnabled(true);
		column.drag_setDropEnabled(true);
		// Set up event forwarding for selection and drag-drop
		setupSelectionNotification(column);
		setupChildDragDropForwarding(column);
		return column;
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
	public void enablePersistenceForProject(final CProject project) {
		if (project != null && project.getId() != null) {
			final String namespace = "kanbanBoard_project" + project.getId();
			persist_enableMultiValue(namespace);
			LOGGER.info("[Persistence] Enabled for namespace: {}", namespace);
		}
	}

	/** Filters sprint items based on the provided criteria. Applies the following filters: - Entity type filter (if specified in criteria) -
	 * Responsible user filter (if specified in criteria)
	 * @param criteria The filter criteria to apply
	 * @return Filtered list of sprint items matching the criteria */
	private List<CSprintItem>
			filterSprintItems(final CAbstractFilterToolbar.FilterCriteria<CSprintItem> criteria) {
		final List<CSprintItem> filtered = new ArrayList<>();
		final Class<?> entityType = criteria.getValue(CEntityTypeFilter.FILTER_KEY);
		final CResponsibleUserFilter.ResponsibleFilterMode responsibleMode =
				criteria.getValue(CResponsibleUserFilter.FILTER_KEY);
		for (final CSprintItem sprintItem : allSprintItems) {
			if (sprintItem == null || sprintItem.getParentItem() == null) {
				continue;
			}
			if (!matchesTypeFilter(sprintItem, entityType)) {
				continue;
			}
			if (!matchesResponsibleFilter(sprintItem, responsibleMode)) {
				continue;
			}
			filtered.add(sprintItem);
		}
		return filtered;
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

	/** Kanban board does not expose a direct entity service. */
	@Override
	public CAbstractService<?> getEntityService() { return null; }

	List<CComponentKanbanColumn> getKanbanColumns() {
		final List<CComponentKanbanColumn> columns = new ArrayList<>();
		final List<Component> columnComponents = layoutColumns.getChildren().toList();
		for (final Component component : columnComponents) {
			if (component instanceof CComponentKanbanColumn) {
				columns.add((CComponentKanbanColumn) component);
			}
		}
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
			LOGGER.error("[DragDrop] Failed to load sprint items for Kanban board", e);
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			filterToolbar.setAvailableItems(allSprintItems);
		}
	}

	/** Loads available sprints for the active project. */
	private void loadSprintsForActiveProject() {
		availableSprints = new ArrayList<>();
		final CProject project = sessionService.getActiveProject().orElse(null);
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
			// Keep sprint selection constrained to the active project and preselect the
			// newest sprint
			// so the board always opens with the freshest work.
			availableSprints = sprintService.listByProject(project);
			availableSprints.sort(sprintRecencyComparator.reversed());
			final CSprint defaultSprint = resolveDefaultSprint(availableSprints);
			filterToolbar.setAvailableSprints(availableSprints, defaultSprint);
			currentSprint = filterToolbar.getCurrentCriteria().getValue(CSprintFilter.FILTER_KEY);
			loadSprintItemsForSprint(currentSprint);
		} catch (final Exception e) {
			LOGGER.error("Failed to load sprints for Kanban board", e);
			filterToolbar.setAvailableSprints(List.of(), null);
			filterToolbar.setAvailableItems(List.of());
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			currentSprint = null;
		}
	}

	/** Filters items by responsible mode. */
	private boolean matchesResponsibleFilter(final CSprintItem sprintItem,
			final CResponsibleUserFilter.ResponsibleFilterMode mode) {
		// LOGGER.debug("Checking responsible filter for Kanban board item {}",
		// sprintItem != null ? sprintItem.getId() : "null");
		if (mode == null || mode == CResponsibleUserFilter.ResponsibleFilterMode.ALL) {
			return true;
		}
		if (mode == CResponsibleUserFilter.ResponsibleFilterMode.CURRENT_USER) {
			final CUser activeUser = sessionService.getActiveUser().orElse(null);
			Check.notNull(activeUser, "Active user not available for Kanban board filtering");
			return matchesResponsibleUser(sprintItem, activeUser);
		}
		return true;
	}

	/** Handles selection of backlog items to display details in the entity view. Similar to postit selection but for items selected from the backlog
	 * grid. */
	private void on_backlog_item_selected(final CSelectEvent selectEvent) {
		// Get the backlog component from the event source
		Check.instanceOf(selectEvent.getSource(), CComponentBacklog.class, "Selection event source must be CComponentBacklog");
		final CComponentBacklog backlogComponent = (CComponentBacklog) selectEvent.getSource();
		final CProjectItem<?> selectedItem = backlogComponent.getSelectedBacklogItem();
		LOGGER.debug("Kanban board backlog item selection changed to {}", selectedItem != null ? selectedItem.getId() : "null");
		// Clear postit selection when backlog item is selected
		if (selectedPostit != null) {
			selectedPostit.setSelected(false);
			selectedPostit = null;
		}
		if (selectedItem == null) {
			CDynamicPageRouter.displayEntityInDynamicOnepager(null, currentEntityPageRouter, sessionService, this);
			return;
		}
		// Display the selected backlog item in the details view
		CDynamicPageRouter.displayEntityInDynamicOnepager(selectedItem, currentEntityPageRouter, sessionService, this);
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
		final ISprintableItem sprintableEntity = postit.resolveSprintableItem();
		Check.instanceOf(sprintableEntity, CProjectItem.class, "Sprintable item must be a CEntityDB for Kanban board details display");
		CDynamicPageRouter.displayEntityInDynamicOnepager((CProjectItem<?>) sprintableEntity, currentEntityPageRouter, sessionService, this);
		// Persist selected item ID (only if not restoring)
		if (!isRestoring && persist_isEnabled()) {
			persistValue("selectedSprintItemId", postit.getEntity().getId());
			LOGGER.debug("Persisted selected sprint item ID: {}", postit.getEntity().getId());
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
				}, () -> {
					LOGGER.warn("[Persistence] No postit found for persisted sprint item ID: {}", sprintItemId);
					return;
				});
			} catch (final Exception e) {
				LOGGER.error("[Persistence] Error restoring selected sprint item", e);
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
	@SuppressWarnings ("static-method")
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
		LOGGER.debug("Refreshing Kanban board component");
		layoutColumns.removeAll();
		selectedPostit = null;
		final CKanbanLine currentLine = resolveLineForDisplay(getValue());
		if (currentLine == null) {
			final CDiv div = new CDiv("Select a Kanban line to display its board.");
			div.addClassName("kanban-board-placeholder");
			layoutColumns.add(div);
			return;
		}
		// Create backlog column as first column if we have a current sprint
		if (currentSprint != null && currentSprint.getProject() != null) {
			backlogColumn = createBacklogColumn(currentSprint.getProject());
			layoutColumns.add(backlogColumn);
		}
		// Create regular kanban columns from the kanban line configuration
		final List<CKanbanColumn> columns = new ArrayList<>(currentLine.getKanbanColumns());
		columns.sort(Comparator.comparing(CKanbanColumn::getItemOrder, Comparator.nullsLast(Integer::compareTo)));
		assignKanbanColumns(sprintItems, columns);
		for (final CKanbanColumn column : columns) {
			final CComponentKanbanColumn columnComponent = new CComponentKanbanColumn();
			columnComponent.drag_setDragEnabled(true);
			columnComponent.drag_setDropEnabled(true);
			setupSelectionNotification(columnComponent);
			setupChildDragDropForwarding(columnComponent);
			// ==================== ONE REFRESH ONLY PATTERN ====================
			// CRITICAL: Set value BEFORE items to avoid double refresh (50% performance
			// improvement)
			//
			// WRONG ORDER (causes double refresh):
			// columnComponent.setItems(sprintItems); // Refresh #1: value is null, skipped
			// columnComponent.setValue(column); // Refresh #2: items already set, full
			// refresh
			// Result: 2 refreshes per column
			//
			// CORRECT ORDER (one refresh only):
			// columnComponent.setValue(column); // Sets configuration, items empty → skips
			// refresh
			// columnComponent.setItems(sprintItems); // Triggers SINGLE refresh (value is
			// set)
			// Result: 1 refresh per column (50% reduction)
			//
			// With 5 columns: 10 refreshes → 5 refreshes = significant performance gain
			// With 10 columns: 20 refreshes → 10 refreshes = 50% less CPU time
			columnComponent.setValue(column);
			LOGGER.info("[DragDrop] Setting {} items to column {}", sprintItems != null ? sprintItems.size() : "null", column.getName());
			columnComponent.setItems(sprintItems);
			layoutColumns.add(columnComponent);
		}
		// on_postit_selected(null);
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

	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null when registering Kanban board");
		pageService.registerComponent(getComponentName(), this);
		LOGGER.debug("[BindDebug] Registered Kanban board component as '{}'", getComponentName());
	}
	// ==================== IHasMultiValuePersistence Implementation ====================

	/** Reloads sprint items from database to reflect persisted changes. This method is called after drag-drop operations to ensure the UI displays
	 * the latest data from the database. Without reloading, the in-memory list contains stale objects that don't reflect recent kanbanColumnId or
	 * status changes. After reloading, filters are reapplied to maintain the current filter state. */
	public void reloadSprintItems() {
		LOGGER.info("[DragDrop] reloadSprintItems called");
		if (currentSprint != null && currentSprint.getId() != null) {
			loadSprintItemsForSprint(currentSprint);
			LOGGER.info("[DragDrop] After loadSprintItemsForSprint - sprintItems size: {}", sprintItems != null ? sprintItems.size() : "null");
			// Reapply filters to maintain filter state after reload
			final CAbstractFilterToolbar.FilterCriteria<CSprintItem> criteria =
					filterToolbar.getCurrentCriteria();
			sprintItems = filterSprintItems(criteria);
			LOGGER.info("[DragDrop] After filterSprintItems - sprintItems size: {}", sprintItems != null ? sprintItems.size() : "null");
		} else {
			LOGGER.warn("[DragDrop] reloadSprintItems called but currentSprint is null or has no ID");
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
			} else if (selectEvent.getSource() instanceof CComponentBacklog) {
				// Selection from backlog component
				on_backlog_item_selected(selectEvent);
			}
		}
	}

	@Override
	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners() {
		return selectListeners;
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
