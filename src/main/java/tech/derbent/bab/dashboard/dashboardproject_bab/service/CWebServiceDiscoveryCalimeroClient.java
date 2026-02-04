package tech.derbent.bab.dashboard.dashboardproject_bab.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTOWebServiceEndpoint;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Helper client responsible for retrieving Calimero webservice metadata via HTTP API.
 * <p>
 * This client performs API introspection to discover what operations Calimero supports.
 * Useful for development and debugging.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>list - Get list of all available API endpoints</li>
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
public class CWebServiceDiscoveryCalimeroClient extends CAbstractCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CWebServiceDiscoveryCalimeroClient.class);
	
	
	public CWebServiceDiscoveryCalimeroClient(final CClientProject clientProject) {
		super(clientProject);
	}
	
	/**
	 * Fetch list of available webservice endpoints from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="webservice", operation="list"
	 * <p>
	 * Response JSON structure:
	 * <pre>
	 * {
	 *   "status": 200,
	 *   "data": {
	 *     "systemservices": [ array of endpoint objects ]
	 *   }
	 * }
	 * </pre>
	 * <p>
	 * Note: Response key is "systemservices" (confusing name, but that's what Calimero returns)
	 * 
	 * @return List of API endpoints (empty on failure)
	 */
	public List<CDTOWebServiceEndpoint> fetchEndpoints() {
		final List<CDTOWebServiceEndpoint> endpoints = new ArrayList<>();
		
		try {
			LOGGER.debug("Fetching webservice endpoints from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("webservice")
					.operation("list")
					.build();
			
			final CCalimeroResponse response = getClientProject().sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to load webservice endpoints: " + response.getErrorMessage();
				LOGGER.warn(message);
				// Don't show notification - graceful degradation when Calimero unavailable
				return endpoints;
			}
			
			final JsonObject data = toJsonObject(response);
			
			// Response key is "services" (NOT "systemservices")
			// Calimero returns: {"data": {"services": [...], "count": 27, "version": "1.0.0"}}
			if (data.has("services") && data.get("services").isJsonArray()) {
				final JsonArray endpointArray = data.getAsJsonArray("services");
				for (final JsonElement element : endpointArray) {
					if (element.isJsonObject()) {
						final CDTOWebServiceEndpoint endpoint = CDTOWebServiceEndpoint.createFromJson(element.getAsJsonObject());
						endpoints.add(endpoint);
					}
				}
			}
			
			LOGGER.info("Fetched {} webservice endpoints from Calimero", endpoints.size());
			return endpoints;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch webservice endpoints: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to fetch webservice endpoints", e);
			return Collections.emptyList();
		}
	}
	
}
