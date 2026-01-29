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

/** CBabNodeEthernet - Ethernet communication node. Following Derbent pattern: Concrete entity with specific fields. */
@Entity
@Table (name = "cbab_node_ethernet")
public class CBabNodeEthernet extends CBabNode<CBabNodeEthernet> {

	public static final String DEFAULT_COLOR = "#4CAF50";
	public static final String DEFAULT_ICON = "vaadin:plug";
	public static final String ENTITY_TITLE_PLURAL = "Ethernet Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "Ethernet Node";
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeEthernet.class);
	public static final String VIEW_NAME = "Ethernet Node Configuration";
	@Column (name = "dhcp_enabled", nullable = false)
	@AMetaData (displayName = "DHCP", required = true, readOnly = false, description = "Use DHCP for IP configuration", hidden = false)
	private Boolean dhcpEnabled = false;
	@Column (name = "gateway", nullable = true, length = 45)
	@Size (max = 45)
	@AMetaData (displayName = "Gateway", required = false, readOnly = false, description = "Default gateway", hidden = false, maxLength = 45)
	private String gateway;
	@Column (name = "interface_name", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Interface", required = false, readOnly = false, description = "Linux network interface name (e.g., eth0)", hidden = false,
			maxLength = 50
	)
	private String interfaceName;
	@Column (name = "ip_address", nullable = true, length = 45)
	@Size (max = 45)
	@AMetaData (displayName = "IP Address", required = false, readOnly = false, description = "Static IP address", hidden = false, maxLength = 45)
	private String ipAddress;
	@Column (name = "subnet_mask", nullable = true, length = 45)
	@Size (max = 45)
	@AMetaData (displayName = "Subnet Mask", required = false, readOnly = false, description = "Subnet mask", hidden = false, maxLength = 45)
	private String subnetMask;

	/** Default constructor for JPA. */
	protected CBabNodeEthernet() {
		super();
	}

	public CBabNodeEthernet(final String name, final CBabDevice device) {
		super(CBabNodeEthernet.class, name, device, "Ethernet");
		initializeDefaults();
	}

	public Boolean getDhcpEnabled() { return dhcpEnabled; }

	public String getGateway() { return gateway; }

	// Getters and Setters
	public String getInterfaceName() { return interfaceName; }

	public String getIpAddress() { return ipAddress; }

	public String getSubnetMask() { return subnetMask; }

	private final void initializeDefaults() {
		interfaceName = "eth0";
		dhcpEnabled = true;
		ipAddress = "";
		subnetMask = "";
		gateway = "";
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setDhcpEnabled(final Boolean dhcpEnabled) {
		this.dhcpEnabled = dhcpEnabled;
		updateLastModified();
	}

	public void setGateway(final String gateway) {
		this.gateway = gateway;
		updateLastModified();
	}

	public void setInterfaceName(final String interfaceName) {
		this.interfaceName = interfaceName;
		updateLastModified();
	}

	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
		updateLastModified();
	}

	public void setSubnetMask(final String subnetMask) {
		this.subnetMask = subnetMask;
		updateLastModified();
	}
}
