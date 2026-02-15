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

	// Modbus specific queries
	
	/**
	 * Find Modbus node by slave ID and interface and project.
	 * Used for unique slave ID validation per interface per project.
	 */
	@Query("SELECT n FROM CBabModbusNode n WHERE n.slaveId = :slaveId AND n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabModbusNode> findBySlaveIdAndInterfaceAndProject(@Param("slaveId") Integer slaveId, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
}
