package tech.derbent.plm.sprints.planning.view.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import java.util.Collections;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.value.ValueChangeMode;

import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
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
	public static final String ID_COMBOBOX_SPRINT_STATUS = "custom-sprint-planning-sprint-status-filter-combobox";
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
	private final CComboBox<String> comboBoxSprintStatus;
	private final Span spanSelectedSprintMetrics;
	private final CTextField backlogParentSearchField;
	private final CTextField backlogLeafSearchField;
	private final CTextField sprintSearchField;
	private CProject<?> currentProject;
	private boolean internalUpdate;
	private EStateFilter backlogStateFilter = EStateFilter.ALL;
	private EStateFilter sprintStateFilter = EStateFilter.ALL;
	private final List<Consumer<Void>> changeListeners = new ArrayList<>();
	private final CSprintService sprintService;
	private final CProjectItemStatusService projectItemStatusService;
	private Runnable addToSprintHandler;

	public CSprintPlanningFilterToolbar() {
		sprintService = CSpringContext.getBean(CSprintService.class);
		projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		setId(ID_TOOLBAR);
		CFilterToolbarSupport.configureWrappingToolbar(this, "crud-toolbar");

		backlogParentSearchField = CFilterToolbarSupport.createSearchField("Search", "Search...", null, null, ValueChangeMode.EAGER, 200,
				value -> notifyChangeListeners());
		backlogParentSearchField.setId("custom-sprint-planning-backlog-parent-search-field");
		backlogParentSearchField.getStyle().set("min-width", "0");

		backlogLeafSearchField = CFilterToolbarSupport.createSearchField("Search", "Search...", null, null, ValueChangeMode.EAGER, 200,
				value -> notifyChangeListeners());
		backlogLeafSearchField.setId("custom-sprint-planning-backlog-leaf-search-field");
		backlogLeafSearchField.getStyle().set("min-width", "0");

		sprintSearchField = CFilterToolbarSupport.createSearchField("Search", "Search...", null, null, ValueChangeMode.EAGER, 200,
				value -> notifyChangeListeners());
		sprintSearchField.setId("custom-sprint-planning-sprint-search-field");
		sprintSearchField.getStyle().set("min-width", "0");

		comboBoxSprint = new CComboBox<>("Sprint");
		comboBoxSprint.setId(ID_COMBOBOX_SPRINT);
		comboBoxSprint.setClearButtonVisible(true);
		// Keep the sprint selector compact when it is hosted in the Gnnt header quick-access panel.
		comboBoxSprint.setWidth("200px");
		comboBoxSprint.setEnabled(true);
		comboBoxSprint.setItemLabelGenerator(sprint -> sprint != null ? sprint.getName() : "");
		comboBoxSprint.addValueChangeListener(event -> notifyChangeListeners());

		comboBoxSprintStatus = new CComboBox<>("Sprint Status");
		comboBoxSprintStatus.setId(ID_COMBOBOX_SPRINT_STATUS);
		comboBoxSprintStatus.setClearButtonVisible(true);
		comboBoxSprintStatus.setWidth("200px");
		comboBoxSprintStatus.addValueChangeListener(event -> notifyChangeListeners());

		// Quick filters: backlog state (active/closed).
		buttonBacklogOpen = createStateButton("Backlog: Active", () -> setBacklogStateFilter(EStateFilter.ACTIVE));
		buttonBacklogClosed = createStateButton("Backlog: Closed", () -> setBacklogStateFilter(EStateFilter.CLOSED));
		buttonBacklogAll = createStateButton("Backlog: All", () -> setBacklogStateFilter(EStateFilter.ALL));

		// Quick filters: sprint state.
		buttonSprintsOpen = createStateButton("Sprints: Active", () -> setSprintStateFilter(EStateFilter.ACTIVE));
		buttonSprintsClosed = createStateButton("Sprints: Closed", () -> setSprintStateFilter(EStateFilter.CLOSED));
		buttonSprintsAll = createStateButton("Sprints: All", () -> setSprintStateFilter(EStateFilter.ALL));

		spanSelectedSprintMetrics = new Span("Sprint: - | 0/0 tasks, 0/0 SP");
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
		buttonClear.setIcon(VaadinIcon.CLOSE_SMALL.create());
		buttonClear.addThemeVariants(ButtonVariant.LUMO_SMALL);

		// Default state button visuals.
		updateStateButtonStyles();

		// Main toolbar stays compact (Jira-like): sprint selection + core actions.
		// Backlog search belongs next to the backlog parent browser (folder-browser UX).
		add(comboBoxSprint, comboBoxSprintStatus, buttonAddToSprint, buttonClear);
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
		final List<Component> controls = List.of(comboBoxSprint, comboBoxSprintStatus, sprintSearchField, buttonAddToSprint, buttonClear,
				spanSelectedSprintMetrics);
		controls.forEach(control -> control.getElement().removeFromParent());

		prepareForQuickAccessControls();
		return Collections.unmodifiableList(controls);
	}

	private void prepareForQuickAccessControls() {
		// The quick-access header is compact; remove labels and rely on placeholders + tooltips.
		comboBoxSprint.setLabel("");
		comboBoxSprint.setPlaceholder("Sprint");
		comboBoxSprint.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		comboBoxSprint.setWidth("160px");
		comboBoxSprint.getStyle().set("min-width", "0");

		comboBoxSprintStatus.setLabel("");
		comboBoxSprintStatus.setPlaceholder("Status");
		comboBoxSprintStatus.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		comboBoxSprintStatus.setWidth("160px");
		comboBoxSprintStatus.getStyle().set("min-width", "0");

		makeIconOnly(buttonAddToSprint, "Add to sprint");
		makeIconOnly(buttonClear, "Clear");

		sprintSearchField.setLabel("");
		sprintSearchField.setPlaceholder("Search");
		sprintSearchField.setWidth("180px");
		sprintSearchField.getStyle().set("min-width", "0");

		spanSelectedSprintMetrics.getStyle().set("white-space", "nowrap");
	}

	private void makeIconOnly(final CButton button, final String label) {
		if (button == null) {
			return;
		}
		// Icon-only buttons must not keep the global text-button min-width.
		button.setText("");
		button.addThemeVariants(ButtonVariant.LUMO_ICON);
		button.getStyle().remove("min-width");
		button.getStyle().set("padding", "var(--lumo-space-xs)");
		button.getElement().setAttribute("aria-label", label);
		button.getElement().setAttribute("title", label);
	}

	public void clearFilters() {
		internalUpdate = true;
		try {
			backlogParentSearchField.clear();
			backlogLeafSearchField.clear();
			sprintSearchField.clear();
			comboBoxSprint.clear();
			comboBoxSprintStatus.clear();
			comboBoxSprint.setEnabled(true);
			backlogStateFilter = EStateFilter.ALL;
			sprintStateFilter = EStateFilter.ALL;
			spanSelectedSprintMetrics.setText("Sprint: - | 0/0 tasks, 0/0 SP");
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

	public String getBacklogParentSearchText() {
		return backlogParentSearchField.getValue();
	}

	public String getBacklogLeafSearchText() {
		return backlogLeafSearchField.getValue();
	}

	public String getSprintSearchText() {
		return sprintSearchField.getValue();
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
		// Backlog parent browsing has its own search so it does not filter sprint/leaf grids.
		backlogParentSearchField.setLabel("");
		backlogParentSearchField.setPlaceholder("Search parents");
		remove(backlogParentSearchField);
		return List.of(backlogParentSearchField);
	}

	public List<Component> getBacklogLeafFilterComponents() {
		// Leaf backlog search is attached to the leaf quick-access header.
		backlogLeafSearchField.setLabel("");
		backlogLeafSearchField.setPlaceholder("Search backlog");
		return List.of(backlogLeafSearchField);
	}

	public List<Component> getSprintFilterComponents() {
		// Sprint search is attached to the sprint quick-access header.
		sprintSearchField.setLabel("");
		sprintSearchField.setPlaceholder("Search sprint");
		return List.of(sprintSearchField);
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
		// Always refresh available sprints/statuses; sample initializers and workflow changes can add options after first load.
		currentProject = project;
		internalUpdate = true;
		try {
			final Long selectedId = comboBoxSprint.getValue() != null ? comboBoxSprint.getValue().getId() : null;
			final String selectedStatus = comboBoxSprintStatus.getValue();
			final List<CSprint> sprints = currentProject != null ? sprintService.listByProject(currentProject) : List.of();
			comboBoxSprint.setItems(sprints);

			// Sprint status filtering should show all configured project-item statuses (not just those currently used by existing sprint rows).
			// This avoids confusing "only one status" dropdowns in fresh sample projects.
			final List<String> statusNames = currentProject != null
					? projectItemStatusService.listByCompany(currentProject.getCompany()).stream()
							.map(CProjectItemStatus::getName)
							.filter(status -> status != null && !status.isBlank())
							.distinct()
							.sorted(String.CASE_INSENSITIVE_ORDER)
							.toList()
					: List.of();
			comboBoxSprintStatus.setItems(statusNames);
			comboBoxSprintStatus.setValue(statusNames.contains(selectedStatus) ? selectedStatus : null);

			final CSprint preserved = selectedId != null
					? sprints.stream().filter(sprint -> selectedId.equals(sprint.getId())).findFirst().orElse(null)
					: null;
			comboBoxSprint.setValue(preserved);
		} finally {
			internalUpdate = false;
		}
	}

	public void setAddToSprintHandler(final Runnable handler) {
		addToSprintHandler = handler;
	}

	public void setSelectedSprintMetrics(final CSprint sprint, final CSprintPlanningSprintMetrics metrics) {
		final String sprintName = sprint != null ? sprint.getName() : "-";
		final CSprintPlanningSprintMetrics safeMetrics = metrics != null ? metrics : new CSprintPlanningSprintMetrics(0, 0, 0, 0);
		final Integer velocity = sprint != null ? sprint.getVelocity() : null;
		final boolean showVelocity = velocity != null && velocity > 0;
		final boolean overloaded = showVelocity && safeMetrics.storyPointsTotal() > velocity;

		// Agile-friendly metric: done/total + planned SP vs historical velocity (warning if overloaded).
		final String text = showVelocity
				? "Sprint: %s | %s | Velocity: %d".formatted(sprintName, safeMetrics.formatRollup(), velocity)
				: "Sprint: %s | %s".formatted(sprintName, safeMetrics.formatRollup());
		spanSelectedSprintMetrics.setText(text);
		spanSelectedSprintMetrics.getStyle().set("color", overloaded ? "var(--lumo-error-text-color)" : "var(--lumo-secondary-text-color)");
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

	private boolean isClosed(final CProjectItem<?, ?> item) {
		final CProjectItemStatus status = item != null ? item.getStatus() : null;
		return status != null && Boolean.TRUE.equals(status.getFinalStatus());
	}

	private boolean isClosed(final CSprint sprint) {
		final CProjectItemStatus status = sprint != null ? sprint.getStatus() : null;
		return status != null && Boolean.TRUE.equals(status.getFinalStatus());
	}

	public boolean shouldIncludeItem(final CProjectItem<?, ?> item) {
		if (item == null) {
			return false;
		}
		// Sprint search must not reuse backlog search fields; keep grids independent.
		return CSearchTextFilterSupport.matches(getSprintSearchText(), item.getName(), item.getDescription());
	}

	public boolean shouldIncludeBacklogItem(final CProjectItem<?, ?> item) {
		// Leaf backlog grid uses its own search input; do not reuse sprint/parent searches.
		if (item == null || !CSearchTextFilterSupport.matches(getBacklogLeafSearchText(), item.getName(), item.getDescription())) {
			return false;
		}
		if (backlogStateFilter == EStateFilter.ALL) {
			return true;
		}
		final boolean closed = isClosed(item);
		return backlogStateFilter == EStateFilter.CLOSED ? closed : !closed;
	}

	public boolean shouldIncludeBacklogParentItem(final CProjectItem<?, ?> item) {
		if (item == null || !CSearchTextFilterSupport.matches(getBacklogParentSearchText(), item.getName(), item.getDescription())) {
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
		final String requiredStatus = comboBoxSprintStatus.getValue();
		if (requiredStatus != null && !requiredStatus.isBlank()) {
			final String sprintStatus = sprint.getStatus() != null ? sprint.getStatus().getName() : null;
			if (sprintStatus == null || !requiredStatus.equalsIgnoreCase(sprintStatus)) {
				return false;
			}
		}
		if (sprintStateFilter == EStateFilter.ALL) {
			return true;
		}
		final boolean closed = isClosed(sprint);
		return sprintStateFilter == EStateFilter.CLOSED ? closed : !closed;
	}
}
