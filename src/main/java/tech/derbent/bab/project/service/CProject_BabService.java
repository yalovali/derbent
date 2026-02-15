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
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.projects.service.CProjectTypeService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOAudioDevice;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOSerialPort;
import tech.derbent.bab.dashboard.dashboardinterfaces.dto.CDTOUsbDevice;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CBabPolicyRule;
import tech.derbent.bab.dashboard.dashboardpolicy.service.CBabPolicyRuleService;
import tech.derbent.bab.dashboard.dashboardproject_bab.dto.CDTONetworkInterface;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.project.domain.CProject_Bab;

@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CProject_BabService extends CProjectService<CProject_Bab> {

	/** Interface summary data class. */
	public static class InterfaceSummary {

		private final int audioCount;
		private final boolean gpioAvailable;
		private final int networkCount;
		private final int serialCount;
		private final int usbCount;
		private final int videoCount;

		public InterfaceSummary() {
			this(0, 0, 0, 0, 0, false);
		}

		public InterfaceSummary(final int usbCount, final int audioCount, final int serialCount, final int networkCount, final int videoCount,
				final boolean gpioAvailable) {
			this.usbCount = usbCount;
			this.audioCount = audioCount;
			this.serialCount = serialCount;
			this.networkCount = networkCount;
			this.videoCount = videoCount;
			this.gpioAvailable = gpioAvailable;
		}

		public int getAudioCount() { return audioCount; }

		public int getNetworkCount() { return networkCount; }

		public int getSerialCount() { return serialCount; }

		public int getTotalCount() { return usbCount + audioCount + serialCount + networkCount + videoCount; }

		public int getUsbCount() { return usbCount; }

		public int getVideoCount() { return videoCount; }

		public boolean isGpioAvailable() { return gpioAvailable; }
	}

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_BabService.class);

	/** Data provider callback used by CProject_Bab @AMetaData(autoCalculate=true) for policyRules.
	 * @param project the BAB project
	 * @return policy rules in the same project */
	public static List<CBabPolicyRule> updatePolicyRules(final CProject_Bab project) {
		if (project == null || project.getId() == null) {
			return Collections.emptyList();
		}
		// return CSpringContext.getBean(CBabPolicyRuleService.class).listByProject(project);
		project.setPolicyRules(CSpringContext.getBean(CBabPolicyRuleService.class).listByProject(project));
		return project.getPolicyRules();
	}

	public CProject_BabService(final IProject_BabRepository repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher, final CProjectTypeService projectTypeService,
			final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, eventPublisher, projectTypeService, statusService);
	}

	/** Get audio devices from cached JSON.
	 * @param project the BAB project
	 * @return list of audio devices */
	public List<CDTOAudioDevice> getAudioDevices(final CProject_Bab project) {
		return parseInterfacesJson(project, "system_interfaces.audio_devices", CDTOAudioDevice[].class);
	}

	@Override
	public Class<CProject_Bab> getEntityClass() { return CProject_Bab.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProject_BabInitializerService.class; }

	/** Get interface summary counts from cached JSON.
	 * @param project the BAB project
	 * @return map of interface type to count */
	public InterfaceSummary getInterfaceSummary(final CProject_Bab project) {
		try {
			final String json = project.getInterfacesJson();
			if (json == null || json.isBlank() || "{}".equals(json)) {
				return new InterfaceSummary();
			}
			final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			if (root.has("summary")) {
				final JsonObject summary = root.getAsJsonObject("summary");
				return new InterfaceSummary(summary.has("usb_count") ? summary.get("usb_count").getAsInt() : 0,
						summary.has("audio_count") ? summary.get("audio_count").getAsInt() : 0,
						summary.has("serial_count") ? summary.get("serial_count").getAsInt() : 0,
						summary.has("network_count") ? summary.get("network_count").getAsInt() : 0,
						summary.has("video_count") ? summary.get("video_count").getAsInt() : 0,
						summary.has("gpio_available") && summary.get("gpio_available").getAsBoolean());
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Error parsing interface summary: {}", e.getMessage());
		}
		return new InterfaceSummary();
	}

	/** Get network interfaces from cached JSON.
	 * @param project the BAB project
	 * @return list of network interfaces */
	public List<CDTONetworkInterface> getNetworkInterfaces(final CProject_Bab project) {
		return parseInterfacesJson(project, "system_interfaces.network_interfaces", CDTONetworkInterface[].class);
	}
	// ==========================================
	// Centralized Interface JSON Management
	// ==========================================

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProject_Bab.class; }

	/** Get serial ports from cached JSON.
	 * @param project the BAB project
	 * @return list of serial ports */
	public List<CDTOSerialPort> getSerialPorts(final CProject_Bab project) {
		return parseInterfacesJson(project, "system_interfaces.serial_ports", CDTOSerialPort[].class);
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Get USB devices from cached JSON.
	 * @param project the BAB project
	 * @return list of USB devices */
	public List<CDTOUsbDevice> getUsbDevices(final CProject_Bab project) {
		return parseInterfacesJson(project, "system_interfaces.usb_devices", CDTOUsbDevice[].class);
	}

	@Override
	public CProject_Bab newEntity() {
		// Constructor already calls initializeDefaults() which calls initializeNewEntity()
		// No need to call initializeNewEntity() again - that would be double initialization
		return new CProject_Bab("New BAB Project",
				sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company for BAB project creation")));
	}

	/** Generic JSON parsing helper for interface data.
	 * @param project    the BAB project
	 * @param jsonPath   dot-separated path (e.g., "system_interfaces.usb_devices")
	 * @param arrayClass the array class to deserialize to
	 * @return list of parsed objects */
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
				// ✅ FIX: Parse each JSON object using DTO's fromJson() method
				// This ensures proper field mapping (ip_address, mac_address, etc.)
				return parseJsonArray(element.getAsJsonArray(), arrayClass);
			}
		} catch (final Exception e) {
			LOGGER.error("❌ Error parsing interfaces JSON at path '{}': {}", jsonPath, e.getMessage());
		}
		return Collections.emptyList();
	}

	/** Parse JSON array using DTO's fromJson() method. This is critical for DTOs like CDTONetworkInterface that have custom field mapping.
	 * @param <T>        DTO type
	 * @param jsonArray  JSON array to parse
	 * @param arrayClass array class (e.g., CDTONetworkInterface[].class)
	 * @return list of parsed DTOs */
	@SuppressWarnings ("unchecked")
	private <T> List<T> parseJsonArray(final com.google.gson.JsonArray jsonArray, final Class<T[]> arrayClass) {
		final List<T> result = new ArrayList<>();
		final Class<?> componentType = arrayClass.getComponentType();
		// Check if DTO has createFromJson factory method
		try {
			final java.lang.reflect.Method factoryMethod = componentType.getMethod("createFromJson", JsonObject.class);
			// Use factory method if available (e.g., CDTONetworkInterface.createFromJson)
			for (final JsonElement element : jsonArray) {
				if (element.isJsonObject()) {
					final T dto = (T) factoryMethod.invoke(null, element.getAsJsonObject());
					result.add(dto);
				}
			}
			return result;
		} catch (final NoSuchMethodException e) {
			// No factory method - fall back to GSON direct parsing
			LOGGER.debug("No createFromJson factory for {}, using GSON", componentType.getSimpleName());
			final T[] array = GSON.fromJson(jsonArray, arrayClass);
			return new ArrayList<>(Arrays.asList(array));
		} catch (final Exception e) {
			LOGGER.error("Error parsing JSON array using factory method: {}", e.getMessage());
			// Fall back to GSON
			final T[] array = GSON.fromJson(jsonArray, arrayClass);
			return new ArrayList<>(Arrays.asList(array));
		}
	}

	/** Refresh all interface data from Calimero server and save to JSON field.
	 * <p>
	 * This is the ONLY method that should call Calimero for interface data. All interface components should call this method for refresh, then parse
	 * the stored JSON for their specific interface type.
	 * <p>
	 * Calimero API: POST /api/request with type="iot", operation="getAllInterfaces"
	 * @param project the BAB project to refresh interfaces for
	 * @return true if refresh succeeded, false otherwise */
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
			final CCalimeroRequest request = CCalimeroRequest.builder().type("iot").operation("getAllInterfaces").build();
			final CCalimeroResponse response = client.sendRequest(request);
			if (response.isSuccess()) {
				// Store raw JSON response
				final String jsonData = GSON.toJson(response.getData());
				project.setInterfacesJson(jsonData);
				project.setInterfacesLastUpdated(LocalDateTime.now());
				save(project);
				LOGGER.info("✅ Interfaces JSON refreshed for project '{}' - {} bytes", project.getName(), jsonData.length());
				return true;
			}
			LOGGER.error("❌ Failed to refresh interfaces for project '{}': {}", project.getName(), response.getErrorMessage());
			return false;
		} catch (final Exception e) {
			LOGGER.error("❌ Error refreshing interfaces JSON for project '{}'", project.getName(), e);
			return false;
		}
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
}
