package tech.derbent.app.kanban.kanbanline.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;
import tech.derbent.app.sprints.service.CSprintItemService;
import tech.derbent.app.sprints.service.CSprintService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CComponentKanbanBoard - Displays a kanban line as a board with vertical columns and post-it style project items. */
public class CComponentKanbanBoard extends CComponentBase<CKanbanLine> implements IContentOwner {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanBoard.class);
	private static final long serialVersionUID = 1L;

        private static boolean matchesResponsibleUser(final CProjectItem<?> item, final CUser targetUser) {
                if (item.getResponsible() == null || item.getResponsible().getId() == null || targetUser.getId() == null) {
                        return false;
                }
                return item.getResponsible().getId().equals(targetUser.getId());
        }

        private static boolean matchesTypeFilter(final CProjectItem<?> item, final Class<?> entityClass) {
                if (entityClass == null) {
			return true;
		}
		return entityClass.isAssignableFrom(item.getClass());
	}

	private List<CProjectItem<?>> allProjectItems;
	private final CComponentKanbanBoardFilterToolbar filterToolbar;
	private final CHorizontalLayout layoutColumns;
	final CVerticalLayout layoutDetails = new CVerticalLayout();
	private List<CSprint> availableSprints;
	private List<CProjectItem<?>> projectItems;
	private CSprint currentSprint;
	private final CSprintItemService sprintItemService;
	private final CSprintService sprintService;
	private final ISessionService sessionService;
	protected SplitLayout splitLayout = new SplitLayout();

	public CComponentKanbanBoard() {
		LOGGER.debug("Initializing Kanban board component");
		sessionService = CSpringContext.getBean(ISessionService.class);
		sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		sprintService = CSpringContext.getBean(CSprintService.class);
		Check.notNull(sessionService, "Session service cannot be null for Kanban board");
		Check.notNull(sprintItemService, "Sprint item service cannot be null for Kanban board");
		Check.notNull(sprintService, "Sprint service cannot be null for Kanban board");
		allProjectItems = new ArrayList<>();
		availableSprints = new ArrayList<>();
		projectItems = new ArrayList<>();
		layoutColumns = new CHorizontalLayout();
		layoutColumns.setSizeFull();
		layoutColumns.setSpacing(true);
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

	private void applyFilters() {
		LOGGER.debug("Applying filters to Kanban board component");
		final CKanbanLine currentLine = getValue();
		Check.notNull(currentLine, "Kanban line must be set before applying filters");
		final CComponentKanbanBoardFilterToolbar.FilterCriteria criteria = filterToolbar.getCurrentCriteria();
		if (!isSameSprint(criteria.getSprint(), currentSprint)) {
			currentSprint = criteria.getSprint();
			loadProjectItemsForSprint(currentSprint);
		}
		final List<CProjectItem<?>> filtered = new ArrayList<>();
		for (final CProjectItem<?> item : allProjectItems) {
			if (item == null) {
				continue;
			}
			if (!matchesTypeFilter(item, criteria.getEntityType())) {
				continue;
			}
			if (!matchesResponsibleFilter(item, criteria)) {
				continue;
			}
			filtered.add(item);
		}
		projectItems = filtered;
		refreshComponent();
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		LOGGER.debug("Creating new entity instance is not supported for Kanban board component");
		return null;
	}

	@Override
	public String getCurrentEntityIdString() {
		final CKanbanLine currentLine = getValue();
		if (currentLine == null || currentLine.getId() == null) {
			return null;
		}
		return currentLine.getId().toString();
	}

	@Override
	public CAbstractService<?> getEntityService() { return null; }

	private boolean isSameSprint(final CSprint first, final CSprint second) {
		if (first == null && second == null) {
			return true;
		}
		if (first == null || second == null) {
			return false;
		}
		if (first.getId() != null && second.getId() != null) {
			return first.getId().equals(second.getId());
		}
		return Objects.equals(first, second);
	}

	private void loadProjectItemsForSprint(final CSprint sprint) {
		if (sprint == null || sprint.getId() == null) {
			allProjectItems = new ArrayList<>();
			projectItems = new ArrayList<>();
			filterToolbar.setAvailableItems(allProjectItems);
			return;
		}
		try {
			final List<CSprintItem> sprintItems = sprintItemService.findByMasterIdWithItems(sprint.getId());
			final List<CProjectItem<?>> sprintProjectItems = sprintItems.stream().map(CSprintItem::getItem).filter(Objects::nonNull)
					.filter(CProjectItem.class::isInstance).map(item -> (CProjectItem<?>) item).collect(Collectors.toList());
			allProjectItems = new ArrayList<>(sprintProjectItems);
			projectItems = new ArrayList<>(allProjectItems);
			filterToolbar.setAvailableItems(allProjectItems);
		} catch (final Exception e) {
			LOGGER.error("Failed to load sprint items for Kanban board", e);
			allProjectItems = new ArrayList<>();
			projectItems = new ArrayList<>();
			filterToolbar.setAvailableItems(allProjectItems);
		}
	}

        private void loadSprintsForActiveProject() {
                availableSprints = new ArrayList<>();
                final CProject project = sessionService.getActiveProject().orElse(null);
		if (project == null) {
			filterToolbar.setAvailableSprints(List.of(), null);
			filterToolbar.setAvailableItems(List.of());
			allProjectItems = new ArrayList<>();
			projectItems = new ArrayList<>();
			currentSprint = null;
			return;
		}
		try {
			availableSprints = sprintService.listByProject(project);
			availableSprints.sort(sprintRecencyComparator());
			final CSprint defaultSprint = resolveDefaultSprint(availableSprints);
			filterToolbar.setAvailableSprints(availableSprints, defaultSprint);
			currentSprint = filterToolbar.getCurrentCriteria().getSprint();
			loadProjectItemsForSprint(currentSprint);
		} catch (final Exception e) {
			LOGGER.error("Failed to load sprints for Kanban board", e);
			filterToolbar.setAvailableSprints(List.of(), null);
			filterToolbar.setAvailableItems(List.of());
			allProjectItems = new ArrayList<>();
			projectItems = new ArrayList<>();
			currentSprint = null;
		}
	}

        private boolean matchesResponsibleFilter(final CProjectItem<?> item, final CComponentKanbanBoardFilterToolbar.FilterCriteria criteria) {
                LOGGER.debug("Checking responsible filter for Kanban board item {}", item != null ? item.getId() : "null");
                final CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode mode = criteria.getResponsibleMode();
                if (mode == null || mode == CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode.ALL) {
                        return true;
                }
                if (mode == CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode.CURRENT_USER) {
                        final CUser activeUser = sessionService.getActiveUser().orElse(null);
                        Check.notNull(activeUser, "Active user not available for Kanban board filtering");
                        return matchesResponsibleUser(item, activeUser);
                }
                return true;
        }

	private void on_postit_selected() {
		layoutDetails.removeAll();
		layoutDetails.add(new CDiv("Select a card to view its details."));
	}

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
	public void populateForm() {
		LOGGER.debug("Populating Kanban board component");
		refreshComponent();
	}

	@Override
	public void refreshComponent() {
		LOGGER.debug("Refreshing Kanban board component");
		layoutColumns.removeAll();
		final CKanbanLine currentLine = getValue();
		if (currentLine == null) {
			final CDiv div = new CDiv("Select a Kanban line to display its board.");
			div.addClassName("kanban-board-placeholder");
			layoutColumns.add(div);
			return;
		}
		final List<CKanbanColumn> columns = new ArrayList<>(currentLine.getKanbanColumns());
		columns.sort(Comparator.comparing(CKanbanColumn::getItemOrder, Comparator.nullsLast(Integer::compareTo)));
		for (final CKanbanColumn column : columns) {
			final CComponentKanbanColumn columnComponent = new CComponentKanbanColumn();
			columnComponent.setItems(projectItems);
			columnComponent.setValue(column);
			layoutColumns.add(columnComponent);
		}
		on_postit_selected();
	}

	private CSprint resolveDefaultSprint(final List<CSprint> sprints) {
		return sprints.stream().max(sprintRecencyComparator()).orElse(null);
	}

	public void setProjectItems(final List<CProjectItem<?>> projectItems) {
		LOGGER.debug("Setting project items for Kanban board component");
		Check.notNull(getValue(), "Kanban line must be set before setting project items");
		Check.notNull(projectItems, "Project items cannot be null for kanban board");
		allProjectItems = new ArrayList<>(projectItems);
		filterToolbar.setAvailableItems(allProjectItems);
		applyFilters();
	}

	@Override
	public void setValue(CEntityDB<?> entity) {
		super.setValue((CKanbanLine) entity);
	}

        private Comparator<CSprint> sprintRecencyComparator() {
                return Comparator.<CSprint, LocalDateTime>comparing(CSprint::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed()
                                .thenComparing(CSprint::getStartDate, Comparator.nullsLast(LocalDate::compareTo)).reversed()
                                .thenComparing(CSprint::getCreatedDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed()
                                .thenComparing(CSprint::getId, Comparator.nullsLast(Long::compareTo)).reversed();
        }
}
