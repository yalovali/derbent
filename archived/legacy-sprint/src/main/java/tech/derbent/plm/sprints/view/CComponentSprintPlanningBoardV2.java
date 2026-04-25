package tech.derbent.plm.sprints.view;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBacklog;
import tech.derbent.api.ui.component.enhanced.CComponentListSprintItems;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.meetings.service.CMeetingService;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;
import tech.derbent.plm.sprints.service.CSprintItemService;
import tech.derbent.plm.sprints.service.CSprintService;

/**
 * Sprint Planning Board (v2).
 * <p>
 * Goals:
 * <ul>
 * <li>Provide a single planning surface: backlog (left) + sprint timeline (right) + sprint items (bottom-right).</li>
 * <li>Support drag/drop backlog → sprint and sprint → backlog without touching the existing Sprint Editing screen.</li>
 * <li>Keep implementation minimal by reusing existing components and the {@link ISprintableItem} move helpers.</li>
 * </ul>
 * </p>
 */
public class CComponentSprintPlanningBoardV2 extends CComponentBase<CSprint> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentSprintPlanningBoardV2.class);
	private static final long serialVersionUID = 1L;

	private final ISessionService sessionService;
	private final CSprintService sprintService;

	private CProject<?> currentProject;
	private CComponentBacklog componentBacklog;
	private CGrid<CSprint> gridSprints;
	private CComponentListSprintItems componentSprintItems;
	private TextField fieldSprintFilter;

	// Drag state shared across multiple grids (Vaadin drag/drop does not automatically provide the payload).
	private ISprintableItem draggedItem;
	private boolean draggedFromBacklog;

	public CComponentSprintPlanningBoardV2(final ISessionService sessionService) {
		Check.notNull(sessionService, "sessionService cannot be null");
		this.sessionService = sessionService;
		this.sprintService = CSpringContext.getBean(CSprintService.class);
		setSizeFull();
		setSpacing(false);
		getStyle().set("gap", "12px");
	}

	@Override
	protected void onValueChanged(final CSprint oldValue, final CSprint newValue, final boolean fromClient) {
		// The board is bound to the selected sprint so it can reuse project context and highlight the current sprint.
		if (!Objects.equals(oldValue, newValue)) {
			refreshComponent();
		}
	}

	@Override
	protected void refreshComponent() {
		try {
			ensureUiBuiltForCurrentProject();
			refreshSprintList();
			refreshSprintItemsSelection();
			if (componentBacklog != null) {
				componentBacklog.refreshGrid();
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to refresh Sprint Planning Board v2", e);
			CNotificationService.showException("Failed to refresh Sprint Planning Board v2", e);
		}
	}

	private void ensureUiBuiltForCurrentProject() {
		final CSprint selectedSprint = getValue();
		final CProject<?> project = selectedSprint != null ? selectedSprint.getProject() : sessionService.getActiveProject().orElse(null);
		if (project == null) {
			removeAll();
			final Div placeholder = new Div();
			placeholder.setText("Select a sprint to use Sprint Planning Board (v2).");
			placeholder.getStyle().set("color", "#666");
			add(placeholder);
			currentProject = null;
			return;
		}
		if (currentProject != null && currentProject.getId() != null && project.getId() != null
				&& currentProject.getId().equals(project.getId()) && componentBacklog != null && gridSprints != null
				&& componentSprintItems != null) {
			return;
		}
		currentProject = project;
		removeAll();

		// Left: backlog (compact mode to keep focus on drag/drop priority + story points).
		componentBacklog = new CComponentBacklog(project, true);
		configureBacklogDragAndDrop();

		// Right-top: sprints timeline grid.
		final CVerticalLayout layoutRight = new CVerticalLayout();
		layoutRight.setSizeFull();
		layoutRight.setSpacing(false);
		layoutRight.getStyle().set("gap", "8px");
		layoutRight.getStyle().set("min-width", "0"); // allow grid to shrink inside split panes

		fieldSprintFilter = new TextField("Sprint Filter");
		fieldSprintFilter.setPlaceholder("Filter by sprint name...");
		fieldSprintFilter.setValueChangeMode(ValueChangeMode.EAGER);
		fieldSprintFilter.addValueChangeListener(e -> refreshSprintList());
		fieldSprintFilter.setWidthFull();

		gridSprints = createSprintsGrid();
		layoutRight.add(fieldSprintFilter, gridSprints);
		layoutRight.setFlexGrow(1, gridSprints);

		// Right-bottom: items in selected sprint (reuses existing component to keep ordering and selection behavior consistent).
		final CSprintItemService sprintItemService = CSpringContext.getBean(CSprintItemService.class);
		final CActivityService activityService = CSpringContext.getBean(CActivityService.class);
		final CMeetingService meetingService = CSpringContext.getBean(CMeetingService.class);
		componentSprintItems = new CComponentListSprintItems(sprintItemService, activityService, meetingService);
		componentSprintItems.setSizeFull();
		configureSprintItemsDragAndDrop();
		layoutRight.add(componentSprintItems);
		layoutRight.setFlexGrow(1, componentSprintItems);

		// Wrap the backlog component so we can control width; CComponentBacklog itself is a Composite and does not expose HasSize.
		final CVerticalLayout layoutBacklog = new CVerticalLayout();
		layoutBacklog.setPadding(false);
		layoutBacklog.setSpacing(false);
		layoutBacklog.getStyle().set("min-width", "0");
		layoutBacklog.add(componentBacklog);
		layoutBacklog.setFlexGrow(1, componentBacklog);
		layoutBacklog.setWidth("40%");
		layoutRight.setWidth("60%");

		final CHorizontalLayout layoutMain = new CHorizontalLayout(layoutBacklog, layoutRight);
		layoutMain.setSizeFull();
		layoutMain.setSpacing(false);
		layoutMain.getStyle().set("gap", "12px");
		add(layoutMain);
		setFlexGrow(1, layoutMain);
	}

	private void configureBacklogDragAndDrop() {
		final Grid<?> backlogGrid = componentBacklog.getGrid();
		if (backlogGrid == null) {
			return;
		}
		backlogGrid.setRowsDraggable(true);
		backlogGrid.addDragStartListener(event -> {
			if (event.getDraggedItems().isEmpty()) {
				return;
			}
			final Object item = event.getDraggedItems().get(0);
			if (item instanceof ISprintableItem) {
				draggedItem = (ISprintableItem) item;
				draggedFromBacklog = true;
			}
		});

		// Optional: allow dropping sprint items into backlog to remove them from sprint.
		backlogGrid.setDropMode(GridDropMode.ON_TOP);
		backlogGrid.addDropListener(event -> {
			if (draggedItem == null || draggedFromBacklog) {
				return;
			}
			handleDropToBacklog(draggedItem);
		});
	}

	private void configureSprintItemsDragAndDrop() {
		final Grid<?> sprintItemsGrid = componentSprintItems.getGrid();
		if (sprintItemsGrid == null) {
			return;
		}
		sprintItemsGrid.setRowsDraggable(true);
		sprintItemsGrid.addDragStartListener(event -> {
			if (event.getDraggedItems().isEmpty()) {
				return;
			}
			final Object item = event.getDraggedItems().get(0);
			if (item instanceof CSprintItem) {
				final CSprintItem sprintItem = (CSprintItem) item;
				if (sprintItem.getParentItem() instanceof ISprintableItem) {
					draggedItem = (ISprintableItem) sprintItem.getParentItem();
					draggedFromBacklog = false;
				}
			}
		});
	}

	private CGrid<CSprint> createSprintsGrid() {
		final CGrid<CSprint> grid = new CGrid<>(CSprint.class);
		grid.setHeight("280px");
		grid.setWidthFull();
		grid.getStyle().set("min-width", "0");

		grid.addExpandingShortTextColumn(CSprint::getName, "Sprint", "name");
		grid.addShortTextColumn(sprint -> {
			final LocalDate start = sprint.getStartDate();
			final LocalDate end = sprint.getEndDate();
			if (start == null || end == null) {
				return "";
			}
			return "%s → %s".formatted(start, end);
		}, "Dates", "dates");

		// Timeline bar gives a quick visual of sprint placement without introducing a second Gnnt board.
		grid.addComponentColumn(this::createTimelineBarCell).setHeader("Timeline").setKey("timeline").setAutoWidth(true)
				.setFlexGrow(0);

		grid.setDropMode(GridDropMode.ON_TOP);
		grid.addDropListener(event -> handleDropToSprint(event.getDropTargetItem()));

		grid.asSingleSelect().addValueChangeListener(e -> {
			if (e.getValue() != null) {
				componentSprintItems.setValue(e.getValue());
			}
		});
		return grid;
	}

	private Component createTimelineBarCell(final CSprint sprint) {
		final Div container = new Div();
		container.getStyle().set("position", "relative").set("width", "220px").set("height", "10px")
				.set("background", "#EEE").set("border-radius", "6px");

		final LocalDate sprintStart = sprint.getStartDate();
		final LocalDate sprintEnd = sprint.getEndDate();
		final LocalDate minStart = getMinSprintStartDate();
		final LocalDate maxEnd = getMaxSprintEndDate();
		if (sprintStart == null || sprintEnd == null || minStart == null || maxEnd == null) {
			return container;
		}

		final long totalDays = Math.max(1, ChronoUnit.DAYS.between(minStart, maxEnd) + 1);
		final long startOffset = Math.max(0, ChronoUnit.DAYS.between(minStart, sprintStart));
		final long duration = Math.max(1, ChronoUnit.DAYS.between(sprintStart, sprintEnd) + 1);

		final double leftPct = Math.min(100.0, (startOffset * 100.0) / totalDays);
		final double widthPct = Math.min(100.0 - leftPct, (duration * 100.0) / totalDays);

		final Div bar = new Div();
		bar.getStyle().set("position", "absolute").set("left", "%.2f%%".formatted(leftPct)).set("width", "%.2f%%".formatted(widthPct))
				.set("top", "0").set("bottom", "0").set("background", sprint.getColor() != null ? sprint.getColor() : "#8377C5")
				.set("border-radius", "6px");
		container.add(bar);
		return container;
	}

	private LocalDate getMaxSprintEndDate() {
		if (gridSprints == null) {
			return null;
		}
		return gridSprints.getListDataView().getItems().map(CSprint::getEndDate).filter(Objects::nonNull).max(Comparator.naturalOrder())
				.orElse(null);
	}

	private LocalDate getMinSprintStartDate() {
		if (gridSprints == null) {
			return null;
		}
		return gridSprints.getListDataView().getItems().map(CSprint::getStartDate).filter(Objects::nonNull).min(Comparator.naturalOrder())
				.orElse(null);
	}

	private void handleDropToBacklog(final ISprintableItem item) {
		try {
			LOGGER.debug("[SprintBoardV2] Moving item {} to backlog", item.getId());
			item.moveSprintItemToBacklog();
			refreshComponent();
			CNotificationService.showSuccess("Moved item to backlog");
		} catch (final Exception e) {
			LOGGER.error("[SprintBoardV2] Failed to move item to backlog", e);
			CNotificationService.showException("Failed to move item to backlog", e);
		}
	}

	private void handleDropToSprint(final Optional<CSprint> targetSprintOptional) {
		try {
			if (draggedItem == null) {
				return;
			}
			final CSprint targetSprint = targetSprintOptional.orElse(null);
			if (targetSprint == null || targetSprint.getId() == null) {
				return;
			}
			LOGGER.debug("[SprintBoardV2] Moving item {} to sprint {}", draggedItem.getId(), targetSprint.getId());
			draggedItem.moveSprintItemToSprint(targetSprint);
			refreshComponent();
			CNotificationService.showSuccess("Moved item to sprint '%s'".formatted(targetSprint.getName()));
		} catch (final Exception e) {
			LOGGER.error("[SprintBoardV2] Failed to move item to sprint", e);
			CNotificationService.showException("Failed to move item to sprint", e);
		}
	}

	private void refreshSprintItemsSelection() {
		if (componentSprintItems == null) {
			return;
		}
		final CSprint selectedSprint = gridSprints != null ? gridSprints.asSingleSelect().getValue() : null;
		final CSprint sprint = selectedSprint != null ? selectedSprint : getValue();
		if (sprint != null) {
			componentSprintItems.setValue(sprint);
		}
	}

	private void refreshSprintList() {
		if (gridSprints == null || currentProject == null) {
			return;
		}
		final List<CSprint> sprints = new ArrayList<>(sprintService.listByProject(currentProject));
		sprints.sort(Comparator.comparing(CSprint::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())));
		final String filter = fieldSprintFilter != null ? fieldSprintFilter.getValue() : "";
		final String normalizedFilter = filter != null ? filter.trim().toLowerCase() : "";
		if (!normalizedFilter.isBlank()) {
			sprints.removeIf(sprint -> sprint.getName() == null || !sprint.getName().toLowerCase().contains(normalizedFilter));
		}
		gridSprints.setItems(sprints);
		// If the current bean sprint is still in the filtered list, keep it selected.
		final CSprint beanSprint = getValue();
		if (beanSprint != null) {
			final Optional<CSprint> match = sprints.stream().filter(s -> s.getId() != null && s.getId().equals(beanSprint.getId())).findFirst();
			match.ifPresent(value -> gridSprints.asSingleSelect().setValue(value));
		}
	}
}
