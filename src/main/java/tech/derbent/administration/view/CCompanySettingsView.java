package tech.derbent.administration.view;

import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.IIconSet;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CGrid;
import tech.derbent.administration.domain.CCompanySettings;
import tech.derbent.administration.service.CCompanySettingsService;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.session.service.CSessionService;

/** CCompanySettingsView - View for managing company-wide administration settings. Layer: View (MVC) Provides a comprehensive interface for
 * administrators to configure company-wide settings including workflow defaults, working hours, notifications, project management preferences, and UI
 * customizations. Each company has one settings record. */
@Route ("administration/company-settings/:company_settings_id?/:action?(edit)")
@PageTitle ("Company Administration Settings")
@Menu (order = 3.5, icon = "class:tech.derbent.administration.view.CCompanySettingsView", title = "Settings.Company Settings")
@PermitAll // When security is enabled, allow all authenticated users
public class CCompanySettingsView extends CAbstractEntityDBPage<CCompanySettings> implements IIconSet {
	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return "#6c757d"; // Gray color for admin/settings
	}

	public static String getIconFilename() { return "vaadin:cogs"; }

	private final String ENTITY_ID_FIELD = "company_settings_id";
	private final CCompanySettingsService companySettingsService;

	/** Constructor for CCompanySettingsView. Annotated with @Autowired to let Spring inject dependencies.
	 * @param entityService  the CCompanySettingsService instance
	 * @param sessionService */
	@Autowired
	public CCompanySettingsView(final CCompanySettingsService entityService, final CSessionService sessionService) {
		super(CCompanySettings.class, entityService, sessionService);
		this.companySettingsService = entityService;
	}

	@Override
	protected CButton createDeleteButton(final String buttonText) {
		return CButton.createError("Reset to Defaults", null, event -> {
			LOGGER.debug("Reset to defaults button clicked");
			final var currentEntity = getCurrentEntity();
			if (currentEntity == null) {
				showWarningDialog("No settings selected to reset.");
				return;
			}
			// Show confirmation dialog
			final var confirmDialog =
					new CConfirmationDialog("Are you sure you want to reset all settings to default values? This action cannot be undone.",
							() -> performReset(currentEntity));
			confirmDialog.open();
		});
	}

	/** Creates the entity details form using AMetaData annotations. The form is automatically generated based on the CCompanySettings entity
	 * annotations. */
	@Override
	protected void createDetailsLayout() {
		try {
			// Create form using AMetaData annotations and CEntityFormBuilder
			final var formLayout = CEntityFormBuilder.buildForm(CCompanySettings.class, getBinder());
			// Add form to base details layout
			getBaseDetailsLayout().add(formLayout);
			LOGGER.debug("Entity details form created successfully for CCompanySettings");
		} catch (final Exception e) {
			LOGGER.error("Error creating entity details form for CCompanySettings", e);
			Notification.show("Error creating form: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
		}
	}

	@Override
	public void createGridForEntity(final CGrid<CCompanySettings> grid) {
		LOGGER.debug("createGridForEntity called for CCompanySettingsView");
		try {
			// Configure grid columns for company settings
			grid.addReferenceColumn(settings -> settings.getCompany() != null ? settings.getCompany().getName() : "", "Company");
			grid.addShortTextColumn(CCompanySettings::getDefaultProjectStatus, "Default Project Status", "defaultProjectStatus");
			grid.addShortTextColumn(CCompanySettings::getCompanyTimezone, "Timezone", "companyTimezone");
			grid.addShortTextColumn(settings -> settings.getWorkingHoursPerDay() + " hrs/day", "Working Hours", null);
			grid.addBooleanColumn(CCompanySettings::isEmailNotificationsEnabled, "Email Notifications", "Enabled", "Disabled");
			LOGGER.debug("Grid configured successfully for CCompanySettingsView");
		} catch (final Exception e) {
			LOGGER.error("Error configuring grid for CCompanySettingsView", e);
			Notification.show("Error configuring grid: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
		}
	}

	@Override
	protected CCompanySettings createNewEntity() {
		final CCompanySettings settings = entityService.createEntity();
		return settings;
	}

	@Override
	protected CButton createSaveButton(final String buttonText) {
		LOGGER.debug("createSaveButton called with buttonText: {}", buttonText);
		return CButton.createPrimary(buttonText, null, event -> {
			LOGGER.debug("Save button clicked for company settings");
			try {
				final var currentEntity = getCurrentEntity();
				if (currentEntity == null) {
					showWarningDialog("No settings selected for saving.");
					return;
				}
				// Validate form
				getBinder().writeBean(currentEntity);
				// Save through service
				final var savedEntity = companySettingsService.updateSettings(currentEntity);
				// Refresh grid and show success notification
				refreshGrid();
				Notification.show("Company settings saved successfully", 3000, Notification.Position.TOP_CENTER);
				LOGGER.info("Company settings saved successfully for company: {}",
						savedEntity.getCompany() != null ? savedEntity.getCompany().getName() : "unknown");
			} catch (final ValidationException e) {
				LOGGER.warn("Validation failed when saving company settings", e);
				Notification.show("Please fix validation errors and try again", 3000, Notification.Position.MIDDLE);
			} catch (final Exception e) {
				LOGGER.error("Error saving company settings", e);
				Notification.show("Error saving settings: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
			}
		});
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	public void onLayoutModeChanged(final tech.derbent.session.service.LayoutService.LayoutMode newMode) {
		LOGGER.debug("onLayoutModeChanged called for CCompanySettingsView with mode: {}", newMode);
		// Refresh data when layout changes
		refreshGrid();
	}

	/** Performs the actual reset to default values.
	 * @param entity the entity to reset */
	private void performReset(final CCompanySettings entity) {
		LOGGER.debug("performReset called for company: {}", entity.getCompany() != null ? entity.getCompany().getName() : "unknown");
		try {
			// Create new entity with defaults but keep company and ID
			final var company = entity.getCompany();
			// Copy the ID from the existing entity (since CEntityDB doesn't have public
			// setId) We'll update through the service which will handle the ID properly
			// Update through service - this will preserve the ID
			entity.setDefaultProjectStatus("PLANNED");
			entity.setDefaultActivityStatus("TODO");
			entity.setDefaultActivityPriority("MEDIUM");
			entity.setEmailNotificationsEnabled(true);
			entity.setRequireTimeTracking(true);
			// Reset other fields to defaults...
			final var updatedSettings = companySettingsService.updateSettings(entity);
			// Refresh form with new values
			getBinder().readBean(updatedSettings);
			refreshGrid();
			Notification.show("Settings reset to defaults successfully", 3000, Notification.Position.TOP_CENTER);
			LOGGER.info("Company settings reset to defaults for company: {}", company.getName());
		} catch (final Exception e) {
			LOGGER.error("Error resetting settings to defaults", e);
			Notification.show("Error resetting settings: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
		}
	}

	/** Shows a warning dialog with the specified message.
	 * @param message the warning message */
	private void showWarningDialog(final String message) {
		final var warningDialog = new CWarningDialog(message);
		warningDialog.open();
	}
}
