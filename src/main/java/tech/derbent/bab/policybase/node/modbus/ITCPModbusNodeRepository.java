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
			SELECT DISTINCT e FROM #{#entityName} e
		LEFT JOIN FETCH e.project
		LEFT JOIN FETCH e.createdBy
		LEFT JOIN FETCH e.attachments
		LEFT JOIN FETCH e.comments
		LEFT JOIN FETCH e.links
		WHERE e.id = :id
		""")
	Optional<CBabTCPModbusNode> findById(@Param("id") Long id);
	
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
	List<CBabTCPModbusNode> listByProjectForPageView(@Param("project") CProject<?> project);

	// TCP Modbus specific queries
	
	/**
	 * Find TCP Modbus node by port and unit ID and interface and project.
	 * Used for unique port+unit validation per interface per project.
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.serverPort = :port AND e.unitId = :unitId AND e.physicalInterface = :physicalInterface AND e.project = :project")
	Optional<CBabTCPModbusNode> findByPortAndUnitIdAndInterfaceAndProject(@Param("port") Integer port, @Param("unitId") Integer unitId, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
}
