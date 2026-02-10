package tech.derbent.api.setup.service;

import tech.derbent.api.setup.domain.CSystemSettings;

/**
 * ISystemSettingsService - Interface for system settings services.
 * 
 * Provides access to system-wide configuration settings.
 * 
 * @author Derbent Team
 * @since 2024
 */
public interface ISystemSettingsService {
	
	/**
	 * Get the default login view.
	 * Used by authentication system.
	 * 
	 * @return default login view name
	 */
	String getDefaultLoginView();
	
	/**
	 * Get the system settings (read-only).
	 * Returns null if no settings exist.
	 * 
	 * @return system settings or null
	 */
	CSystemSettings<?> getSystemSettings();
}
