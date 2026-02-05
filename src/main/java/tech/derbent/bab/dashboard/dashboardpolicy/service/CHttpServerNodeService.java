package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.time.Clock;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.node.domain.CHttpServerNode;
import tech.derbent.base.session.service.ISessionService;

/**
 * CHttpServerNodeService - Service for HTTP Server virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete service with @Service annotation.
 * 
 * Provides business logic for HTTP server node management:
 * - HTTP server configuration validation
 * - Port conflict detection and resolution
 * - SSL/TLS configuration management
 * - Calimero HTTP integration
 * - Endpoint path validation
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CHttpServerNodeService extends CNodeEntityService<CHttpServerNode> 
    implements IEntityRegistrable, IEntityWithView {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CHttpServerNodeService.class);
    
    // HTTP server validation patterns
    private static final Pattern ENDPOINT_PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_-]*$");
    private static final Pattern ETHERNET_INTERFACE_PATTERN = Pattern.compile("^eth[0-9]+$");
    
    // Port ranges
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final int MIN_USER_PORT = 1024;
    
    public CHttpServerNodeService(final IHttpServerNodeRepository repository,
                                final Clock clock,
                                final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    public Class<CHttpServerNode> getEntityClass() {
        return CHttpServerNode.class;
    }
    
    @Override
    protected void validateNodeSpecificFields(final CHttpServerNode entity) {
        // HTTP server specific validation
        
        // Server port validation
        if (entity.getServerPort() == null) {
            throw new CValidationException("Server port is required");
        }
        
        validateNumericField(entity.getServerPort(), "Server Port", MAX_PORT);
        
        if (entity.getServerPort() < MIN_PORT || entity.getServerPort() > MAX_PORT) {
            throw new CValidationException(String.format(
                "Server port must be between %d and %d", MIN_PORT, MAX_PORT));
        }
        
        // Warn about privileged ports
        if (entity.getServerPort() < MIN_USER_PORT) {
            LOGGER.warn("Server port {} is in privileged range (< {}). May require elevated privileges.", 
                       entity.getServerPort(), MIN_USER_PORT);
        }
        
        // Endpoint path validation
        if (entity.getEndpointPath() == null || entity.getEndpointPath().trim().isEmpty()) {
            throw new CValidationException("Endpoint path is required");
        }
        
        validateStringLength(entity.getEndpointPath(), "Endpoint Path", 200);
        
        if (!ENDPOINT_PATH_PATTERN.matcher(entity.getEndpointPath()).matches()) {
            throw new CValidationException(
                "Endpoint path must start with '/' and contain only alphanumeric characters, slashes, hyphens, and underscores");
        }
        
        // Protocol validation
        if (entity.getProtocol() == null || entity.getProtocol().trim().isEmpty()) {
            throw new CValidationException("Protocol is required");
        }
        
        validateStringLength(entity.getProtocol(), "Protocol", 10);
        
        final String protocol = entity.getProtocol().toUpperCase();
        if (!protocol.equals("HTTP") && !protocol.equals("HTTPS")) {
            throw new CValidationException("Protocol must be 'HTTP' or 'HTTPS'");
        }
        
        // SSL validation
        if ("HTTPS".equals(protocol) && (entity.getSslEnabled() == null || !entity.getSslEnabled())) {
            throw new CValidationException("SSL must be enabled for HTTPS protocol");
        }
        
        if ("HTTP".equals(protocol) && entity.getSslEnabled() != null && entity.getSslEnabled()) {
            LOGGER.warn("SSL is enabled but protocol is HTTP for server '{}'", entity.getName());
        }
        
        // Connection limits validation
        if (entity.getMaxConnections() != null) {
            validateNumericField(entity.getMaxConnections(), "Max Connections", 10000);
            
            if (entity.getMaxConnections() <= 0) {
                throw new CValidationException("Max connections must be greater than 0");
            }
        }
        
        // Timeout validation
        if (entity.getTimeoutSeconds() != null) {
            validateNumericField(entity.getTimeoutSeconds(), "Timeout", 3600);
            
            if (entity.getTimeoutSeconds() <= 0) {
                throw new CValidationException("Timeout must be greater than 0 seconds");
            }
        }
        
        // Port conflict validation
        validatePortConflict(entity);
    }
    
    @Override
    protected void validatePhysicalInterfaceFormat(final CHttpServerNode entity, final String physicalInterface) {
        // HTTP servers typically use ethernet interfaces
        if (!ETHERNET_INTERFACE_PATTERN.matcher(physicalInterface).matches()) {
            LOGGER.warn("Physical interface '{}' does not match typical ethernet pattern (eth0, eth1, etc.) for HTTP server '{}'", 
                       physicalInterface, entity.getName());
        }
        
        // Call parent for basic validation
        super.validatePhysicalInterfaceFormat(entity, physicalInterface);
    }
    
    /**
     * Validate that the port is not already in use by another server on the same interface.
     */
    private void validatePortConflict(final CHttpServerNode entity) {
        final IHttpServerNodeRepository httpRepo = (IHttpServerNodeRepository) repository;
        
        final boolean portConflict = httpRepo.existsByPhysicalInterfaceAndPortExcluding(
            entity.getPhysicalInterface(),
            entity.getServerPort(),
            entity.getProject(),
            entity.getId());
        
        if (portConflict) {
            throw new CValidationException(String.format(
                "Port %d is already in use by another HTTP server on interface '%s'",
                entity.getServerPort(), entity.getPhysicalInterface()));
        }
    }
    
    @Override
    protected void validateJsonConfiguration(final CHttpServerNode entity, final String configJson) {
        // Basic JSON validation for HTTP server configuration
        // This is a simplified validation - in production, you might want to use a JSON schema validator
        
        if (!configJson.trim().startsWith("{") || !configJson.trim().endsWith("}")) {
            throw new CValidationException("Configuration must be a valid JSON object");
        }
        
        // Validate that required fields are present in JSON
        if (!configJson.contains("\"nodeType\"") || !configJson.contains("\"httpConfig\"")) {
            throw new CValidationException("Configuration JSON must contain 'nodeType' and 'httpConfig' fields");
        }
        
        // Additional HTTP-specific validation can be added here
        // For example, checking for required HTTP configuration fields
        if (!configJson.contains("\"port\"") || !configJson.contains("\"endpoint\"")) {
            throw new CValidationException("HTTP configuration must contain 'port' and 'endpoint' fields");
        }
    }
    
    @Override
    protected String generateDefaultNodeConfiguration(final CHttpServerNode entity) {
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
                    "timeoutSeconds": %d,
                    "fullUrl": "%s"
                },
                "calimeroConfig": {
                    "enabled": true,
                    "exportFormat": "HTTP_GATEWAY",
                    "monitoringEnabled": true
                }
            }
            """, 
            entity.getId() != null ? entity.getId().toString() : "new",
            entity.getPhysicalInterface() != null ? entity.getPhysicalInterface() : "eth0",
            entity.getIsActive() != null ? entity.getIsActive() : true,
            entity.getPriorityLevel() != null ? entity.getPriorityLevel() : 50,
            entity.getServerPort() != null ? entity.getServerPort() : 8080,
            entity.getEndpointPath() != null ? entity.getEndpointPath() : "/api",
            entity.getProtocol() != null ? entity.getProtocol() : "HTTP",
            entity.getSslEnabled() != null ? entity.getSslEnabled() : false,
            entity.getMaxConnections() != null ? entity.getMaxConnections() : 100,
            entity.getTimeoutSeconds() != null ? entity.getTimeoutSeconds() : 30,
            entity.getFullUrl());
    }
    
    @Override
    protected void initializeNodeSpecificDefaults(final CHttpServerNode entity) {
        // HTTP server specific initialization
        if (entity.getServerPort() == null) {
            entity.setServerPort(8080);
        }
        
        if (entity.getEndpointPath() == null || entity.getEndpointPath().isEmpty()) {
            entity.setEndpointPath("/api");
        }
        
        if (entity.getProtocol() == null || entity.getProtocol().isEmpty()) {
            entity.setProtocol("HTTP");
        }
        
        if (entity.getSslEnabled() == null) {
            entity.setSslEnabled(false);
        }
        
        if (entity.getMaxConnections() == null) {
            entity.setMaxConnections(100);
        }
        
        if (entity.getTimeoutSeconds() == null) {
            entity.setTimeoutSeconds(30);
        }
        
        // Set default physical interface for HTTP servers
        if (entity.getPhysicalInterface() == null || entity.getPhysicalInterface().isEmpty()) {
            entity.setPhysicalInterface("eth0");
        }
        
        LOGGER.debug("Initialized HTTP server node '{}' on {}:{}", 
                    entity.getName(), entity.getPhysicalInterface(), entity.getServerPort());
    }
    
    @Override
    protected void copyNodeSpecificFields(final CHttpServerNode source, final CHttpServerNode target, final CCloneOptions options) {
        // Copy HTTP server specific fields
        target.setServerPort(source.getServerPort());
        target.setEndpointPath(source.getEndpointPath());
        target.setProtocol(source.getProtocol());
        target.setSslEnabled(source.getSslEnabled());
        target.setMaxConnections(source.getMaxConnections());
        target.setTimeoutSeconds(source.getTimeoutSeconds());
        
        LOGGER.debug("Copied HTTP server specific fields from '{}' to '{}'", 
                    source.getName(), target.getName());
    }
    
    // HTTP Server specific business methods
    
    /**
     * Find HTTP servers by port and project.
     */
    @Transactional(readOnly = true)
    public List<CHttpServerNode> findByServerPort(final Integer port, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(port, "Port cannot be null");
        Check.notNull(project, "Project cannot be null");
        
        return ((IHttpServerNodeRepository) repository).findByServerPortAndProject(port, project);
    }
    
    /**
     * Find HTTP servers by protocol.
     */
    @Transactional(readOnly = true)
    public List<CHttpServerNode> findByProtocol(final String protocol, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(protocol, "Protocol cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        return ((IHttpServerNodeRepository) repository).findByProtocolAndProject(protocol, project);
    }
    
    /**
     * Find SSL-enabled HTTP servers.
     */
    @Transactional(readOnly = true)
    public List<CHttpServerNode> findSslEnabled(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        
        return ((IHttpServerNodeRepository) repository).findSslEnabledByProject(project);
    }
    
    /**
     * Check if a port is available on a specific interface.
     */
    @Transactional(readOnly = true)
    public boolean isPortAvailable(final String physicalInterface, final Integer port, 
                                 final tech.derbent.api.projects.domain.CProject<?> project, final Long excludeNodeId) {
        Check.notBlank(physicalInterface, "Physical interface cannot be blank");
        Check.notNull(port, "Port cannot be null");
        Check.notNull(project, "Project cannot be null");
        
        return !((IHttpServerNodeRepository) repository).existsByPhysicalInterfaceAndPortExcluding(
            physicalInterface, port, project, excludeNodeId);
    }
    
    /**
     * Find high-capacity servers.
     */
    @Transactional(readOnly = true)
    public List<CHttpServerNode> findHighCapacityServers(final Integer minConnections, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(minConnections, "Min connections cannot be null");
        Check.notNull(project, "Project cannot be null");
        
        return ((IHttpServerNodeRepository) repository).findHighCapacityServers(minConnections, project);
    }
    
    /**
     * Find servers by endpoint pattern.
     */
    @Transactional(readOnly = true)
    public List<CHttpServerNode> findByEndpointPattern(final String pattern, final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notBlank(pattern, "Pattern cannot be blank");
        Check.notNull(project, "Project cannot be null");
        
        return ((IHttpServerNodeRepository) repository).findByEndpointPathPattern(pattern, project);
    }
    
    /**
     * Update server configuration and regenerate JSON.
     */
    @Transactional
    public void updateServerConfiguration(final CHttpServerNode server, 
                                        final Integer port, 
                                        final String endpointPath, 
                                        final String protocol,
                                        final Boolean sslEnabled) {
        Check.notNull(server, "Server cannot be null");
        
        // Update configuration
        if (port != null) {
            server.setServerPort(port);
        }
        if (endpointPath != null) {
            server.setEndpointPath(endpointPath);
        }
        if (protocol != null) {
            server.setProtocol(protocol);
        }
        if (sslEnabled != null) {
            server.setSslEnabled(sslEnabled);
        }
        
        // Regenerate configuration JSON
        regenerateNodeConfiguration(server);
        
        LOGGER.info("Updated configuration for HTTP server '{}'", server.getName());
    }
    
    // IEntityRegistrable implementation
    @Override
    public Class<?> getInitializerServiceClass() { 
        return Object.class; // Placeholder - will be updated in Phase 8
    }
    
    @Override
    public Class<?> getPageServiceClass() { 
        return Object.class; // Placeholder - will be updated in Phase 8
    }
    
    @Override
    public Class<?> getServiceClass() { 
        return this.getClass(); 
    }
}