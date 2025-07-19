package tech.derbent.users.view;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

public class CUserProjectSettingsGrid extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final Grid<CUserProjectSettings> grid = new Grid<>(CUserProjectSettings.class, false);
	private final CProjectService projectService;
	private Supplier<List<CUserProjectSettings>> getProjectSettings;
	private Consumer<List<CUserProjectSettings>> setProjectSettings;
	private Runnable saveEntity;
	private CUser currentUser;

	public CUserProjectSettingsGrid(final CProjectService projectService) {
		LOGGER.info("CUserProjectSettingsGrid constructor called");
		this.projectService = projectService;
		setupGrid();
		setupButtons();
		add(grid);
	}

	private void deleteSelected() {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if ((selected != null) && (getProjectSettings != null) && (setProjectSettings != null)) {
			final List<CUserProjectSettings> settings = getProjectSettings.get();
			settings.remove(selected);
			setProjectSettings.accept(settings);
			if (saveEntity != null) {
				saveEntity.run();
			}
			refresh();
		}
	}

	private String getPermissionAsString(final CUserProjectSettings settings) {
		if ((settings.getPermission() == null) || settings.getPermission().isEmpty()) {
			return "";
		}
		return settings.getPermission();
	}

	private String getProjectName(final CUserProjectSettings settings) {
		if (settings.getProjectId() == null) {
			return "Unknown Project";
		}
		// Get project by ID from service
		return projectService.get(settings.getProjectId()).map(CProject::getName).orElse("Project #" + settings.getProjectId());
	}

	private String getRoleAsString(final CUserProjectSettings settings) {
		if ((settings.getRole() == null) || settings.getRole().isEmpty()) {
			return "";
		}
		return settings.getRole();
	}

	private void onSettingsSaved(final CUserProjectSettings settings) {
		if ((getProjectSettings != null) && (setProjectSettings != null)) {
			final List<CUserProjectSettings> settingsList = getProjectSettings.get();
			// Check if this is an update or a new addition
			boolean found = false;
			for (int i = 0; i < settingsList.size(); i++) {
				final CUserProjectSettings existing = settingsList.get(i);
				if ((existing.getId() != null) && existing.getId().equals(settings.getId())) {
					settingsList.set(i, settings);
					found = true;
					break;
				}
			}
			if (!found) {
				settingsList.add(settings);
			}
			setProjectSettings.accept(settingsList);
			if (saveEntity != null) {
				saveEntity.run();
			}
			refresh();
		}
	}

	private void openAddDialog() {
		if (currentUser == null) {
			LOGGER.warn("Cannot add project settings - current user is null");
			return;
		}
		final CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(projectService, null, // null for new settings
			currentUser, this::onSettingsSaved);
		dialog.open();
	}

	private void openEditDialog() {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if ((selected != null) && (currentUser != null)) {
			final CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(projectService, selected, currentUser, this::onSettingsSaved);
			dialog.open();
		}
	}

	public void refresh() {
		LOGGER.info("Refreshing CUserProjectSettingsGrid");
		if (getProjectSettings != null) {
			grid.setItems(getProjectSettings.get());
		}
	}

	public void setCurrentUser(final CUser user) { this.currentUser = user; }

	public void setProjectSettingsAccessors(final Supplier<List<CUserProjectSettings>> getProjectSettings, final Consumer<List<CUserProjectSettings>> setProjectSettings, final Runnable saveEntity) {
		LOGGER.info("Setting project settings accessors in CUserProjectSettingsGrid");
		this.getProjectSettings = getProjectSettings;
		this.setProjectSettings = setProjectSettings;
		this.saveEntity = saveEntity;
		refresh();
	}

	private void setupButtons() {
		final Button addButton = new Button("Add Project", VaadinIcon.PLUS.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(e -> openAddDialog());
		final Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
		editButton.addClickListener(e -> openEditDialog());
		editButton.setEnabled(false);
		final Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		deleteButton.addClickListener(e -> deleteSelected());
		deleteButton.setEnabled(false);
		// Enable/disable edit and delete buttons based on selection
		grid.addSelectionListener(selection -> {
			final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
			editButton.setEnabled(hasSelection);
			deleteButton.setEnabled(hasSelection);
		});
		final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
		buttonLayout.setSpacing(true);
		add(buttonLayout);
	}

	private void setupGrid() {
		// Add columns for project name, roles, and permissions
		grid.addColumn(CUserProjectSettings::getId).setHeader("ID").setAutoWidth(true);
		grid.addColumn(CUserProjectSettings::getUser).setHeader("User").setAutoWidth(true);
		grid.addColumn(this::getProjectName).setHeader("Project Name").setAutoWidth(true).setSortable(true);
		grid.addColumn(this::getRoleAsString).setHeader("Role").setAutoWidth(true);
		grid.addColumn(this::getPermissionAsString).setHeader("Permission").setAutoWidth(true);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
	}
}