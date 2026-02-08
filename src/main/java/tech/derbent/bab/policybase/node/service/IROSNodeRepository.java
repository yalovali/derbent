package tech.derbent.bab.policybase.node.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabROSNode;

/**
 * IROSNodeRepository - Repository interface for ROS nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Provides data access methods for ROS virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support.
 */
@Profile("bab")
public interface IROSNodeRepository extends INodeEntityRepository<CBabROSNode> {
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabROSNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CBabROSNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabROSNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.project = :project
		ORDER BY n.name ASC
		""")
	List<CBabROSNode> listByProjectForPageView(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabROSNode n WHERE n.project = :project AND n.isActive = true")
	long countActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabROSNode n WHERE n.project = :project AND n.connectionStatus = :connectionStatus")
	long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM CBabROSNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabROSNode n WHERE n.project = :project AND n.isActive = true ORDER BY n.name ASC")
	List<CBabROSNode> findActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabROSNode n WHERE n.connectionStatus = :connectionStatus ORDER BY n.name ASC")
	List<CBabROSNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
	
	@Override
	@Query("SELECT n FROM CBabROSNode n WHERE n.project = :project ORDER BY n.name ASC")
	List<CBabROSNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabROSNode n WHERE n.physicalInterface = :physicalInterface ORDER BY n.name ASC")
	List<CBabROSNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
	
	@Override
	@Query("SELECT n FROM CBabROSNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabROSNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	// ROS specific queries
	
	/**
	 * Find ROS node by master port and interface and project.
	 * Used for unique port validation per interface per project.
	 */
	@Query("SELECT n FROM CBabROSNode n WHERE n.rosMasterPort = :port AND n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabROSNode> findByMasterPortAndInterfaceAndProject(@Param("port") Integer port, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	/**
	 * Find all ROS nodes by ROS version (ROS1 or ROS2).
	 */
	@Query("SELECT n FROM CBabROSNode n WHERE n.rosVersion = :version ORDER BY n.name ASC")
	List<CBabROSNode> findByRosVersion(@Param("version") String version);
	
	/**
	 * Find all ROS nodes by namespace.
	 */
	@Query("SELECT n FROM CBabROSNode n WHERE n.nodeNamespace = :namespace ORDER BY n.name ASC")
	List<CBabROSNode> findByNodeNamespace(@Param("namespace") String namespace);
	
	/**
	 * Count ROS nodes by version in project.
	 */
	@Query("SELECT COUNT(n) FROM CBabROSNode n WHERE n.rosVersion = :version AND n.project = :project")
	long countByRosVersionAndProject(@Param("version") String version, @Param("project") CProject<?> project);
}
