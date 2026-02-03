package tech.derbent.bab.dashboard.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dto.CSystemProcess;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.api.ui.notifications.CNotificationService;

/**
 * Helper client responsible for retrieving system process information via Calimero HTTP API.
 * <p>
 * Supported Operations:
 * <ul>
 *   <li>processes - Get list of running processes</li>
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
public class CSystemProcessCalimeroClient extends CAbstractCalimeroClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemProcessCalimeroClient.class);
	
	
	public CSystemProcessCalimeroClient(final CClientProject clientProject) {
		super(clientProject);
	}
	
	/**
	 * Fetch list of running processes from Calimero server.
	 * <p>
	 * Calimero API: POST /api/request with type="system", operation="processes"
	 * 
	 * @return List of processes (empty on failure)
	 */
	public List<CSystemProcess> fetchProcesses() {
		final List<CSystemProcess> processes = new ArrayList<>();
		
		try {
			LOGGER.debug("Fetching process list from Calimero server");
			
			final CCalimeroRequest request = CCalimeroRequest.builder()
					.type("system")
					.operation("processes")
					.build();
			
			final CCalimeroResponse response = clientProject.sendRequest(request);
			
			if (!response.isSuccess()) {
				final String message = "Failed to load process list: " + response.getErrorMessage();
				LOGGER.warn(message);
				// Don't show notification - Calimero may not be running (graceful degradation)
				// UI components should show "Service unavailable" message instead
				return processes;
			}
			
			final JsonObject data = toJsonObject(response);
			
			if (data.has("processes") && data.get("processes").isJsonArray()) {
				final JsonArray processArray = data.getAsJsonArray("processes");
				for (final JsonElement element : processArray) {
					if (element.isJsonObject()) {
						final CSystemProcess process = CSystemProcess.createFromJson(element.getAsJsonObject());
						processes.add(process);
					}
				}
			}
			
			LOGGER.info("Fetched {} processes from Calimero", processes.size());
			return processes;
			
		} catch (final Exception e) {
			LOGGER.error("Failed to fetch processes: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to fetch process list", e);
			return Collections.emptyList();
		}
	}
	
}
