package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CNodeEntity;
import tech.derbent.base.session.service.ISessionService;

/**
 * CNodeEntityService - Abstract service for virtual network nodes in BAB Actions Dashboard.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Abstract service with NO @Service annotation.
 * 
 * Provides common business logic for all virtual network node types:
 * - HTTP Server nodes, Vehicle nodes, File Input nodes
 * - Node configuration validation and generation
 * - Calimero integration support
 * - Physical interface conflict detection
 * 
 * Abstract services do NOT implement IEntityRegistrable or IEntityWithView.
 * Only concrete services implement these interfaces.
 */
@Profile("bab")
@PreAuthorize("isAuthenticated()")
// NO @Service - Abstract services are NOT Spring beans
// NO IEntityRegistrable, IEntityWithView - Only concrete services implement these
public abstract class CNodeEntityService<NodeType extends CNodeEntity<NodeType>> 
    extends CEntityOfProjectService<NodeType> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CNodeEntityService.class);
    
    protected CNodeEntityService(final INodeEntityRepository<NodeType> repository, 
                               final Clock clock, 
                               final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }
    
    @Override
    protected void validateEntity(final NodeType entity) {
        super.validateEntity(entity);
        
        // Required fields - explicit validation for critical node fields
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        Check.notBlank(entity.getNodeType(), "Node Type is required");
        Check.notBlank(entity.getPhysicalInterface(), "Physical Interface is required");
        Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
        
        // String length validation - MANDATORY helper usage
        validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
        validateStringLength(entity.getNodeType(), "Node Type", 50);
        validateStringLength(entity.getPhysicalInterface(), "Physical Interface", 100);
        
        if (entity.getConnectionStatus() != null) {
            validateStringLength(entity.getConnectionStatus(), "Connection Status", 20);
        }
        
        // Numeric validation - MANDATORY helper usage
        if (entity.getPriorityLevel() != null) {
            validateNumericField(entity.getPriorityLevel(), "Priority Level", 100);
        }
        
        // Unique name validation - MANDATORY helper usage
        validateUniqueNameInProject(
            (INodeEntityRepository<NodeType>) repository,
            entity, entity.getName(), entity.getProject());
        
        // Node-specific validation
        validateNodeSpecificFields(entity);
        
        // Physical interface validation
        validatePhysicalInterface(entity);
        
        // Node configuration validation
        validateNodeConfiguration(entity);
    }
    
    /**
     * Validate node-specific fields.
     * Abstract method implemented by concrete services for type-specific validation.
     */
    protected abstract void validateNodeSpecificFields(final NodeType entity);
    
    /**
     * Validate physical interface configuration.
     * Checks for interface conflicts and availability.
     */
    protected void validatePhysicalInterface(final NodeType entity) {
        final String physicalInterface = entity.getPhysicalInterface();
        if (physicalInterface == null || physicalInterface.trim().isEmpty()) {
            throw new CValidationException("Physical interface cannot be empty");
        }
        
        // Check for interface conflicts (excluding current entity)
        final INodeEntityRepository<NodeType> nodeRepo = (INodeEntityRepository<NodeType>) repository;
        final boolean interfaceInUse = nodeRepo.existsByPhysicalInterfaceAndProject(
            physicalInterface, entity.getProject());
        
        if (interfaceInUse) {
            // If interface is in use, check if it's the same entity (update scenario)
            final var existingNode = nodeRepo.findByPhysicalInterfaceAndProject(
                physicalInterface, entity.getProject());
            
            if (existingNode.isPresent() && 
                (entity.getId() == null || !existingNode.get().getId().equals(entity.getId()))) {
                throw new CValidationException(String.format(
                    "Physical interface '%s' is already in use by node '%s'",
                    physicalInterface, existingNode.get().getName()));
            }
        }
        
        // Validate interface format (subclasses can override for specific validation)
        validatePhysicalInterfaceFormat(entity, physicalInterface);
    }
    
    /**
     * Validate physical interface format.
     * Can be overridden by concrete services for specific interface validation.
     */
    protected void validatePhysicalInterfaceFormat(final NodeType entity, final String physicalInterface) {
        // Base validation - concrete services can override for specific formats
        if (!isValidPhysicalInterface(physicalInterface)) {
            throw new CValidationException(String.format(
                "Invalid physical interface format: '%s'", physicalInterface));
        }
    }
    
    /**
     * Check if physical interface format is valid.
     * Base implementation - concrete services should override for specific validation.
     */
    protected boolean isValidPhysicalInterface(final String physicalInterface) {
        // Basic validation - alphanumeric with common separators
        return physicalInterface.matches("^[a-zA-Z0-9._-]+$");
    }
    
    /**
     * Validate node configuration JSON.
     * Ensures JSON is valid and contains required fields.
     */
    protected void validateNodeConfiguration(final NodeType entity) {
        final String configJson = entity.getNodeConfigJson();
        if (configJson != null && !configJson.trim().isEmpty()) {
            try {
                // Basic JSON validation - concrete services can add type-specific validation
                validateJsonConfiguration(entity, configJson);
            } catch (final Exception e) {
                throw new CValidationException(String.format(
                    "Invalid node configuration JSON: %s", e.getMessage()));
            }
        }
    }
    
    /**
     * Validate JSON configuration format and content.
     * Abstract method for type-specific JSON validation.
     */
    protected abstract void validateJsonConfiguration(final NodeType entity, final String configJson);
    
    @Override
    public void initializeNewEntity(final Object entity) {
        super.initializeNewEntity(entity);
        
        if (!(entity instanceof CNodeEntity)) {
            return;
        }
        
        @SuppressWarnings("unchecked")
        final NodeType nodeEntity = (NodeType) entity;
        
        LOGGER.debug("Initializing new {} node entity", nodeEntity.getNodeType());
        
        // Set default connection status if not set
        if (nodeEntity.getConnectionStatus() == null || nodeEntity.getConnectionStatus().isEmpty()) {
            nodeEntity.setConnectionStatus("DISCONNECTED");
        }
        
        // Generate initial node configuration if not set
        if (nodeEntity.getNodeConfigJson() == null || nodeEntity.getNodeConfigJson().isEmpty()) {
            final String defaultConfig = generateDefaultNodeConfiguration(nodeEntity);
            nodeEntity.setNodeConfigJson(defaultConfig);
        }
        
        // Node-specific initialization
        initializeNodeSpecificDefaults(nodeEntity);
        
        LOGGER.debug("Node entity initialization complete for: {}", nodeEntity.getName());
    }
    
    /**
     * Generate default node configuration JSON.
     * Abstract method implemented by concrete services for type-specific configuration.
     */
    protected abstract String generateDefaultNodeConfiguration(final NodeType entity);
    
    /**
     * Initialize node-specific default values.
     * Abstract method for type-specific initialization.
     */
    protected abstract void initializeNodeSpecificDefaults(final NodeType entity);
    
    /**
     * Copy entity fields from source to target.
     * Implements service-based copy pattern for node entities.
     */
    @Override
    public void copyEntityFieldsTo(final NodeType source, final CEntityDB<?> target, final CCloneOptions options) {
        // STEP 1: ALWAYS call parent first
        super.copyEntityFieldsTo(source, target, options);
        
        // STEP 2: Type-check target (use pattern matching if Java 17+)
        if (!(target instanceof CNodeEntity)) {
            return;
        }
        @SuppressWarnings("unchecked")
        final NodeType targetNode = (NodeType) target;
        
        // STEP 3: Copy common node fields using DIRECT setter/getter
        targetNode.setNodeType(source.getNodeType());
        
        // Make physical interface unique for copy
        if (source.getPhysicalInterface() != null) {
            final String uniqueInterface = source.getPhysicalInterface() + "_copy";
            targetNode.setPhysicalInterface(uniqueInterface);
        }
        
        targetNode.setIsActive(source.getIsActive());
        targetNode.setConnectionStatus("DISCONNECTED"); // Reset connection status for copy
        targetNode.setPriorityLevel(source.getPriorityLevel());
        
        // Copy configuration JSON (will be regenerated if needed)
        targetNode.setNodeConfigJson(source.getNodeConfigJson());
        
        // Node-specific field copying
        copyNodeSpecificFields(source, targetNode, options);
        
        LOGGER.debug("Copied {} node '{}' with options: {}", 
                    source.getNodeType(), source.getName(), options);
    }
    
    /**
     * Copy node-specific fields from source to target.
     * Abstract method for type-specific field copying.
     */
    protected abstract void copyNodeSpecificFields(final NodeType source, final NodeType target, final CCloneOptions options);
    
    // Node management operations
    
    /**
     * Find all nodes by physical interface.
     */
    @Transactional(readOnly = true)
    public List<NodeType> findByPhysicalInterface(final String physicalInterface) {
        Check.notBlank(physicalInterface, "Physical interface cannot be blank");
        return ((INodeEntityRepository<NodeType>) repository).findByPhysicalInterface(physicalInterface);
    }
    
    /**
     * Find active nodes by project.
     */
    @Transactional(readOnly = true)
    public List<NodeType> findActiveByProject(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        return ((INodeEntityRepository<NodeType>) repository).findActiveByProject(project);
    }
    
    /**
     * Find nodes by connection status.
     */
    @Transactional(readOnly = true)
    public List<NodeType> findByConnectionStatus(final String connectionStatus) {
        Check.notBlank(connectionStatus, "Connection status cannot be blank");
        return ((INodeEntityRepository<NodeType>) repository).findByConnectionStatus(connectionStatus);
    }
    
    /**
     * Count active nodes by project.
     */
    @Transactional(readOnly = true)
    public long countActiveByProject(final tech.derbent.api.projects.domain.CProject<?> project) {
        Check.notNull(project, "Project cannot be null");
        return ((INodeEntityRepository<NodeType>) repository).countActiveByProject(project);
    }
    
    /**
     * Update node connection status.
     */
    @Transactional
    public void updateConnectionStatus(final NodeType node, final String connectionStatus) {
        Check.notNull(node, "Node cannot be null");
        Check.notBlank(connectionStatus, "Connection status cannot be blank");
        
        node.setConnectionStatus(connectionStatus);
        save(node);
        
        LOGGER.debug("Updated connection status for node '{}' to: {}", 
                    node.getName(), connectionStatus);
    }
    
    /**
     * Generate updated node configuration JSON.
     * Regenerates configuration based on current node settings.
     */
    @Transactional
    public String regenerateNodeConfiguration(final NodeType node) {
        Check.notNull(node, "Node cannot be null");
        
        final String newConfig = generateDefaultNodeConfiguration(node);
        node.setNodeConfigJson(newConfig);
        save(node);
        
        LOGGER.debug("Regenerated configuration for node '{}'", node.getName());
        return newConfig;
    }
}