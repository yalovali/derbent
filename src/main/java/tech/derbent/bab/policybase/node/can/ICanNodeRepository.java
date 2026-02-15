package tech.derbent.bab.policybase.node.can;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/** ICanNodeRepository - Repository interface for CAN Bus nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete repository with HQL queries. Provides data access methods for CAN Bus virtual network nodes. Includes eager loading patterns for UI
 * display and polymorphic support. */
@Profile ("bab")
public interface ICanNodeRepository extends INodeEntityRepository<CBabCanNode> {
	@Override
	@Query ("""
				SELECT DISTINCT n FROM CBabCanNode n
				LEFT JOIN FETCH n.project
				LEFT JOIN FETCH n.createdBy
			LEFT JOIN FETCH n.attachments
			LEFT JOIN FETCH n.comments
			LEFT JOIN FETCH n.links
			WHERE n.id = :id
			""")
	Optional<CBabCanNode> findById(@Param ("id") Long id);

	@Override
	@Query ("""
				SELECT DISTINCT n FROM CBabCanNode n
			LEFT JOIN FETCH n.project
			LEFT JOIN FETCH n.createdBy
			LEFT JOIN FETCH n.attachments
			LEFT JOIN FETCH n.comments
			LEFT JOIN FETCH n.links
			WHERE n.project = :project
			ORDER BY n.name ASC
			""")
	List<CBabCanNode> listByProjectForPageView(@Param ("project") CProject<?> project);
}
