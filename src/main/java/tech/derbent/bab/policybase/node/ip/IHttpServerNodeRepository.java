package tech.derbent.bab.policybase.node.ip;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/** IHttpServerNodeRepository - Repository interface for HTTP Server nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Concrete repository with HQL queries. Provides data access methods for HTTP Server virtual network nodes. Includes eager loading
 * patterns for UI display and polymorphic support. */
@Profile ("bab")
public interface IHttpServerNodeRepository extends INodeEntityRepository<CBabHttpServerNode> {

	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			WHERE e.id = :id
			""")
	Optional<CBabHttpServerNode> findById(@Param ("id") Long id);

	// HTTP Server specific queries
	/** Find HTTP server node by port and project. Used for unique port validation per project. */
	@Query ("SELECT e FROM #{#entityName} e WHERE e.serverPort = :port AND e.project = :project")
	Optional<CBabHttpServerNode> findByServerPortAndProject(@Param ("port") Integer port, @Param ("project") CProject<?> project);

	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			WHERE e.project = :project
			ORDER BY e.name ASC
			""")
	List<CBabHttpServerNode> listByProject(@Param ("project") CProject<?> project);
	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			WHERE e.project = :project
			ORDER BY e.name ASC
			""")
	List<CBabHttpServerNode> listByProjectForPageView(@Param ("project") CProject<?> project);
}
