package tech.derbent.bab.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.bab.device.domain.CBabDevice;

/** CBabNodeModbus - Modbus RTU/TCP communication node. Following Derbent pattern: Concrete entity with specific fields. */
@Entity
@Table (name = "cbab_node_modbus")
public class CBabNodeModbus extends CBabNode<CBabNodeModbus> {

	public static final String DEFAULT_COLOR = "#2196F3";
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Modbus Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "Modbus Node";
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeModbus.class);
	public static final String VIEW_NAME = "Modbus Node Configuration";
	@Column (name = "baud_rate", nullable = true)
	@AMetaData (displayName = "Baud Rate", required = false, readOnly = false, description = "Serial baud rate for RTU", hidden = false)
	private Integer baudRate;
	@Column (name = "host_address", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (displayName = "Host", required = false, readOnly = false, description = "TCP host address", hidden = false, maxLength = 100)
	private String hostAddress;
	@Column (name = "parity", nullable = true, length = 10)
	@Size (max = 10)
	@AMetaData (
			displayName = "Parity", required = false, readOnly = false, description = "Serial parity (None, Even, Odd)", hidden = false,
			maxLength = 10
	)
	private String parity;
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

	/** Default constructor for JPA. */
	protected CBabNodeModbus() {
		super();
	}

	public CBabNodeModbus(final String name, final CBabDevice device) {
		super(CBabNodeModbus.class, name, device, "Modbus");
		initializeDefaults();
	}

	@Override
	protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target,
			@SuppressWarnings ("rawtypes") final tech.derbent.api.entity.service.CAbstractService serviceTarget,
			final tech.derbent.api.interfaces.CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityTo(target, serviceTarget, options);
		// STEP 2: Type-check target
		if (target instanceof CBabNodeModbus) {
			final CBabNodeModbus targetNode = (CBabNodeModbus) target;
			// STEP 3: Copy basic fields (always)
			copyField(this::getProtocolType, targetNode::setProtocolType);
			copyField(this::getSlaveId, targetNode::setSlaveId);
			copyField(this::getBaudRate, targetNode::setBaudRate);
			copyField(this::getParity, targetNode::setParity);
			copyField(this::getHostAddress, targetNode::setHostAddress);
			// STEP 4: Handle relations (conditional)
			if (options.includesRelations()) {
				copyField(this::getDevice, targetNode::setDevice);
			}
			// STEP 5: Log for debugging
			LOGGER.debug("Copied Modbus node {} with options: {}", getName(), options);
		}
	}

	public Integer getBaudRate() { return baudRate; }

	public String getHostAddress() { return hostAddress; }

	public String getParity() { return parity; }

	// Getters and Setters
	public String getProtocolType() { return protocolType; }

	public Integer getSlaveId() { return slaveId; }

	private final void initializeDefaults() {
		protocolType = "RTU";
		slaveId = 1;
		baudRate = 9600;
		hostAddress = "";
		parity = "None";
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
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
