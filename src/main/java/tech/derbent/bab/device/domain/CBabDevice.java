package tech.derbent.bab.device.domain;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.base.users.domain.CUser;

/** CBabDevice - IoT gateway device entity. Following Derbent pattern: Entity extends CEntityOfCompany. Represents single unique device instance per
 * company. */
@Entity
@Table (name = "cbab_device", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"company_id"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "device_id"))
public class CBabDevice extends CEntityOfCompany<CBabDevice> {

	public static final String DEFAULT_COLOR = "#6B5FA7";
	public static final String DEFAULT_ICON = "vaadin:server";
	public static final String ENTITY_TITLE_PLURAL = "Devices";
	public static final String ENTITY_TITLE_SINGULAR = "Device";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabDevice.class);
	@SuppressWarnings ("unused")
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Device Management";
	@Column (name = "serial_number", nullable = true, length = 255, unique = true)
	@Size (max = 255)
	@AMetaData (
			displayName = "Serial Number", required = false, readOnly = false, description = "Device serial number", hidden = false, maxLength = 255
	)
	private String serialNumber;
	@Column (name = "firmware_version", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Firmware Version", required = false, readOnly = false, description = "Current firmware version", hidden = false,
			maxLength = 100
	)
	private String firmwareVersion;
	@Column (name = "hardware_revision", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Hardware Revision", required = false, readOnly = false, description = "Hardware revision", hidden = false, maxLength = 100
	)
	private String hardwareRevision;
	@Column (name = "device_status", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current device status (Online, Offline, Error)",
			hidden = false, maxLength = 50
	)
	private String deviceStatus;
	@Column (name = "last_seen", nullable = true)
	@AMetaData (displayName = "Last Seen", required = false, readOnly = true, description = "Last time device was online", hidden = false)
	private LocalDateTime lastSeen;
	@Column (name = "ip_address", nullable = true, length = 45)
	@Size (max = 45)
	@AMetaData (displayName = "IP Address", required = false, readOnly = false, description = "Device IP address", hidden = false, maxLength = 45)
	private String ipAddress;
	@Column (name = "mac_address", nullable = true, length = 17)
	@Size (max = 17)
	@AMetaData (displayName = "MAC Address", required = false, readOnly = false, description = "Device MAC address", hidden = false, maxLength = 17)
	private String macAddress;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "created_by_id", nullable = true)
	@AMetaData (
			displayName = "Created By", required = false, readOnly = true, description = "User who created this device", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser createdBy;

	/** Default constructor for JPA. */
	public CBabDevice() {
		super();
	}

	public CBabDevice(final String name, final CCompany company) {
		super(CBabDevice.class, name, company);
	}

	public CUser getCreatedBy() { return createdBy; }

	public String getDeviceStatus() { return deviceStatus; }

	public String getFirmwareVersion() { return firmwareVersion; }

	public String getHardwareRevision() { return hardwareRevision; }

	public String getIpAddress() { return ipAddress; }

	public LocalDateTime getLastSeen() { return lastSeen; }

	public String getMacAddress() { return macAddress; }

	// Getters and Setters
	public String getSerialNumber() { return serialNumber; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (deviceStatus == null) {
			deviceStatus = "Offline";
		}
	}

	public void setCreatedBy(final CUser createdBy) { this.createdBy = createdBy; }

	public void setDeviceStatus(final String deviceStatus) {
		this.deviceStatus = deviceStatus;
		updateLastModified();
	}

	public void setFirmwareVersion(final String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
		updateLastModified();
	}

	public void setHardwareRevision(final String hardwareRevision) {
		this.hardwareRevision = hardwareRevision;
		updateLastModified();
	}

	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
		updateLastModified();
	}

	public void setLastSeen(final LocalDateTime lastSeen) {
		this.lastSeen = lastSeen;
		updateLastModified();
	}

	public void setMacAddress(final String macAddress) {
		this.macAddress = macAddress;
		updateLastModified();
	}

	public void setSerialNumber(final String serialNumber) {
		this.serialNumber = serialNumber;
		updateLastModified();
	}
}
