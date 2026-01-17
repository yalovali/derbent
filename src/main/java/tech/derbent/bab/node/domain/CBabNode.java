package tech.derbent.bab.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.base.users.domain.CUser;

/** CBabNode - Base class for all communication nodes. Following Derbent pattern: Abstract entity class with proper inheritance. A node represents a
 * communication interface on the IoT gateway device. */
@Entity
@Table (name = "cbab_node")
@Inheritance (strategy = InheritanceType.JOINED)
public abstract class CBabNode extends CEntityOfCompany<CBabNode> {

	public static final String DEFAULT_COLOR = "#4CAF50"; // Green for BAB nodes
	public static final String DEFAULT_ICON = "vaadin:cluster";
	public static final String ENTITY_TITLE_PLURAL = "BAB Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "BAB Node";
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNode.class);
	private static final long serialVersionUID = 1L;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "device_id", nullable = false)
	@AMetaData (displayName = "Device", required = true, readOnly = true, description = "Device this node belongs to", hidden = false)
	private CBabDevice device;
	@Column (name = "node_type", nullable = false, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Node Type", required = true, readOnly = true, description = "Type of communication node (CAN, Modbus, Ethernet, ROS)",
			hidden = false, maxLength = 50
	)
	private String nodeType;
	@Column (name = "enabled", nullable = false)
	@AMetaData (displayName = "Enabled", required = true, readOnly = false, description = "Whether this node is enabled", hidden = false)
	private Boolean enabled;
	@Column (name = "node_status", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current node status (Active, Inactive, Error)", hidden = false,
			maxLength = 50
	)
	private String nodeStatus;
	@Column (name = "port_number", nullable = true)
	@AMetaData (displayName = "Port", required = false, readOnly = false, description = "Port number or identifier", hidden = false)
	private Integer portNumber;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "created_by_id", nullable = true)
	@AMetaData (
			displayName = "Created By", required = false, readOnly = true, description = "User who created this node", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser createdBy;

	/** Default constructor for JPA. */
	protected CBabNode() {
		super();
	}

	@SuppressWarnings ("rawtypes")
	public CBabNode(final Class<? extends CBabNode> clazz, final String name, final CBabDevice device, final String nodeType) {
		super((Class) clazz, name, device.getCompany());
		this.device = device;
		this.nodeType = nodeType;
		enabled = true;
	}

	public CUser getCreatedBy() { return createdBy; }

	// Getters and Setters
	public CBabDevice getDevice() { return device; }

	public Boolean getEnabled() { return enabled; }

	public String getNodeStatus() { return nodeStatus; }

	public String getNodeType() { return nodeType; }

	public Integer getPortNumber() { return portNumber; }

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

	protected void setNodeType(final String nodeType) {
		this.nodeType = nodeType;
		updateLastModified();
	}

	public void setPortNumber(final Integer portNumber) {
		this.portNumber = portNumber;
		updateLastModified();
	}
}
