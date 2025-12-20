package tech.derbent.base.setup.view;

import java.util.concurrent.CompletableFuture;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.config.CDataInitializer;
import tech.derbent.api.entity.view.CAbstractPage;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.dialogs.CDialogProgress;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.ui.theme.CFontSizeService;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.domain.CSystemSettings;
import tech.derbent.base.setup.service.CSystemSettingsService;

/** CSystemSettingsView - View for managing system-wide configuration settings. Layer: View (MVC) Provides a comprehensive interface for system
 * administrators to configure application-wide settings including security, file management, email configuration, database settings, backup
 * preferences, and UI themes. Unlike company settings, this is a singleton configuration that applies to the entire system. */
@Route ("csystemsettingsview")
@PageTitle ("System Setup & Configuration")
@Menu (order = 100.1, icon = "class:tech.derbent.base.setup.view.CSystemSettingsView", title = "Setup.System Settings")
@PermitAll // When security is enabled, allow all authenticated users
public class CSystemSettingsView extends CAbstractPage {

	public static final String DEFAULT_COLOR = "#91856C"; // OpenWindows Border Dark - settings view (darker)
	public static final String DEFAULT_ICON = "vaadin:sliders";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "System Settings View";
	private final CEnhancedBinder<CSystemSettings> binder;
	private CSystemSettings currentSettings;
	private Div formContainer;
	private VerticalLayout mainLayout;
	private ISessionService sessionService;
	private final CSystemSettingsService systemSettingsService;

	public CSystemSettingsView(final CSystemSettingsService systemSettingsService, final ISessionService sessionService) {
		this.systemSettingsService = systemSettingsService;
		this.sessionService = sessionService;
		binder = CBinderFactory.createBinder(CSystemSettings.class);
		LOGGER.info("CSystemSettingsView constructor called with systemSettingsService: {}", systemSettingsService.getClass().getSimpleName());
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		if ((systemSettingsService != null) && (currentSettings == null)) {
			loadSystemSettings();
		}
	}

	private Div createButtonLayout() {
		final var buttonLayout = new Div();
		buttonLayout.addClassName("button-layout");
		final var resetDbMinimal = new CButton("Reset DB Min", null, null);
		resetDbMinimal.addClickListener(e -> on_actionResetDatabaseMinimal());
		final var resetDbButton = new CButton("Reset DB Full", null, null);
		resetDbButton.addClickListener(e -> on_actionResetDatabase());
		final var saveButton = new CButton("Save Settings", null, null);
		saveButton.addClickListener(e -> on_actionSaveSettings());
		final var cancelButton = new CButton("Cancel", null, null);
		cancelButton.addClickListener(e -> on_actionCancelChanges());
		// Reset to Defaults button
		final var resetButton = new CButton("Reset to Defaults", null, null);
		resetButton.addClickListener(e -> {
			try {
				on_resetToDefaults();
			} catch (final Exception e1) {
				
				e1.printStackTrace();
			}
		});
		buttonLayout.add(resetDbButton, resetDbMinimal, saveButton, cancelButton, resetButton);
		return buttonLayout;
	}

	/** Creates the header section with title and description. */
	private void createHeaderSection() {
		LOGGER.debug("createHeaderSection called");
		final var header = new VerticalLayout();
		header.addClassName("header-section");
		header.setPadding(true);
		header.setSpacing(false);
		final var title = new H2("System Configuration");
		title.addClassName("view-title");
		final var description = new Paragraph("Configure system-wide settings that apply to all companies and users. "
				+ "These settings control application behavior, security, file management, "
				+ "email configuration, and system maintenance preferences.");
		description.addClassName("view-description");
		header.add(title, description);
		add(header);
	}

	/** Creates the main layout for the settings form. */
	private void createMainLayout() {
		mainLayout = new VerticalLayout();
		mainLayout.addClassName("main-content");
		mainLayout.setPadding(true);
		mainLayout.setSpacing(true);
		formContainer = new Div();
		formContainer.addClassName("form-container");
		mainLayout.add(formContainer);
		add(mainLayout);
	}

