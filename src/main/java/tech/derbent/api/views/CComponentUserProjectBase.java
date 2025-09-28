package tech.derbent.api.views;

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
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;

public abstract class CComponentUserProjectBase<MasterClass extends CEntityNamed<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CComponentRelationBase<MasterClass, CUserProjectSettings> {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CUserProjectSettingsService userProjectSettingsService;

	public CComponentUserProjectBase(final String title, IContentOwner parentContent, final CEnhancedBinder<MasterClass> beanValidationBinder,
			final Class<MasterClass> entityClass, final CAbstractService<MasterClass> entityService,
			final CUserProjectSettingsService userProjectSettingsService) {
		super(title, parentContent, beanValidationBinder, entityClass, entityService, CUserProjectSettings.class);
		Check.notNull(userProjectSettingsService, "User project settings service cannot be null");
		this.userProjectSettingsService = userProjectSettingsService;
		setupGrid();
		setupButtons();
		closePanel();
	}

	protected String createDeleteConfirmationMessage(final CUserProjectSettings selected) {
		Check.notNull(selected, "Selected settings cannot be null");
		Check.notNull(selected.getProject(), "Project cannot be null");
		final String projectName = selected.getProject().getName();
		return String.format("Are you sure you want to delete the project setting for '%s'? This action cannot be undone.", projectName);
	}

	protected void deleteSelected() {
		final CUserProjectSettings selected = grid.asSingleSelect().getValue();
		Check.notNull(selected, "Please select a project setting to delete.");
		try {
			final String confirmationMessage = createDeleteConfirmationMessage(selected);
			new CConfirmationDialog(confirmationMessage, () -> {
				try {
					userProjectSettingsService.delete(selected);
					refresh();
					LOGGER.info("Deleted user project setting: {}", selected);
				} catch (final Exception e) {
					LOGGER.error("Error deleting user project setting: {}", e.getMessage(), e);
					new CWarningDialog("Failed to delete project setting: " + e.getMessage()).open();
				}
			}).open();
		} catch (Exception e) {
			LOGGER.error("Failed to show delete confirmation: {}", e.getMessage(), e);
			new CWarningDialog("Failed to delete project setting").open();
		}
	}

	protected String getDisplayText(final CUserProjectSettings settings, final String type) {
		Check.notNull(settings, "Settings cannot be null when getting display text");
		try {
			switch (type) {
			case "project":
				Check.notNull(settings.getProject(), "Project cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getProject());
			case "user":
				Check.notNull(settings.getUser(), "User cannot be null");
				return CColorUtils.getDisplayTextFromEntity(settings.getUser());
			case "role":
				return settings.getRole() != null ? CColorUtils.getDisplayTextFromEntity(settings.getRole()) : "";
			case "permission":
				return settings.getPermission() != null ? settings.getPermission() : "";
			default:
				return "";
			}
		} catch (Exception e) {
			LOGGER.error("Failed to get display text for type {}: {}", type, e.getMessage(), e);
			return "";
		}
	}

	protected abstract void onSettingsSaved(final CUserProjectSettings settings);
	protected abstract void openAddDialog() throws Exception;
	protected abstract void openEditDialog() throws Exception;

	private void setupButtons() {
		try {
			final CButton addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> {
				try {
					openAddDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening add dialog: {}", ex.getMessage(), ex);
					new CWarningDialog("Failed to open add dialog: " + ex.getMessage()).open();
				}
			});
			final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> {
				try {
					openEditDialog();
				} catch (final Exception ex) {
					LOGGER.error("Error opening edit dialog: {}", ex.getMessage(), ex);
					new CWarningDialog("Failed to open edit dialog: " + ex.getMessage()).open();
				}
			});
			editButton.setEnabled(false);
			final CButton deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> deleteSelected());
			deleteButton.setEnabled(false);
			grid.addSelectionListener(selection -> {
				final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
				editButton.setEnabled(hasSelection);
				deleteButton.setEnabled(hasSelection);
			});
			final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
			buttonLayout.setSpacing(true);
			add(buttonLayout);
		} catch (Exception e) {
			LOGGER.error("Failed to setup buttons: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup buttons", e);
		}
	}

	private void setupGrid() {
		try {
			grid.addColumn(settings -> getDisplayText(settings, "project")).setHeader("Project").setAutoWidth(true);
			grid.addColumn(settings -> getDisplayText(settings, "user")).setHeader("User").setAutoWidth(true);
			grid.addColumn(settings -> getDisplayText(settings, "role")).setHeader("Role").setAutoWidth(true);
			grid.addColumn(settings -> getDisplayText(settings, "permission")).setHeader("Permissions").setAutoWidth(true);
			grid.setWidthFull();
			grid.setHeight("300px");
			add(grid);
		} catch (Exception e) {
			LOGGER.error("Failed to setup grid: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to setup grid", e);
		}
	}
}
