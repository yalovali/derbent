package tech.derbent.bab.policybase.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.interfaces.IHasColor;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.IHasLinks;

/** CBabNodeEntity - Abstract base class for virtual network nodes in BAB Actions Dashboard. Layer: Domain (MVC) Active when: 'bab' profile is active
 * JPA Inheritance Strategy: JOINED - Base table: cbab_node (common fields for all node types) - Child tables: cnode_http_server, cnode_vehicle,
 * cnode_file_input (type-specific fields) - Discriminator column: node_type (identifies concrete type: HTTP_SERVER, VEHICLE, FILE_INPUT) This allows
 * CProject_Bab to hold a single polymorphic list containing all node types. Represents virtual network entities mapped to physical network
 * interfaces: - HTTP Server nodes (httpserver1 → eth0) - Vehicle nodes (vehicleX → can1) - File Input nodes (fileInput → file) All virtual nodes have
 * common properties for policy rule engine integration and Calimero gateway configuration export. */
@Entity // Changed from @MappedSuperclass to support polymorphic collections
@Table (name = "cbab_node") // Base table for all nodes
@Inheritance (strategy = InheritanceType.JOINED) // JOINED strategy - separate tables for type-specific fields
@DiscriminatorColumn (name = "node_type", discriminatorType = DiscriminatorType.STRING) // Type identifier column
@Profile ("bab")
public abstract class CBabNodeEntity<EntityClass> extends CEntityOfProject<EntityClass>
		implements IHasColor, IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {

	// Base constants (protected - not final, can be overridden by subclasses)
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeEntity.class);
	@Column (name = "connection_status", length = 20, nullable = false)
	@AMetaData (
			displayName = "Connection Status", required = false, readOnly = true,
			description = "Current connection status (CONNECTED, DISCONNECTED, ERROR)", hidden = false, maxLength = 20
	)
	private String connectionStatus = "DISCONNECTED";
	// Node operational state - initialized at declaration (RULE 6)
	@Column (name = "is_active", nullable = false)
	@AMetaData (
			displayName = "Active", required = true, readOnly = false, description = "Whether this virtual node is currently active in the network",
			hidden = false
	)
	private Boolean isActive = true;
	// Calimero integration configuration
	@Column (name = "node_config", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Node Configuration", required = false, readOnly = false, description = "JSON configuration for Calimero gateway export",
			hidden = false
	)
	private String nodeConfigJson;
	// Core node identification fields
	// NOTE: node_type column is managed by @DiscriminatorColumn - no field mapping needed
	// Use getClass().getSimpleName() to get the node type at runtime
	@Column (name = "physical_interface", length = 100, nullable = false)
	@AMetaData (
			displayName = "Physical Interface", required = true, readOnly = false,
			description = "Physical network interface mapping (eth0, can1, file, etc.)", hidden = false, maxLength = 100
	)
	private String physicalInterface;
	@Column (name = "priority_level", nullable = false)
	@AMetaData (
			displayName = "Priority Level", required = false, readOnly = false, description = "Node priority for policy rule processing (0-100)",
			hidden = false
	)
	private Integer priorityLevel = 50;

	/** Default constructor for JPA. */
	protected CBabNodeEntity() {
		// Abstract JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	protected CBabNodeEntity(Class<EntityClass> clazz, String name, CProject<?> project) {
		super(clazz, name, project);
		// nodeType is managed by @DiscriminatorColumn - no manual assignment needed
		// Abstract constructors do NOT call initializeDefaults()
		// Concrete subclasses will call initializeDefaults() which will chain to abstract implementation
	}
	// Abstract initializeDefaults - implemented by subclasses
	// No implementation here - each concrete class implements

	/** Check if this node can be used as a destination in policy rules.
	 * @return true if node can be a rule destination */
	public boolean canBeRuleDestination() {
		return isActive != null && isActive;
	}

	/** Check if this node can be used as a source in policy rules.
	 * @return true if node can be a rule source */
	public boolean canBeRuleSource() {
		return isActive != null && isActive && "CONNECTED".equals(connectionStatus);
	}

	/** Generate default node configuration JSON for Calimero. Subclasses should override with type-specific configuration.
	 * @return default JSON configuration */
	protected String generateDefaultNodeConfig() {
		return """
				{
				    "nodeId": "%s",
				    "nodeType": "%s",
				    "physicalInterface": "%s",
				    "active": %s,
				    "priority": %d
				}
				""".formatted(getId(), getClass().getSimpleName(), physicalInterface, isActive, priorityLevel);
	}

	public String getConnectionStatus() { return connectionStatus; }

	public Boolean getIsActive() { return isActive; }

	public String getNodeConfigJson() { return nodeConfigJson; }

	// Common getters and setters
	/** Get the node type from the discriminator value (class simple name).
	 * @return node type identifier */
	public String getNodeType() { 
		return getClass().getSimpleName(); 
	}

	public String getPhysicalInterface() { return physicalInterface; }

	public Integer getPriorityLevel() { return priorityLevel; }

	public boolean isActive() { return isActive != null && isActive; }

	public void setConnectionStatus(String connectionStatus) {
		this.connectionStatus = connectionStatus;
		updateLastModified();
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
		updateLastModified();
	}

	public void setNodeConfigJson(String nodeConfigJson) {
		this.nodeConfigJson = nodeConfigJson;
		updateLastModified();
	}

	// NOTE: setNodeType() removed - nodeType is managed by @DiscriminatorColumn (immutable)

	public void setPhysicalInterface(String physicalInterface) {
		this.physicalInterface = physicalInterface;
		updateLastModified();
	}

	public void setPriorityLevel(Integer priorityLevel) {
		this.priorityLevel = priorityLevel;
		updateLastModified();
	}
}
