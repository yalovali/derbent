package tech.derbent.bab.policybase.node.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabHttpServerNode;

/**
 * IHttpServerNodeRepository - Repository interface for HTTP Server nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Provides data access methods for HTTP Server virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support.
 */
@Profile("bab")
public interface IHttpServerNodeRepository extends INodeEntityRepository<CBabHttpServerNode> {
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabHttpServerNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CBabHttpServerNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabHttpServerNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.project = :project
		ORDER BY n.name ASC
		""")
	List<CBabHttpServerNode> listByProjectForPageView(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabHttpServerNode n WHERE n.project = :project AND n.isActive = true")
	long countActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabHttpServerNode n WHERE n.project = :project AND n.connectionStatus = :connectionStatus")
	long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM CBabHttpServerNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabHttpServerNode n WHERE n.project = :project AND n.isActive = true ORDER BY n.name ASC")
	List<CBabHttpServerNode> findActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabHttpServerNode n WHERE n.connectionStatus = :connectionStatus ORDER BY n.name ASC")
	List<CBabHttpServerNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
	
	@Override
	@Query("SELECT n FROM CBabHttpServerNode n WHERE n.project = :project ORDER BY n.name ASC")
	List<CBabHttpServerNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabHttpServerNode n WHERE n.physicalInterface = :physicalInterface ORDER BY n.name ASC")
	List<CBabHttpServerNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
	
	@Override
	@Query("SELECT n FROM CBabHttpServerNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabHttpServerNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	// HTTP Server specific queries
	
	/**
	 * Find HTTP server node by port and project.
	 * Used for unique port validation per project.
	 */
	@Query("SELECT n FROM CBabHttpServerNode n WHERE n.serverPort = :port AND n.project = :project")
	Optional<CBabHttpServerNode> findByServerPortAndProject(@Param("port") Integer port, @Param("project") CProject<?> project);
	
	/**
	 * Find all HTTP server nodes by protocol type.
	 */
	@Query("SELECT n FROM CBabHttpServerNode n WHERE n.protocol = :protocol ORDER BY n.name ASC")
	List<CBabHttpServerNode> findByProtocol(@Param("protocol") String protocol);
	
	/**
	 * Find all HTTPS-enabled nodes (SSL enabled).
	 */
	@Query("SELECT n FROM CBabHttpServerNode n WHERE n.sslEnabled = true ORDER BY n.name ASC")
	List<CBabHttpServerNode> findSslEnabledNodes();
	
	/**
	 * Count HTTP server nodes by protocol in project.
	 */
	@Query("SELECT COUNT(n) FROM CBabHttpServerNode n WHERE n.protocol = :protocol AND n.project = :project")
	long countByProtocolAndProject(@Param("protocol") String protocol, @Param("project") CProject<?> project);
}
