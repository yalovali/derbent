package tech.derbent.bab.dashboard.dashboardproject_bab.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTOCpuInfo;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;

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
public class CCpuInfoCalimeroClient extends CAbstractCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CCpuInfoCalimeroClient.class);
	
	
	public CCpuInfoCalimeroClient(final CClientProject clientProject) {
		super(clientProject);
	}
	
	/**
	 * Fetch detailed CPU information from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="system", operation="cpuInfo"
	 * 
	 * @return Optional containing CPU info or empty on failure
	 */
	public Optional<CDTOCpuInfo> fetchCpuInfo() {
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
			final CDTOCpuInfo cpuInfo = CDTOCpuInfo.createFromJson(data);
			
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
	
}
