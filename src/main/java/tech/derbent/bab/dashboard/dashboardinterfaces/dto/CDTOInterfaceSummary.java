package tech.derbent.bab.dashboard.dashboardinterfaces.dto;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDTOInterfaceSummary - Complete interface summary from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents the complete system interface summary from getAllInterfaces operation.
 * <p>
 * JSON structure from Calimero getAllInterfaces:
 *
 * <pre>
 * {
 *   "network_interfaces": [...],
 *   "usb_devices": [...],
 *   "serial_ports": [...],
 *   "audio_devices": [...],
 *   "summary": {
 *     "total_interfaces": 42,
 *     "network_count": 3,
 *     "usb_count": 7,
 *     "serial_count": 32,
 *     "audio_count": 2
 *   },
 *   "timestamp": "2026-02-06T07:22:39.638Z"
 * }
 * </pre>
 */
public class CDTOInterfaceSummary extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDTOInterfaceSummary.class);

	// Collections from API
	private List<CDTOUsbDevice> usbDevices = new ArrayList<>();
	private List<CDTOSerialPort> serialPorts = new ArrayList<>();
	private List<CDTOAudioDevice> audioDevices = new ArrayList<>();
	
	// Summary counts
	private int totalInterfaces = 0;
	private int networkCount = 0;
	private int usbCount = 0;
	private int serialCount = 0;
	private int audioCount = 0;
	private String timestamp = "";

	/**
	 * Create DTO from Calimero JSON response.
	 * @param json JsonObject from Calimero getAllInterfaces operation
	 * @return CDTOInterfaceSummary instance
	 */
	public static CDTOInterfaceSummary createFromJson(final JsonObject json) {
		final CDTOInterfaceSummary summary = new CDTOInterfaceSummary();
		summary.fromJson(json);
		return summary;
	}

	public CDTOInterfaceSummary() {
		super();
	}

	@Override
	protected void fromJson(final JsonObject json) {
		try {
			if (json == null) {
				LOGGER.warn("Null JSON object passed to fromJson()");
				return;
			}

			// Parse USB devices
			if (json.has("usb_devices") && json.get("usb_devices").isJsonArray()) {
				final JsonArray usbArray = json.getAsJsonArray("usb_devices");
				for (final JsonElement element : usbArray) {
					if (element.isJsonObject()) {
						usbDevices.add(CDTOUsbDevice.createFromJson(element.getAsJsonObject()));
					}
				}
			}

			// Parse serial ports
			if (json.has("serial_ports") && json.get("serial_ports").isJsonArray()) {
				final JsonArray serialArray = json.getAsJsonArray("serial_ports");
				for (final JsonElement element : serialArray) {
					if (element.isJsonObject()) {
						serialPorts.add(CDTOSerialPort.createFromJson(element.getAsJsonObject()));
					}
				}
			}

			// Parse audio devices
			if (json.has("audio_devices") && json.get("audio_devices").isJsonArray()) {
				final JsonArray audioArray = json.getAsJsonArray("audio_devices");
				for (final JsonElement element : audioArray) {
					if (element.isJsonObject()) {
						audioDevices.add(CDTOAudioDevice.createFromJson(element.getAsJsonObject()));
					}
				}
			}

			// Parse summary section
			if (json.has("summary") && json.get("summary").isJsonObject()) {
				final JsonObject summaryObj = json.getAsJsonObject("summary");
				
				if (summaryObj.has("total_interfaces") && !summaryObj.get("total_interfaces").isJsonNull()) {
					totalInterfaces = summaryObj.get("total_interfaces").getAsInt();
				}
				if (summaryObj.has("network_count") && !summaryObj.get("network_count").isJsonNull()) {
					networkCount = summaryObj.get("network_count").getAsInt();
				}
				if (summaryObj.has("usb_count") && !summaryObj.get("usb_count").isJsonNull()) {
					usbCount = summaryObj.get("usb_count").getAsInt();
				}
				if (summaryObj.has("serial_count") && !summaryObj.get("serial_count").isJsonNull()) {
					serialCount = summaryObj.get("serial_count").getAsInt();
				}
				if (summaryObj.has("audio_count") && !summaryObj.get("audio_count").isJsonNull()) {
					audioCount = summaryObj.get("audio_count").getAsInt();
				}
			} else {
				// Fallback: calculate from parsed data
				usbCount = usbDevices.size();
				serialCount = serialPorts.size();
				audioCount = audioDevices.size();
				totalInterfaces = networkCount + usbCount + serialCount + audioCount;
			}

			if (json.has("timestamp") && !json.get("timestamp").isJsonNull()) {
				timestamp = json.get("timestamp").getAsString();
			}
			
			LOGGER.debug("Parsed interface summary: total={}, usb={}, serial={}, network={}, audio={}", 
				totalInterfaces, usbCount, serialCount, networkCount, audioCount);
		} catch (final Exception e) {
			LOGGER.error("Failed to parse interface summary from JSON: {}", e.getMessage(), e);
		}
	}

	@Override
	protected String toJson() {
		return "{}"; // Not needed for read-only DTOs
	}

	// Getters
	public List<CDTOUsbDevice> getUsbDevices() { return usbDevices; }
	public List<CDTOSerialPort> getSerialPorts() { return serialPorts; }
	public List<CDTOAudioDevice> getAudioDevices() { return audioDevices; }
	public int getTotalInterfaces() { return totalInterfaces; }
	public int getNetworkCount() { return networkCount; }
	public int getUsbCount() { return usbCount; }
	public int getSerialCount() { return serialCount; }
	public int getAudioCount() { return audioCount; }
	public String getTimestamp() { return timestamp; }

	// Utility methods
	public String getFormattedSummary() {
		return String.format("%d interfaces (Network: %d, USB: %d, Serial: %d, Audio: %d)",
			totalInterfaces, networkCount, usbCount, serialCount, audioCount);
	}

	public boolean hasData() {
		return totalInterfaces > 0 || !usbDevices.isEmpty() || !serialPorts.isEmpty();
	}

	public int getActiveUsbDevicesCount() {
		return (int) usbDevices.stream()
			.filter(device -> !device.getDriver().isEmpty() && !"unknown".equals(device.getDriver()))
			.count();
	}

	public int getAvailableSerialPortsCount() {
		return (int) serialPorts.stream()
			.filter(port -> port.getAvailable())
			.count();
	}

	public int getAvailableAudioDevicesCount() {
		return (int) audioDevices.stream()
			.filter(device -> Boolean.TRUE.equals(device.getAvailable()))
			.count();
	}

	@Override
	public String toString() {
		return String.format("Interface Summary[%s]", getFormattedSummary());
	}
}