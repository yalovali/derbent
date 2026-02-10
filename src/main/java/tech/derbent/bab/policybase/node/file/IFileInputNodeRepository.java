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
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabFileInputNode n WHERE n.project = :project AND n.isActive = true")
	long countActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT COUNT(n) FROM CBabFileInputNode n WHERE n.project = :project AND n.connectionStatus = :connectionStatus")
	long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM CBabFileInputNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.project = :project AND n.isActive = true ORDER BY n.name ASC")
	List<CBabFileInputNode> findActiveByProject(@Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.connectionStatus = :connectionStatus ORDER BY n.name ASC")
	List<CBabFileInputNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
	
	@Override
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.project = :project ORDER BY n.name ASC")
	List<CBabFileInputNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
	
	@Override
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.physicalInterface = :physicalInterface ORDER BY n.name ASC")
	List<CBabFileInputNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
	
	@Override
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.physicalInterface = :physicalInterface AND n.project = :project")
	Optional<CBabFileInputNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
	
	// File Input specific queries
	
	/**
	 * Find file input node by file path.
	 * Used for unique file path validation per project.
	 */
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.filePath = :filePath AND n.project = :project")
	Optional<CBabFileInputNode> findByFilePathAndProject(@Param("filePath") String filePath, @Param("project") CProject<?> project);
	
	/**
	 * Find all file input nodes by file format.
	 */
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.fileFormat = :fileFormat ORDER BY n.name ASC")
	List<CBabFileInputNode> findByFileFormat(@Param("fileFormat") String fileFormat);
	
	/**
	 * Find all directory watchers (watchDirectory = true).
	 */
	@Query("SELECT n FROM CBabFileInputNode n WHERE n.watchDirectory = true ORDER BY n.name ASC")
	List<CBabFileInputNode> findDirectoryWatchers();
	
	/**
	 * Count file input nodes by format in project.
	 */
	@Query("SELECT COUNT(n) FROM CBabFileInputNode n WHERE n.fileFormat = :fileFormat AND n.project = :project")
	long countByFileFormatAndProject(@Param("fileFormat") String fileFormat, @Param("project") CProject<?> project);
}
