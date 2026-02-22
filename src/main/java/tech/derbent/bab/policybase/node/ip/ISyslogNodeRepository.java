package tech.derbent.bab.policybase.node.ip;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

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
			SELECT DISTINCT e FROM #{#entityName} e
		LEFT JOIN FETCH e.project
		LEFT JOIN FETCH e.createdBy
		LEFT JOIN FETCH e.attachments
		LEFT JOIN FETCH e.comments
		LEFT JOIN FETCH e.links
		WHERE e.id = :id
		""")
	Optional<CBabSyslogNode> findById(@Param("id") Long id);
	
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
	List<CBabSyslogNode> listByProjectForPageView(@Param("project") CProject<?> project);

	// Syslog specific queries
	
	/**
	 * Find Syslog node by port and interface and project.
	 * Used for unique port validation per interface per project.
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.listenPort = :port AND e.physicalInterface = :physicalInterface AND e.project = :project")
	Optional<CBabSyslogNode> findByListenPortAndInterfaceAndProject(@Param("port") Integer port, @Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
}
