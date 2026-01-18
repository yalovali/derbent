package tech.derbent.bab.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.bab.device.domain.CBabDevice;

/** CBabNodeModbus - Modbus RTU/TCP communication node. Following Derbent pattern: Concrete entity with specific fields. */
@Entity
@Table (name = "cbab_node_modbus")
public class CBabNodeModbus extends CBabNode {

	public static final String DEFAULT_COLOR = "#2196F3";
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Modbus Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "Modbus Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeModbus.class);
	@SuppressWarnings ("unused")
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Modbus Node Configuration";
	@Column (name = "protocol_type", nullable = true, length = 10)
	@Size (max = 10)
	@AMetaData (
			displayName = "Protocol", required = false, readOnly = false, description = "Modbus protocol type (RTU or TCP)", hidden = false,
			maxLength = 10
	)
	private String protocolType;
	@Column (name = "slave_id", nullable = true)
	@AMetaData (displayName = "Slave ID", required = false, readOnly = false, description = "Modbus slave/unit ID", hidden = false)
	private Integer slaveId;
	@Column (name = "baud_rate", nullable = true)
	@AMetaData (displayName = "Baud Rate", required = false, readOnly = false, description = "Serial baud rate for RTU", hidden = false)
	private Integer baudRate;
	@Column (name = "parity", nullable = true, length = 10)
	@Size (max = 10)
	@AMetaData (
			displayName = "Parity", required = false, readOnly = false, description = "Serial parity (None, Even, Odd)", hidden = false,
			maxLength = 10
	)
	private String parity;
	@Column (name = "host_address", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (displayName = "Host", required = false, readOnly = false, description = "TCP host address", hidden = false, maxLength = 100)
	private String hostAddress;

	/** Default constructor for JPA. */
	public CBabNodeModbus() {
		super();
	}

	public CBabNodeModbus(final String name, final CBabDevice device) {
		super(CBabNodeModbus.class, name, device, "Modbus");
	}

	public Integer getBaudRate() { return baudRate; }

	public String getHostAddress() { return hostAddress; }

	public String getParity() { return parity; }

	// Getters and Setters
	public String getProtocolType() { return protocolType; }

	public Integer getSlaveId() { return slaveId; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (protocolType == null) {
			protocolType = "RTU";
		}
		if (slaveId == null) {
			slaveId = 1;
		}
		if (baudRate == null) {
			baudRate = 9600;
		}
		if (parity == null) {
			parity = "None";
		}
	}

	public void setBaudRate(final Integer baudRate) {
		this.baudRate = baudRate;
		updateLastModified();
	}

	public void setHostAddress(final String hostAddress) {
		this.hostAddress = hostAddress;
		updateLastModified();
	}

	public void setParity(final String parity) {
		this.parity = parity;
		updateLastModified();
	}

	public void setProtocolType(final String protocolType) {
		this.protocolType = protocolType;
		updateLastModified();
	}

	public void setSlaveId(final Integer slaveId) {
		this.slaveId = slaveId;
		updateLastModified();
	}
}
