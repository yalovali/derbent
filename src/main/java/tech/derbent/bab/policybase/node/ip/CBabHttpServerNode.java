package tech.derbent.bab.policybase.node.ip;

import java.util.HashSet;
import java.util.Set;
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

/** CBabHttpServerNode - HTTP Server virtual network node entity. Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete entity with @Entity annotation. JPA Inheritance: JOINED strategy with @DiscriminatorValue - Inherits common fields from cbab_node table -
 * Stores HTTP-specific fields in cnode_http_server table - node_type discriminator = "HTTP_SERVER" Represents HTTP server virtual nodes mapped to
 * physical ethernet interfaces. Example: httpserver1 mapped to eth0 interface for web service routing. Used in BAB Actions Dashboard policy rule
 * engine for HTTP traffic management. */
@Entity
@Table (name = "cnode_http_server", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		}), @UniqueConstraint (columnNames = {
				"project_id", "physical_interface", "server_port"
		})
})
@DiscriminatorValue ("HTTP_SERVER")
@Profile ("bab")
public class CBabHttpServerNode extends CBabNodeEntity<CBabHttpServerNode> {
	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#4CAF50"; // Green - HTTP/Web services
	public static final String DEFAULT_ICON = "vaadin:server";
	public static final String ENTITY_TITLE_PLURAL = "HTTP Server Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "HTTP Server Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabHttpServerNode.class);
	public static final String VIEW_NAME = "HTTP Server Nodes View";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "http_server_node_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this HTTP server node",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "http_server_node_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this HTTP server node", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "endpoint_path", length = 200, nullable = false)
	@AMetaData (
			displayName = "Endpoint Path", required = true, readOnly = false,
			description = "HTTP endpoint path for API or web service (e.g., /api, /service)", hidden = false, maxLength = 200
	)
	private String endpointPath = "/api";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "http_server_node_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this HTTP server node", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Column (name = "max_connections", nullable = false)
	@AMetaData (
			displayName = "Max Connections", required = false, readOnly = false, description = "Maximum concurrent connections allowed",
			hidden = false
	)
	private Integer maxConnections = 100;
	@Column (name = "protocol", length = 10, nullable = false)
	@AMetaData (
			displayName = "Protocol", required = true, readOnly = false, description = "HTTP protocol type (HTTP or HTTPS)", hidden = false,
			maxLength = 10
	)
	private String protocol = "HTTP";
	// HTTP server specific fields
	@Column (name = "server_port", nullable = false)
	@AMetaData (
			displayName = "Server Port", required = true, readOnly = false, description = "HTTP server listening port (e.g., 8080, 80, 443)",
			hidden = false
	)
	private Integer serverPort = 8080;
	@Column (name = "ssl_enabled", nullable = false)
	@AMetaData (
			displayName = "SSL Enabled", required = false, readOnly = false, description = "Whether SSL/TLS encryption is enabled for this server",
			hidden = false
	)
	private Boolean sslEnabled = false;
	@Column (name = "timeout_seconds", nullable = false)
	@AMetaData (displayName = "Timeout (seconds)", required = false, readOnly = false, description = "Connection timeout in seconds", hidden = false)
	private Integer timeoutSeconds = 30;

	/** Default constructor for JPA. */
	protected CBabHttpServerNode() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabHttpServerNode(final String name, final CProject<?> project) {
		super(CBabHttpServerNode.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public CBabHttpServerNode(final String name, final CProject<?> project, final String physicalInterface, final Integer serverPort) {
		super(CBabHttpServerNode.class, name, project);
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
		return DEFAULT_COLOR; // HTTP servers are green
	}

	@Override
	public Set<CComment> getComments() { return comments; }

	public String getEndpointPath() { return endpointPath; }

	public String getEntityColor() { return DEFAULT_COLOR; }

	/** Get the full HTTP URL for this server node.
	 * @return complete HTTP URL */
	public String getFullUrl() {
		final String protocolLower = protocol.toLowerCase();
		return "%s://%s:%d%s".formatted(protocolLower, getPhysicalInterface(), serverPort, endpointPath);
	}

	@Override
	public Set<CLink> getLinks() { return links; }

	public Integer getMaxConnections() { return maxConnections; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	public String getProtocol() { return protocol; }

	// HTTP server specific getters and setters
	public Integer getServerPort() { return serverPort; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	public Boolean getSslEnabled() { return sslEnabled; }

	public Integer getTimeoutSeconds() { return timeoutSeconds; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults (already done in field declarations)
		// HTTP server specific defaults
		if (serverPort == null) {
			serverPort = 8080;
		}
		if ((endpointPath == null) || endpointPath.isEmpty()) {
			endpointPath = "/api";
		}
		if ((protocol == null) || protocol.isEmpty()) {
			protocol = "HTTP";
		}
		if (sslEnabled == null) {
			sslEnabled = false;
		}
		if (maxConnections == null) {
			maxConnections = 100;
		}
		if (timeoutSeconds == null) {
			timeoutSeconds = 30;
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

	public void setEndpointPath(final String endpointPath) {
		this.endpointPath = endpointPath;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setMaxConnections(final Integer maxConnections) {
		this.maxConnections = maxConnections;
		updateLastModified();
	}

	public void setProtocol(final String protocol) {
		this.protocol = protocol;
		updateLastModified();
	}

	public void setServerPort(final Integer serverPort) {
		this.serverPort = serverPort;
		updateLastModified();
	}

	public void setSslEnabled(final Boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
		updateLastModified();
	}

	public void setTimeoutSeconds(final Integer timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
		updateLastModified();
	}

	/** Check if this HTTP server can handle HTTPS requests.
	 * @return true if HTTPS is supported */
	public boolean supportsHttps() {
		return (sslEnabled != null) && sslEnabled && "HTTPS".equalsIgnoreCase(protocol);
	}
}
