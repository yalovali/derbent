package tech.derbent.bab.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.base.users.domain.CUser;

/** CBabNode - Abstract base class for all communication nodes. Following Derbent pattern: Abstract entity with @MappedSuperclass. A node represents a
 * communication interface on the IoT gateway device. */
@MappedSuperclass
public abstract class CBabNode<EntityClass> extends CEntityOfCompany<EntityClass> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNode.class);
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "created_by_id", nullable = true)
	@AMetaData (
			displayName = "Created By", required = false, readOnly = true, description = "User who created this node", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser createdBy;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "device_id", nullable = false)
	@AMetaData (displayName = "Device", required = true, readOnly = true, description = "Device this node belongs to", hidden = false)
	private CBabDevice device;
	@Column (name = "enabled", nullable = false)
	@AMetaData (displayName = "Enabled", required = true, readOnly = false, description = "Whether this node is enabled", hidden = false)
	private Boolean enabled = false;
	@Column (name = "node_status", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current node status (Active, Inactive, Error)", hidden = false,
			maxLength = 50
	)
	private String nodeStatus;
	@Column (name = "node_type", nullable = false, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Node Type", required = true, readOnly = true, description = "Type of communication node (CAN, Modbus, Ethernet, ROS)",
			hidden = false, maxLength = 50
	)
	private String nodeType;
	@Column (name = "port_number", nullable = true)
	@AMetaData (displayName = "Port", required = false, readOnly = false, description = "Port number or identifier", hidden = false)
	private Integer portNumber;

	/** Default constructor for JPA. */
	protected CBabNode() {}

	protected CBabNode(final Class<EntityClass> clazz, final String name, final CBabDevice device, final String nodeType) {
		super(clazz, name, device.getCompany());
		initializeDefaults();
		this.device = device;
		this.nodeType = nodeType;
		enabled = true;
	}

	// Getters and Setters
	public CUser getCreatedBy() { return createdBy; }

	public CBabDevice getDevice() { return device; }

	public Boolean getEnabled() { return enabled; }

	public String getNodeStatus() { return nodeStatus; }

	public String getNodeType() { return nodeType; }

	public Integer getPortNumber() { return portNumber; }

	private final void initializeDefaults() {
		enabled = true;
		nodeStatus = "Inactive";
	}

	public void setCreatedBy(final CUser createdBy) { this.createdBy = createdBy; }

	public void setDevice(final CBabDevice device) {
		this.device = device;
		if (device != null) {
			setCompany(device.getCompany());
		}
		updateLastModified();
	}

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled;
		updateLastModified();
	}

	public void setNodeStatus(final String nodeStatus) {
		this.nodeStatus = nodeStatus;
		updateLastModified();
	}

	public void setNodeType(final String nodeType) {
		this.nodeType = nodeType;
		updateLastModified();
	}

	public void setPortNumber(final Integer portNumber) {
		this.portNumber = portNumber;
		updateLastModified();
	}
}
