package tech.derbent.base.setup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.base.setup.domain.CSystemSettings;

/**
 * CSystemSettingsServiceBridge - Bridge service for accessing system settings across profiles.
 * Layer: Service (MVC)
 * 
 * This service provides a unified interface for accessing system settings regardless of the active profile.
 * It dynamically locates the correct profile-specific service (BAB or Derbent) and delegates calls to it.
 */
@Service("CSystemSettingsService") // Keep the original name for compatibility
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CSystemSettingsServiceBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSystemSettingsServiceBridge.class);

    @Lazy
    private CSystemSettingsService<? extends CSystemSettings<?>> delegateService;

    /**
     * Get the profile-specific system settings service.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private CSystemSettingsService<? extends CSystemSettings<?>> getDelegate() {
        if (delegateService == null) {
            try {
                // Try to get BAB service first
                delegateService = (CSystemSettingsService) CSpringContext.getBean("CSystemSettings_BabService");
                LOGGER.debug("Using BAB system settings service");
            } catch (Exception e) {
                try {
                    // Fall back to Derbent service
                    delegateService = (CSystemSettingsService) CSpringContext.getBean("CSystemSettings_DerbentService");
                    LOGGER.debug("Using Derbent system settings service");
                } catch (Exception e2) {
                    LOGGER.error("Could not find any system settings service implementation");
                    throw new IllegalStateException("No system settings service available", e2);
                }
            }
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
            final CSystemSettings<?> settings = getSystemSettings();
            return settings.getLastVisitedView();
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
            final CSystemSettings<?> settings = getSystemSettings();
            return settings.getFontSizeScale();
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
}