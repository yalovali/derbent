package tech.derbent.abstracts.views;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Base class for managing user-project relationships in both directions. This class provides common functionality for both user->project and
 * project->user panels. */
public abstract class CPanelUserProjectBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CPanelRelationalBase<MasterClass, CUserProjectSettings> {
	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CUserProjectSettingsService userProjectSettingsService;

	public CPanelUserProjectBase(final String title, final MasterClass currentEntity, final CEnhancedBinder<MasterClass> beanValidationBinder,
			final Class<MasterClass> entityClass, final CAbstractService<MasterClass> entityService,
			final CUserProjectSettingsService userProjectSettingsService) {
		super(title, currentEntity, beanValidationBinder, entityClass, entityService, CUserProjectSettings.class);
		this.userProjectSettingsService = userProjectSettingsService;
		setupGrid();
		setupButtons();
		closePanel();
	}

	protected String createDeleteConfirmationMessage(final CUserProjectSettings selected) {
		final String projectName = selected.getProject().getName();
		return String.format("Are you sure you want to delete the project setting for '%s'? This action cannot be undone.", projectName);
	}

	/** Creates a user avatar with profile picture if available */
	protected Avatar createUserAvatar(final CUser user) {
		final Avatar avatar = new Avatar();
		Check.notNull(user, "User cannot be null when creating avatar");
		avatar.setName(user.getName() + " " + (user.getLastname() != null ? user.getLastname() : ""));
		if ((user.getProfilePictureData() != null) && (user.getProfilePictureData().length > 0)) {
			// TODO: Convert byte array to StreamResource for avatar For now, just use
			// initials
		}
		avatar.setAbbreviation(getInitials(user));
		return avatar;
	}

	/** Deletes the selected user-project relationship */
	@SuppressWarnings ("unchecked")
	protected void deleteSelected() {
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
			final CProject project = selected.getProject();
			project.getUserSettings().remove(selected);
			final CUser user = selected.getUser();
			user.getProjectSettings().remove(selected);
			if (entityService instanceof CProjectService) {
				entityService.save((MasterClass) project);
			} else if (entityService instanceof CUserService) {
				entityService.save((MasterClass) user);
			}
			// userProjectSettingsService.delete(selected); refresh();
			saveEntity.run();
		}).open();
	}

	/** Gets user initials for avatar */
	private String getInitials(final CUser user) {
		Check.notNull(user, "User cannot be null when generating initials");
		final StringBuilder initials = new StringBuilder();
		if ((user.getName() != null) && !user.getName().isEmpty()) {
			initials.append(user.getName().charAt(0));
		}
		if ((user.getLastname() != null) && !user.getLastname().isEmpty()) {
			initials.append(user.getLastname().charAt(0));
		}
		return initials.length() > 0 ? initials.toString().toUpperCase() : "?";
	}

	/** Gets the permission as a formatted string */
	protected String getPermissionAsString(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting permission string");
		if ((settings.getPermission() == null) || settings.getPermission().isEmpty()) {
			return "";
		}
		return settings.getPermission();
	}

	/** Gets the role as a formatted string */
	protected String getRoleAsString(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting role string");
		if ((settings.getRole() == null) || settings.getRole().isEmpty()) {
			return "";
		}
		return settings.getRole();
	}

	/** Gets the user name with avatar from a settings object */
	protected HorizontalLayout getUserWithAvatar(final CUserProjectSettings settings) {
		Check.notNull(settings, "Settings cannot be null when getting user with avatar");
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		layout.setSpacing(true);
		if (settings.getUser() != null) {
			final Avatar avatar = createUserAvatar(settings.getUser());
			avatar.setWidth("24px");
			avatar.setHeight("24px");
			final Span userName =
					new Span(settings.getUser().getName() + " " + (settings.getUser().getLastname() != null ? settings.getUser().getLastname() : ""));
			layout.add(avatar, userName);
		} else {
			layout.add(new Span("Unknown User"));
		}
		return layout;
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
			try {
				openEditDialog();
			} catch (final Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
		addToContent(buttonLayout);
	}

	protected void setupGrid() {
		// Add columns for project name, roles, and permissions
		grid.addColumn(CUserProjectSettings::getId).setHeader("ID").setAutoWidth(true);
		grid.addComponentColumn(this::getUserWithAvatar).setHeader("User").setAutoWidth(true);
		grid.addColumn(CUserProjectSettings::getProjectName).setHeader("Project Name").setAutoWidth(true).setSortable(true);
		grid.addColumn(this::getRoleAsString).setHeader("Role").setAutoWidth(true);
		grid.addColumn(this::getPermissionAsString).setHeader("Permission").setAutoWidth(true);
		grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);
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
