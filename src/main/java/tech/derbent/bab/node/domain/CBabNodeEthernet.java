package tech.derbent.bab.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
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
	@Column (name = "gateway", nullable = true, length = 45)
	@Size (max = 45)
	@AMetaData (displayName = "Gateway", required = false, readOnly = false, description = "Default gateway", hidden = false, maxLength = 45)
	private String gateway;
	@Column (name = "dhcp_enabled", nullable = false)
	@AMetaData (displayName = "DHCP", required = true, readOnly = false, description = "Use DHCP for IP configuration", hidden = false)
	private Boolean dhcpEnabled;

	/** Default constructor for JPA. */
	public CBabNodeEthernet() {
		super();
		initializeDefaults(); // ✅ MANDATORY call in concrete class constructor
	}

	public CBabNodeEthernet(final String name, final CBabDevice device) {
		super(CBabNodeEthernet.class, name, device, "Ethernet");
		initializeDefaults(); // ✅ MANDATORY call in concrete class constructor
	}

	public Boolean getDhcpEnabled() { return dhcpEnabled; }

	public String getGateway() { return gateway; }

	// Getters and Setters
	public String getInterfaceName() { return interfaceName; }

	public String getIpAddress() { return ipAddress; }

	public String getSubnetMask() { return subnetMask; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		interfaceName = "eth0";
		dhcpEnabled = true;
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

	@Override
	protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, @SuppressWarnings("rawtypes") final tech.derbent.api.entity.service.CAbstractService serviceTarget, final tech.derbent.api.interfaces.CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityTo(target, serviceTarget, options);
		
		// STEP 2: Type-check target
		if (target instanceof CBabNodeEthernet) {
			final CBabNodeEthernet targetNode = (CBabNodeEthernet) target;
			
			// STEP 3: Copy basic fields (always)
			copyField(this::getInterfaceName, targetNode::setInterfaceName);
			copyField(this::getIpAddress, targetNode::setIpAddress);
			copyField(this::getSubnetMask, targetNode::setSubnetMask);
			copyField(this::getGateway, targetNode::setGateway);
			copyField(this::getDhcpEnabled, targetNode::setDhcpEnabled);
			
			// STEP 4: Handle relations (conditional)
			if (options.includesRelations()) {
				copyField(this::getDevice, targetNode::setDevice);
			}
			
			// STEP 5: Log for debugging
			LOGGER.debug("Copied Ethernet node {} with options: {}", getName(), options);
		}
	}
}
