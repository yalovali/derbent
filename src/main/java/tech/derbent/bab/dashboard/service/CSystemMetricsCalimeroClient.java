package tech.derbent.bab.dashboard.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dto.CSystemMetrics;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Helper client responsible for retrieving system metrics via Calimero HTTP API.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>metrics - Get current system metrics (CPU, memory, disk)</li>
 *   <li>cpuInfo - Get detailed CPU information</li>
 *   <li>memInfo - Get detailed memory information</li>
 *   <li>diskUsage - Get detailed disk usage information</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe. Multiple instances can be created
 * but share the same underlying HTTP client connection.
 * <p>
 * Error Handling: All methods return Optional, never throw exceptions.
 * Check logs for error details.
 * 
 * @see CClientProject
 * @see CCalimeroRequest
 * @see CCalimeroResponse
 */
public class CSystemMetricsCalimeroClient extends CAbstractCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemMetricsCalimeroClient.class);
	
	public CSystemMetricsCalimeroClient(final CClientProject clientProject) {
		super(clientProject);
	}
	
	/**
	 * Fetch current system metrics from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="system", operation="metrics"
	 * 
	 * @return Optional containing system metrics or empty on failure
	 */
	public Optional<CSystemMetrics> fetchMetrics() {
		try {
			LOGGER.debug("Fetching system metrics from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("system")
					.operation("metrics")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to fetch system metrics: " + response.getErrorMessage();
				LOGGER.warn(message);
				// Don't show notification - graceful degradation when Calimero unavailable
				return Optional.empty();
			}
			
			final JsonObject data = toJsonObject(response);
			final CSystemMetrics metrics = CSystemMetrics.createFromJson(data);
			
			LOGGER.info("Fetched system metrics - CPU: {}%, Memory: {}%, Disk: {}%",
					metrics.getCpuUsagePercent(),
					metrics.getMemoryUsagePercent(),
					metrics.getDiskUsagePercent());
			
			return Optional.of(metrics);
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch system metrics: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to fetch system metrics", e);
			return Optional.empty();
		}
	}
	
	/**
	 * Fetch detailed CPU information from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="system", operation="cpuInfo"
	 * 
	 * @return Optional containing JSON object with CPU details or empty on failure
	 */
	public Optional<JsonObject> fetchCpuInfo() {
		try {
			LOGGER.debug("Fetching CPU information from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("system")
					.operation("cpuInfo")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				LOGGER.warn("Failed to fetch CPU info: {}", response.getErrorMessage());
				return Optional.empty();
			}
			
			final JsonObject data = toJsonObject(response);
			LOGGER.debug("Fetched CPU information");
			return Optional.of(data);
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch CPU info: {}", e.getMessage(), e);
			return Optional.empty();
		}
	}
	
	/**
	 * Fetch detailed memory information from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="system", operation="memInfo"
	 * 
	 * @return Optional containing JSON object with memory details or empty on failure
	 */
	public Optional<JsonObject> fetchMemoryInfo() {
		try {
			LOGGER.debug("Fetching memory information from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("system")
					.operation("memInfo")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				LOGGER.warn("Failed to fetch memory info: {}", response.getErrorMessage());
				return Optional.empty();
			}
			
			final JsonObject data = toJsonObject(response);
			LOGGER.debug("Fetched memory information");
			return Optional.of(data);
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch memory info: {}", e.getMessage(), e);
			return Optional.empty();
		}
	}
	
	/**
	 * Fetch disk usage information from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="system", operation="diskUsage"
	 * 
	 * @return Optional containing JSON object with disk usage details or empty on failure
	 */
	public Optional<JsonObject> fetchDiskUsage() {
		try {
			LOGGER.debug("Fetching disk usage from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("system")
					.operation("diskUsage")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				LOGGER.warn("Failed to fetch disk usage: {}", response.getErrorMessage());
				return Optional.empty();
			}
			
			final JsonObject data = toJsonObject(response);
			LOGGER.debug("Fetched disk usage information");
			return Optional.of(data);
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch disk usage: {}", e.getMessage(), e);
			return Optional.empty();
		}
	}
}
