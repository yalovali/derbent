package tech.derbent.api.views;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.component.CButton;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserProjectSettings;
import tech.derbent.base.users.service.CUserProjectSettingsService;

/** Base class for managing user-project relationships in both directions. This class provides common functionality for both user->project and
 * project->user panels. */
public abstract class CPanelUserProjectBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CPanelRelationalBase<MasterClass, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CUserProjectSettingsService userProjectSettingsService;

	public CPanelUserProjectBase(final String title, final IContentOwner parentContent, final CEnhancedBinder<MasterClass> beanValidationBinder,
			final Class<MasterClass> entityClass, final CAbstractService<MasterClass> entityService,
			final CUserProjectSettingsService userProjectSettingsService) {
		super(title, parentContent, beanValidationBinder, entityClass, entityService, CUserProjectSettings.class);
		this.userProjectSettingsService = userProjectSettingsService;
		setupGrid();
		setupButtons();
		closePanel();
	}

	protected String createDeleteConfirmationMessage(final CUserProjectSettings selected) {
		final String projectName = selected.getProject().getName();
		return String.format("Are you sure you want to delete the project setting for '%s'? This action cannot be undone.", projectName);
	}

	/** Deletes the selected user-project relationship
	 * @throws Exception */
	protected void deleteSelected() throws Exception {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			new CWarningDialog("Please select a relationship to delete.").open();
			return;
		}
		if ((getSettings == null)) {
			new CWarningDialog("Settings handlers are not available. Please refresh the page.").open();
			return;
		}
		final String confirmMessage = createDeleteConfirmationMessage(selected);
		new CConfirmationDialog(confirmMessage, () -> {
			// Use service layer to properly handle lazy-loaded collections and transactions
			final CProject project = selected.getProject();
			final CUser user = selected.getUser();
			try {
				userProjectSettingsService.deleteByUserProject(user, project);
				LOGGER.debug("Successfully removed user {} from project {}", user, project);
			} catch (final Exception e) {
				LOGGER.error("Error removing user from project.");
				new CWarningDialog("Failed to remove user from project: " + e.getMessage()).open();
				return;
			}
			// Refresh the view after successful deletion
			if (saveEntity != null) {
				saveEntity.run();
			}
			refresh();
		}).open();
	}

	/** Gets the permission as a formatted string */
	protected String getPermissionAsString(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting permission string");
		if ((settings.getPermission() == null) || settings.getPermission().isEmpty()) {
			return "";
		}
		return settings.getPermission();
	}

	/** Abstract method to handle settings save events */
	protected abstract void onSettingsSaved(final CUserProjectSettings settings);
	/** Abstract method to open the add dialog
	 * @throws Exception */
	protected abstract void openAddDialog() throws Exception;
	/** Abstract method to open the edit dialog
	 * @throws Exception */
	protected abstract void openEditDialog() throws Exception;

	/** Refreshes the grid data */
	/** Sets up the action buttons (Add, Edit, Delete) */
	private void setupButtons() {
		final CButton addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> {
			try {
				openAddDialog();
			} catch (final Exception e1) {
				CNotificationService.showException("Error opening add dialog", e1);
			}
		});
		final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
			try {
				openEditDialog();
			} catch (final Exception e1) {
				CNotificationService.showException("Error opening edit dialog", e1);
			}
		});
		editButton.setEnabled(false);
		final CButton deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> {
			try {
				deleteSelected();
			} catch (final Exception e1) {
				CNotificationService.showException("Error deleting selected relationship", e1);
			}
		});
		deleteButton.setEnabled(false);
		// Enable/disable edit and delete buttons based on selection
		grid.addSelectionListener(selection -> {
			final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
			editButton.setEnabled(hasSelection);
			deleteButton.setEnabled(hasSelection);
		});
		final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
		buttonLayout.setSpacing(true);
		addToContent(buttonLayout);
	}

	protected void setupGrid() {
		// Add columns for project name, roles, and permissions
		grid.addColumn(CUserProjectSettings::getId).setHeader("ID").setAutoWidth(true);
		grid.addComponentColumn(settings -> CColorUtils.getEntityWithIcon(settings.getUser()))
				.setHeader(CColorUtils.createStyledHeader("User", "#1565C0")).setAutoWidth(true);
		grid.addColumn(CUserProjectSettings::getProjectName).setHeader(CColorUtils.createStyledHeader("Project Name", "#1a65C0")).setAutoWidth(true)
				.setSortable(true);
		grid.addColumn(this::getPermissionAsString).setHeader(CColorUtils.createStyledHeader("Permission", "#1a65Cf")).setAutoWidth(true);
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
		com.vaadin.flow.component.grid.GridSingleSelectionModel<CUserProjectSettings> sm =
				(com.vaadin.flow.component.grid.GridSingleSelectionModel<CUserProjectSettings>) grid.getSelectionModel();
		sm.setDeselectAllowed(false);
		addToContent(grid);
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(new ArrayList<String>());
	}

	/** Common validation helper for entity selection in grid. Shows warning dialog and returns false if no item is selected.
	 * @param actionName Name of the action for error message (e.g., "edit", "delete")
	 * @return true if an item is selected, false otherwise */
	protected boolean validateGridSelection(final String actionName) {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			final String message = String.format("Please select an item to %s.", actionName);
			new CWarningDialog(message).open();
			return false;
		}
		return true;
	}

	/** Common validation helper for service availability. Shows warning dialog and returns false if service is unavailable.
	 * @param serviceName Name of the service for error message
	 * @return true if projectService is available, false otherwise */
	protected boolean validateServiceAvailability(final String serviceName) {
		if (entityService == null) {
			new CWarningDialog(serviceName + " service is not available. Please try again later.").open();
			return false;
		}
		return true;
	}
}
