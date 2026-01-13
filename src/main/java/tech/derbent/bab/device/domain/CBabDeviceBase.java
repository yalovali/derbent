package tech.derbent.bab.device.domain;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.companies.domain.CCompany;

/**
 * CBabDeviceBase - Abstract base class for BAB IoT gateway devices.
 * Provides common device properties like serial number, firmware version, and status.
 */
@MappedSuperclass
public abstract class CBabDeviceBase extends CBabItem<CBabDevice> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabDeviceBase.class);

	@Column(name = "serial_number", nullable = true, length = 255, unique = true)
	@Size(max = 255)
	@AMetaData(
		displayName = "Serial Number", required = false, readOnly = false, description = "Device serial number", hidden = false, maxLength = 255
	)
	private String serialNumber;

	@Column(name = "firmware_version", nullable = true, length = 100)
	@Size(max = 100)
	@AMetaData(
		displayName = "Firmware Version", required = false, readOnly = false, description = "Current firmware version", hidden = false,
		maxLength = 100
	)
	private String firmwareVersion;

	@Column(name = "hardware_revision", nullable = true, length = 100)
	@Size(max = 100)
	@AMetaData(
		displayName = "Hardware Revision", required = false, readOnly = false, description = "Hardware revision", hidden = false, maxLength = 100
	)
	private String hardwareRevision;

	@Column(name = "device_status", nullable = true, length = 50)
	@Size(max = 50)
	@AMetaData(
		displayName = "Status", required = false, readOnly = false, description = "Current device status (Online, Offline, Error, etc.)",
		hidden = false, maxLength = 50
	)
	private String deviceStatus;

	@Column(name = "last_seen", nullable = true)
	@AMetaData(
		displayName = "Last Seen", required = false, readOnly = true, description = "Last time device was online", hidden = false
	)
	private LocalDateTime lastSeen;

	@Column(name = "ip_address", nullable = true, length = 45)
	@Size(max = 45)
	@AMetaData(displayName = "IP Address", required = false, readOnly = false, description = "Device IP address", hidden = false, maxLength = 45)
	private String ipAddress;

	@Column(name = "mac_address", nullable = true, length = 17)
	@Size(max = 17)
	@AMetaData(
		displayName = "MAC Address", required = false, readOnly = false, description = "Device MAC address", hidden = false, maxLength = 17
	)
	private String macAddress;

	/** Default constructor for JPA. */
	protected CBabDeviceBase() {
		super();
	}

	public CBabDeviceBase(final Class<?> clazz, final String name, final CCompany company) {
		super((Class) clazz, name, company);
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(final String serialNumber) {
		this.serialNumber = serialNumber;
		updateLastModified();
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(final String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
		updateLastModified();
	}

	public String getHardwareRevision() {
		return hardwareRevision;
	}

	public void setHardwareRevision(final String hardwareRevision) {
		this.hardwareRevision = hardwareRevision;
		updateLastModified();
	}

	public String getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(final String deviceStatus) {
		this.deviceStatus = deviceStatus;
		updateLastModified();
	}

	public LocalDateTime getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(final LocalDateTime lastSeen) {
		this.lastSeen = lastSeen;
		updateLastModified();
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
		updateLastModified();
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(final String macAddress) {
		this.macAddress = macAddress;
		updateLastModified();
	}
}
