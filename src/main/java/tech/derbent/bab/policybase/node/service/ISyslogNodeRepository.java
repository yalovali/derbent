package tech.derbent.bab.policybase.node.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabSyslogNode;

/**
 * ISyslogNodeRepository - Repository interface for Syslog nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Provides data access methods for Syslog server virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support.
 */
@Profile("bab")
public interface ISyslogNodeRepository extends INodeEntityRepository<CBabSyslogNode> {
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabSyslogNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CBabSyslogNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabSyslogNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.project = :project
		ORDER BY n.name ASC
		""")
	List<CBabSyslogNode> listByProjectForPageView(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabSyslogNode n WHERE n.project = :project AND n.isActive = true")
	long countActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabSyslogNode n WHERE n.project = :project AND n.connectionStatus = :connectionStatus")
	long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM CBabSyslogNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabSyslogNode n WHERE n.project = :project AND n.isActive = true ORDER BY n.name ASC")
	List<CBabSyslogNode> findActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabSyslogNode n WHERE n.connectionStatus = :connectionStatus ORDER BY n.name ASC")
	List<CBabSyslogNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
	
	@Override
	@Query("SELECT n FROM CBabSyslogNode n WHERE n.project = :project ORDER BY n.name ASC")
	List<CBabSyslogNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabSyslogNode n WHERE n.physicalInterface = :physicalInterface ORDER BY n.name ASC")
	List<CBabSyslogNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
	
	@Override
	@Query("SELECT n FROM CBabSyslogNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabSyslogNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	// Syslog specific queries
	
	/**
	 * Find Syslog node by port and interface and project.
	 * Used for unique port validation per interface per project.
	 */
	@Query("SELECT n FROM CBabSyslogNode n WHERE n.listenPort = :port AND n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabSyslogNode> findByListenPortAndInterfaceAndProject(@Param("port") Integer port, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	/**
	 * Find all Syslog nodes by protocol type (UDP or TCP).
	 */
	@Query("SELECT n FROM CBabSyslogNode n WHERE n.protocol = :protocol ORDER BY n.name ASC")
	List<CBabSyslogNode> findByProtocol(@Param("protocol") String protocol);
	
	/**
	 * Find all TLS-enabled Syslog nodes.
	 */
	@Query("SELECT n FROM CBabSyslogNode n WHERE n.enableTls = true ORDER BY n.name ASC")
	List<CBabSyslogNode> findTlsEnabledNodes();
	
	/**
	 * Count Syslog nodes by protocol in project.
	 */
	@Query("SELECT COUNT(n) FROM CBabSyslogNode n WHERE n.protocol = :protocol AND n.project = :project")
	long countByProtocolAndProject(@Param("protocol") String protocol, @Param("project") CProject<?> project);
}