	/** Creates the system settings form using AMetaData annotations. */
	private void createSystemSettingsForm() {
		try {
			formContainer.removeAll();
			final var formLayout = CFormBuilder.buildForm(CSystemSettings.class, binder);
			binder.readBean(currentSettings);
			final var buttonLayout = createButtonLayout();
			formContainer.add(buttonLayout, formLayout);
		} catch (final Exception e) {
			LOGGER.error("Error creating system settings form", e);
			CNotificationService.showException("Error creating form", e);
		}
	}

	/** Gets the form binder.
	 * @return the BeanValidationBinder for CSystemSettings */
	public CEnhancedBinder<CSystemSettings> getBinder() { return binder; }

	/** Gets the current system settings.
	 * @return the current CSystemSettings or null if not loaded */
	public CSystemSettings getCurrentSettings() { return currentSettings; }

	@Override
	public String getPageTitle() { 
		return "System Setup & Configuration";
	}

	@Override
	protected void initPage() {
		LOGGER.debug("initPage called for CSystemSettingsView");
		try {
			createHeaderSection();
			createMainLayout();
		} catch (final Exception e) {
			LOGGER.error("Error initializing system settings view", e);
			CNotificationService.showException("Error initializing view", e);
		}
	}

	/** Loads the current system settings and creates the form. */
	private void loadSystemSettings() {
		if (systemSettingsService == null) {
			LOGGER.error("systemSettingsService is null - dependency injection may not be complete");
			final var errorDiv = new Div();
			errorDiv.addClassName("error-message");
			errorDiv.add(new Paragraph("System settings service is not available. Please refresh the page."));
			formContainer.add(errorDiv);
			return;
		}
		try {
			// Get or create system settings
			currentSettings = systemSettingsService.getOrCreateSystemSettings();
			// Create form
			createSystemSettingsForm();
			LOGGER.info("System settings loaded successfully with ID: {}", currentSettings.getId());
		} catch (final Exception e) {
			LOGGER.error("Error loading system settings", e);
			// Show error message
			final var errorDiv = new Div();
			errorDiv.addClassName("error-message");
			errorDiv.add(new Paragraph("Error loading system settings: " + e.getMessage()));
			formContainer.add(errorDiv);
			CNotificationService.showException("Error loading system settings", e);
		}
	}

	private void on_actionCancelChanges() {
		try {
			if (currentSettings == null) {
				LOGGER.warn("No current settings available to revert to");
				CNotificationService.showWarning("No settings loaded to revert to.");
				return;
			}
			// Reload the current settings from the database to revert any unsaved changes
			final var freshSettings = systemSettingsService.getOrCreateSystemSettings();
			currentSettings = freshSettings;
			// Refresh the form with the original values
			binder.readBean(currentSettings);
			// Show confirmation that changes were cancelled
			CNotificationService.showInfo("Changes cancelled - form reverted to saved state");
			LOGGER.info("Changes cancelled successfully, form reverted to saved state");
		} catch (final Exception e) {
			CNotificationService.showException("Error cancelling changes", e);
		}
	}

	/** Reloads the system settings from the database. */
	private void on_actionResetDatabase() {
		try {
			CNotificationService.showConfirmationDialog("Veritabanı SIFIRLANACAK ve örnek veriler yeniden yüklenecek. Devam edilsin mi?",
					"Evet, sıfırla", () -> {
						runDatabaseReset(false, "Sample data yeniden yüklendi.", "Örnek veriler ve varsayılan veriler yeniden oluşturuldu.");
					});
		} catch (final Exception e) {
			CNotificationService.showException("Error showing confirmation dialog", e);
		}
	}

	private void on_actionResetDatabaseMinimal() {
		try {
			CNotificationService.showConfirmationDialog("Veritabanı SIFIRLANACAK ve minimum örnek veriler yeniden yüklenecek. Devam edilsin mi?",
					"Evet, sıfırla", () -> {
						runDatabaseReset(true, "Minimum örnek veri yeniden yüklendi.", "Minimum örnek veriler ve varsayılan veriler yeniden oluşturuldu.");
					});
		} catch (final Exception e) {
			CNotificationService.showException("Error showing confirmation dialog", e);
		}
	}

