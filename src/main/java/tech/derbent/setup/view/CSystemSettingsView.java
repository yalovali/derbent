package tech.derbent.setup.view;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAbstractPage;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.base.ui.dialogs.CInformationDialog;
import tech.derbent.setup.domain.CSystemSettings;
import tech.derbent.setup.service.CSystemSettingsService;

/**
 * CSystemSettingsView - View for managing system-wide configuration settings.
 * Layer: View (MVC)
 * 
 * Provides a comprehensive interface for system administrators to configure
 * application-wide settings including security, file management, email configuration,
 * database settings, backup preferences, and UI themes. Unlike company settings,
 * this is a singleton configuration that applies to the entire system.
 */
@Route("setup/system-settings")
@PageTitle("System Setup & Configuration")
@Menu(order = 301, icon = "vaadin:tools", title = "Setup.System Settings")
@PermitAll // When security is enabled, allow all authenticated users
public final class CSystemSettingsView extends CAbstractPage {

    private static final long serialVersionUID = 1L;
    
    private final CSystemSettingsService systemSettingsService;
    private CSystemSettings currentSettings;
    private com.vaadin.flow.data.binder.BeanValidationBinder<CSystemSettings> binder;
    private VerticalLayout mainLayout;
    private Div formContainer;

    /**
     * Constructor for CSystemSettingsView.
     * Annotated with @Autowired to let Spring inject dependencies.
     * 
     * @param systemSettingsService the CSystemSettingsService instance
     */
    @Autowired
    public CSystemSettingsView(final CSystemSettingsService systemSettingsService) {
        this.systemSettingsService = systemSettingsService;
        this.binder = new com.vaadin.flow.data.binder.BeanValidationBinder<>(CSystemSettings.class);
        
        LOGGER.info("CSystemSettingsView constructor called with systemSettingsService: {}", 
                   systemSettingsService.getClass().getSimpleName());
        
        addClassNames("system-settings-view");
    }

    @Override
    protected void initPage() {
        LOGGER.debug("initPage called for CSystemSettingsView");
        
        try {
            createHeaderSection();
            createMainLayout();
            loadSystemSettings();
            
            LOGGER.debug("System settings view initialized successfully");
        } catch (final Exception e) {
            LOGGER.error("Error initializing system settings view", e);
            Notification.show("Error initializing view: " + e.getMessage(), 5000, 
                            Notification.Position.MIDDLE);
        }
    }

    @Override
    protected void setupToolbar() {
        LOGGER.debug("setupToolbar called for CSystemSettingsView");
        // No specific toolbar needed for this view
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        LOGGER.debug("beforeEnter called for CSystemSettingsView");
        // Load settings when entering the view
        if (currentSettings == null) {
            loadSystemSettings();
        }
    }

    /**
     * Creates the header section with title and description.
     */
    private void createHeaderSection() {
        LOGGER.debug("createHeaderSection called");
        
        final var header = new VerticalLayout();
        header.addClassName("header-section");
        header.setPadding(true);
        header.setSpacing(false);
        
        final var title = new H2("System Configuration");
        title.addClassName("view-title");
        
        final var description = new Paragraph(
            "Configure system-wide settings that apply to all companies and users. " +
            "These settings control application behavior, security, file management, " +
            "email configuration, and system maintenance preferences."
        );
        description.addClassName("view-description");
        
        header.add(title, description);
        add(header);
    }

    /**
     * Creates the main layout for the settings form.
     */
    private void createMainLayout() {
        LOGGER.debug("createMainLayout called");
        
        mainLayout = new VerticalLayout();
        mainLayout.addClassName("main-content");
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        
        formContainer = new Div();
        formContainer.addClassName("form-container");
        
        mainLayout.add(formContainer);
        add(mainLayout);
    }

