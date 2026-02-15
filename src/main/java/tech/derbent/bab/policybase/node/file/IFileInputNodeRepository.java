package tech.derbent.bab.policybase.node.file;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/**
 * IFileInputNodeRepository - Repository interface for File Input nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Provides data access methods for File Input virtual network nodes.
 * Includes eager loading patterns for UI display and polymorphic support.
 */
@Profile("bab")
public interface IFileInputNodeRepository extends INodeEntityRepository<CBabFileInputNode> {
	
	@Override
	@Query("""
			SELECT DISTINCT n FROM CBabFileInputNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
		WHERE n.id = :id
		""")
	Optional<CBabFileInputNode> findById(@Param("id") Long id);
	
	@Override
	@Query("""
		SELECT DISTINCT n FROM CBabFileInputNode n
		LEFT JOIN FETCH n.project
		LEFT JOIN FETCH n.createdBy
		LEFT JOIN FETCH n.attachments
		LEFT JOIN FETCH n.comments
		LEFT JOIN FETCH n.links
			WHERE n.project = :project
			ORDER BY n.name ASC
			""")
	List<CBabFileInputNode> listByProjectForPageView(@Param("project") CProject<?> project);

	// File Input specific queries
	
	/**
	 * Find file input node by file path.
	 * Used for unique file path validation per project.
	 */
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.filePath = :filePath AND n.project = :project")
	Optional<CBabFileInputNode> findByFilePathAndProject(@Param("filePath") String filePath, @Param("project") CProject<?> project);
}
