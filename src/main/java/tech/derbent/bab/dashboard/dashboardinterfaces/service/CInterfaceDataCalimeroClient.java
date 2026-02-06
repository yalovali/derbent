package tech.derbent.bab.dashboard.dashboardinterfaces.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOAudioDevice;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOInterfaceSummary;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOSerialPort;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOUsbDevice;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;

/**
 * CInterfaceDataCalimeroClient - Client for fetching interface information via Calimero HTTP API.
 * <p>
 * Provides access to:
 * <ul>
 * <li>USB devices (getUsbDevices operation)</li>
 * <li>Serial ports (getSerialPorts operation)</li>
 * <li>Complete interface summary (getAllInterfaces operation)</li>
 * </ul>
 * <p>
 * Thread Safety: This class is thread-safe.
 */
public class CInterfaceDataCalimeroClient extends CAbstractCalimeroClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(CInterfaceDataCalimeroClient.class);

	public CInterfaceDataCalimeroClient(final CClientProject clientProject) {
		super(clientProject);
	}

	/**
	 * Fetch USB devices from Calimero server.
	 * <p>
	 * Calls Calimero getUsbDevices operation to retrieve all connected USB devices.
	 * 
	 * @return CCalimeroResponse containing List<CDTOUsbDevice> data or error
	 */
	public CCalimeroResponse getUsbDevices() {
		final CCalimeroRequest request = CCalimeroRequest.builder()
			.type("iot")
			.operation("getUsbDevices")
			.build();

		LOGGER.debug("📤 Fetching USB devices from Calimero");
		final CCalimeroResponse response = sendRequest(request);

		if (!response.isSuccess()) {
			LOGGER.warn("⚠️ Failed to fetch USB devices: {}", response.getErrorMessage());
			return CCalimeroResponse.error(response.getErrorMessage());
		}

		try {
			final List<CDTOUsbDevice> devices = new ArrayList<>();
			final JsonObject data = toJsonObject(response);

			if (data.has("usb_devices") && data.get("usb_devices").isJsonArray()) {
				final JsonArray deviceArray = data.getAsJsonArray("usb_devices");
				for (final JsonElement element : deviceArray) {
					if (element.isJsonObject()) {
						devices.add(CDTOUsbDevice.createFromJson(element.getAsJsonObject()));
					}
				}
			}

			LOGGER.debug("✅ Fetched {} USB devices from Calimero", devices.size());
			
			// Create success response with devices as Map data
			final Map<String, Object> resultData = new HashMap<>();
			resultData.put("devices", devices);
			return CCalimeroResponse.success(resultData);

		} catch (final Exception e) {
			LOGGER.error("❌ Failed to parse USB devices response: {}", e.getMessage(), e);
			return CCalimeroResponse.error("Failed to parse USB devices: " + e.getMessage());
		}
	}

	/**
	 * Fetch serial ports from Calimero server.
	 * <p>
	 * Calls Calimero getSerialPorts operation to retrieve all available serial ports.
	 * 
	 * @return CCalimeroResponse containing List<CDTOSerialPort> data or error
	 */
	public CCalimeroResponse getSerialPorts() {
		final CCalimeroRequest request = CCalimeroRequest.builder()
			.type("iot")
			.operation("getSerialPorts")
			.build();

		LOGGER.debug("📤 Fetching serial ports from Calimero");
		final CCalimeroResponse response = sendRequest(request);

		if (!response.isSuccess()) {
			LOGGER.warn("⚠️ Failed to fetch serial ports: {}", response.getErrorMessage());
			return CCalimeroResponse.error(response.getErrorMessage());
		}

		try {
			final List<CDTOSerialPort> ports = new ArrayList<>();
			final JsonObject data = toJsonObject(response);

			if (data.has("serial_ports") && data.get("serial_ports").isJsonArray()) {
				final JsonArray portArray = data.getAsJsonArray("serial_ports");
				for (final JsonElement element : portArray) {
					if (element.isJsonObject()) {
						ports.add(CDTOSerialPort.createFromJson(element.getAsJsonObject()));
					}
				}
			}

			LOGGER.debug("✅ Fetched {} serial ports from Calimero", ports.size());
			
			// Create success response with ports as Map data
			final Map<String, Object> resultData = new HashMap<>();
			resultData.put("ports", ports);
			return CCalimeroResponse.success(resultData);

		} catch (final Exception e) {
			LOGGER.error("❌ Failed to parse serial ports response: {}", e.getMessage(), e);
			return CCalimeroResponse.error("Failed to parse serial ports: " + e.getMessage());
		}
	}

	/**
	 * Fetch complete interface summary from Calimero server.
	 * <p>
	 * Calls Calimero getAllInterfaces operation to retrieve comprehensive interface information.
	 * This includes USB devices, serial ports, network interfaces, and audio devices.
	 * 
	 * @return CCalimeroResponse containing CDTOInterfaceSummary data or error
	 */
	public CCalimeroResponse getAllInterfaces() {
		final CCalimeroRequest request = CCalimeroRequest.builder()
			.type("iot")
			.operation("getAllInterfaces")
			.build();

		LOGGER.debug("📤 Fetching complete interface summary from Calimero");
		final CCalimeroResponse response = sendRequest(request);

		if (!response.isSuccess()) {
			LOGGER.warn("⚠️ Failed to fetch interface summary: {}", response.getErrorMessage());
			return CCalimeroResponse.error(response.getErrorMessage());
		}

		try {
			final JsonObject data = toJsonObject(response);
			final CDTOInterfaceSummary summary = CDTOInterfaceSummary.createFromJson(data);

			LOGGER.debug("✅ Fetched interface summary from Calimero: {}", summary.getFormattedSummary());
			
			// Create success response with summary as Map data
			final Map<String, Object> resultData = new HashMap<>();
			resultData.put("summary", summary);
			return CCalimeroResponse.success(resultData);

		} catch (final Exception e) {
			LOGGER.error("❌ Failed to parse interface summary response: {}", e.getMessage(), e);
			return CCalimeroResponse.error("Failed to parse interface summary: " + e.getMessage());
		}
	}

	/**
	 * Fetch audio devices from Calimero server.
	 * <p>
	 * Calls Calimero getAudioDevices operation to retrieve all audio devices.
	 * 
	 * @return CCalimeroResponse containing List<CDTOAudioDevice> data or error
	 */
	public CCalimeroResponse getAudioDevices() {
		final CCalimeroRequest request = CCalimeroRequest.builder()
			.type("iot")
			.operation("getAudioDevices")
			.build();

		LOGGER.debug("📤 Fetching audio devices from Calimero");
		final CCalimeroResponse response = sendRequest(request);

		if (!response.isSuccess()) {
			LOGGER.warn("⚠️ Failed to fetch audio devices: {}", response.getErrorMessage());
			return CCalimeroResponse.error(response.getErrorMessage());
		}

		try {
			final List<CDTOAudioDevice> devices = new ArrayList<>();
			final JsonObject data = toJsonObject(response);

			if (data.has("audio_devices") && data.get("audio_devices").isJsonArray()) {
				final JsonArray deviceArray = data.getAsJsonArray("audio_devices");
				for (final JsonElement element : deviceArray) {
					if (element.isJsonObject()) {
						devices.add(CDTOAudioDevice.createFromJson(element.getAsJsonObject()));
					}
				}
			}

			LOGGER.debug("✅ Fetched {} audio devices from Calimero", devices.size());
			
			// Create success response with devices as Map data
			final Map<String, Object> resultData = new HashMap<>();
			resultData.put("devices", devices);
			return CCalimeroResponse.success(resultData);

		} catch (final Exception e) {
			LOGGER.error("❌ Failed to parse audio devices response: {}", e.getMessage(), e);
			return CCalimeroResponse.error("Failed to parse audio devices: " + e.getMessage());
		}
	}

	/**
	 * Check if Calimero server is available and responding.
	 * <p>
	 * Quick health check - useful for components to verify connectivity before making requests.
	 * 
	 * @return true if server is reachable and responding, false otherwise
	 */
	public boolean isServerAvailable() {
		try {
			// Use a lightweight request to check server status
			final CCalimeroRequest request = CCalimeroRequest.builder()
				.type("iot")
				.operation("getUsbDevices")
				.build();

			final CCalimeroResponse response = sendRequest(request);
			return response.isSuccess();

		} catch (final Exception e) {
			LOGGER.debug("Server availability check failed: {}", e.getMessage());
			return false;
		}
	}
}