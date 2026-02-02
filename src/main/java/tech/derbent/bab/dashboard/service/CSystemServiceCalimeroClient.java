package tech.derbent.bab.dashboard.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dto.CSystemService;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Helper client responsible for retrieving system service information via Calimero HTTP API.
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
public class CSystemServiceCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemServiceCalimeroClient.class);
	private static final Gson GSON = new Gson();
	
	private final CClientProject clientProject;
	
	public CSystemServiceCalimeroClient(final CClientProject clientProject) {
		this.clientProject = clientProject;
	}
	
	/**
	 * Fetch list of system services from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="servicediscovery", operation="list"
	 * 
	 * @return List of services (empty on failure)
	 */
	public List<CSystemService> fetchServices() {
		final List<CSystemService> services = new ArrayList<>();
		
		try {
			LOGGER.debug("Fetching system services from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("servicediscovery")
					.operation("list")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to load service list: " + response.getErrorMessage();
				LOGGER.warn(message);
				// Don't show notification - graceful degradation when Calimero unavailable
				return services;
			}
			
			final JsonObject data = toJsonObject(response);
			
			if (data.has("services") && data.get("services").isJsonArray()) {
				final JsonArray serviceArray = data.getAsJsonArray("services");
				for (final JsonElement element : serviceArray) {
					if (element.isJsonObject()) {
						final CSystemService service = CSystemService.createFromJson(element.getAsJsonObject());
						services.add(service);
					}
				}
			}
			
			LOGGER.info("Fetched {} system services from Calimero", services.size());
			return services;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch system services: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to fetch service list", e);
			return Collections.emptyList();
		}
	}
	
	private JsonObject toJsonObject(final CCalimeroResponse response) {
		return GSON.fromJson(GSON.toJson(response.getData()), JsonObject.class);
	}
}
