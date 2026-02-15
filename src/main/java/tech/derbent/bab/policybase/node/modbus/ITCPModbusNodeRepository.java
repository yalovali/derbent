package tech.derbent.bab.policybase.node.modbus;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/**
 * ITCPModbusNodeRepository - Repository interface for TCP Modbus nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Provides data access methods for Modbus TCP virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support.
 */
@Profile("bab")
public interface ITCPModbusNodeRepository extends INodeEntityRepository<CBabTCPModbusNode> {
	
	@Override
	@Query("""
			SELECT DISTINCT n FROM CBabTCPModbusNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CBabTCPModbusNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabTCPModbusNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
			WHERE n.project = :project
			ORDER BY n.name ASC
			""")
	List<CBabTCPModbusNode> listByProjectForPageView(@Param("project") CProject<?> project);

	// TCP Modbus specific queries
	
	/**
	 * Find TCP Modbus node by port and unit ID and interface and project.
	 * Used for unique port+unit validation per interface per project.
	 */
	@Query("SELECT n FROM CBabTCPModbusNode n WHERE n.serverPort = :port AND n.unitId = :unitId AND n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabTCPModbusNode> findByPortAndUnitIdAndInterfaceAndProject(@Param("port") Integer port, @Param("unitId") Integer unitId, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
}
