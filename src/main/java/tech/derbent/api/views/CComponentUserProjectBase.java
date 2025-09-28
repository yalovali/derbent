package tech.derbent.api.views;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.utils.Check;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.views.components.CButton;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;

/** Base class for managing user-project relationships in both directions. This class provides common functionality for both user->project and
 * project->user components. */
public abstract class CComponentUserProjectBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CComponentRelationBase<MasterClass, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CUserProjectSettingsService userProjectSettingsService;

	public CComponentUserProjectBase(final String title, IContentOwner parentContent, final CEnhancedBinder<MasterClass> beanValidationBinder,
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

	/** Deletes the selected user-project relationship */
	protected void deleteSelected() {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			new CWarningDialog("Please select a project setting to delete.").open();
			return;
		}
		final String confirmationMessage = createDeleteConfirmationMessage(selected);
		new CConfirmationDialog(confirmationMessage, () -> {
			try {
				userProjectSettingsService.delete(selected);
				refresh();
				LOGGER.info("Deleted user project setting: {}", selected);
			} catch (final Exception e) {
				LOGGER.error("Error deleting user project setting", e);
				new CWarningDialog("Failed to delete project setting: " + e.getMessage()).open();
			}
		}).open();
	}

	/** Gets the project from a user-project relationship */
	protected CProject getProjectFromSettings(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting project");
		return settings.getProject();
	}

	/** Gets the user from a user-project relationship */
	protected CUser getUserFromSettings(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting user");
		return settings.getUser();
	}

	/** Gets role display text from settings */
	protected String getRoleDisplayText(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting role display text");
		if (settings.getRole() == null) {
			return "";
		}
		return CColorUtils.getDisplayTextFromEntity(settings.getRole());
	}

	/** Gets user display text from settings */
	protected String getUserDisplayText(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting user display text");
		final CUser user = getUserFromSettings(settings);
		return CColorUtils.getDisplayTextFromEntity(user);
	}

	/** Gets project display text from settings */
	protected String getProjectDisplayText(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting project display text");
		final CProject project = getProjectFromSettings(settings);
		return CColorUtils.getDisplayTextFromEntity(project);
	}

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
				LOGGER.error("Error opening add dialog: {}", e1.getMessage(), e1);
				throw new RuntimeException("Failed to open add dialog", e1);
			}
		});
		final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
			try {
				openEditDialog();
			} catch (final Exception e1) {
				LOGGER.error("Error opening edit dialog: {}", e1.getMessage(), e1);
				throw new RuntimeException("Failed to open edit dialog", e1);
			}
		});
		editButton.setEnabled(false);
		final CButton deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> deleteSelected());
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

	/** Sets up the grid with default columns and styling */
	private void setupGrid() {
		// Add basic columns - subclasses can customize
		grid.addColumn(this::getProjectDisplayText).setHeader("Project").setAutoWidth(true);
		grid.addColumn(this::getUserDisplayText).setHeader("User").setAutoWidth(true);
		grid.addColumn(this::getRoleDisplayText).setHeader("Role").setAutoWidth(true);
		grid.addColumn(this::getPermissionAsString).setHeader("Permissions").setAutoWidth(true);
		grid.setWidthFull();
		grid.setHeight("300px");
		add(grid);
	}
}
