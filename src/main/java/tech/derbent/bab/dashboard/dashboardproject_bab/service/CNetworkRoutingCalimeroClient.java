package tech.derbent.bab.dashboard.dashboardproject_bab.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkRoute;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Helper client responsible for retrieving network routing and DNS information via Calimero HTTP API.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>getRoutes - Get routing table entries</li>
 *   <li>getDns - Get DNS configuration</li>
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
public class CNetworkRoutingCalimeroClient extends CAbstractCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CNetworkRoutingCalimeroClient.class);
	
	
	public CNetworkRoutingCalimeroClient(final CClientProject clientProject) {
		super(clientProject);
	}
	
	/**
	 * Fetch routing table from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="network", operation="getRoutes"
	 * 
	 * @return List of routes (empty on failure)
	 */
	public List<CDTONetworkRoute> fetchRoutes() {
		final List<CDTONetworkRoute> routes = new ArrayList<>();
		
		try {
			LOGGER.debug("Fetching routing table from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("network")
					.operation("getRoutes")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to load routing table: " + response.getErrorMessage();
				LOGGER.warn(message);
				// Don't show notification - graceful degradation when Calimero unavailable
				return routes;
			}
			
			final JsonObject data = toJsonObject(response);
			
			if (data.has("routes") && data.get("routes").isJsonArray()) {
				final JsonArray routeArray = data.getAsJsonArray("routes");
				for (final JsonElement element : routeArray) {
					if (element.isJsonObject()) {
						final CDTONetworkRoute route = CDTONetworkRoute.createFromJson(element.getAsJsonObject());
						routes.add(route);
					}
				}
			}
			
			LOGGER.info("Fetched {} routes from Calimero", routes.size());
			return routes;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch routes: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to fetch routing table", e);
			return Collections.emptyList();
		}
	}
	
	/**
	 * Fetch DNS servers from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="network", operation="getDns"
	 * 
	 * @return List of DNS servers (empty on failure)
	 */
	public List<String> fetchDnsServers() {
		final List<String> dnsServers = new ArrayList<>();
		
		try {
			LOGGER.debug("Fetching DNS servers from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("network")
					.operation("getDns")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				LOGGER.warn("Failed to load DNS servers: {}", response.getErrorMessage());
				return dnsServers;
			}
			
			final JsonObject data = toJsonObject(response);
			
			if (data.has("nameservers") && data.get("nameservers").isJsonArray()) {
				final JsonArray dnsArray = data.getAsJsonArray("nameservers");
				for (final JsonElement element : dnsArray) {
					dnsServers.add(element.getAsString());
				}
			}
			
			LOGGER.info("Fetched {} DNS servers from Calimero", dnsServers.size());
			return dnsServers;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch DNS servers: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}
	
}
