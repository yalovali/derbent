package tech.derbent.bab.dashboard.dashboardinterfaces.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDTOSerialPort - Serial port model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a single serial port with its configuration.
 * <p>
 * JSON structure from Calimero getSerialPorts:
 *
 * <pre>
 * {
 *   "port": "/dev/ttyS0",
 *   "type": "8250",
 *   "device": "ttyS0",
 *   "description": "16550A UART",
 *   "vendor": "Unknown",
 *   "product": "Unknown",
 *   "serial_number": "",
 *   "location": "0000:00:16.3",
 *   "manufacturer": "Intel",
 *   "available": true
 * }
 * </pre>
 */
public class CDTOSerialPort extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDTOSerialPort.class);

	// Fields from Calimero API
	private String port = "";
	private String type = "";
	private String device = "";
	private String description = "";
	private String vendor = "";
	private String product = "";
	private String serialNumber = "";
	private String location = "";
	private String manufacturer = "";
	private Boolean available = false;

	/**
	 * Create DTO from Calimero JSON response.
	 * @param json JsonObject from Calimero getSerialPorts operation
	 * @return CDTOSerialPort instance
	 */
	public static CDTOSerialPort createFromJson(final JsonObject json) {
		final CDTOSerialPort port = new CDTOSerialPort();
		port.fromJson(json);
		return port;
	}

	public CDTOSerialPort() {
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
			if (json.has("type") && !json.get("type").isJsonNull()) {
				type = json.get("type").getAsString();
			}
			if (json.has("device") && !json.get("device").isJsonNull()) {
				device = json.get("device").getAsString();
			}
			if (json.has("description") && !json.get("description").isJsonNull()) {
				description = json.get("description").getAsString();
			}
			if (json.has("vendor") && !json.get("vendor").isJsonNull()) {
				vendor = json.get("vendor").getAsString();
			}
			if (json.has("product") && !json.get("product").isJsonNull()) {
				product = json.get("product").getAsString();
			}
			if (json.has("serial_number") && !json.get("serial_number").isJsonNull()) {
				serialNumber = json.get("serial_number").getAsString();
			}
			if (json.has("location") && !json.get("location").isJsonNull()) {
				location = json.get("location").getAsString();
			}
			if (json.has("manufacturer") && !json.get("manufacturer").isJsonNull()) {
				manufacturer = json.get("manufacturer").getAsString();
			}
			if (json.has("available") && !json.get("available").isJsonNull()) {
				available = json.get("available").getAsBoolean();
			}
			
			LOGGER.debug("Parsed serial port: {} ({})", device, type);
		} catch (final Exception e) {
			LOGGER.error("Failed to parse serial port from JSON: {}", e.getMessage(), e);
		}
	}

	@Override
	protected String toJson() {
		return "{}"; // Not needed for read-only DTOs
	}

	// Getters
	public String getPort() { return port; }
	public String getType() { return type; }
	public String getDevice() { return device; }
	public String getDescription() { return description; }
	public String getVendor() { return vendor; }
	public String getProduct() { return product; }
	public String getSerialNumber() { return serialNumber; }
	public String getLocation() { return location; }
	public String getManufacturer() { return manufacturer; }
	public Boolean getAvailable() { return available; }

	// Utility methods
	public String getDisplayName() {
		if (!description.isEmpty() && !"Unknown".equals(description)) {
			return String.format("%s (%s)", device, description);
		}
		return device;
	}

	public String getVendorInfo() {
		if (!vendor.isEmpty() && !"Unknown".equals(vendor)) {
			if (!product.isEmpty() && !"Unknown".equals(product)) {
				return vendor + " " + product;
			}
			return vendor;
		}
		if (!manufacturer.isEmpty() && !"Unknown".equals(manufacturer)) {
			return manufacturer;
		}
		return "Unknown Manufacturer";
	}

	public String getStatusColor() {
		if (!available) {
			return "#f44336"; // Red - not available
		}
		if ("USB".equalsIgnoreCase(type) || type.contains("USB")) {
			return "#4caf50"; // Green - USB serial (modern)
		}
		if ("8250".equals(type) || "16550A".equals(type)) {
			return "#2196f3"; // Blue - classic UART
		}
		return "#ff9800"; // Orange - other types
	}

	public boolean isUsbSerial() {
		return "USB".equalsIgnoreCase(type) || type.contains("USB") || 
		       description.toLowerCase().contains("usb") ||
		       vendor.toLowerCase().contains("usb");
	}

	@Override
	public String toString() {
		return String.format("Serial Port[%s: %s (%s)]", device, description, type);
	}
}