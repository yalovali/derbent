package tech.derbent.bab.dashboard.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.view.CCpuInfo;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Helper client responsible for retrieving CPU information via Calimero HTTP API.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>cpuInfo - Get detailed CPU information and usage statistics</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe.
 * <p>
 * Error Handling: All methods return Optional, never throw exceptions.
 * Check logs for error details.
 * 
 * @see CClientProject
 * @see CCalimeroRequest
 * @see CCalimeroResponse
 */
public class CCpuInfoCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CCpuInfoCalimeroClient.class);
	private static final Gson GSON = new Gson();
	
	private final CClientProject clientProject;
	
	public CCpuInfoCalimeroClient(final CClientProject clientProject) {
		this.clientProject = clientProject;
	}
	
	/**
	 * Fetch detailed CPU information from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="system", operation="cpuInfo"
	 * 
	 * @return Optional containing CPU info or empty on failure
	 */
	public Optional<CCpuInfo> fetchCpuInfo() {
		try {
			LOGGER.debug("Fetching CPU information from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("system")
					.operation("cpuInfo")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to fetch CPU info: " + response.getErrorMessage();
				LOGGER.warn(message);
				// Don't show notification - graceful degradation when Calimero unavailable
				return Optional.empty();
			}
			
			final JsonObject data = toJsonObject(response);
			final CCpuInfo cpuInfo = CCpuInfo.createFromJson(data);
			
			LOGGER.info("Fetched CPU info - Usage: {}%, Cores: {}, Temperature: {}°C",
					cpuInfo.getUsagePercent(), cpuInfo.getCores(), cpuInfo.getTemperature());
			
			return Optional.of(cpuInfo);
			
		} catch (final Exception e) {
			// Graceful degradation - log but don't show exception to user
			// This is normal when Calimero server is not available (e.g., test environments)
			LOGGER.debug("Failed to fetch CPU info: {} (Calimero unavailable - expected in test mode)", e.getMessage());
			return Optional.empty();
		}
	}
	
	private JsonObject toJsonObject(final CCalimeroResponse response) {
		return GSON.fromJson(GSON.toJson(response.getData()), JsonObject.class);
	}
}
