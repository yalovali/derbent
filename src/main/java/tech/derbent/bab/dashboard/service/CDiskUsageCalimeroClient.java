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
import tech.derbent.bab.dashboard.view.CDiskInfo;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Helper client responsible for retrieving disk usage information via Calimero HTTP API.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>list - Get list of all mounted filesystems</li>
 *   <li>usage - Get disk usage for all or specific filesystem</li>
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
public class CDiskUsageCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CDiskUsageCalimeroClient.class);
	private static final Gson GSON = new Gson();
	
	private final CClientProject clientProject;
	
	public CDiskUsageCalimeroClient(final CClientProject clientProject) {
		this.clientProject = clientProject;
	}
	
	/**
	 * Fetch list of all mounted filesystems from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="disk", operation="list"
	 * 
	 * @return List of disk info (empty on failure)
	 */
	public List<CDiskInfo> fetchDiskList() {
		final List<CDiskInfo> disks = new ArrayList<>();
		
		try {
			LOGGER.debug("Fetching disk list from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("disk")
					.operation("list")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to load disk list: " + response.getErrorMessage();
				LOGGER.warn(message);
				CNotificationService.showWarning(message);
				return disks;
			}
			
			final JsonObject data = toJsonObject(response);
			
			if (data.has("disks") && data.get("disks").isJsonArray()) {
				final JsonArray diskArray = data.getAsJsonArray("disks");
				for (final JsonElement element : diskArray) {
					if (element.isJsonObject()) {
						final CDiskInfo diskInfo = CDiskInfo.createFromJson(element.getAsJsonObject());
						disks.add(diskInfo);
					}
				}
			}
			
			LOGGER.info("Fetched {} disk entries from Calimero", disks.size());
			return disks;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch disk list: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to fetch disk list", e);
			return Collections.emptyList();
		}
	}
	
	/**
	 * Fetch disk usage information for all filesystems.
	 * <p>
	 * Calimero API: POST /api/request with type="disk", operation="usage"
	 * 
	 * @return List of disk usage info (empty on failure)
	 */
	public List<CDiskInfo> fetchDiskUsage() {
		return fetchDiskUsage(null);
	}
	
	/**
	 * Fetch disk usage information for specific filesystem.
	 * <p>
	 * Calimero API: POST /api/request with type="disk", operation="usage", path="/mount/point"
	 * 
	 * @param mountPoint specific mount point or null for all
	 * @return List of disk usage info (empty on failure)
	 */
	public List<CDiskInfo> fetchDiskUsage(final String mountPoint) {
		final List<CDiskInfo> disks = new ArrayList<>();
		
		try {
			LOGGER.debug("Fetching disk usage from Calimero server" + 
					(mountPoint != null ? " for " + mountPoint : ""));
			
			final CCalimeroRequest.Builder builder = CCalimeroRequest.builder()
					.type("disk")
					.operation("usage");
			
			if (mountPoint != null && !mountPoint.isEmpty()) {
				builder.parameter("path", mountPoint);
			}
			
			final CCalimeroRequest request = builder.build();
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				LOGGER.warn("Failed to fetch disk usage: {}", response.getErrorMessage());
				return disks;
			}
			
			final JsonObject data = toJsonObject(response);
			
			// Check for single disk or array
			if (data.has("disks") && data.get("disks").isJsonArray()) {
				final JsonArray diskArray = data.getAsJsonArray("disks");
				for (final JsonElement element : diskArray) {
					if (element.isJsonObject()) {
						final CDiskInfo diskInfo = CDiskInfo.createFromJson(element.getAsJsonObject());
						disks.add(diskInfo);
					}
				}
			} else {
				// Single disk response
				final CDiskInfo diskInfo = CDiskInfo.createFromJson(data);
				disks.add(diskInfo);
			}
			
			LOGGER.info("Fetched disk usage for {} entries", disks.size());
			return disks;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch disk usage: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}
	
	private JsonObject toJsonObject(final CCalimeroResponse response) {
		return GSON.fromJson(GSON.toJson(response.getData()), JsonObject.class);
	}
}
