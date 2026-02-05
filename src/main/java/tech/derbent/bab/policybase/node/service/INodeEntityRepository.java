package tech.derbent.bab.policybase.node.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CNodeEntity;

/**
 * INodeEntityRepository - Abstract repository interface for virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Abstract repository with @NoRepositoryBean annotation.
 * 
 * Base repository for all virtual network node entities (HTTP servers, vehicles, file inputs).
 * Provides common query methods for node management and eager loading patterns.
 * 
 * Note: Uses @NoRepositoryBean to prevent Spring instantiation of this abstract interface.
 * Concrete repositories (IHttpServerNodeRepository, etc.) implement the actual HQL queries.
 */
@Profile("bab")
@NoRepositoryBean  // MANDATORY - Abstract repositories are not beans
public interface INodeEntityRepository<NodeType extends CNodeEntity<NodeType>> 
    extends IEntityOfProjectRepository<NodeType> {
    
    // Abstract method declarations for node-specific queries
    // No HQL queries in abstract interface - only method signatures
    
    /**
     * Find nodes by physical interface.
     * Concrete repositories implement with specific entity type.
     */
    List<NodeType> findByPhysicalInterface(String physicalInterface);
    
    /**
     * Find active nodes by project.
     * Concrete repositories implement with eager loading.
     */
    List<NodeType> findActiveByProject(CProject<?> project);
    
    /**
     * Find nodes by connection status.
     * Concrete repositories implement with specific status filtering.
     */
    List<NodeType> findByConnectionStatus(String connectionStatus);
    
    /**
     * Find nodes by type and project.
     * Concrete repositories implement with node type filtering.
     */
    List<NodeType> findByNodeTypeAndProject(String nodeType, CProject<?> project);
    
    /**
     * Check if interface is already used by another node.
     * Concrete repositories implement for interface validation.
     */
    boolean existsByPhysicalInterfaceAndProject(String physicalInterface, CProject<?> project);
    
    /**
     * Find node by interface and project (for uniqueness validation).
     * Concrete repositories implement for constraint checking.
     */
    Optional<NodeType> findByPhysicalInterfaceAndProject(String physicalInterface, CProject<?> project);
    
    /**
     * Count total nodes by project.
     * Inherited from base but can be overridden for optimization.
     */
    @Override
    @Query("SELECT COUNT(n) FROM #{#entityName} n WHERE n.project = :project")
    long countByProject(@Param("project") CProject<?> project);
    
    /**
     * Count active nodes by project.
     * Abstract method - concrete repositories implement with specific queries.
     */
    long countActiveByProject(CProject<?> project);
    
    /**
     * Count nodes by connection status.
     * Abstract method - concrete repositories implement for dashboard statistics.
     */
    long countByConnectionStatusAndProject(String connectionStatus, CProject<?> project);
}