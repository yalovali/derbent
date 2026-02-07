package tech.derbent.bab.policybase.node.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CVehicleNode;

/**
 * IVehicleNodeRepository - Repository interface for Vehicle nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Provides data access methods for Vehicle virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support.
 */
@Profile("bab")
public interface IVehicleNodeRepository extends INodeEntityRepository<CVehicleNode> {
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CVehicleNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CVehicleNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CVehicleNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.project = :project
		ORDER BY n.name ASC
		""")
	List<CVehicleNode> listByProjectForPageView(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CVehicleNode n WHERE n.project = :project AND n.isActive = true")
	long countActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CVehicleNode n WHERE n.project = :project AND n.connectionStatus = :connectionStatus")
	long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM CVehicleNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CVehicleNode n WHERE n.project = :project AND n.isActive = true ORDER BY n.name ASC")
	List<CVehicleNode> findActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CVehicleNode n WHERE n.connectionStatus = :connectionStatus ORDER BY n.name ASC")
	List<CVehicleNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
	
	@Override
	@Query("SELECT n FROM CVehicleNode n WHERE n.project = :project ORDER BY n.name ASC")
	List<CVehicleNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CVehicleNode n WHERE n.physicalInterface = :physicalInterface ORDER BY n.name ASC")
	List<CVehicleNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
	
	@Override
	@Query("SELECT n FROM CVehicleNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CVehicleNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	// Vehicle specific queries
	
	/**
	 * Find vehicle node by vehicle ID (VIN).
	 * Used for unique vehicle identification.
	 */
	@Query("SELECT n FROM CVehicleNode n WHERE n.vehicleId = :vehicleId AND n.project = :project")
	Optional<CVehicleNode> findByVehicleIdAndProject(@Param("vehicleId") String vehicleId, @Param("project") CProject<?> project);
	
	/**
	 * Find all vehicles by type.
	 */
	@Query("SELECT n FROM CVehicleNode n WHERE n.vehicleType = :vehicleType ORDER BY n.name ASC")
	List<CVehicleNode> findByVehicleType(@Param("vehicleType") String vehicleType);
	
	/**
	 * Find all vehicles by manufacturer.
	 */
	@Query("SELECT n FROM CVehicleNode n WHERE n.manufacturer = :manufacturer ORDER BY n.name ASC")
	List<CVehicleNode> findByManufacturer(@Param("manufacturer") String manufacturer);
	
	/**
	 * Find all vehicles using CAN-FD protocol.
	 */
	@Query("SELECT n FROM CVehicleNode n WHERE n.canProtocol = 'CAN-FD' ORDER BY n.name ASC")
	List<CVehicleNode> findCanFdVehicles();
	
	/**
	 * Count vehicles by type in project.
	 */
	@Query("SELECT COUNT(n) FROM CVehicleNode n WHERE n.vehicleType = :vehicleType AND n.project = :project")
	long countByVehicleTypeAndProject(@Param("vehicleType") String vehicleType, @Param("project") CProject<?> project);
}
