package tech.derbent.base.setup.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.setup.domain.CSystemSettings;

/**
 * CSystemSettingsService - Abstract service for CSystemSettings hierarchy.
 * Layer: Service (MVC)
 * 
 * Base service for system-wide configuration settings management.
 * Follows the same pattern as CProjectService.
 * 
 * NO @Service annotation - abstract services are not Spring beans.
 * 
 * Concrete implementations: CSystemSettings_BabService, CSystemSettings_DerbentService
 */
@PreAuthorize("isAuthenticated()")
public abstract class CSystemSettingsService<SettingsClass extends CSystemSettings<SettingsClass>> extends CAbstractService<SettingsClass> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsService.class);
    private final ISystemSettingsRepository<SettingsClass> settingsRepository;

    public CSystemSettingsService(final ISystemSettingsRepository<SettingsClass> repository, final Clock clock, final ISessionService sessionService) {
        super(repository, clock, sessionService);
        this.settingsRepository = repository;
    }

    @Override
    public String checkDeleteAllowed(final SettingsClass settings) {
        // System settings should generally not be deleted, only modified
        return "System settings cannot be deleted. They can only be modified.";
    }

    /**
     * Get or create the system settings.
     * System settings follow singleton pattern - only one record should exist.
     */
    @Transactional
    public SettingsClass getOrCreateSystemSettings() {
        LOGGER.debug("Getting or creating system settings");
        
        Optional<SettingsClass> existing = settingsRepository.findFirst();
        if (existing.isPresent()) {
            LOGGER.debug("Found existing system settings: {}", existing.get().getApplicationName());
            return existing.get();
        }
        
        // Create new system settings
        LOGGER.info("Creating new system settings record");
        try {
            SettingsClass newSettings = newEntity();
            return save(newSettings);
        } catch (Exception e) {
            LOGGER.error("Failed to create new system settings", e);
            throw new RuntimeException("Failed to create system settings", e);
        }
    }

    /**
     * Get the default login view.
     * Used by authentication system.
     */
    @Transactional(readOnly = true)
    public String getDefaultLoginView() {
        try {
            SettingsClass settings = getSystemSettings();
            return settings != null && settings.getDefaultLoginView() != null ? settings.getDefaultLoginView() : "home";
        } catch (Exception e) {
            LOGGER.warn("Could not get default login view, using default: {}", e.getMessage());
            return "home";
        }
    }

    /**
     * Update system settings.
     * Used by UI for saving changes.
     */
    @Transactional
    public SettingsClass updateSystemSettings(final SettingsClass settings) {
        return save(settings);
    }

    /**
     * Get the system settings (read-only).
     * Returns null if no settings exist.
     */
    @Transactional(readOnly = true)
    public SettingsClass getSystemSettings() {
        return settingsRepository.findFirst().orElse(null);
    }

    /**
     * Check if system settings exist.
     */
    @Transactional(readOnly = true)
    public boolean existsSystemSettings() {
        return settingsRepository.existsAny();
    }

    /**
     * Get session timeout in minutes.
     * Used by session management.
     */
    @Transactional(readOnly = true)
    public Integer getSessionTimeoutMinutes() {
        SettingsClass settings = getSystemSettings();
        return settings != null ? settings.getSessionTimeoutMinutes() : 60; // Default 60 minutes
    }

    /**
     * Get maximum login attempts.
     * Used by security/authentication.
     */
    @Transactional(readOnly = true)
    public Integer getMaxLoginAttempts() {
        SettingsClass settings = getSystemSettings();
        return settings != null ? settings.getMaxLoginAttempts() : 3; // Default 3 attempts
    }

    /**
     * Check if maintenance mode is enabled.
     * Used by application startup/health checks.
     */
    @Transactional(readOnly = true)
    public boolean isMaintenanceModeEnabled() {
        SettingsClass settings = getSystemSettings();
        return settings != null && settings.isMaintenanceModeEnabled() != null && settings.isMaintenanceModeEnabled();
    }

    /**
     * Get the last visited view for navigation.
     * Used by UI routing.
     */
    @Transactional(readOnly = true)
    public String getLastVisitedView() {
        try {
            SettingsClass settings = getSystemSettings();
            return settings != null && settings.getLastVisitedView() != null ? settings.getLastVisitedView() : "home";
        } catch (Exception e) {
            LOGGER.warn("Could not get last visited view, using default: {}", e.getMessage());
            return "home";
        }
    }

    /**
     * Get the font size scale for UI theming.
     * Used by UI rendering.
     */
    @Transactional(readOnly = true)
    public String getFontSizeScale() {
        try {
            SettingsClass settings = getSystemSettings();
            return settings != null && settings.getFontSizeScale() != null ? settings.getFontSizeScale() : "medium";
        } catch (Exception e) {
            LOGGER.warn("Could not get font size scale, using default: {}", e.getMessage());
            return "medium";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SettingsClass> getById(final Long id) {
        Check.notNull(id, "ID cannot be null");
        return settingsRepository.findByIdForPageView(id);
    }

    // Abstract methods implemented by concrete services
    public abstract Class<SettingsClass> getEntityClass();
    public abstract Class<?> getInitializerServiceClass();
    public abstract Class<?> getPageServiceClass();
    public abstract Class<?> getServiceClass();

    @Override
    protected void validateEntity(final SettingsClass entity) {
        super.validateEntity(entity);
        
        // 1. Required Fields
        Check.notBlank(entity.getApplicationName(), ValidationMessages.NAME_REQUIRED);
        Check.notBlank(entity.getApplicationVersion(), "Application version is required");
        Check.notBlank(entity.getDatabaseName(), "Database name is required");
        
        // 2. Length Checks
        if (entity.getApplicationName().length() > 255) {
            throw new IllegalArgumentException("Application name cannot exceed 255 characters");
        }
        
        if (entity.getApplicationVersion().length() > 255) {
            throw new IllegalArgumentException("Application version cannot exceed 255 characters");
        }
        
        // 3. Unique Checks
        // Application name should be unique (but we allow update of existing record)
        Optional<SettingsClass> existing = settingsRepository.findByApplicationName(entity.getApplicationName());
        if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
            throw new IllegalArgumentException("System settings with this application name already exist");
        }
        
        // 4. Business Rules
        if (entity.getSessionTimeoutMinutes() != null && entity.getSessionTimeoutMinutes() < 5) {
            throw new IllegalArgumentException("Session timeout must be at least 5 minutes");
        }
        
        if (entity.getMaxLoginAttempts() != null && entity.getMaxLoginAttempts() < 1) {
            throw new IllegalArgumentException("Max login attempts must be at least 1");
        }
        
        if (entity.getMaxFileUploadSizeMb() != null && entity.getMaxFileUploadSizeMb().doubleValue() <= 0) {
            throw new IllegalArgumentException("Max file upload size must be positive");
        }
    }
}