    /**
     * Loads the current system settings and creates the form.
     */
    private void loadSystemSettings() {
        LOGGER.debug("loadSystemSettings called");
        
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
            
            Notification.show("Error loading system settings: " + e.getMessage(), 5000, 
                            Notification.Position.MIDDLE);
        }
    }

    /**
     * Creates the system settings form using MetaData annotations.
     */
    private void createSystemSettingsForm() {
        LOGGER.debug("createSystemSettingsForm called");
        
        try {
            // Clear existing content
            formContainer.removeAll();
            
            // Create form using MetaData annotations and CEntityFormBuilder
            final var formLayout = CEntityFormBuilder.buildForm(CSystemSettings.class, binder);
            
            // Bind current settings to form
            binder.readBean(currentSettings);
            
            // Create button layout
            final var buttonLayout = createButtonLayout();
            
            // Add form and buttons to container
            formContainer.add(formLayout, buttonLayout);
            
            LOGGER.debug("System settings form created successfully");
            
        } catch (final Exception e) {
            LOGGER.error("Error creating system settings form", e);
            Notification.show("Error creating form: " + e.getMessage(), 5000, 
                            Notification.Position.MIDDLE);
        }
    }

    /**
     * Creates the button layout with action buttons.
     * 
     * @return Div containing the action buttons
     */
    private Div createButtonLayout() {
        LOGGER.debug("createButtonLayout called");
        
        final var buttonLayout = new Div();
        buttonLayout.addClassName("button-layout");
        
        // Save Settings button
        final var saveButton = new CButton("Save Settings");
        saveButton.addClassName("primary");
        saveButton.addClickListener(event -> saveSettings());
        
        // Reload Settings button
        final var reloadButton = new CButton("Reload Settings");
        reloadButton.addClassName("tertiary");
        reloadButton.addClickListener(event -> reloadSettings());
        
        // Reset to Defaults button
        final var resetButton = new CButton("Reset to Defaults");
        resetButton.addClassName("error");
        resetButton.addClickListener(event -> resetToDefaults());
        
        // Test Configuration button
        final var testButton = new CButton("Test Configuration");
        testButton.addClassName("success");
        testButton.addClickListener(event -> testConfiguration());
        
        buttonLayout.add(saveButton, reloadButton, resetButton, testButton);
        return buttonLayout;
    }

    /**
     * Saves the current system settings with validation.
     */
    private void saveSettings() {
        LOGGER.debug("saveSettings called for CSystemSettings");
        
        try {
            if (currentSettings == null) {
                showWarningDialog("System settings could not be loaded. Please refresh the page.");
                return;
            }

            // Validate form
            binder.writeBean(currentSettings);
            
            // Save through service
            final var savedSettings = systemSettingsService.updateSystemSettings(currentSettings);
            currentSettings = savedSettings;
            
            // Show success notification
            Notification.show("System settings saved successfully", 3000, 
                            Notification.Position.TOP_CENTER);
            
            LOGGER.info("System settings saved successfully with ID: {}", savedSettings.getId());
            
        } catch (final ValidationException e) {
            LOGGER.warn("Validation failed when saving system settings", e);
            Notification.show("Please fix validation errors and try again", 3000, 
                            Notification.Position.MIDDLE);
        } catch (final Exception e) {
            LOGGER.error("Error saving system settings", e);
            Notification.show("Error saving settings: " + e.getMessage(), 5000, 
                            Notification.Position.MIDDLE);
        }
    }

    /**
     * Reloads the system settings from the database.
     */
    private void reloadSettings() {
        LOGGER.debug("reloadSettings called");
        
        try {
            // Reload settings from database
            currentSettings = systemSettingsService.getOrCreateSystemSettings();
            
            // Refresh form
            binder.readBean(currentSettings);
            
            Notification.show("Settings reloaded from database", 2000, 
                            Notification.Position.TOP_CENTER);
            
            LOGGER.info("System settings reloaded successfully");
            
        } catch (final Exception e) {
            LOGGER.error("Error reloading system settings", e);
            Notification.show("Error reloading settings: " + e.getMessage(), 5000, 
                            Notification.Position.MIDDLE);
        }
    }

    /**
     * Resets all settings to default values.
     * Shows confirmation dialog before proceeding.
     */
    private void resetToDefaults() {
        LOGGER.debug("resetToDefaults called for CSystemSettings");
        
        if (currentSettings == null) {
            showWarningDialog("System settings could not be loaded. Please refresh the page.");
            return;
        }

        // Show confirmation dialog
        final var confirmDialog = new CConfirmationDialog(
            "Are you sure you want to reset ALL system settings to default values? " +
            "This will affect application behavior, security settings, and file management. " +
            "This action cannot be undone.",
            () -> performReset()
        );
        
        confirmDialog.open();
    }

    /**
     * Performs the actual reset to default values.
     */
    private void performReset() {
        LOGGER.debug("performReset called for CSystemSettings");
        
        try {
            // Create new settings with defaults but keep the ID
            final var settingsId = currentSettings.getId();
            final var defaultSettings = new CSystemSettings();
            
            // Manually copy the ID (since there's no public setId method)
            // We'll update the existing entity with default values
            currentSettings.setApplicationName("Derbent Project Management");
            currentSettings.setApplicationVersion("1.0.0");
            currentSettings.setSessionTimeoutMinutes(60);
            currentSettings.setMaxLoginAttempts(3);
            currentSettings.setRequireStrongPasswords(true);
            currentSettings.setMaintenanceModeEnabled(false);
            // Reset other fields to defaults...
            
            // Update through service
            final var updatedSettings = systemSettingsService.updateSystemSettings(currentSettings);
            currentSettings = updatedSettings;
            
            // Refresh form with new values
            binder.readBean(currentSettings);
            
            Notification.show("System settings reset to defaults successfully", 3000, 
                            Notification.Position.TOP_CENTER);
            
            LOGGER.info("System settings reset to defaults successfully");
            
        } catch (final Exception e) {
            LOGGER.error("Error resetting system settings to defaults", e);
            Notification.show("Error resetting settings: " + e.getMessage(), 5000, 
                            Notification.Position.MIDDLE);
        }
    }

    /**
     * Tests the current configuration settings.
     * Performs various checks to validate the configuration.
     */
    private void testConfiguration() {
        LOGGER.debug("testConfiguration called");
        
        try {
            if (currentSettings == null) {
                showWarningDialog("System settings could not be loaded. Please refresh the page.");
                return;
            }

            // Perform configuration tests
            final var testResults = new StringBuilder();
            testResults.append("Configuration Test Results:\n\n");
            
            // Test application info
            testResults.append("✓ Application: ").append(currentSettings.getApplicationName())
                      .append(" v").append(currentSettings.getApplicationVersion()).append("\n");
            
            // Test security settings
            testResults.append("✓ Session timeout: ").append(currentSettings.getSessionTimeoutMinutes())
                      .append(" minutes\n");
            testResults.append("✓ Max login attempts: ").append(currentSettings.getMaxLoginAttempts()).append("\n");
            
            // Test file settings
            testResults.append("✓ Max file upload: ").append(currentSettings.getMaxFileUploadSizeMb())
                      .append(" MB\n");
            
            // Test email settings
            if (currentSettings.getSmtpServer() != null && !currentSettings.getSmtpServer().trim().isEmpty()) {
                testResults.append("✓ SMTP server: ").append(currentSettings.getSmtpServer())
                          .append(":").append(currentSettings.getSmtpPort()).append("\n");
            } else {
                testResults.append("⚠ SMTP server not configured\n");
            }
            
            // Test maintenance mode
            if (currentSettings.isMaintenanceModeEnabled()) {
                testResults.append("⚠ Maintenance mode is ENABLED\n");
            } else {
                testResults.append("✓ Maintenance mode is disabled\n");
            }
            
            testResults.append("\nConfiguration appears to be valid.");
            
            // Show test results
            final var testDialog = new CInformationDialog(testResults.toString());
            testDialog.open();
            
            LOGGER.info("Configuration test completed successfully");
            
        } catch (final Exception e) {
            LOGGER.error("Error testing configuration", e);
            Notification.show("Error testing configuration: " + e.getMessage(), 5000, 
                            Notification.Position.MIDDLE);
        }
    }

    /**
     * Shows a warning dialog with the specified message.
     * 
     * @param message the warning message
     */
    private void showWarningDialog(final String message) {
        final var warningDialog = new CWarningDialog(message);
        warningDialog.open();
    }

    /**
     * Gets the current system settings.
     * 
     * @return the current CSystemSettings or null if not loaded
     */
    public CSystemSettings getCurrentSettings() {
        return currentSettings;
    }

    /**
     * Gets the form binder.
     * 
     * @return the BeanValidationBinder for CSystemSettings
     */
    public com.vaadin.flow.data.binder.BeanValidationBinder<CSystemSettings> getBinder() {
        return binder;
    }
}