package tech.derbent.plm.setup.view;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
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
import tech.derbent.plm.setup.domain.CSystemSettings_Derbent;
import tech.derbent.plm.setup.service.CSystemSettings_DerbentService;

/**
 * CSystemSettingsView_Derbent - View for Derbent PLM system configuration.
 * Layer: View (MVC)
 * Active when: default profile or 'derbent' profile (NOT 'bab' profile)
 * 
 * Provides comprehensive configuration interface for full-featured PLM environments.
 * Follows Derbent pattern: Concrete class marked final.
 */
@Route("csystemsettingsview")
@PageTitle("Derbent System Setup & Configuration")
@Menu(order = 100.1, icon = "class:tech.derbent.plm.setup.view.CSystemSettingsView_Derbent", title = "Setup.System Settings")
@PermitAll
@Profile("derbent")
public final class CSystemSettingsView_Derbent extends CAbstractPage {

    public static final String DEFAULT_COLOR = "#91856C";
    public static final String DEFAULT_ICON = "vaadin:sliders";
    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsView_Derbent.class);
    private static final long serialVersionUID = 1L;
    public static final String VIEW_NAME = "Derbent System Settings View";
    
    private final CEnhancedBinder<CSystemSettings_Derbent> binder;
    private final CSystemSettings_DerbentService systemSettingsService;
    private final ISessionService sessionService;
    private CSystemSettings_Derbent currentSettings;
    private Div formContainer;
    private VerticalLayout mainLayout;

    public CSystemSettingsView_Derbent(
            final CSystemSettings_DerbentService systemSettingsService, 
            final ISessionService sessionService) {
        this.systemSettingsService = systemSettingsService;
        this.sessionService = sessionService;
        this.binder = CBinderFactory.createBinder(CSystemSettings_Derbent.class);
        LOGGER.info("CSystemSettingsView_Derbent constructor called");
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        if (systemSettingsService != null && currentSettings == null) {
            loadSystemSettings();
        }
    }

    @Override
    protected void initPage() {
        LOGGER.debug("initPage called for CSystemSettingsView_Derbent");
        try {
            createHeaderSection();
            createMainLayout();
        } catch (final Exception e) {
            LOGGER.error("Error initializing system settings view", e);
            CNotificationService.showException("Error initializing view", e);
        }
    }

    @Override
    protected void setupToolbar() {
        // No specific toolbar needed
    }

    @Override
    public String getPageTitle() {
        return "Derbent System Setup & Configuration";
    }

    @PostConstruct
    private void postConstruct() {
        LOGGER.debug("postConstruct called - loading system settings");
        loadSystemSettings();
    }

    private void createHeaderSection() {
        final var header = new VerticalLayout();
        header.addClassName("header-section");
        header.setPadding(true);
        header.setSpacing(false);
        
        final var title = new H2("Derbent PLM System Configuration");
        title.addClassName("view-title");
        
        final var description = new Paragraph(
            "Configure comprehensive system settings for project lifecycle management. " +
            "These settings control PLM features, security, reporting, integrations, and system maintenance.");
        description.addClassName("view-description");
        
        header.add(title, description);
        add(header);
    }

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

    private void loadSystemSettings() {
        if (systemSettingsService == null) {
            LOGGER.error("systemSettingsService is null");
            showError("System settings service is not available. Please refresh the page.");
            return;
        }
        
        try {
            currentSettings = systemSettingsService.getOrCreateSystemSettings();
            createSystemSettingsForm();
            LOGGER.info("System settings loaded successfully with ID: {}", currentSettings.getId());
        } catch (final Exception e) {
            LOGGER.error("Error loading system settings", e);
            showError("Error loading system settings: " + e.getMessage());
            CNotificationService.showException("Error loading system settings", e);
        }
    }

    private void createSystemSettingsForm() {
        try {
            formContainer.removeAll();
            
            final var formLayout = CFormBuilder.buildForm(CSystemSettings_Derbent.class, binder);
            binder.readBean(currentSettings);
            
            final var buttonLayout = createButtonLayout();
            formContainer.add(buttonLayout, formLayout);
        } catch (final Exception e) {
            LOGGER.error("Error creating system settings form", e);
            CNotificationService.showException("Error creating form", e);
        }
    }

    private Div createButtonLayout() {
        final var buttonLayout = new Div();
        buttonLayout.addClassName("button-layout");
        
        final var saveButton = new CButton("Save Settings", null, null);
        saveButton.addClickListener(event -> on_actionSaveSettings());
        
        final var cancelButton = new CButton("Cancel", null, null);
        cancelButton.addClickListener(event -> on_actionCancelChanges());
        
        final var resetButton = new CButton("Reset to Defaults", null, null);
        resetButton.addClickListener(event -> on_resetToDefaults());
        
        final var resetDbButton = new CButton("Reset DB Full", null, null);
        resetDbButton.addClickListener(event -> on_actionResetDatabase());
        
        final var resetDbMinimal = new CButton("Reset DB Min", null, null);
        resetDbMinimal.addClickListener(event -> on_actionResetDatabaseMinimal());
        
        buttonLayout.add(saveButton, cancelButton, resetButton, resetDbButton, resetDbMinimal);
        return buttonLayout;
    }

    private void on_actionSaveSettings() {
        try {
            if (currentSettings == null) {
                CNotificationService.showWarning("System settings could not be loaded. Please refresh.");
                return;
            }
            
            binder.writeBean(currentSettings);
            final var savedSettings = systemSettingsService.updateSystemSettings(currentSettings);
            currentSettings = savedSettings;
            
            // Apply font size scale if changed
            final String fontSizeScale = savedSettings.getFontSizeScale();
            if (fontSizeScale != null) {
                CFontSizeService.applyFontSizeScale(fontSizeScale);
                CFontSizeService.storeFontSizeScale(fontSizeScale);
            }
            
            CNotificationService.showInfo("System settings saved successfully");
            LOGGER.info("System settings saved successfully with ID: {}", savedSettings.getId());
        } catch (final ValidationException e) {
            CNotificationService.showException("Please fix validation errors", e);
        } catch (final Exception e) {
            CNotificationService.showException("Error saving settings", e);
        }
    }

    private void on_actionCancelChanges() {
        try {
            if (currentSettings == null) {
                CNotificationService.showWarning("No settings loaded to revert to");
                return;
            }
            
            final var freshSettings = systemSettingsService.getOrCreateSystemSettings();
            currentSettings = freshSettings;
            binder.readBean(currentSettings);
            
            CNotificationService.showInfo("Changes cancelled - form reverted to saved state");
        } catch (final Exception e) {
            CNotificationService.showException("Error cancelling changes", e);
        }
    }

    private void on_resetToDefaults() {
        try {
            CNotificationService.showConfirmationDialog(
                "Reset ALL system settings to defaults? This cannot be undone.",
                this::performReset);
        } catch (final Exception e) {
            LOGGER.error("Error showing confirmation dialog", e);
            CNotificationService.showException("Error", e);
        }
    }

    private void performReset() {
        try {
            // Reset to default values
            currentSettings.setApplicationName("Derbent Project Management");
            currentSettings.setApplicationVersion("1.0.0");
            currentSettings.setSessionTimeoutMinutes(60);
            currentSettings.setMaxLoginAttempts(3);
            currentSettings.setRequireStrongPasswords(true);
            currentSettings.setMaintenanceModeEnabled(false);
            
            final var updatedSettings = systemSettingsService.updateSystemSettings(currentSettings);
            currentSettings = updatedSettings;
            binder.readBean(currentSettings);
            
            CNotificationService.showSuccess("System settings reset to defaults");
            LOGGER.info("System settings reset to defaults");
        } catch (final Exception e) {
            LOGGER.error("Error resetting system settings", e);
            CNotificationService.showException("Error resetting settings", e);
        }
    }

    private void on_actionResetDatabase() {
        try {
            CNotificationService.showConfirmationDialog(
                "Veritabanı SIFIRLANACAK ve örnek veriler yeniden yüklenecek. Devam edilsin mi?",
                "Evet, sıfırla",
                () -> runDatabaseReset(false, "Sample data yeniden yüklendi.", 
                                      "Örnek veriler ve varsayılan veriler yeniden oluşturuldu."));
        } catch (final Exception e) {
            LOGGER.error("Error showing confirmation dialog", e);
            CNotificationService.showException("Error", e);
        }
    }

    private void on_actionResetDatabaseMinimal() {
        try {
            CNotificationService.showConfirmationDialog(
                "Veritabanı SIFIRLANACAK ve minimum örnek veriler yeniden yüklenecek. Devam edilsin mi?",
                "Evet, sıfırla",
                () -> runDatabaseReset(true, "Minimum örnek veri yeniden yüklendi.",
                                      "Minimum örnek veriler ve varsayılan veriler yeniden oluşturuldu."));
        } catch (final Exception e) {
            LOGGER.error("Error showing confirmation dialog", e);
            CNotificationService.showException("Error", e);
        }
    }

    private void runDatabaseReset(final boolean minimal, final String successMessage, final String infoMessage) {
        final UI ui = getUI().orElse(null);
        Check.notNull(ui, "UI must be available");
        
        final CDialogProgress progressDialog = CNotificationService.showProgressDialog(
            "Database Reset", "Veritabanı yeniden hazırlanıyor...");
        
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

    private void showError(final String message) {
        final var errorDiv = new Div();
        errorDiv.addClassName("error-message");
        errorDiv.add(new Paragraph(message));
        formContainer.add(errorDiv);
    }
}
