package tech.derbent.bab.policybase.node.can;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/**
 * ICanNodeRepository - Repository interface for CAN Bus nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Provides data access methods for CAN Bus virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support.
 */
@Profile("bab")
public interface ICanNodeRepository extends INodeEntityRepository<CBabCanNode> {
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabCanNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CBabCanNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabCanNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.project = :project
		ORDER BY n.name ASC
		""")
	List<CBabCanNode> listByProjectForPageView(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabCanNode n WHERE n.project = :project AND n.isActive = true")
	long countActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabCanNode n WHERE n.project = :project AND n.connectionStatus = :connectionStatus")
	long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM CBabCanNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabCanNode n WHERE n.project = :project AND n.isActive = true ORDER BY n.name ASC")
	List<CBabCanNode> findActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabCanNode n WHERE n.connectionStatus = :connectionStatus ORDER BY n.name ASC")
	List<CBabCanNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
	
	@Override
	@Query("SELECT n FROM CBabCanNode n WHERE n.project = :project ORDER BY n.name ASC")
	List<CBabCanNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabCanNode n WHERE n.physicalInterface = :physicalInterface ORDER BY n.name ASC")
	List<CBabCanNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
	
	@Override
	@Query("SELECT n FROM CBabCanNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabCanNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	// CAN specific queries
	
	/**
	 * Find CAN node by bitrate and project.
	 * Used for unique bitrate validation per interface per project.
	 */
	@Query("SELECT n FROM CBabCanNode n WHERE n.bitrate = :bitrate AND n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabCanNode> findByBitrateAndInterfaceAndProject(@Param("bitrate") Integer bitrate, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	/**
	 * Find all CAN nodes by bitrate.
	 */
	@Query("SELECT n FROM CBabCanNode n WHERE n.bitrate = :bitrate ORDER BY n.name ASC")
	List<CBabCanNode> findByBitrate(@Param("bitrate") Integer bitrate);
	
	/**
	 * Find all high-speed CAN nodes (bitrate >= 500000).
	 */
	@Query("SELECT n FROM CBabCanNode n WHERE n.bitrate >= 500000 ORDER BY n.name ASC")
	List<CBabCanNode> findHighSpeedNodes();
	
	/**
	 * Count CAN nodes by bitrate in project.
	 */
	@Query("SELECT COUNT(n) FROM CBabCanNode n WHERE n.bitrate = :bitrate AND n.project = :project")
	long countByBitrateAndProject(@Param("bitrate") Integer bitrate, @Param("project") CProject<?> project);
}
