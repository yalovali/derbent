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
			SELECT DISTINCT e FROM #{#entityName} e
		LEFT JOIN FETCH e.project
		LEFT JOIN FETCH e.createdBy
		LEFT JOIN FETCH e.attachments
		LEFT JOIN FETCH e.comments
		LEFT JOIN FETCH e.links
		WHERE e.id = :id
		""")
	Optional<CBabFileInputNode> findById(@Param("id") Long id);
	
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
	List<CBabFileInputNode> listByProjectForPageView(@Param("project") CProject<?> project);

	// File Input specific queries
	
	/**
	 * Find file input node by file path.
	 * Used for unique file path validation per project.
	 */
	@Query("SELECT e FROM #{#entityName} e WHERE e.filePath = :filePath AND e.project = :project")
	Optional<CBabFileInputNode> findByFilePathAndProject(@Param("filePath") String filePath, @Param("project") CProject<?> project);
}
