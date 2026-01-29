package tech.derbent.bab.setup.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.base.setup.domain.CSystemSettings;

/** CSystemSettings_Bab - BAB IoT Gateway-specific system settings. Layer: Domain (MVC) Active when: 'bab' profile is active Minimal configuration for
 * IoT gateway functionality: - Basic application identity - Network/connectivity settings - Essential security - Simple file management. Follows
 * Derbent pattern: Concrete class marked final. */
@Entity
@Table (name = "csystemsettings_bab", uniqueConstraints = {
		@jakarta.persistence.UniqueConstraint (columnNames = {
				"application_name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "system_settings_id"))
@DiscriminatorValue ("BAB")
public final class CSystemSettings_Bab extends CSystemSettings<CSystemSettings_Bab> {

	public static final String DEFAULT_COLOR = "#FF5722"; // Material Orange - IoT/hardware focused
	public static final String DEFAULT_ICON = "vaadin:cogs";
	public static final String ENTITY_TITLE_PLURAL = "BAB Gateway Settings";
	public static final String ENTITY_TITLE_SINGULAR = "BAB Gateway Settings";
	public static final String VIEW_NAME = "BAB Gateway Settings View";
	@Column (name = "device_scan_interval_seconds", nullable = false)
	@Min (value = 5, message = "Device scan interval must be at least 5 seconds")
	@Max (value = 3600, message = "Device scan interval cannot exceed 3600 seconds (1 hour)")
	@AMetaData (
			displayName = "Device Scan Interval (Seconds)", required = true, readOnly = false, defaultValue = "30",
			description = "How often to scan for connected IoT devices", hidden = false
	)
	private Integer deviceScanIntervalSeconds = 30;
	@Column (name = "enable_device_auto_discovery", nullable = false)
	@AMetaData (
			displayName = "Enable Device Auto-Discovery", required = true, readOnly = false, defaultValue = "true",
			description = "Automatically discover and register new IoT devices", hidden = false
	)
	private Boolean enableDeviceAutoDiscovery = Boolean.TRUE;
	// Gateway-specific fields for IoT connectivity
	// Gateway-specific fields for IoT connectivity
	@Column (name = "gateway_ip_address", length = 45) // 45 chars supports IPv6
	@Pattern (
			regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$|^$",
			message = "Invalid IP address format (IPv4 or IPv6)"
	)
	@AMetaData (
			displayName = "Gateway IP Address", required = false, readOnly = false, description = "Primary IP address of the IoT gateway",
			hidden = false, maxLength = 45
	)
	private String gatewayIpAddress = "";
	@Column (name = "gateway_port", nullable = false)
	@Min (value = 1024, message = "Gateway port must be at least 1024")
	@Max (value = 65535, message = "Gateway port must be valid")
	@AMetaData (
			displayName = "Gateway Port", required = true, readOnly = false, defaultValue = "8080",
			description = "Primary communication port for IoT gateway", hidden = false
	)
	private Integer gatewayPort = 8080;
	@Column (name = "max_concurrent_connections", nullable = false)
	@Min (value = 1, message = "Max concurrent connections must be at least 1")
	@Max (value = 1000, message = "Max concurrent connections cannot exceed 1000")
	@AMetaData (
			displayName = "Max Concurrent Connections", required = true, readOnly = false, defaultValue = "50",
			description = "Maximum number of concurrent device connections", hidden = false
	)
	private Integer maxConcurrentConnections = 50;

	/** Default constructor for JPA. */
	protected CSystemSettings_Bab() {}

	/** Business constructor for creating new BAB gateway settings. */
	public CSystemSettings_Bab(final String applicationName) {
		super(CSystemSettings_Bab.class, applicationName);
		initializeDefaults();
	}


	public Integer getDeviceScanIntervalSeconds() { return deviceScanIntervalSeconds; }

	public Boolean getEnableDeviceAutoDiscovery() { return enableDeviceAutoDiscovery; }

	public String getGatewayIpAddress() { return gatewayIpAddress; }

	public Integer getGatewayPort() { return gatewayPort; }

	public Integer getMaxConcurrentConnections() { return maxConcurrentConnections; }

	private final void initializeDefaults() {
		// Gateway-specific defaults for IoT
		setApplicationName("BAB IoT Gateway");
		setApplicationDescription("IoT device gateway and management interface");
		setDefaultSystemTheme("lumo");
		setDefaultLoginView("dashboard");
		// Simplified settings for gateway environment
		setSessionTimeoutMinutes(240); // 4 hours for long-running operations
		setMaxLoginAttempts(5);
		setEnableAutomaticBackups(Boolean.FALSE); // Gateway typically doesn't need backups
		setEnableFileVersioning(Boolean.FALSE); // Simplified file management
		setShowSystemInfo(Boolean.TRUE); // Important for gateway monitoring
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public Boolean isEnableDeviceAutoDiscovery() { return enableDeviceAutoDiscovery; }

	public void setDeviceScanIntervalSeconds(final Integer deviceScanIntervalSeconds) { this.deviceScanIntervalSeconds = deviceScanIntervalSeconds; }

	public void setEnableDeviceAutoDiscovery(final Boolean enableDeviceAutoDiscovery) { this.enableDeviceAutoDiscovery = enableDeviceAutoDiscovery; }

	public void setGatewayIpAddress(final String gatewayIpAddress) { this.gatewayIpAddress = gatewayIpAddress; }

	public void setGatewayPort(final Integer gatewayPort) { this.gatewayPort = gatewayPort; }

	public void setMaxConcurrentConnections(final Integer maxConcurrentConnections) { this.maxConcurrentConnections = maxConcurrentConnections; }

	@Override
	public String toString() {
		return "CSystemSettings_Bab{" + "applicationName='" + getApplicationName() + '\'' + ", gatewayIpAddress='" + gatewayIpAddress + '\''
				+ ", gatewayPort=" + gatewayPort + ", deviceScanIntervalSeconds=" + deviceScanIntervalSeconds + ", maxConcurrentConnections="
				+ maxConcurrentConnections + ", enableDeviceAutoDiscovery=" + enableDeviceAutoDiscovery + '}';
	}
}
