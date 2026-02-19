package tech.derbent.bab.policybase.node.file;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/** IFileOutputNodeRepository - Repository interface for File Output nodes.
 *
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 *
 * Provides data access methods for File Output virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support. */
@Profile ("bab")
public interface IFileOutputNodeRepository extends INodeEntityRepository<CBabFileOutputNode> {

	@Override
	@Query ("""
			SELECT DISTINCT n FROM CBabFileOutputNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CBabFileOutputNode> findById(@Param ("id") Long id);

	@Override
	@Query ("""
		SELECT DISTINCT n FROM CBabFileOutputNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
			WHERE n.project = :project
			ORDER BY n.name ASC
			""")
	List<CBabFileOutputNode> listByProjectForPageView(@Param ("project") CProject<?> project);

	/** Find file output node by file path.
	 * Used for unique file path validation per project. */
	@Query ("SELECT n FROM CBabFileOutputNode n WHERE n.filePath = :filePath AND n.project = :project")
	Optional<CBabFileOutputNode> findByFilePathAndProject(@Param ("filePath") String filePath, @Param ("project") CProject<?> project);
}
