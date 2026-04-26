package tech.derbent.plm.sprints.planning.view.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import java.util.Collections;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.filter.CFilterToolbarSupport;
import tech.derbent.api.utils.CSearchTextFilterSupport;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.planning.domain.ESprintPlanningScope;
import tech.derbent.plm.sprints.service.CSprintService;

/**
 * Filter toolbar for the Sprint Planning Board (v2).
 *
 * <p>Compared to {@code CGnntBoardFilterToolbar} we keep the filters deliberately
 * sprint-planning focused: scope (Backlog/Sprint/All), sprint selection,
 * and free-text search.</p>
 */
public class CSprintPlanningFilterToolbar extends CHorizontalLayout {

	public static final String ID_TOOLBAR = "custom-sprint-planning-filter-toolbar";
	public static final String ID_BUTTON_ADD_TO_SPRINT = "custom-sprint-planning-add-to-sprint-button";
	public static final String ID_BUTTON_CLEAR = "custom-sprint-planning-clear-button";
	public static final String ID_COMBOBOX_SPRINT = "custom-sprint-planning-sprint-filter-combobox";
	public static final String ID_SELECTED_SPRINT_METRICS = "custom-sprint-planning-selected-sprint-metrics";
	public static final String ID_BACKLOG_METRICS = "custom-sprint-planning-backlog-metrics";
	private static final long serialVersionUID = 1L;

	private enum EStateFilter {
		ALL,
		ACTIVE,
		CLOSED
	}

	private final CButton buttonAddToSprint;
	private final CButton buttonBacklogAll;
	private final CButton buttonBacklogClosed;
	private final CButton buttonBacklogOpen;
	private final CButton buttonClear;
	private final CButton buttonSprintsAll;
	private final CButton buttonSprintsClosed;
	private final CButton buttonSprintsOpen;
	private final CComboBox<CSprint> comboBoxSprint;
	private final Span spanBacklogMetrics;
	private final Span spanSelectedSprintMetrics;
	private final CTextField searchField;
	private CProject<?> currentProject;
	private boolean internalUpdate;
	private EStateFilter backlogStateFilter = EStateFilter.ALL;
	private EStateFilter sprintStateFilter = EStateFilter.ALL;
	private final List<Consumer<Void>> changeListeners = new ArrayList<>();
	private final CSprintService sprintService;
	private Runnable addToSprintHandler;

	public CSprintPlanningFilterToolbar() {
		sprintService = CSpringContext.getBean(CSprintService.class);
		setId(ID_TOOLBAR);
		CFilterToolbarSupport.configureWrappingToolbar(this, "crud-toolbar");

		searchField = CFilterToolbarSupport.createSearchField("Search", "Search...", null, null, ValueChangeMode.EAGER, 250,
				value -> notifyChangeListeners());
		searchField.setId("custom-sprint-planning-backlog-search-field");

		comboBoxSprint = new CComboBox<>("Sprint");
		comboBoxSprint.setId(ID_COMBOBOX_SPRINT);
		comboBoxSprint.setClearButtonVisible(true);
		// Keep the sprint selector compact when it is hosted in the Gnnt header quick-access panel.
		comboBoxSprint.setWidth("200px");
		comboBoxSprint.setEnabled(true);
		comboBoxSprint.setItemLabelGenerator(sprint -> sprint != null ? sprint.getName() : "");
		comboBoxSprint.addValueChangeListener(event -> notifyChangeListeners());


		// Quick filters: backlog state (active/closed).
		buttonBacklogOpen = createStateButton("Backlog: Active", () -> setBacklogStateFilter(EStateFilter.ACTIVE));
		buttonBacklogClosed = createStateButton("Backlog: Closed", () -> setBacklogStateFilter(EStateFilter.CLOSED));
		buttonBacklogAll = createStateButton("Backlog: All", () -> setBacklogStateFilter(EStateFilter.ALL));

		// Quick filters: sprint state.
		buttonSprintsOpen = createStateButton("Sprints: Active", () -> setSprintStateFilter(EStateFilter.ACTIVE));
		buttonSprintsClosed = createStateButton("Sprints: Closed", () -> setSprintStateFilter(EStateFilter.CLOSED));
		buttonSprintsAll = createStateButton("Sprints: All", () -> setSprintStateFilter(EStateFilter.ALL));

		spanBacklogMetrics = new Span("Backlog: Items: 0 | SP: 0");
		spanBacklogMetrics.setId(ID_BACKLOG_METRICS);
		spanBacklogMetrics.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)")
				.set("padding", "0 6px");