	private void runDatabaseReset(final boolean minimal, final String successMessage, final String infoMessage) {
		final UI ui = getUI().orElse(null);
		Check.notNull(ui, "UI must be available to run database reset");
		final CDialogProgress progressDialog = CNotificationService.showProgressDialog("Database Reset", "Veritabanı yeniden hazırlanıyor...");
		CompletableFuture.runAsync(() -> {
			try {
				final CDataInitializer init = new CDataInitializer(sessionService);
				init.reloadForced(minimal);
				ui.access(() -> {
					progressDialog.close();
					CNotificationService.showSuccess(successMessage);
					CNotificationService.showInfoDialog(infoMessage);
				});
			} catch (final Exception ex) {
				ui.access(() -> {
					progressDialog.close();
					CNotificationService.showException("Hata", ex);
				});
			}
		});
	}

	/** Saves the current system settings with validation. */
	private void on_actionSaveSettings() {
		LOGGER.debug("saveSettings called for CSystemSettings");
		try {
			if (currentSettings == null) {
				CNotificationService.showWarning("System settings could not be loaded. Please refresh the page.");
				return;
			}
			// Validate form
			binder.writeBean(currentSettings);
			// Save through service
			final var savedSettings = systemSettingsService.updateSystemSettings(currentSettings);
			currentSettings = savedSettings;
			// Apply font size scale if it was changed
			final String fontSizeScale = savedSettings.getFontSizeScale();
			if (fontSizeScale != null) {
				CFontSizeService.applyFontSizeScale(fontSizeScale);
				CFontSizeService.storeFontSizeScale(fontSizeScale);
				LOGGER.info("Applied font size scale: {}", fontSizeScale);
			}
			// Show success notification
			CNotificationService.showInfo("System settings saved successfully");
			LOGGER.info("System settings saved successfully with ID: {}", savedSettings.getId());
		} catch (final ValidationException e) {
			CNotificationService.showException("Please fix validation errors and try again", e);
		} catch (final Exception e) {
			CNotificationService.showException("Please fix validation errors and try again", e);
		}
	}

	/** Resets all settings to default values. Shows confirmation dialog before proceeding.
	 * @throws Exception */
	private void on_resetToDefaults() {
		try {
			// Show confirmation dialog
			CNotificationService.showConfirmationDialog(
					"Are you sure you want to reset ALL system settings to default values? "
							+ "This will affect application behavior, security settings, and file management. " + "This action cannot be undone.",
					() -> performReset());
			CNotificationService.showSuccess("System settings reset to defaults successfully");
		} catch (final Exception e) {
			CNotificationService.showException("Error showing confirmation dialog", e);
		}
	}

	/** Performs the actual reset to default values. */
	private void performReset() {
		LOGGER.debug("performReset called for CSystemSettings");
		try {
			// Manually copy the ID (since there's no public setId method) We'll update
			// the existing entity with default values
			currentSettings.setApplicationName("Derbent Project Management");
			currentSettings.setApplicationVersion("1.0.0");
			currentSettings.setSessionTimeoutMinutes(60);
			currentSettings.setMaxLoginAttempts(3);
			currentSettings.setRequireStrongPasswords(true);
			currentSettings.setMaintenanceModeEnabled(false);
			// Reset other fields to defaults... Update through service
			final var updatedSettings = systemSettingsService.updateSystemSettings(currentSettings);
			currentSettings = updatedSettings;
			// Refresh form with new values
			binder.readBean(currentSettings);
			CNotificationService.showSuccess("System settings reset to defaults successfully");
			LOGGER.info("System settings reset to defaults successfully");
		} catch (final Exception e) {
			LOGGER.error("Error resetting system settings to defaults", e);
			CNotificationService.showException("Error resetting settings", e);
		}
	}

	/** Post-construction initialization method called after dependency injection is complete. This ensures that systemSettingsService is properly
	 * injected before we try to use it. */
	@PostConstruct
	private void postConstruct() {
		LOGGER.debug("postConstruct called for CSystemSettingsView - loading system settings");
		// Now it's safe to load system settings since all dependencies are injected
		loadSystemSettings();
	}

	/** Sets the session service. This is called after bean creation via configuration class.
	 * @param sessionService the session service to set */
	public void setSessionService(final ISessionService sessionService) {
		this.sessionService = sessionService;
		LOGGER.debug("SessionService injected into CSystemSettingsView via setter");
	}

	@Override
	protected void setupToolbar() {
		LOGGER.debug("setupToolbar called for CSystemSettingsView");
		// No specific toolbar needed for this view
	}
}
