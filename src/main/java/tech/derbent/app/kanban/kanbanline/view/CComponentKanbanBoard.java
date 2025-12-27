package tech.derbent.app.kanban.kanbanline.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;
import tech.derbent.app.sprints.service.CSprintService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CComponentKanbanBoard - Displays a kanban line as a board with vertical columns and post-it style project items. */
public class CComponentKanbanBoard extends CComponentBase<CKanbanLine> implements IContentOwner {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanBoard.class);
	private static final long serialVersionUID = 1L;

	/** Returns true when the sprint item is owned by the target user. */
	private static boolean matchesResponsibleUser(final CSprintItem sprintItem, final CUser targetUser) {
		final ISprintableItem item = sprintItem.getItem();
		if (item == null || item.getResponsible() == null || item.getResponsible().getId() == null || targetUser.getId() == null) {
			return false;
		}
		return item.getResponsible().getId().equals(targetUser.getId());
	}

	/** Returns true when the sprint item matches the selected type filter. */
	private static boolean matchesTypeFilter(final CSprintItem sprintItem, final Class<?> entityClass) {
		if (entityClass == null) {
			return true;
		}
		final ISprintableItem item = sprintItem.getItem();
		return item != null && entityClass.isAssignableFrom(item.getClass());
	}

	private List<CSprintItem> allSprintItems;
	private final CComponentKanbanBoardFilterToolbar filterToolbar;
	private final CHorizontalLayout layoutColumns;
	final CVerticalLayout layoutDetails = new CVerticalLayout();
	private List<CSprint> availableSprints;
	private List<CSprintItem> sprintItems;
	private CSprint currentSprint;
	private CComponentKanbanPostit selectedPostit;
	private final Comparator<CSprint> sprintRecencyComparator;
	private final CKanbanLineService kanbanLineService;
	private final CSprintItemService sprintItemService;
	private final CSprintService sprintService;
	private final ISessionService sessionService;
	protected SplitLayout splitLayout = new SplitLayout();

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
		filterToolbar.addKanbanFilterChangeListener(criteria -> applyFilters());
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
		// splitLayout.setFlexGrow(1, layoutColumns);
		add(filterToolbar, splitLayout);
		expand(splitLayout);
	}

	/** Applies current filters and refreshes the board. */
	private void applyFilters() {
		LOGGER.debug("Applying filters to Kanban board component");
		final CKanbanLine currentLine = getValue();
		Check.notNull(currentLine, "Kanban line must be set before applying filters");
		final CComponentKanbanBoardFilterToolbar.FilterCriteria criteria = filterToolbar.getCurrentCriteria();
		if (!isSameSprint(criteria.getSprint())) {
			currentSprint = criteria.getSprint();
			loadSprintItemsForSprint(currentSprint);
		}
		final List<CSprintItem> filtered = new ArrayList<>();
		for (final CSprintItem sprintItem : allSprintItems) {
			if (sprintItem == null || sprintItem.getItem() == null) {
				continue;
			}
			if (!matchesTypeFilter(sprintItem, criteria.getEntityType())) {
				continue;
			}
			if (!matchesResponsibleFilter(sprintItem, criteria)) {
				continue;
			}
			filtered.add(sprintItem);
		}
		sprintItems = filtered;
		refreshComponent();
	}

	/** Assigns each sprint item to a kanban column id before rendering. */
	private void assignKanbanColumns(final List<CSprintItem> items, final List<CKanbanColumn> columns) {
		LOGGER.debug("Assigning Kanban columns to sprint items for board display");
		if (items == null || items.isEmpty() || columns == null || columns.isEmpty()) {
			return;
		}
		final Map<Long, Long> statusToColumnId = prepareStatusToColumnIdMap(columns);
		for (final CSprintItem sprintItem : items) {
			if (sprintItem == null) {
				continue;
			}
			final ISprintableItem sprintableItem = sprintItem.getItem();
			final Long statusId = sprintableItem.getStatus().getId();
			final Long columnId = statusToColumnId.computeIfAbsent(statusId, key -> statusToColumnId.getOrDefault(-1L, -1L));
			if (columnId == -1L) {
				LOGGER.warn("No kanban column found for status id {} in line {}", statusId, getValue() != null ? getValue().getName() : "null");
				continue;
			}
			sprintItem.setKanbanColumnId(columnId);
		}
	}

	/** Kanban board does not support creating entities here. */
	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		LOGGER.debug("Creating new entity instance is not supported for Kanban board component");
		return null;
	}

	/** Returns the current line id as string. */
	@Override
	public String getCurrentEntityIdString() {
		final CKanbanLine currentLine = getValue();
		if (currentLine == null || currentLine.getId() == null) {
			return null;
		}
		return currentLine.getId().toString();
	}

	/** Kanban board does not expose a direct entity service. */
	@Override
	public CAbstractService<?> getEntityService() { return null; }

	/** Returns true when the sprintable item has workflow and status assigned. */
	private boolean hasStatusAndWorkflow(final ISprintableItem item) {
		if (item == null) {
			return false;
		}
		if (!(item instanceof final IHasStatusAndWorkflow<?> workflowItem)) {
			return false;
		}
		if (item.getStatus() == null || item.getStatus().getId() == null) {
			return false;
		}
		if (workflowItem.getWorkflow() == null || workflowItem.getWorkflow().getId() == null) {
			return false;
		}
		return true;
	}

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
		if (sprint == null || sprint.getId() == null) {
			allSprintItems = new ArrayList<>();
			sprintItems = new ArrayList<>();
			filterToolbar.setAvailableItems(allSprintItems);
			return;
		}
		try {
			// Sprint items already encode the project scope; use them as the single source of truth
			// for the board cards to keep project selection aligned with the sprint filter.
			final List<CSprintItem> sprintItems = sprintItemService.findByMasterIdWithItems(sprint.getId());
			allSprintItems = new ArrayList<>(sprintItems);
			this.sprintItems = new ArrayList<>(allSprintItems);
			filterToolbar.setAvailableItems(allSprintItems);
		} catch (final Exception e) {
			LOGGER.error("Failed to load sprint items for Kanban board", e);
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
		try {
			// Keep sprint selection constrained to the active project and preselect the newest sprint
			// so the board always opens with the freshest work.
			availableSprints = sprintService.listByProject(project);
			availableSprints.sort(sprintRecencyComparator.reversed());
			final CSprint defaultSprint = resolveDefaultSprint(availableSprints);
			filterToolbar.setAvailableSprints(availableSprints, defaultSprint);
			currentSprint = filterToolbar.getCurrentCriteria().getSprint();
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
	private boolean matchesResponsibleFilter(final CSprintItem sprintItem, final CComponentKanbanBoardFilterToolbar.FilterCriteria criteria) {
		LOGGER.debug("Checking responsible filter for Kanban board item {}", sprintItem != null ? sprintItem.getId() : "null");
		final CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode mode = criteria.getResponsibleMode();
		if (mode == null || mode == CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode.ALL) {
			return true;
		}
		if (mode == CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode.CURRENT_USER) {
			final CUser activeUser = sessionService.getActiveUser().orElse(null);
			Check.notNull(activeUser, "Active user not available for Kanban board filtering");
			return matchesResponsibleUser(sprintItem, activeUser);
		}
		return true;
	}

	/** Updates selection state and details area. */
	private void on_postit_selected(final CComponentKanbanPostit postit) {
		if (selectedPostit != null && selectedPostit != postit) {
			selectedPostit.setSelected(false);
		}
		selectedPostit = postit;
		if (selectedPostit != null) {
			selectedPostit.setSelected(true);
		}
		layoutDetails.removeAll();
		layoutDetails.add(new CDiv("Select a card to view its details."));
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

	/** Populates and refreshes the board view. */
	@Override
	public void populateForm() {
		LOGGER.debug("Populating Kanban board component");
		refreshComponent();
	}

	Map<Long, Long> prepareStatusToColumnIdMap(final List<CKanbanColumn> columns) {
		final Map<Long, Long> statusToColumnId = new LinkedHashMap<>();
		for (final CKanbanColumn column : columns) {
			if (column == null || column.getId() == null) {
				continue;
			}
			if (Boolean.TRUE.equals(column.getDefaultColumn())) {
				statusToColumnId.putIfAbsent(-1L, column.getId());
			}
			if (column.getIncludedStatuses() == null) {
				continue;
			}
			for (final var status : column.getIncludedStatuses()) {
				if (status == null || status.getId() == null) {
					continue;
				}
				statusToColumnId.putIfAbsent(status.getId(), column.getId());
			}
		}
		return statusToColumnId;
	}

	/** Rebuilds the column layout with current items. */
	@Override
	public void refreshComponent() {
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
		final List<CKanbanColumn> columns = new ArrayList<>(currentLine.getKanbanColumns());
		columns.sort(Comparator.comparing(CKanbanColumn::getItemOrder, Comparator.nullsLast(Integer::compareTo)));
		assignKanbanColumns(sprintItems, columns);
		for (final CKanbanColumn column : columns) {
			final CComponentKanbanColumn columnComponent = new CComponentKanbanColumn();
			columnComponent.setPostitSelectionListener(this::on_postit_selected);
			columnComponent.setItems(sprintItems);
			columnComponent.setValue(column);
			layoutColumns.add(columnComponent);
		}
		on_postit_selected(null);
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
