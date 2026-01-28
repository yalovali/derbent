package tech.derbent.base.setup.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.base.setup.domain.CSystemSettings;

/**
 * CSystemSettingsServiceAdapter - Profile-aware adapter for system settings access.
 * Layer: Service (MVC)
 * 
 * This adapter provides a unified interface for accessing system settings while
 * maintaining the proper pattern by delegating to the correct profile-specific service.
 * 
 * Unlike the old bridge, this follows dependency injection patterns and maintains
 * service hierarchy integrity.
 */
@Service("CSystemSettingsService") // Keep this name for backward compatibility
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CSystemSettingsServiceAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsServiceAdapter.class);

    @Autowired
    private ApplicationContext applicationContext;
    
    private CSystemSettingsService<? extends CSystemSettings<?>> delegateService;

    /**
     * Get the profile-specific system settings service.
     * Uses proper dependency injection to locate the correct implementation.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private CSystemSettingsService<? extends CSystemSettings<?>> getDelegate() {
        if (delegateService == null) {
            // Get all CSystemSettingsService beans and pick the active one
            List<CSystemSettingsService> services = applicationContext.getBeansOfType(CSystemSettingsService.class)
                .values().stream().toList();
            
            if (services.isEmpty()) {
                throw new IllegalStateException("No CSystemSettingsService implementation found");
            }
            
            if (services.size() > 1) {
                LOGGER.warn("Multiple CSystemSettingsService implementations found. Using first available.");
            }
            
            delegateService = services.get(0);
            LOGGER.info("Using system settings service: {}", delegateService.getClass().getSimpleName());
        }
        return delegateService;
    }

    /**
     * Get or create system settings.
     */
    @Transactional
    public CSystemSettings<?> getOrCreateSystemSettings() {
        return getDelegate().getOrCreateSystemSettings();
    }

    /**
     * Get system settings.
     */
    public CSystemSettings<?> getSystemSettings() {
        return getDelegate().getSystemSettings();
    }

    /**
     * Get the last visited view for UI navigation.
     */
    public String getLastVisitedView() {
        try {
            return getDelegate().getLastVisitedView();
        } catch (Exception e) {
            LOGGER.warn("Could not get last visited view, using default: {}", e.getMessage());
            return "home";
        }
    }

    /**
     * Get the font size scale for UI theming.
     */
    public String getFontSizeScale() {
        try {
            return getDelegate().getFontSizeScale();
        } catch (Exception e) {
            LOGGER.warn("Could not get font size scale, using default: {}", e.getMessage());
            return "medium";
        }
    }

    /**
     * Check if maintenance mode is enabled.
     */
    public boolean isMaintenanceModeEnabled() {
        return getDelegate().isMaintenanceModeEnabled();
    }

    /**
     * Get the session timeout in minutes.
     */
    public Integer getSessionTimeoutMinutes() {
        return getDelegate().getSessionTimeoutMinutes();
    }

    /**
     * Get the maximum login attempts.
     */
    public Integer getMaxLoginAttempts() {
        return getDelegate().getMaxLoginAttempts();
    }

    /**
     * Check if system settings exist.
     */
    public boolean existsSystemSettings() {
        return getDelegate().existsSystemSettings();
    }

    /**
     * Get the default login view.
     * Used by authentication system.
     */
    public String getDefaultLoginView() {
        try {
            return getDelegate().getDefaultLoginView();
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
    public CSystemSettings<?> updateSystemSettings(final CSystemSettings<?> settings) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        CSystemSettingsService delegate = getDelegate();
        return delegate.updateSystemSettings(settings);
    }
}