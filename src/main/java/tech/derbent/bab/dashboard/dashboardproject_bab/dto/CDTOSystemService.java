package tech.derbent.bab.dashboard.dashboardproject_bab.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDTOSystemService - System service information model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a systemd service with its status.
 * <p>
 * JSON structure from Calimero (ACTUAL format verified from documentation):
 * <pre>
 * {
 *   "name": "nginx.service",
 *   "description": "A high performance web server",
 *   "load": "loaded",
 *   "active": "active",
 *   "sub": "running",
 *   "following": "",
 *   "path": "/org/freedesktop/systemd1/unit/nginx_2eservice",
 *   "unitFileState": "enabled",
 *   "isRunning": true,
 *   "isActive": true,
 *   "isFailed": false,
 *   "isLoaded": true
 * }
 * </pre>
 * 
 * IMPORTANT: 
 * - Calimero uses SHORT field names (load, active, sub) in JSON
 * - We store them as LONG names (loadState, activeState, subState) internally
 * - Calimero also provides pre-calculated boolean helpers (isRunning, isActive, etc.)
 * - We prefer Calimero's boolean values if present, falling back to our calculations
 */
public class CDTOSystemService extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDTOSystemService.class);
	
	public static CDTOSystemService createFromJson(final JsonObject json) {
		final CDTOSystemService service = new CDTOSystemService();
		service.fromJson(json);
		return service;
	}
	
	private String name = "";
	private String description = "";
	private String loadState = "";
	private String activeState = "";
	private String subState = "";
	private String following = "";
	private String path = "";
	private String unitFileState = "";
	
	// Pre-calculated boolean flags from Calimero (optional, calculated if not provided)
	private Boolean isRunningCache = null;
	private Boolean isActiveCache = null;
	private Boolean isFailedCache = null;
	private Boolean isLoadedCache = null;
	
	public CDTOSystemService() {
		// Default constructor
	}
	
	@Override
	protected void fromJson(final JsonObject json) {
		try {
			if (json.has("name")) {
				name = json.get("name").getAsString();
			}
			if (json.has("description")) {
				description = json.get("description").getAsString();
			}
			
			// Calimero uses short field names: "load", "active", "sub"
			// NOT "loadState", "activeState", "subState"
			if (json.has("load")) {
				loadState = json.get("load").getAsString();
			}
			if (json.has("active")) {
				activeState = json.get("active").getAsString();
			}
			if (json.has("sub")) {
				subState = json.get("sub").getAsString();
			}
			
			// These fields use full names
			if (json.has("following")) {
				following = json.get("following").getAsString();
			}
			if (json.has("path")) {
				path = json.get("path").getAsString();
			}
			if (json.has("unitFileState")) {
				unitFileState = json.get("unitFileState").getAsString();
			}
			
			// Parse pre-calculated boolean helpers from Calimero (if present)
			// These are efficiency optimization - if Calimero provides them, use them!
			if (json.has("isRunning")) {
				isRunningCache = json.get("isRunning").getAsBoolean();
			}
			if (json.has("isActive")) {
				isActiveCache = json.get("isActive").getAsBoolean();
			}
			if (json.has("isFailed")) {
				isFailedCache = json.get("isFailed").getAsBoolean();
			}
			if (json.has("isLoaded")) {
				isLoadedCache = json.get("isLoaded").getAsBoolean();
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CDTOSystemService from JSON: {}", e.getMessage());
		}
	}
	
	@Override
	protected String toJson() {
		// System services are read-only from server
		return "{}";
	}
	
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getLoadState() { return loadState; }
	public String getActiveState() { return activeState; }
	public String getSubState() { return subState; }
	public String getFollowing() { return following; }
	public String getPath() { return path; }
	public String getUnitFileState() { return unitFileState; }
	
	/**
	 * Check if service is active (running).
	 * <p>
	 * Uses pre-calculated value from Calimero if available, 
	 * otherwise calculates from activeState field.
	 * 
	 * @return true if service is active
	 */
	public boolean isActive() {
		// Prefer Calimero's pre-calculated value for efficiency
		if (isActiveCache != null) {
			return isActiveCache;
		}
		// Fallback to calculation
		return "active".equalsIgnoreCase(activeState);
	}
	
	/**
	 * Check if service is loaded.
	 * <p>
	 * Uses pre-calculated value from Calimero if available,
	 * otherwise calculates from loadState field.
	 * 
	 * @return true if service is loaded
	 */
	public boolean isLoaded() {
		// Prefer Calimero's pre-calculated value for efficiency
		if (isLoadedCache != null) {
			return isLoadedCache;
		}
		// Fallback to calculation
		return "loaded".equalsIgnoreCase(loadState);
	}
	
	/**
	 * Check if service is enabled (starts on boot).
	 * <p>
	 * Calculated from unitFileState field.
	 * 
	 * @return true if service is enabled
	 */
	public boolean isEnabled() {
		return "enabled".equalsIgnoreCase(unitFileState);
	}
	
	/**
	 * Check if service is running.
	 * <p>
	 * Uses pre-calculated value from Calimero if available,
	 * otherwise calculates from subState field.
	 * 
	 * @return true if service is running
	 */
	public boolean isRunning() {
		// Prefer Calimero's pre-calculated value for efficiency
		if (isRunningCache != null) {
			return isRunningCache;
		}
		// Fallback to calculation
		return "running".equalsIgnoreCase(subState);
	}
	
	/**
	 * Check if service is failed.
	 * <p>
	 * Uses pre-calculated value from Calimero if available,
	 * otherwise calculates from activeState field.
	 * 
	 * @return true if service is failed
	 */
	public boolean isFailed() {
		// Prefer Calimero's pre-calculated value for efficiency
		if (isFailedCache != null) {
			return isFailedCache;
		}
		// Fallback to calculation
		return "failed".equalsIgnoreCase(activeState);
	}
	
	/**
	 * Get status display string.
	 * @return Formatted status (e.g., "active (running)")
	 */
	public String getStatusDisplay() {
		return String.format("%s (%s)", activeState, subState);
	}
}
