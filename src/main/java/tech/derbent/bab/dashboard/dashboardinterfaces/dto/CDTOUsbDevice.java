package tech.derbent.bab.dashboard.dashboardinterfaces.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDTOUsbDevice - USB device model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a single USB device with its configuration.
 * <p>
 * JSON structure from Calimero getUsbDevices:
 *
 * <pre>
 * {
 *   "port": "Port001",
 *   "bus": "003",
 *   "device": "013",
 *   "vendor_id": "0b05",
 *   "product_id": "1847",
 *   "class": "Audio",
 *   "driver": "snd-usb-audio",
 *   "speed": "12M",
 *   "name": "ASUSTek Computer, Inc. Jabra EVOLVE 20 MS",
 *   "device_path": "/dev/bus/usb/003/013"
 * }
 * </pre>
 */
public class CDTOUsbDevice extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDTOUsbDevice.class);

	// Fields from Calimero API
	private String port = "";
	private String bus = "";
	private String device = "";
	private String vendorId = "";
	private String productId = "";
	private String deviceClass = "";
	private String driver = "";
	private String speed = "";
	private String name = "";
	private String devicePath = "";

	/**
	 * Create DTO from Calimero JSON response.
	 * @param json JsonObject from Calimero getUsbDevices operation
	 * @return CDTOUsbDevice instance
	 */
	public static CDTOUsbDevice createFromJson(final JsonObject json) {
		final CDTOUsbDevice device = new CDTOUsbDevice();
		device.fromJson(json);
		return device;
	}

	public CDTOUsbDevice() {
		super();
	}

	@Override
	protected void fromJson(final JsonObject json) {
		try {
			if (json == null) {
				LOGGER.warn("Null JSON object passed to fromJson()");
				return;
			}

			// Parse fields with null checks
			if (json.has("port") && !json.get("port").isJsonNull()) {
				port = json.get("port").getAsString();
			}
			if (json.has("bus") && !json.get("bus").isJsonNull()) {
				bus = json.get("bus").getAsString();
			}
			if (json.has("device") && !json.get("device").isJsonNull()) {
				device = json.get("device").getAsString();
			}
			if (json.has("vendor_id") && !json.get("vendor_id").isJsonNull()) {
				vendorId = json.get("vendor_id").getAsString();
			}
			if (json.has("product_id") && !json.get("product_id").isJsonNull()) {
				productId = json.get("product_id").getAsString();
			}
			if (json.has("class") && !json.get("class").isJsonNull()) {
				deviceClass = json.get("class").getAsString();
			}
			if (json.has("driver") && !json.get("driver").isJsonNull()) {
				driver = json.get("driver").getAsString();
			}
			if (json.has("speed") && !json.get("speed").isJsonNull()) {
				speed = json.get("speed").getAsString();
			}
			if (json.has("name") && !json.get("name").isJsonNull()) {
				name = json.get("name").getAsString();
			}
			if (json.has("device_path") && !json.get("device_path").isJsonNull()) {
				devicePath = json.get("device_path").getAsString();
			}
			
			LOGGER.debug("Parsed USB device: {} ({})", name, port);
		} catch (final Exception e) {
			LOGGER.error("Failed to parse USB device from JSON: {}", e.getMessage(), e);
		}
	}

	@Override
	protected String toJson() {
		return "{}"; // Not needed for read-only DTOs
	}

	// Getters
	public String getPort() { return port; }
	public String getBus() { return bus; }
	public String getDevice() { return device; }
	public String getVendorId() { return vendorId; }
	public String getProductId() { return productId; }
	public String getDeviceClass() { return deviceClass; }
	public String getDriver() { return driver; }
	public String getSpeed() { return speed; }
	public String getName() { return name; }
	public String getDevicePath() { return devicePath; }

	// Utility methods
	public String getVendorProductId() {
		if (vendorId.isEmpty() || productId.isEmpty()) {
			return "Unknown";
		}
		return vendorId.toUpperCase() + ":" + productId.toUpperCase();
	}

	public boolean isHighSpeed() {
		return speed.contains("G") || speed.contains("480M");
	}

	public String getStatusColor() {
		if (driver.isEmpty() || "unknown".equals(driver)) {
			return "#f44336"; // Red - no driver
		}
		if (isHighSpeed()) {
			return "#4caf50"; // Green - high speed
		}
		return "#2196f3"; // Blue - normal
	}

	@Override
	public String toString() {
		return String.format("USB Device[%s: %s (%s)]", port, name, getVendorProductId());
	}
}