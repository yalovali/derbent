package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CSystemService - System service information model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a systemd service with its status.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "name": "nginx.service",
 *   "description": "A high performance web server",
 *   "loadState": "loaded",
 *   "activeState": "active",
 *   "subState": "running",
 *   "following": "",
 *   "path": "/org/freedesktop/systemd1/unit/nginx_2eservice",
 *   "unitFileState": "enabled"
 * }
 * </pre>
 */
public class CSystemService extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemService.class);
	
	public static CSystemService createFromJson(final JsonObject json) {
		final CSystemService service = new CSystemService();
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
	
	public CSystemService() {
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
			if (json.has("loadState")) {
				loadState = json.get("loadState").getAsString();
			}
			if (json.has("activeState")) {
				activeState = json.get("activeState").getAsString();
			}
			if (json.has("subState")) {
				subState = json.get("subState").getAsString();
			}
			if (json.has("following")) {
				following = json.get("following").getAsString();
			}
			if (json.has("path")) {
				path = json.get("path").getAsString();
			}
			if (json.has("unitFileState")) {
				unitFileState = json.get("unitFileState").getAsString();
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CSystemService from JSON: {}", e.getMessage());
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
	 * @return true if activeState is "active"
	 */
	public boolean isActive() {
		return "active".equalsIgnoreCase(activeState);
	}
	
	/**
	 * Check if service is loaded.
	 * @return true if loadState is "loaded"
	 */
	public boolean isLoaded() {
		return "loaded".equalsIgnoreCase(loadState);
	}
	
	/**
	 * Check if service is enabled (starts on boot).
	 * @return true if unitFileState is "enabled"
	 */
	public boolean isEnabled() {
		return "enabled".equalsIgnoreCase(unitFileState);
	}
	
	/**
	 * Check if service is running.
	 * @return true if subState is "running"
	 */
	public boolean isRunning() {
		return "running".equalsIgnoreCase(subState);
	}
	
	/**
	 * Check if service is failed.
	 * @return true if activeState is "failed"
	 */
	public boolean isFailed() {
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
