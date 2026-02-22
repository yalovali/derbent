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
			SELECT DISTINCT e FROM #{#entityName} e
		LEFT JOIN FETCH e.project
		LEFT JOIN FETCH e.createdBy
		LEFT JOIN FETCH e.attachments
		LEFT JOIN FETCH e.comments
		LEFT JOIN FETCH e.links
		WHERE e.id = :id
		""")
	Optional<CBabROSNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT e FROM #{#entityName} e
		LEFT JOIN FETCH e.project
		LEFT JOIN FETCH e.createdBy
		LEFT JOIN FETCH e.attachments
		LEFT JOIN FETCH e.comments
		LEFT JOIN FETCH e.links
			WHERE e.project = :project
			ORDER BY e.name ASC
			""")
	List<CBabROSNode> listByProjectForPageView(@Param("project") CProject<?> project);

	// ROS specific queries
	
	/**
	 * Find ROS node by master port and interface and project.
	 * Used for unique port validation per interface per project.
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.rosMasterPort = :port AND e.physicalInterface = :physicalInterface AND e.project = :project")
	Optional<CBabROSNode> findByMasterPortAndInterfaceAndProject(@Param("port") Integer port, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
}
