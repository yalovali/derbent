package tech.derbent.bab.policybase.node.modbus;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabModbusNode;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/**
 * IModbusNodeRepository - Repository interface for Modbus nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Provides data access methods for Modbus RTU/ASCII virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support.
 */
@Profile("bab")
public interface IModbusNodeRepository extends INodeEntityRepository<CBabModbusNode> {
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabModbusNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CBabModbusNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabModbusNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.project = :project
		ORDER BY n.name ASC
		""")
	List<CBabModbusNode> listByProjectForPageView(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabModbusNode n WHERE n.project = :project AND n.isActive = true")
	long countActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabModbusNode n WHERE n.project = :project AND n.connectionStatus = :connectionStatus")
	long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM CBabModbusNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabModbusNode n WHERE n.project = :project AND n.isActive = true ORDER BY n.name ASC")
	List<CBabModbusNode> findActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabModbusNode n WHERE n.connectionStatus = :connectionStatus ORDER BY n.name ASC")
	List<CBabModbusNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
	
	@Override
	@Query("SELECT n FROM CBabModbusNode n WHERE n.project = :project ORDER BY n.name ASC")
	List<CBabModbusNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabModbusNode n WHERE n.physicalInterface = :physicalInterface ORDER BY n.name ASC")
	List<CBabModbusNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
	
	@Override
	@Query("SELECT n FROM CBabModbusNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabModbusNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	// Modbus specific queries
	
	/**
	 * Find Modbus node by slave ID and interface and project.
	 * Used for unique slave ID validation per interface per project.
	 */
	@Query("SELECT n FROM CBabModbusNode n WHERE n.slaveId = :slaveId AND n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabModbusNode> findBySlaveIdAndInterfaceAndProject(@Param("slaveId") Integer slaveId, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	/**
	 * Find all Modbus nodes by mode (RTU or ASCII).
	 */
	@Query("SELECT n FROM CBabModbusNode n WHERE n.modbusMode = :mode ORDER BY n.name ASC")
	List<CBabModbusNode> findByModbusMode(@Param("mode") String mode);
	
	/**
	 * Find all Modbus nodes by baudrate.
	 */
	@Query("SELECT n FROM CBabModbusNode n WHERE n.baudrate = :baudrate ORDER BY n.name ASC")
	List<CBabModbusNode> findByBaudrate(@Param("baudrate") Integer baudrate);
	
	/**
	 * Count Modbus nodes by mode in project.
	 */
	@Query("SELECT COUNT(n) FROM CBabModbusNode n WHERE n.modbusMode = :mode AND n.project = :project")
	long countByModbusModeAndProject(@Param("mode") String mode, @Param("project") CProject<?> project);
}
