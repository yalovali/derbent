package tech.derbent.bab.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.device.domain.CBabItem;

/**
 * CBabNode - Abstract base class for all communication nodes.
 * A node represents a communication interface on the IoT gateway device
 * (CAN, Modbus, Ethernet, ROS, etc.).
 */
@MappedSuperclass
public abstract class CBabNode extends CBabItem<CBabNode> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNode.class);

	@Column(name = "node_type", nullable = false, length = 50)
	@Size(max = 50)
	@AMetaData(
		displayName = "Node Type", required = true, readOnly = true, description = "Type of communication node (CAN, Modbus, Ethernet, ROS)",
		hidden = false, maxLength = 50
	)
	private String nodeType;

	@Column(name = "enabled", nullable = false)
	@AMetaData(displayName = "Enabled", required = true, readOnly = false, description = "Whether this node is enabled", hidden = false)
	private Boolean enabled;

	@Column(name = "node_status", nullable = true, length = 50)
	@Size(max = 50)
	@AMetaData(
		displayName = "Status", required = false, readOnly = false, description = "Current node status (Active, Inactive, Error)", hidden = false,
		maxLength = 50
	)
	private String nodeStatus;

	@Column(name = "port_number", nullable = true)
	@AMetaData(displayName = "Port", required = false, readOnly = false, description = "Port number or identifier", hidden = false)
	private Integer portNumber;

	/** Default constructor for JPA. */
	protected CBabNode() {
		super();
	}

	public CBabNode(final Class<?> clazz, final String name, final CBabDevice device, final String nodeType) {
		super((Class) clazz, name, device);
		this.nodeType = nodeType;
		this.enabled = true;
	}

	public String getNodeType() {
		return nodeType;
	}

	protected void setNodeType(final String nodeType) {
		this.nodeType = nodeType;
		updateLastModified();
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled;
		updateLastModified();
	}

	public String getNodeStatus() {
		return nodeStatus;
	}

	public void setNodeStatus(final String nodeStatus) {
		this.nodeStatus = nodeStatus;
		updateLastModified();
	}

	public Integer getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(final Integer portNumber) {
		this.portNumber = portNumber;
		updateLastModified();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (enabled == null) {
			enabled = true;
		}
		if (nodeStatus == null) {
			nodeStatus = "Inactive";
		}
	}
}
