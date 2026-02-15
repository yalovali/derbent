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
				SELECT DISTINCT n FROM CBabHttpServerNode n
			LEFT JOIN FETCH n.project
			LEFT JOIN FETCH n.createdBy
			LEFT JOIN FETCH n.attachments
			LEFT JOIN FETCH n.comments
			LEFT JOIN FETCH n.links
				WHERE n.id = :id
				""")
	Optional<CBabHttpServerNode> findById(@Param ("id") Long id);

	// HTTP Server specific queries
	/** Find HTTP server node by port and project. Used for unique port validation per project. */
	@Query ("SELECT n FROM CBabHttpServerNode n WHERE n.serverPort = :port AND n.project = :project")
	Optional<CBabHttpServerNode> findByServerPortAndProject(@Param ("port") Integer port, @Param ("project") CProject<?> project);

	@Override
	@Query ("""
				SELECT DISTINCT n FROM CBabHttpServerNode n
			LEFT JOIN FETCH n.project
			LEFT JOIN FETCH n.createdBy
			LEFT JOIN FETCH n.attachments
			LEFT JOIN FETCH n.comments
			LEFT JOIN FETCH n.links
			WHERE n.project = :project
			ORDER BY n.name ASC
			""")
	List<CBabHttpServerNode> listByProject(@Param ("project") CProject<?> project);
	@Override
	@Query ("""
			SELECT DISTINCT n FROM CBabHttpServerNode n
			LEFT JOIN FETCH n.project
			LEFT JOIN FETCH n.createdBy
			LEFT JOIN FETCH n.attachments
			LEFT JOIN FETCH n.comments
			LEFT JOIN FETCH n.links
			WHERE n.project = :project
			ORDER BY n.name ASC
			""")
	List<CBabHttpServerNode> listByProjectForPageView(@Param ("project") CProject<?> project);
}
