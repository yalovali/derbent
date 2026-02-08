package tech.derbent.bab.project.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.projects.service.CProjectTypeService;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOAudioDevice;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOSerialPort;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOUsbDevice;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkInterface;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.base.session.service.ISessionService;

@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CProject_BabService extends CProjectService<CProject_Bab> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_BabService.class);
	private static final Gson GSON = new Gson();

	public CProject_BabService(final IProject_BabRepository repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher, final CProjectTypeService projectTypeService,
			final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, eventPublisher, projectTypeService, statusService);
	}

	@Override
	public Class<CProject_Bab> getEntityClass() { return CProject_Bab.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProject_BabInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProject_Bab.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public CProject_Bab newEntity() {
		// Constructor already calls initializeDefaults() which calls initializeNewEntity()
		// No need to call initializeNewEntity() again - that would be double initialization
		return new CProject_Bab("New BAB Project",
				sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company for BAB project creation")));
	}

	@Override
	protected void validateEntity(final CProject_Bab entity) {
		super.validateEntity(entity);
		// IP Address Validation
		if (!(entity.getIpAddress() != null && !entity.getIpAddress().isBlank())) {
			return;
		}
		// Use validateStringLength helper for length validation
		validateStringLength(entity.getIpAddress(), "IP Address", 45);
		// Regex for IPv4 or IPv6
		final String ipRegex =
				"^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$";
		if (!entity.getIpAddress().matches(ipRegex)) {
			throw new CValidationException("Invalid IP address format (IPv4 or IPv6)");
		}
	}

	// ==========================================
	// Centralized Interface JSON Management
	// ==========================================

	/**
	 * Refresh all interface data from Calimero server and save to JSON field.
	 * <p>
	 * This is the ONLY method that should call Calimero for interface data.
	 * All interface components should call this method for refresh, then parse
	 * the stored JSON for their specific interface type.
	 * <p>
	 * Calimero API: POST /api/request with type="iot", operation="getAllInterfaces"
	 * 
	 * @param project the BAB project to refresh interfaces for
	 * @return true if refresh succeeded, false otherwise
	 */
	@Transactional
	public boolean refreshInterfacesJson(final CProject_Bab project) {
		LOGGER.info("🔄 Refreshing interfaces JSON for project '{}'", project.getName());

		try {
			// Get HTTP client
			final CClientProject client = project.getHttpClient();
			if (client == null || !client.isConnected()) {
				LOGGER.warn("⚠️ Cannot refresh interfaces - Calimero not connected for project '{}'", project.getName());
				return false;
			}

			// Call Calimero API to get ALL interfaces
			final CCalimeroRequest request = CCalimeroRequest.builder()
				.type("iot")
				.operation("getAllInterfaces")
				.build();

			final CCalimeroResponse response = client.sendRequest(request);

			if (response.isSuccess()) {
				// Store raw JSON response
				final String jsonData = GSON.toJson(response.getData());
				project.setInterfacesJson(jsonData);
				project.setInterfacesLastUpdated(LocalDateTime.now());
				save(project);

				LOGGER.info("✅ Interfaces JSON refreshed for project '{}' - {} bytes", project.getName(), jsonData.length());
				return true;
			} else {
				LOGGER.error("❌ Failed to refresh interfaces for project '{}': {}", project.getName(), response.getErrorMessage());
				return false;
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Error refreshing interfaces JSON for project '{}'", project.getName(), e);
			return false;
		}
	}

	/**
	 * Get network interfaces from cached JSON.
	 * @param project the BAB project
	 * @return list of network interfaces
	 */
	public List<CDTONetworkInterface> getNetworkInterfaces(final CProject_Bab project) {
		return parseInterfacesJson(project, "system_interfaces.network_interfaces", CDTONetworkInterface[].class);
	}

	/**
	 * Get USB devices from cached JSON.
	 * @param project the BAB project
	 * @return list of USB devices
	 */
	public List<CDTOUsbDevice> getUsbDevices(final CProject_Bab project) {
		return parseInterfacesJson(project, "system_interfaces.usb_devices", CDTOUsbDevice[].class);
	}

	/**
	 * Get serial ports from cached JSON.
	 * @param project the BAB project
	 * @return list of serial ports
	 */
	public List<CDTOSerialPort> getSerialPorts(final CProject_Bab project) {
		return parseInterfacesJson(project, "system_interfaces.serial_ports", CDTOSerialPort[].class);
	}

	/**
	 * Get audio devices from cached JSON.
	 * @param project the BAB project
	 * @return list of audio devices
	 */
	public List<CDTOAudioDevice> getAudioDevices(final CProject_Bab project) {
		return parseInterfacesJson(project, "system_interfaces.audio_devices", CDTOAudioDevice[].class);
	}

	/**
	 * Get interface summary counts from cached JSON.
	 * @param project the BAB project
	 * @return map of interface type to count
	 */
	public InterfaceSummary getInterfaceSummary(final CProject_Bab project) {
		try {
			final String json = project.getInterfacesJson();
			if (json == null || json.isBlank() || "{}".equals(json)) {
				return new InterfaceSummary();
			}

			final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			if (root.has("summary")) {
				final JsonObject summary = root.getAsJsonObject("summary");
				return new InterfaceSummary(
					summary.has("usb_count") ? summary.get("usb_count").getAsInt() : 0,
					summary.has("audio_count") ? summary.get("audio_count").getAsInt() : 0,
					summary.has("serial_count") ? summary.get("serial_count").getAsInt() : 0,
					summary.has("network_count") ? summary.get("network_count").getAsInt() : 0,
					summary.has("video_count") ? summary.get("video_count").getAsInt() : 0,
					summary.has("gpio_available") && summary.get("gpio_available").getAsBoolean()
				);
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Error parsing interface summary: {}", e.getMessage());
		}
		return new InterfaceSummary();
	}

	/**
	 * Generic JSON parsing helper for interface data.
	 * @param project the BAB project
	 * @param jsonPath dot-separated path (e.g., "system_interfaces.usb_devices")
	 * @param arrayClass the array class to deserialize to
	 * @return list of parsed objects
	 */
	private <T> List<T> parseInterfacesJson(final CProject_Bab project, final String jsonPath, final Class<T[]> arrayClass) {
		try {
			final String json = project.getInterfacesJson();
			if (json == null || json.isBlank() || "{}".equals(json)) {
				return Collections.emptyList();
			}

			JsonElement element = JsonParser.parseString(json);

			// Navigate JSON path (e.g., "system_interfaces.usb_devices")
			for (final String key : jsonPath.split("\\.")) {
				if (element.isJsonObject()) {
					element = element.getAsJsonObject().get(key);
					if (element == null) {
						return Collections.emptyList();
					}
				}
			}

			if (element.isJsonArray()) {
				final T[] array = GSON.fromJson(element, arrayClass);
				return new ArrayList<>(Arrays.asList(array));
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Error parsing interfaces JSON at path '{}': {}", jsonPath, e.getMessage());
		}
		return Collections.emptyList();
	}

	/**
	 * Interface summary data class.
	 */
	public static class InterfaceSummary {
		private final int usbCount;
		private final int audioCount;
		private final int serialCount;
		private final int networkCount;
		private final int videoCount;
		private final boolean gpioAvailable;

		public InterfaceSummary() {
			this(0, 0, 0, 0, 0, false);
		}

		public InterfaceSummary(final int usbCount, final int audioCount, final int serialCount,
				final int networkCount, final int videoCount, final boolean gpioAvailable) {
			this.usbCount = usbCount;
			this.audioCount = audioCount;
			this.serialCount = serialCount;
			this.networkCount = networkCount;
			this.videoCount = videoCount;
			this.gpioAvailable = gpioAvailable;
		}

		public int getUsbCount() { return usbCount; }
		public int getAudioCount() { return audioCount; }
		public int getSerialCount() { return serialCount; }
		public int getNetworkCount() { return networkCount; }
		public int getVideoCount() { return videoCount; }
		public boolean isGpioAvailable() { return gpioAvailable; }
		public int getTotalCount() { return usbCount + audioCount + serialCount + networkCount + videoCount; }
	}
}
