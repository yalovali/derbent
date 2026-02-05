package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CHttpServerNode;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/**
 * IHttpServerNodeRepository - Repository interface for HTTP server virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Handles HTTP server node persistence with complete eager loading for UI performance.
 * Provides specialized queries for HTTP server configuration and monitoring.
 */
@Profile("bab")
public interface IHttpServerNodeRepository extends INodeEntityRepository<CHttpServerNode> {
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.attachments
	LEFT JOIN FETCH e.comments
	LEFT JOIN FETCH e.links
	WHERE e.id = :id
	""")
    Optional<CHttpServerNode> findById(@Param("id") Long id);
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.attachments
	LEFT JOIN FETCH e.comments
	LEFT JOIN FETCH e.links
	WHERE e.project = :project
	ORDER BY e.id DESC
	""")
    List<CHttpServerNode> listByProjectForPageView(@Param("project") CProject<?> project);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.physicalInterface = :physicalInterface AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CHttpServerNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	WHERE e.isActive = true AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CHttpServerNode> findActiveByProject(@Param("project") CProject<?> project);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.connectionStatus = :connectionStatus AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CHttpServerNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.nodeType = :nodeType AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CHttpServerNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.physicalInterface = :physicalInterface AND e.project = :project")
    boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT e FROM #{#entityName} e WHERE e.physicalInterface = :physicalInterface AND e.project = :project")
    Optional<CHttpServerNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.isActive = true AND e.project = :project")
    long countActiveByProject(@Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.connectionStatus = :connectionStatus AND e.project = :project")
    long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
    
    // HTTP server specific queries
    
    /**
     * Find HTTP servers by port and project.
     * Useful for port conflict detection.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.serverPort = :port AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CHttpServerNode> findByServerPortAndProject(@Param("port") Integer port, @Param("project") CProject<?> project);
    
    /**
     * Find HTTP servers by protocol and project.
     * Useful for SSL/non-SSL grouping.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.protocol = :protocol AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CHttpServerNode> findByProtocolAndProject(@Param("protocol") String protocol, @Param("project") CProject<?> project);
    
    /**
     * Find SSL-enabled HTTP servers.
     * Useful for security configuration.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.sslEnabled = true AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CHttpServerNode> findSslEnabledByProject(@Param("project") CProject<?> project);
    
    /**
     * Check if port is already used by another server on same interface.
     * Critical for port conflict validation.
     */
    @Query("""
	SELECT COUNT(e) > 0 FROM #{#entityName} e
	WHERE e.physicalInterface = :physicalInterface 
	AND e.serverPort = :port 
	AND e.project = :project
	AND (:excludeId IS NULL OR e.id != :excludeId)
	""")
    boolean existsByPhysicalInterfaceAndPortExcluding(
        @Param("physicalInterface") String physicalInterface,
        @Param("port") Integer port,
        @Param("project") CProject<?> project,
        @Param("excludeId") Long excludeId);
    
    /**
     * Find servers with high connection count for load monitoring.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.maxConnections >= :minConnections AND e.project = :project
	ORDER BY e.maxConnections DESC
	""")
    List<CHttpServerNode> findHighCapacityServers(@Param("minConnections") Integer minConnections, @Param("project") CProject<?> project);
    
    /**
     * Find servers by endpoint pattern for API grouping.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.endpointPath LIKE :pattern AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CHttpServerNode> findByEndpointPathPattern(@Param("pattern") String pattern, @Param("project") CProject<?> project);
}