		spanSelectedSprintMetrics = new Span("Sprint: - | Items: 0 | SP: 0");
		spanSelectedSprintMetrics.setId(ID_SELECTED_SPRINT_METRICS);
		spanSelectedSprintMetrics.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)")
				.set("padding", "0 6px");

		buttonAddToSprint = CButton.createTertiary("Add to sprint", VaadinIcon.PLUS.create(), event -> {
			if (addToSprintHandler != null) {
				addToSprintHandler.run();
			}
		});
		buttonAddToSprint.setId(ID_BUTTON_ADD_TO_SPRINT);
		buttonAddToSprint.addThemeVariants(ButtonVariant.LUMO_SMALL);

		buttonClear = CButton.createTertiary("Clear", null, event -> clearFilters());
		buttonClear.setId(ID_BUTTON_CLEAR);
		buttonClear.addThemeVariants(ButtonVariant.LUMO_SMALL);

		// Default state button visuals.
		updateStateButtonStyles();

		// Main toolbar stays compact (Jira-like): sprint selection + core actions.
		// Backlog search belongs next to the backlog parent browser (folder-browser UX).
		add(comboBoxSprint, buttonAddToSprint, buttonClear);
		// Metrics are rendered in the Gnnt quick-access header (see extractQuickControlsForQuickAccess()).
	}

	public void addChangeListener(final Consumer<Void> listener) {
		if (listener != null) {
			changeListeners.add(listener);
		}
	}

	/**
	 * Extracts quick-filter controls so the board can place them into a grid-header quick-access toolbar.
	 *
	 * <p>Vaadin components can only have one parent, so we physically remove them from this toolbar and
	 * return them for re-attachment elsewhere (for example into {@code CQuickAccessPanel}).</p>
	 */
	public List<Component> extractQuickControlsForQuickAccess() {
		// Sprint planning controls belong in the Gnnt header quick-access panel (compact, aligned).
		remove(comboBoxSprint, buttonAddToSprint, buttonClear);
		return Collections.unmodifiableList(
				List.of(comboBoxSprint, buttonAddToSprint, buttonClear, spanSelectedSprintMetrics, spanBacklogMetrics));
	}

	public void clearFilters() {
		internalUpdate = true;
		try {
			searchField.clear();
			comboBoxSprint.clear();
			comboBoxSprint.setEnabled(true);
			backlogStateFilter = EStateFilter.ALL;
			sprintStateFilter = EStateFilter.ALL;
			spanBacklogMetrics.setText("Backlog: Items: 0 | SP: 0");
			spanSelectedSprintMetrics.setText("Sprint: - | Items: 0 | SP: 0");
			updateStateButtonStyles();
		} finally {
			internalUpdate = false;
		}
		notifyChangeListeners();
	}


	public ESprintPlanningScope getScope() {
		// Scope was removed from UI; always return BACKLOG for backward compatibility
		return ESprintPlanningScope.BACKLOG;
	}

	public String getSearchText() {
		return searchField.getValue();
	}

	public CSprint getSelectedSprint() {
		return comboBoxSprint.getValue();
	}

	/**
	 * Moves the backlog parent-browser search out of the main toolbar.
	 *
	 * <p>Vaadin components can only have one parent, so we remove them here and hand them to the backlog browser
	 * panel (folder-browser UX).</p>
	 */
	public List<Component> getBacklogParentBrowserFilterComponents() {
		// Backlog parent browsing only needs text search; type filtering was removed to keep the header compact.
		remove(searchField);
		return List.of(searchField);
	}

	private void notifyChangeListeners() {
		if (internalUpdate) {
			return;
		}
		changeListeners.forEach((final Consumer<Void> listener) -> listener.accept(null));
	}

	@SuppressWarnings("unused")
	public void setAvailableEntityTypes(final List<CGnntItem> items) {
		// Legacy hook: entity type filtering was removed from the sprint planning board.
	}

	public void setProject(final CProject<?> project) {
		if (currentProject != null && project != null && currentProject.getId() != null && currentProject.getId().equals(project.getId())) {
			return;
		}
		currentProject = project;
		internalUpdate = true;
		try {
			final List<CSprint> sprints = currentProject != null ? sprintService.listByProject(currentProject) : List.of();
			comboBoxSprint.setItems(sprints);
			comboBoxSprint.clear();
		} finally {
			internalUpdate = false;
		}
	}

	public void setAddToSprintHandler(final Runnable handler) {
		addToSprintHandler = handler;
	}

	public void setSelectedSprintMetrics(final CSprint sprint, final int itemCount, final long storyPoints) {
		final String sprintName = sprint != null ? sprint.getName() : "-";
		final Integer velocity = sprint != null ? sprint.getVelocity() : null;
		final boolean showVelocity = velocity != null && velocity > 0;
		final boolean overloaded = showVelocity && storyPoints > velocity;

		// Agile-friendly metric: planned SP vs historical velocity (warning if overloaded).
		final String text = showVelocity
				? "Sprint: %s | Items: %d | SP: %d/%d".formatted(sprintName, itemCount, storyPoints, velocity)
				: "Sprint: %s | Items: %d | SP: %d".formatted(sprintName, itemCount, storyPoints);
		spanSelectedSprintMetrics.setText(text);
		spanSelectedSprintMetrics.getStyle().set("color", overloaded ? "var(--lumo-error-text-color)" : "var(--lumo-secondary-text-color)");
	}

	public void setBacklogMetrics(final int itemCount, final long storyPoints) {
		spanBacklogMetrics.setText("Backlog: Items: %d | SP: %d".formatted(itemCount, storyPoints));
	}

	private CButton createStateButton(final String label, final Runnable onClick) {
		final CButton button = CButton.createTertiary(label, null, event -> {
			if (onClick != null) {
				onClick.run();
			}
		});
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		return button;
	}

	private void setBacklogStateFilter(final EStateFilter filter) {
		backlogStateFilter = filter != null ? filter : EStateFilter.ALL;
		updateStateButtonStyles();
		notifyChangeListeners();
	}

	private void setSprintStateFilter(final EStateFilter filter) {
		sprintStateFilter = filter != null ? filter : EStateFilter.ALL;
		updateStateButtonStyles();
		notifyChangeListeners();
	}

	private void updateStateButtonStyles() {
		applyActiveStyle(buttonBacklogOpen, backlogStateFilter == EStateFilter.ACTIVE);
		applyActiveStyle(buttonBacklogClosed, backlogStateFilter == EStateFilter.CLOSED);
		applyActiveStyle(buttonBacklogAll, backlogStateFilter == EStateFilter.ALL);

		applyActiveStyle(buttonSprintsOpen, sprintStateFilter == EStateFilter.ACTIVE);
		applyActiveStyle(buttonSprintsClosed, sprintStateFilter == EStateFilter.CLOSED);
		applyActiveStyle(buttonSprintsAll, sprintStateFilter == EStateFilter.ALL);
	}

	private void applyActiveStyle(final CButton button, final boolean active) {
		if (button == null) {
			return;
		}
		if (active) {
			button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		} else {
			button.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
		}
	}

	private boolean isClosed(final CProjectItem<?> item) {
		final CProjectItemStatus status = item != null ? item.getStatus() : null;
		return status != null && Boolean.TRUE.equals(status.getFinalStatus());
	}

	private boolean isClosed(final CSprint sprint) {
		final CProjectItemStatus status = sprint != null ? sprint.getStatus() : null;
		return status != null && Boolean.TRUE.equals(status.getFinalStatus());
	}

	public boolean shouldIncludeItem(final CProjectItem<?> item) {
		if (item == null) {
			return false;
		}
		// Shared search behaviour with Gnnt timeline filtering: null-safe + trim/lowercase match.
		return CSearchTextFilterSupport.matches(getSearchText(), item.getName(), item.getDescription());
	}

	public boolean shouldIncludeBacklogItem(final CProjectItem<?> item) {
		if (!shouldIncludeItem(item)) {
			return false;
		}
		if (backlogStateFilter == EStateFilter.ALL) {
			return true;
		}
		final boolean closed = isClosed(item);
		return backlogStateFilter == EStateFilter.CLOSED ? closed : !closed;
	}

	public boolean shouldIncludeSprint(final CSprint sprint) {
		if (sprint == null) {
			return false;
		}
		if (sprintStateFilter == EStateFilter.ALL) {
			return true;
		}
		final boolean closed = isClosed(sprint);
		return sprintStateFilter == EStateFilter.CLOSED ? closed : !closed;
	}
}
