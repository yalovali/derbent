package tech.derbent.bab.policybase.node.modbus;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.links.domain.CLink;

/** CBabTCPModbusNode - Modbus TCP virtual network node entity. Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete entity with @Entity annotation. JPA Inheritance: JOINED strategy with @DiscriminatorValue - Inherits common fields from cbab_node table -
 * Stores Modbus TCP-specific fields in cnode_tcp_modbus table - node_type discriminator = "TCP_MODBUS" Represents Modbus TCP/IP communication nodes
 * mapped to ethernet interfaces. Example: TCP-based Modbus for industrial Ethernet devices. Used in BAB Actions Dashboard policy rule engine for
 * Modbus TCP traffic management. */
@Entity
@Table (name = "cnode_tcp_modbus", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		}), @UniqueConstraint (columnNames = {
				"project_id", "physical_interface", "server_port", "unit_id"
		})
})
@DiscriminatorValue ("TCP_MODBUS")
@Profile ("bab")
public class CBabTCPModbusNode extends CBabNodeEntity<CBabTCPModbusNode> {
	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#673AB7"; // Deep Purple - TCP Modbus
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "TCP Modbus Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "TCP Modbus Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabTCPModbusNode.class);
	public static final String VIEW_NAME = "TCP Modbus Nodes View";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "tcp_modbus_node_id")
	@JsonIgnore
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this TCP Modbus node",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "tcp_modbus_node_id")
	@JsonIgnore
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this TCP Modbus node", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "tcp_modbus_node_id")
	@JsonIgnore
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this TCP Modbus node", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// TCP Modbus specific fields
	@Column (name = "server_port", nullable = false)
	@AMetaData (displayName = "Server Port", required = true, readOnly = false, description = "Modbus TCP port (default: 502)", hidden = false)
	private Integer serverPort = 502;
	@Column (name = "unit_id", nullable = false)
	@AMetaData (displayName = "Unit ID", required = true, readOnly = false, description = "Modbus unit identifier (0-255)", hidden = false)
	private Integer unitId = 1;
	@Column (name = "server_address", length = 100, nullable = false)
	@AMetaData (
			displayName = "Server Address", required = false, readOnly = false, description = "Modbus TCP server IP address or hostname",
			hidden = false, maxLength = 100
	)
	private String serverAddress = "127.0.0.1";
	@Column (name = "connection_timeout_ms", nullable = false)
	@AMetaData (
			displayName = "Connection Timeout (ms)", required = false, readOnly = false, description = "TCP connection timeout in milliseconds",
			hidden = false
	)
	private Integer connectionTimeoutMs = 5000;
	@Column (name = "response_timeout_ms", nullable = false)
	@AMetaData (
			displayName = "Response Timeout (ms)", required = false, readOnly = false, description = "Modbus response timeout in milliseconds",
			hidden = false
	)
	private Integer responseTimeoutMs = 1000;
	@Column (name = "max_connections", nullable = false)
	@AMetaData (
			displayName = "Max Connections", required = false, readOnly = false, description = "Maximum concurrent TCP connections", hidden = false
	)
	private Integer maxConnections = 5;
	@Column (name = "keep_alive", nullable = false)
	@AMetaData (displayName = "Keep Alive", required = false, readOnly = false, description = "Enable TCP keep-alive", hidden = false)
	private Boolean keepAlive = true;

	/** Default constructor for JPA. */
	protected CBabTCPModbusNode() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabTCPModbusNode(final String name, final CProject<?> project) {
		super(CBabTCPModbusNode.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public CBabTCPModbusNode(final String name, final CProject<?> project, final String physicalInterface, final Integer serverPort) {
		super(CBabTCPModbusNode.class, name, project);
		this.serverPort = serverPort;
		setPhysicalInterface(physicalInterface);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// IHasColor implementation
	@Override
	public String getColor() {
		return DEFAULT_COLOR; // TCP Modbus nodes are deep purple
	}

	@Override
	public Set<CComment> getComments() { return comments; }

	public Integer getConnectionTimeoutMs() { return connectionTimeoutMs; }

	/** Get the full Modbus TCP connection URL.
	 * @return complete connection URL */
	public String getConnectionUrl() { return "modbus+tcp://%s:%d".formatted(serverAddress, serverPort); }

	public String getEntityColor() { return DEFAULT_COLOR; }

	public Boolean getKeepAlive() { return keepAlive; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public Integer getMaxConnections() { return maxConnections; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	public Integer getResponseTimeoutMs() { return responseTimeoutMs; }

	public String getServerAddress() { return serverAddress; }

	// TCP Modbus specific getters and setters
	public Integer getServerPort() { return serverPort; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	public Integer getUnitId() { return unitId; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults
		if (serverPort == null) {
			serverPort = 502;
		}
		if (unitId == null) {
			unitId = 1;
		}
		if ((serverAddress == null) || serverAddress.isEmpty()) {
			serverAddress = "127.0.0.1";
		}
		if (connectionTimeoutMs == null) {
			connectionTimeoutMs = 5000;
		}
		if (responseTimeoutMs == null) {
			responseTimeoutMs = 1000;
		}
		if (maxConnections == null) {
			maxConnections = 5;
		}
		if (keepAlive == null) {
			keepAlive = true;
		}
		// Set default physical interface if not set
		if ((getPhysicalInterface() == null) || getPhysicalInterface().isEmpty()) {
			setPhysicalInterface("eth0");
		}
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setColor(final String color) {
		// Color is static for node types, determined by node type constant
		// Not configurable per instance for consistency
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setConnectionTimeoutMs(final Integer connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
		updateLastModified();
	}

	public void setKeepAlive(final Boolean keepAlive) {
		this.keepAlive = keepAlive;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setMaxConnections(final Integer maxConnections) {
		this.maxConnections = maxConnections;
		updateLastModified();
	}

	public void setResponseTimeoutMs(final Integer responseTimeoutMs) {
		this.responseTimeoutMs = responseTimeoutMs;
		updateLastModified();
	}

	public void setServerAddress(final String serverAddress) {
		this.serverAddress = serverAddress;
		updateLastModified();
	}

	public void setServerPort(final Integer serverPort) {
		this.serverPort = serverPort;
		updateLastModified();
	}

	public void setUnitId(final Integer unitId) {
		this.unitId = unitId;
		updateLastModified();
	}
}
