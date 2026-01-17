package tech.derbent.api.views;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.domain.CUserProjectSettings;
import tech.derbent.base.users.service.CUserProjectSettingsService;

/** Base class for managing user-project relationships in both directions. This class provides common functionality for both user->project and
 * project->user panels. */
public abstract class CPanelUserProjectBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CPanelRelationalBase<MasterClass, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;

	/** Gets the permission as a formatted string */
	protected static String getPermissionAsString(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting permission string");
		if (settings.getPermission() == null || settings.getPermission().isEmpty()) {
			return "";
		}
		return settings.getPermission();
	}

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
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

	@Override
	protected String createDeleteConfirmationMessage(final CUserProjectSettings selected) {
		final String projectName = selected.getProject().getName();
		return String.format("Are you sure you want to delete the project setting for '%s'? This action cannot be undone.", projectName);
	}

	/** Deletes the selected user-project relationship
	 * @throws Exception */
	protected void deleteSelected() throws Exception {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			CNotificationService.showWarning("Please select a relationship to delete.");
			return;
		}
		if (getSettings == null) {
			CNotificationService.showWarning("Settings handlers are not available. Please refresh the page.");
			return;
		}
		final String confirmMessage = createDeleteConfirmationMessage(selected);
		CNotificationService.showConfirmationDialog(confirmMessage, () -> {
			// Use service layer to properly handle lazy-loaded collections and transactions
			final CProject project = selected.getProject();
			final CUser user = selected.getUser();
			try {
				userProjectSettingsService.deleteByUserProject(user, project);
				LOGGER.debug("Successfully removed user {} from project {}", user, project);
			} catch (final Exception e) {
				LOGGER.error("Error removing user from project.");
				CNotificationService.showWarning("Failed to remove user from project: " + e.getMessage());
				return;
			}
			// Refresh the view after successful deletion
			if (saveEntity != null) {
				saveEntity.run();
			}
			refresh();
		});
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
		grid.addColumn(CUserProjectSettings::getId, "ID", "id");
		CGrid.styleColumnHeader(grid.addComponentColumn(settings -> CLabelEntity.createUserLabel(settings.getUser())).setAutoWidth(true), "User");
		CGrid.styleColumnHeader(grid.addColumn(CUserProjectSettings::getProjectName).setAutoWidth(true).setSortable(true), "Project Name");
		CGrid.styleColumnHeader(grid.addColumn(CPanelUserProjectBase::getPermissionAsString).setAutoWidth(true), "Permission");
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		final GridSingleSelectionModel<CUserProjectSettings> sm = (GridSingleSelectionModel<CUserProjectSettings>) grid.getSelectionModel();
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
			final String message = String.format("Please  select an item to %s.", actionName);
			CNotificationService.showWarning(message);
			return false;
		}
		return true;
	}

	/** Common validation helper for service availability. Shows warning dialog and returns false if service is unavailable.
	 * @param serviceName Name of the service for error message
	 * @return true if projectService is available, false otherwise */
	protected boolean validateServiceAvailability(final String serviceName) {
		if (entityService == null) {
			CNotificationService.showWarning(serviceName + " service is not available. Please try again later.");
			return false;
		}
		return true;
	}
}
