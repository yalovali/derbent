package tech.derbent.bab.dashboard.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dto.CDTOSystemService;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Helper client responsible for retrieving systemd service information via Calimero HTTP API.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>list - Get list of all systemd services</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe.
 * <p>
 * Error Handling: All methods return List, never throw exceptions.
 * Check logs for error details.
 * 
 * @see CClientProject
 * @see CCalimeroRequest
 * @see CCalimeroResponse
 */
public class CSystemServiceCalimeroClient extends CAbstractCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemServiceCalimeroClient.class);
	
	
	public CSystemServiceCalimeroClient(final CClientProject clientProject) {
		super(clientProject);
	}
	
	/**
	 * Fetch list of systemd services from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="systemservices", operation="list"
	 * <p>
	 * Response JSON structure:
	 * <pre>
	 * {
	 *   "status": 200,
	 *   "data": {
	 *     "systemservices": [ array of service objects ]
	 *   }
	 * }
	 * </pre>
	 * 
	 * @return List of services (empty on failure)
	 */
	public List<CDTOSystemService> fetchServices() {
		final List<CDTOSystemService> services = new ArrayList<>();
		
		try {
			LOGGER.debug("Fetching systemd services from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("systemservices")
					.operation("list")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to load systemd services: " + response.getErrorMessage();
				LOGGER.warn(message);
				// Don't show notification - graceful degradation when Calimero unavailable
				return services;
			}
			
			final JsonObject data = toJsonObject(response);
			
			// Response key is "services" (NOT "systemservices")
			// Calimero returns: {"data": {"services": [...], "count": 70, ...}}
			if (data.has("services") && data.get("services").isJsonArray()) {
				final JsonArray serviceArray = data.getAsJsonArray("services");
				for (final JsonElement element : serviceArray) {
					if (element.isJsonObject()) {
						final CDTOSystemService service = CDTOSystemService.createFromJson(element.getAsJsonObject());
						services.add(service);
					}
				}
			}
			
			LOGGER.info("Fetched {} systemd services from Calimero", services.size());
			return services;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch systemd services: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to fetch systemd services", e);
			return Collections.emptyList();
		}
	}
	
	/**
	 * Start a systemd service.
	 * <p>
	 * Calimero API: POST /api/request with type="systemservices", operation="start"
	 * <p>
	 * Request:
	 * <pre>
	 * {
	 *   "type": "systemservices",
	 *   "data": {
	 *     "operation": "start",
	 *     "serviceName": "nginx.service"
	 *   }
	 * }
	 * </pre>
	 * 
	 * @param serviceName Name of the service to start (e.g., "nginx.service")
	 * @return true if operation succeeded, false otherwise
	 */
	public boolean startService(final String serviceName) {
		return performServiceOperation("start", serviceName);
	}
	
	/**
	 * Stop a systemd service.
	 * <p>
	 * Calimero API: POST /api/request with type="systemservices", operation="stop"
	 * 
	 * @param serviceName Name of the service to stop (e.g., "nginx.service")
	 * @return true if operation succeeded, false otherwise
	 */
	public boolean stopService(final String serviceName) {
		return performServiceOperation("stop", serviceName);
	}
	
	/**
	 * Restart a systemd service.
	 * <p>
	 * Calimero API: POST /api/request with type="systemservices", operation="restart"
	 * 
	 * @param serviceName Name of the service to restart (e.g., "nginx.service")
	 * @return true if operation succeeded, false otherwise
	 */
	public boolean restartService(final String serviceName) {
		return performServiceOperation("restart", serviceName);
	}
	
	/**
	 * Reload a systemd service (configuration only, no restart).
	 * <p>
	 * Calimero API: POST /api/request with type="systemservices", operation="reload"
	 * 
	 * @param serviceName Name of the service to reload (e.g., "nginx.service")
	 * @return true if operation succeeded, false otherwise
	 */
	public boolean reloadService(final String serviceName) {
		return performServiceOperation("reload", serviceName);
	}
	
	/**
	 * Enable a systemd service (auto-start on boot).
	 * <p>
	 * Calimero API: POST /api/request with type="systemservices", operation="enable"
	 * 
	 * @param serviceName Name of the service to enable (e.g., "nginx.service")
	 * @return true if operation succeeded, false otherwise
	 */
	public boolean enableService(final String serviceName) {
		return performServiceOperation("enable", serviceName);
	}
	
	/**
	 * Disable a systemd service (no auto-start on boot).
	 * <p>
	 * Calimero API: POST /api/request with type="systemservices", operation="disable"
	 * 
	 * @param serviceName Name of the service to disable (e.g., "nginx.service")
	 * @return true if operation succeeded, false otherwise
	 */
	public boolean disableService(final String serviceName) {
		return performServiceOperation("disable", serviceName);
	}
	
	/**
	 * Generic method to perform service operations (start, stop, restart, enable, disable, reload).
	 * <p>
	 * All service control operations follow the same pattern:
	 * <ul>
	 *   <li>Request: type="systemservices", operation="<action>", serviceName="<name>"</li>
	 *   <li>Success Response: status=200, data.result="success"</li>
	 *   <li>Failure Response: status=400, error message</li>
	 * </ul>
	 * 
	 * @param operation Operation name (start, stop, restart, enable, disable, reload)
	 * @param serviceName Name of the service (e.g., "nginx.service")
	 * @return true if operation succeeded, false otherwise
	 */
	private boolean performServiceOperation(final String operation, final String serviceName) {
		try {
			LOGGER.info("Performing '{}' operation on service: {}", operation, serviceName);
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("systemservices")
					.operation(operation)
					.parameter("serviceName", serviceName)
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = String.format("Failed to %s service '%s': %s", 
						operation, serviceName, response.getErrorMessage());
				LOGGER.warn(message);
				CNotificationService.showError(message);
				return false;
			}
			
			LOGGER.info("✅ Successfully performed '{}' operation on service: {}", operation, serviceName);
			return true;
			
		} catch (final Exception e) {
			final String message = String.format("Error performing '%s' operation on service '%s'", operation, serviceName);
			LOGGER.error("{}: {}", message, e.getMessage(), e);
			CNotificationService.showException(message, e);
			return false;
		}
	}
	
}
