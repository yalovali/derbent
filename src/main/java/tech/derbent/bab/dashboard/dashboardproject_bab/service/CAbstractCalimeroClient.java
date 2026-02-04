package tech.derbent.bab.dashboard.dashboardproject_bab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;

/**
 * CAbstractCalimeroClient - Base class for all Calimero HTTP API clients.
 * <p>
 * Provides common functionality for communicating with Calimero server:
 * <ul>
 * <li>HTTP client project management</li>
 * <li>JSON parsing utilities</li>
 * <li>Common logging patterns</li>
 * <li>Error handling with graceful degradation</li>
 * </ul>
 * <p>
 * Subclasses implement specific API operations (network, system, disk, etc.).
 * <p>
 * Thread Safety: This class is thread-safe.
 * 
 * @see CNetworkInterfaceCalimeroClient
 * @see CSystemMetricsCalimeroClient
 * @see CDiskUsageCalimeroClient
 */
public abstract class CAbstractCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CAbstractCalimeroClient.class);
	protected static final Gson GSON = new Gson();
	
	protected final CClientProject clientProject;
	
	protected CAbstractCalimeroClient(final CClientProject clientProject) {
		this.clientProject = clientProject;
		LOGGER.debug("Created {} for client project", getClass().getSimpleName());
	}
	
	/**
	 * Send request to Calimero server via HTTP client.
	 * <p>
	 * Handles:
	 * <ul>
	 * <li>Request logging</li>
	 * <li>Response validation</li>
	 * <li>Error logging (warnings for connection issues, errors for parsing failures)</li>
	 * </ul>
	 * 
	 * @param request Calimero request
	 * @return Calimero response (check isSuccess() before using data)
	 */
	protected CCalimeroResponse sendRequest(final CCalimeroRequest request) {
		LOGGER.debug("📤 Sending Calimero request - type: {}, operation: {}", 
			request.getType(), request.getOperation());
		
		final CCalimeroResponse response = clientProject.sendRequest(request);
		
		if (!response.isSuccess()) {
			LOGGER.warn("⚠️ Calimero request failed - type: {}, operation: {}, error: {}", 
				request.getType(), request.getOperation(), response.getErrorMessage());
		} else {
			LOGGER.debug("✅ Calimero request successful - type: {}, operation: {}", 
				request.getType(), request.getOperation());
		}
		
		return response;
	}
	
	/**
	 * Convert Calimero response data to JsonObject.
	 * <p>
	 * Utility method for parsing response data. Handles GSON serialization/deserialization.
	 * 
	 * @param response Calimero response with data
	 * @return JsonObject representation of response data
	 * @throws com.google.gson.JsonSyntaxException if data cannot be parsed
	 */
	protected JsonObject toJsonObject(final CCalimeroResponse response) {
		return GSON.fromJson(GSON.toJson(response.getData()), JsonObject.class);
	}
	
	/**
	 * Get the underlying HTTP client project.
	 * <p>
	 * Provides access to client configuration, authentication, etc.
	 * 
	 * @return HTTP client project
	 */
	public CClientProject getClientProject() {
		return clientProject;
	}
}
