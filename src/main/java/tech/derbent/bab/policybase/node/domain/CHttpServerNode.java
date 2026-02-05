package tech.derbent.bab.policybase.node.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/**
 * CHttpServerNode - HTTP Server virtual network node entity.
 * 
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete entity with @Entity annotation.
 * 
 * Represents HTTP server virtual nodes mapped to physical ethernet interfaces.
 * Example: httpserver1 mapped to eth0 interface for web service routing.
 * 
 * Used in BAB Actions Dashboard policy rule engine for HTTP traffic management.
 */
@Entity
@Table(name = "cnode_http_server", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "name"}),
    @UniqueConstraint(columnNames = {"project_id", "physical_interface", "server_port"})
})
@AttributeOverride(name = "id", column = @Column(name = "http_server_node_id"))
@Profile("bab")
public class CHttpServerNode extends CNodeEntity<CHttpServerNode> 
    implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {
    
    // Entity constants (MANDATORY - overriding base class constants)
    public static final String DEFAULT_COLOR = "#4CAF50"; // Green - HTTP/Web services
    public static final String DEFAULT_ICON = "vaadin:server";
    public static final String ENTITY_TITLE_PLURAL = "HTTP Server Nodes";
    public static final String ENTITY_TITLE_SINGULAR = "HTTP Server Node";
    private static final Logger LOGGER = LoggerFactory.getLogger(CHttpServerNode.class);
    public static final String VIEW_NAME = "HTTP Server Nodes View";
    
    // HTTP server specific fields
    @Column(name = "server_port", nullable = false)
    @AMetaData(
        displayName = "Server Port",
        required = true,
        readOnly = false,
        description = "HTTP server listening port (e.g., 8080, 80, 443)",
        hidden = false
    )
    private Integer serverPort = 8080;
    
    @Column(name = "endpoint_path", length = 200, nullable = false)
    @AMetaData(
        displayName = "Endpoint Path",
        required = true,
        readOnly = false,
        description = "HTTP endpoint path for API or web service (e.g., /api, /service)",
        hidden = false,
        maxLength = 200
    )
    private String endpointPath = "/api";
    
    @Column(name = "protocol", length = 10, nullable = false)
    @AMetaData(
        displayName = "Protocol",
        required = true,
        readOnly = false,
        description = "HTTP protocol type (HTTP or HTTPS)",
        hidden = false,
        maxLength = 10
    )
    private String protocol = "HTTP";
    
    @Column(name = "ssl_enabled", nullable = false)
    @AMetaData(
        displayName = "SSL Enabled",
        required = false,
        readOnly = false,
        description = "Whether SSL/TLS encryption is enabled for this server",
        hidden = false
    )
    private Boolean sslEnabled = false;
    
    @Column(name = "max_connections", nullable = false)
    @AMetaData(
        displayName = "Max Connections",
        required = false,
        readOnly = false,
        description = "Maximum concurrent connections allowed",
        hidden = false
    )
    private Integer maxConnections = 100;
    
    @Column(name = "timeout_seconds", nullable = false)
    @AMetaData(
        displayName = "Timeout (seconds)",
        required = false,
        readOnly = false,
        description = "Connection timeout in seconds",
        hidden = false
    )
    private Integer timeoutSeconds = 30;
    
    // Standard composition fields - initialized at declaration (RULE 5)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "http_server_node_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this HTTP server node",
        hidden = false,
        dataProviderBean = "CAttachmentService",
        createComponentMethod = "createComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "http_server_node_id")
    @AMetaData(
        displayName = "Comments",
        required = false,
        readOnly = false,
        description = "Comments for this HTTP server node",
        hidden = false,
        dataProviderBean = "CCommentService",
        createComponentMethod = "createComponentComment"
    )
    private Set<CComment> comments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "http_server_node_id")
    @AMetaData(
        displayName = "Links",
        required = false,
        readOnly = false,
        description = "Related links for this HTTP server node",
        hidden = false,
        dataProviderBean = "CLinkService",
        createComponentMethod = "createComponent"
    )
    private Set<CLink> links = new HashSet<>();
    
    /** Default constructor for JPA. */
    protected CHttpServerNode() {
        super();
        // JPA constructors do NOT call initializeDefaults() (RULE 1)
    }
    
    public CHttpServerNode(final String name, final CProject<?> project) {
        super(CHttpServerNode.class, name, project, "HTTP_SERVER");
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }
    
    public CHttpServerNode(final String name, final CProject<?> project, 
                          final String physicalInterface, final Integer serverPort) {
        super(CHttpServerNode.class, name, project, "HTTP_SERVER");
        this.serverPort = serverPort;
        setPhysicalInterface(physicalInterface);
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }
    
    /** Initialize intrinsic defaults (RULE 3). */
    private final void initializeDefaults() {
        // Initialize nullable=false fields with defaults (already done in field declarations)
        
        // HTTP server specific defaults
        if (serverPort == null) {
            serverPort = 8080;
        }
        if (endpointPath == null || endpointPath.isEmpty()) {
            endpointPath = "/api";
        }
        if (protocol == null || protocol.isEmpty()) {
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
        if (getPhysicalInterface() == null || getPhysicalInterface().isEmpty()) {
            setPhysicalInterface("eth0");
        }
        
        // Generate initial node configuration JSON
        setNodeConfigJson(generateDefaultNodeConfig());
        
        // MANDATORY: Call service initialization at end (RULE 3)
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
    
    @Override
    protected String generateDefaultNodeConfig() {
        return String.format("""
            {
                "nodeId": "%s",
                "nodeType": "HTTP_SERVER",
                "physicalInterface": "%s",
                "active": %s,
                "priority": %d,
                "httpConfig": {
                    "port": %d,
                    "endpoint": "%s",
                    "protocol": "%s",
                    "sslEnabled": %s,
                    "maxConnections": %d,
                    "timeoutSeconds": %d
                }
            }
            """, getId(), getPhysicalInterface(), getIsActive(), getPriorityLevel(),
                serverPort, endpointPath, protocol, sslEnabled, maxConnections, timeoutSeconds);
    }
    
    @Override
    public String getEntityColor() {
        return DEFAULT_COLOR;
    }
    
    /**
     * Get the full HTTP URL for this server node.
     * @return complete HTTP URL
     */
    public String getFullUrl() {
        String protocolLower = protocol.toLowerCase();
        return String.format("%s://%s:%d%s", protocolLower, getPhysicalInterface(), serverPort, endpointPath);
    }
    
    /**
     * Check if this HTTP server can handle HTTPS requests.
     * @return true if HTTPS is supported
     */
    public boolean supportsHttps() {
        return sslEnabled != null && sslEnabled && "HTTPS".equalsIgnoreCase(protocol);
    }
    
    // Interface implementations
    @Override
    public Set<CAttachment> getAttachments() { return attachments; }
    
    @Override
    public void setAttachments(Set<CAttachment> attachments) { this.attachments = attachments; }
    
    @Override
    public Set<CComment> getComments() { return comments; }
    
    @Override
    public void setComments(Set<CComment> comments) { this.comments = comments; }
    
    @Override
    public Set<CLink> getLinks() { return links; }
    
    @Override
    public void setLinks(Set<CLink> links) { this.links = links; }
    
    // IEntityRegistrable implementation
    @Override
    public Class<?> getServiceClass() { 
        return Object.class; 
    }
    
    @Override
    public Class<?> getPageServiceClass() { 
        return Object.class; 
    }
    
    // HTTP server specific getters and setters
    public Integer getServerPort() { return serverPort; }
    public void setServerPort(Integer serverPort) { 
        this.serverPort = serverPort;
        updateLastModified();
    }
    
    public String getEndpointPath() { return endpointPath; }
    public void setEndpointPath(String endpointPath) { 
        this.endpointPath = endpointPath;
        updateLastModified();
    }
    
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { 
        this.protocol = protocol;
        updateLastModified();
    }
    
    public Boolean getSslEnabled() { return sslEnabled; }
    public void setSslEnabled(Boolean sslEnabled) { 
        this.sslEnabled = sslEnabled;
        updateLastModified();
    }
    
    public Integer getMaxConnections() { return maxConnections; }
    public void setMaxConnections(Integer maxConnections) { 
        this.maxConnections = maxConnections;
        updateLastModified();
    }
    
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { 
        this.timeoutSeconds = timeoutSeconds;
        updateLastModified();
    }
}