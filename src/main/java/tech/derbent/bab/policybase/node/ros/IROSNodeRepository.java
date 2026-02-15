package tech.derbent.bab.policybase.node.ros;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

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

	// ROS specific queries
	
	/**
	 * Find ROS node by master port and interface and project.
	 * Used for unique port validation per interface per project.
	 */
	@Query("SELECT n FROM CBabROSNode n WHERE n.rosMasterPort = :port AND n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabROSNode> findByMasterPortAndInterfaceAndProject(@Param("port") Integer port, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
}
