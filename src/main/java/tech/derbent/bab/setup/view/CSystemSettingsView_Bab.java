package tech.derbent.bab.setup.view;

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
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.bab.setup.service.CSystemSettings_BabService;
import tech.derbent.base.session.service.ISessionService;

/**
 * CSystemSettingsView_Bab - View for BAB IoT Gateway system configuration.
 * Layer: View (MVC)
 * Active when: 'bab' profile is active
 * 
 * Provides simplified configuration interface for IoT gateway environments.
 * Follows Derbent pattern: Concrete class marked final.
 */
@Route("csystemsettingsview")
@PageTitle("BAB Gateway Setup & Configuration")
@Menu(order = 100.1, icon = "class:tech.derbent.bab.setup.view.CSystemSettingsView_Bab", title = "Setup.Gateway Settings")
@PermitAll
@Profile("bab")
public final class CSystemSettingsView_Bab extends CAbstractPage {

    public static final String DEFAULT_COLOR = "#FF5722";
    public static final String DEFAULT_ICON = "vaadin:cogs";
    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsView_Bab.class);
    private static final long serialVersionUID = 1L;
    public static final String VIEW_NAME = "BAB Gateway Settings View";
    
    private final CEnhancedBinder<CSystemSettings_Bab> binder;
    private final CSystemSettings_BabService systemSettingsService;
    private final ISessionService sessionService;
    private CSystemSettings_Bab currentSettings;
    private Div formContainer;
    private VerticalLayout mainLayout;

    public CSystemSettingsView_Bab(
            final CSystemSettings_BabService systemSettingsService, 
            final ISessionService sessionService) {
        this.systemSettingsService = systemSettingsService;
        this.sessionService = sessionService;
        this.binder = CBinderFactory.createBinder(CSystemSettings_Bab.class);
        LOGGER.info("CSystemSettingsView_Bab constructor called");
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        if (systemSettingsService != null && currentSettings == null) {
            loadSystemSettings();
        }
    }

    @Override
    protected void initPage() {
        LOGGER.debug("initPage called for CSystemSettingsView_Bab");
        try {
            createHeaderSection();
            createMainLayout();
        } catch (final Exception e) {
            LOGGER.error("Error initializing gateway settings view", e);
            CNotificationService.showException("Error initializing view", e);
        }
    }

    @Override
    protected void setupToolbar() {
        // No specific toolbar needed
    }

    @Override
    public String getPageTitle() {
        return "BAB Gateway Setup & Configuration";
    }

    @PostConstruct
    private void postConstruct() {
        LOGGER.debug("postConstruct called - loading gateway settings");
        loadSystemSettings();
    }

    private void createHeaderSection() {
        final var header = new VerticalLayout();
        header.addClassName("header-section");
        header.setPadding(true);
        header.setSpacing(false);
        
        final var title = new H2("BAB IoT Gateway Configuration");
        title.addClassName("view-title");
        
        final var description = new Paragraph(
            "Configure IoT gateway settings for device management and connectivity. " +
            "These settings control gateway network, device scanning, and security.");
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
            showError("Gateway settings service is not available. Please refresh the page.");
            return;
        }
        
        try {
            currentSettings = systemSettingsService.getOrCreateSystemSettings();
            createSystemSettingsForm();
            LOGGER.info("Gateway settings loaded successfully with ID: {}", currentSettings.getId());
        } catch (final Exception e) {
            LOGGER.error("Error loading gateway settings", e);
            showError("Error loading gateway settings: " + e.getMessage());
            CNotificationService.showException("Error loading gateway settings", e);
        }
    }

    private void createSystemSettingsForm() {
        try {
            formContainer.removeAll();
            
            final var formLayout = CFormBuilder.buildForm(CSystemSettings_Bab.class, binder);
            binder.readBean(currentSettings);
            
            final var buttonLayout = createButtonLayout();
            formContainer.add(buttonLayout, formLayout);
        } catch (final Exception e) {
            LOGGER.error("Error creating gateway settings form", e);
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
                CNotificationService.showWarning("Gateway settings could not be loaded. Please refresh.");
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
            
            CNotificationService.showInfo("Gateway settings saved successfully");
            LOGGER.info("Gateway settings saved successfully with ID: {}", savedSettings.getId());
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
                "Reset ALL gateway settings to defaults? This cannot be undone.",
                this::performReset);
        } catch (final Exception e) {
            LOGGER.error("Error showing confirmation dialog", e);
            CNotificationService.showException("Error", e);
        }
    }

    private void performReset() {
        try {
            // Reset to default values
            currentSettings.setApplicationName("BAB IoT Gateway");
            currentSettings.setApplicationVersion("1.0.0");
            currentSettings.setSessionTimeoutMinutes(240);
            currentSettings.setMaxLoginAttempts(5);
            currentSettings.setRequireStrongPasswords(true);
            currentSettings.setMaintenanceModeEnabled(false);
            
            final var updatedSettings = systemSettingsService.updateSystemSettings(currentSettings);
            currentSettings = updatedSettings;
            binder.readBean(currentSettings);
            
            CNotificationService.showSuccess("Gateway settings reset to defaults");
            LOGGER.info("Gateway settings reset to defaults");
        } catch (final Exception e) {
            LOGGER.error("Error resetting gateway settings", e);
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
