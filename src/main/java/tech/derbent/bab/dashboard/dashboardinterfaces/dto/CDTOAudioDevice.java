package tech.derbent.bab.dashboard.dashboardinterfaces.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDTOAudioDevice - Audio device model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a single audio device with its configuration.
 * <p>
 * JSON structure from Calimero getAudioDevices:
 *
 * <pre>
 * {
 *   "card": "0",
 *   "device": "0",
 *   "name": "HDA Intel PCH",
 *   "description": "ALC887-VD Analog",
 *   "direction": "playback",
 *   "type": "hw",
 *   "channels": 2,
 *   "sample_rate": "44100",
 *   "available": true,
 *   "default_device": false
 * }
 * </pre>
 */
public class CDTOAudioDevice extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDTOAudioDevice.class);

	// Fields from Calimero API
	private String card = "";
	private String device = "";
	private String name = "";
	private String description = "";
	private String direction = "";
	private String type = "";
	private Integer channels = 0;
	private String sampleRate = "";
	private Boolean available = false;
	private Boolean defaultDevice = false;

	/**
	 * Create DTO from Calimero JSON response.
	 * @param json JsonObject from Calimero getAudioDevices operation
	 * @return CDTOAudioDevice instance
	 */
	public static CDTOAudioDevice createFromJson(final JsonObject json) {
		final CDTOAudioDevice device = new CDTOAudioDevice();
		device.fromJson(json);
		return device;
	}

	public CDTOAudioDevice() {
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
			if (json.has("card") && !json.get("card").isJsonNull()) {
				card = json.get("card").getAsString();
			}
			if (json.has("device") && !json.get("device").isJsonNull()) {
				device = json.get("device").getAsString();
			}
			if (json.has("name") && !json.get("name").isJsonNull()) {
				name = json.get("name").getAsString();
			}
			if (json.has("description") && !json.get("description").isJsonNull()) {
				description = json.get("description").getAsString();
			}
			if (json.has("direction") && !json.get("direction").isJsonNull()) {
				direction = json.get("direction").getAsString();
			}
			if (json.has("type") && !json.get("type").isJsonNull()) {
				type = json.get("type").getAsString();
			}
			if (json.has("channels") && !json.get("channels").isJsonNull()) {
				channels = json.get("channels").getAsInt();
			}
			if (json.has("sample_rate") && !json.get("sample_rate").isJsonNull()) {
				sampleRate = json.get("sample_rate").getAsString();
			}
			if (json.has("available") && !json.get("available").isJsonNull()) {
				available = json.get("available").getAsBoolean();
			}
			if (json.has("default_device") && !json.get("default_device").isJsonNull()) {
				defaultDevice = json.get("default_device").getAsBoolean();
			}
			
			LOGGER.debug("Parsed audio device: {} ({})", name, description);
		} catch (final Exception e) {
			LOGGER.error("Failed to parse audio device from JSON: {}", e.getMessage(), e);
		}
	}

	@Override
	protected String toJson() {
		return "{}"; // Not needed for read-only DTOs
	}

	// Getters
	public String getCard() { return card; }
	public String getDevice() { return device; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getDirection() { return direction; }
	public String getType() { return type; }
	public Integer getChannels() { return channels; }
	public String getSampleRate() { return sampleRate; }
	public Boolean getAvailable() { return available; }
	public Boolean getDefaultDevice() { return defaultDevice; }

	// Utility methods
	public String getDisplayName() {
		if (!name.isEmpty() && !description.isEmpty()) {
			return String.format("%s (%s)", name, description);
		}
		return name.isEmpty() ? description : name;
	}

	public String getDeviceId() {
		if (!card.isEmpty() && !device.isEmpty()) {
			return String.format("hw:%s,%s", card, device);
		}
		return card.isEmpty() ? device : card;
	}

	public String getStatusColor() {
		if (!available) {
			return "#f44336"; // Red - not available
		}
		if (defaultDevice) {
			return "#4caf50"; // Green - default device
		}
		if ("playback".equals(direction)) {
			return "#2196f3"; // Blue - playback
		}
		if ("capture".equals(direction)) {
			return "#ff9800"; // Orange - capture
		}
		return "#9e9e9e"; // Gray - other
	}

	public boolean isPlayback() {
		return "playback".equals(direction);
	}

	public boolean isCapture() {
		return "capture".equals(direction);
	}

	public boolean isHighQuality() {
		try {
			int rate = Integer.parseInt(sampleRate.replaceAll("[^0-9]", ""));
			return rate >= 48000;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("Audio Device[%s: %s (%s)]", getDeviceId(), name, direction);
	}
}