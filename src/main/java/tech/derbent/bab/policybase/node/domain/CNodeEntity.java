package tech.derbent.bab.policybase.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;

/**
 * CNodeEntity - Abstract base class for virtual network nodes in BAB Actions Dashboard.
 * 
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: @MappedSuperclass for inheritance.
 * 
 * Represents virtual network entities mapped to physical network interfaces:
 * - HTTP Server nodes (httpserver1 → eth0)
 * - Vehicle nodes (vehicleX → can1) 
 * - File Input nodes (fileInput → file)
 * 
 * All virtual nodes have common properties for policy rule engine integration
 * and Calimero gateway configuration export.
 */
@MappedSuperclass  // Abstract entities are @MappedSuperclass
@Profile("bab")
public abstract class CNodeEntity<EntityClass> extends CEntityOfProject<EntityClass> {
    
    // Base constants (protected - not final, can be overridden by subclasses)
    protected static final String BASE_DEFAULT_COLOR = "#FF5722"; // Deep Orange - Node entities
    protected static final String BASE_DEFAULT_ICON = "vaadin:cluster";
    protected static final String BASE_ENTITY_TITLE_PLURAL = "Network Nodes";
    protected static final String BASE_ENTITY_TITLE_SINGULAR = "Network Node";
    private static final Logger LOGGER = LoggerFactory.getLogger(CNodeEntity.class);
    protected static final String BASE_VIEW_NAME = "Network Nodes View";
    
    // Core node identification fields
    @Column(name = "node_type", length = 50, nullable = false)
    @AMetaData(
        displayName = "Node Type",
        required = true,
        readOnly = false,
        description = "Type of virtual network node (HTTP_SERVER, VEHICLE, FILE_INPUT, etc.)",
        hidden = false,
        maxLength = 50
    )
    private String nodeType;
    
    @Column(name = "physical_interface", length = 100, nullable = false)
    @AMetaData(
        displayName = "Physical Interface",
        required = true,
        readOnly = false,
        description = "Physical network interface mapping (eth0, can1, file, etc.)",
        hidden = false,
        maxLength = 100
    )
    private String physicalInterface;
    
    // Node operational state - initialized at declaration (RULE 6)
    @Column(name = "is_active", nullable = false)
    @AMetaData(
        displayName = "Active",
        required = true,
        readOnly = false,
        description = "Whether this virtual node is currently active in the network",
        hidden = false
    )
    private Boolean isActive = true;
    
    @Column(name = "connection_status", length = 20, nullable = false)
    @AMetaData(
        displayName = "Connection Status",
        required = false,
        readOnly = true,
        description = "Current connection status (CONNECTED, DISCONNECTED, ERROR)",
        hidden = false,
        maxLength = 20
    )
    private String connectionStatus = "DISCONNECTED";
    
    // Calimero integration configuration
    @Column(name = "node_config", columnDefinition = "TEXT")
    @AMetaData(
        displayName = "Node Configuration",
        required = false,
        readOnly = false,
        description = "JSON configuration for Calimero gateway export",
        hidden = false
    )
    private String nodeConfigJson;
    
    @Column(name = "priority_level", nullable = false)
    @AMetaData(
        displayName = "Priority Level",
        required = false,
        readOnly = false,
        description = "Node priority for policy rule processing (0-100)",
        hidden = false
    )
    private Integer priorityLevel = 50;
    
    /** Default constructor for JPA. */
    protected CNodeEntity() {
        super();
        // Abstract JPA constructors do NOT call initializeDefaults() (RULE 1)
    }
    
    protected CNodeEntity(Class<EntityClass> clazz, String name, CProject<?> project, String nodeType) {
        super(clazz, name, project);
        this.nodeType = nodeType;
        // Abstract constructors do NOT call initializeDefaults()
        // Concrete subclasses will call initializeDefaults() which will chain to abstract implementation
    }
    
    // Abstract initializeDefaults - implemented by subclasses
    // No implementation here - each concrete class implements
    
    /**
     * Get the entity color for this node type.
     * Subclasses should override to return their specific color.
     * @return color string for UI display
     */
    public String getEntityColor() {
        return BASE_DEFAULT_COLOR;
    }
    
    /**
     * Check if this node can be used as a source in policy rules.
     * @return true if node can be a rule source
     */
    public boolean canBeRuleSource() {
        return isActive != null && isActive && "CONNECTED".equals(connectionStatus);
    }
    
    /**
     * Check if this node can be used as a destination in policy rules.
     * @return true if node can be a rule destination
     */
    public boolean canBeRuleDestination() {
        return isActive != null && isActive;
    }
    
    /**
     * Generate default node configuration JSON for Calimero.
     * Subclasses should override with type-specific configuration.
     * @return default JSON configuration
     */
    protected String generateDefaultNodeConfig() {
        return String.format("""
            {
                "nodeId": "%s",
                "nodeType": "%s",
                "physicalInterface": "%s",
                "active": %s,
                "priority": %d
            }
            """, getId(), nodeType, physicalInterface, isActive, priorityLevel);
    }
    
    // Common getters and setters
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { 
        this.nodeType = nodeType;
        updateLastModified();
    }
    
    public String getPhysicalInterface() { return physicalInterface; }
    public void setPhysicalInterface(String physicalInterface) { 
        this.physicalInterface = physicalInterface;
        updateLastModified();
    }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive;
        updateLastModified();
    }
    
    public boolean isActive() { return isActive != null && isActive; }
    
    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { 
        this.connectionStatus = connectionStatus;
        updateLastModified();
    }
    
    public String getNodeConfigJson() { return nodeConfigJson; }
    public void setNodeConfigJson(String nodeConfigJson) { 
        this.nodeConfigJson = nodeConfigJson;
        updateLastModified();
    }
    
    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { 
        this.priorityLevel = priorityLevel;
        updateLastModified();
    }
}