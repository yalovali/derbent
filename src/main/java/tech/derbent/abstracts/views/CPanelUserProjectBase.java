package tech.derbent.abstracts.views;

import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;

/**
 * Base class for managing user-project relationships in both directions. This class
 * provides common functionality for both user->project and project->user panels.
 */
public abstract class CPanelUserProjectBase<EntityClass extends CEntityDB<EntityClass>>
	extends CAccordionDBEntity<EntityClass> {

	private static final long serialVersionUID = 1L;

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected final Grid<CUserProjectSettings> grid =
		new Grid<>(CUserProjectSettings.class, false);

	protected final CProjectService projectService;

	// Handlers for getting and setting user-project settings
	protected Supplier<List<CUserProjectSettings>> getSettings;

	protected CUserProjectSettingsService userProjectSettingsService;

	protected Runnable saveEntity;

	public CPanelUserProjectBase(final String title, final EntityClass currentEntity,
		final CEnhancedBinder<EntityClass> beanValidationBinder,
		final Class<EntityClass> entityClass,
		final CAbstractService<EntityClass> entityService,
		final CProjectService projectService,
		final CUserProjectSettingsService userProjectSettingsService) {
		super(title, currentEntity, beanValidationBinder, entityClass, entityService);
		this.projectService = projectService;
		this.userProjectSettingsService = userProjectSettingsService;
		setupGrid();
		setupButtons();
		closePanel();
	}

	/**
	 * Creates the delete confirmation message
	 */
	protected abstract String
		createDeleteConfirmationMessage(final CUserProjectSettings selected);

	/**
	 * Creates a user avatar with profile picture if available
	 */
	protected Avatar createUserAvatar(final CUser user) {
		final Avatar avatar = new Avatar();

		if (user != null) {
			avatar.setName(user.getName() + " "
				+ (user.getLastname() != null ? user.getLastname() : ""));

			if ((user.getProfilePictureData() != null)
				&& (user.getProfilePictureData().length > 0)) {
				// TODO: Convert byte array to StreamResource for avatar For now, just use
				// initials
			}
			avatar.setAbbreviation(getInitials(user));
		}
		return avatar;
	}

	/**
	 * Deletes the selected user-project relationship
	 */
	protected void deleteSelected() {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();

		if (selected == null) {
			new CWarningDialog("Please select a relationship to delete.").open();
			return;
		}

		if ((getSettings == null)) {
			new CWarningDialog(
				"Settings handlers are not available. Please refresh the page.").open();
			return;
		}
		final String confirmMessage = createDeleteConfirmationMessage(selected);
		new CConfirmationDialog(confirmMessage, () -> {
			userProjectSettingsService.delete(selected);
			refresh();
			saveEntity.run();
		}).open();
	}

	/**
	 * Gets user initials for avatar
	 */
	private String getInitials(final CUser user) {

		if (user == null) {
			return "?";
		}
		final StringBuilder initials = new StringBuilder();

		if ((user.getName() != null) && !user.getName().isEmpty()) {
			initials.append(user.getName().charAt(0));
		}

		if ((user.getLastname() != null) && !user.getLastname().isEmpty()) {
			initials.append(user.getLastname().charAt(0));
		}
		return initials.length() > 0 ? initials.toString().toUpperCase() : "?";
	}

	/**
	 * Gets the permission as a formatted string
	 */
	protected String getPermissionAsString(final CUserProjectSettings settings) {

		if ((settings.getPermission() == null) || settings.getPermission().isEmpty()) {
			return "";
		}
		return settings.getPermission();
	}

	/**
	 * Gets the project name from a settings object
	 */
	protected String getProjectName(final CUserProjectSettings settings) {

		if (settings.getProject() == null) {
			return "Unknown Project";
		}
		return projectService.getById(settings.getProject().getId())
			.map(CProject::getName).orElse("Project #" + settings.getProject().getId());
	}

	/**
	 * Gets the role as a formatted string
	 */
	protected String getRoleAsString(final CUserProjectSettings settings) {

		if ((settings.getRole() == null) || settings.getRole().isEmpty()) {
			return "";
		}
		return settings.getRole();
	}

	/**
	 * Gets the user name with avatar from a settings object
	 */
	protected HorizontalLayout getUserWithAvatar(final CUserProjectSettings settings) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(
			com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		layout.setSpacing(true);

		if (settings.getUser() != null) {
			final Avatar avatar = createUserAvatar(settings.getUser());
			avatar.setWidth("24px");
			avatar.setHeight("24px");
			final Span userName = new Span(settings.getUser().getName() + " "
				+ (settings.getUser().getLastname() != null
					? settings.getUser().getLastname() : ""));
			layout.add(avatar, userName);
		}
		else {
			layout.add(new Span("Unknown User"));
		}
		return layout;
	}

	/**
	 * Abstract method to handle settings save events
	 */
	protected abstract void onSettingsSaved(final CUserProjectSettings settings);
	/**
	 * Abstract method to open the add dialog
	 */
	protected abstract void openAddDialog();
	/**
	 * Abstract method to open the edit dialog
	 */
	protected abstract void openEditDialog();

	/**
	 * Refreshes the grid data
	 */
	public void refresh() {
		LOGGER.debug("Refreshing grid data");

		if (getSettings != null) {
			grid.setItems(getSettings.get());
		}
	}

	/**
	 * Sets the settings accessors (getters, setters, save callback)
	 */
	public void setSettingsAccessors(
		final Supplier<List<CUserProjectSettings>> getSettings,
		final Runnable saveEntity) {
		LOGGER.debug("Setting settings accessors");
		this.getSettings = getSettings;
		this.saveEntity = saveEntity;
		refresh();
	}

	/**
	 * Sets up the action buttons (Add, Edit, Delete)
	 */
	private void setupButtons() {
		final CButton addButton =
			CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> openAddDialog());
		final CButton editButton =
			new CButton("Edit", VaadinIcon.EDIT.create(), e -> openEditDialog());
		editButton.setEnabled(false);
		final CButton deleteButton = CButton.createError("Delete",
			VaadinIcon.TRASH.create(), e -> deleteSelected());
		deleteButton.setEnabled(false);
		// Enable/disable edit and delete buttons based on selection
		grid.addSelectionListener(selection -> {
			final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
			editButton.setEnabled(hasSelection);
			deleteButton.setEnabled(hasSelection);
		});
		final HorizontalLayout buttonLayout =
			new HorizontalLayout(addButton, editButton, deleteButton);
		buttonLayout.setSpacing(true);
		getBaseLayout().add(buttonLayout);
	}

	/**
	 * Sets up the grid with common columns
	 */
	protected abstract void setupGrid();

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(List.of(""));
	}

	/**
	 * Common validation helper for entity selection in grid. Shows warning dialog and
	 * returns false if no item is selected.
	 * @param actionName Name of the action for error message (e.g., "edit", "delete")
	 * @return true if an item is selected, false otherwise
	 */
	protected boolean validateGridSelection(final String actionName) {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();

		if (selected == null) {
			final String message =
				String.format("Please select an item to %s.", actionName);
			new CWarningDialog(message).open();
			return false;
		}
		return true;
	}

	/**
	 * Common validation helper for service availability. Shows warning dialog and returns
	 * false if service is unavailable.
	 * @param serviceName Name of the service for error message
	 * @return true if projectService is available, false otherwise
	 */
	protected boolean validateServiceAvailability(final String serviceName) {

		if (projectService == null) {
			new CWarningDialog(
				serviceName + " service is not available. Please try again later.")
				.open();
			return false;
		}
		return true;
	}
}