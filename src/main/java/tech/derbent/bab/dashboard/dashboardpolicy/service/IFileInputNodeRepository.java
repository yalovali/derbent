package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CFileInputNode;
import tech.derbent.bab.policybase.node.service.INodeEntityRepository;

/**
 * IFileInputNodeRepository - Repository interface for file input virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries.
 * 
 * Handles file input node persistence with complete eager loading for UI performance.
 * Provides specialized queries for file system monitoring configuration and management.
 */
@Profile("bab")
public interface IFileInputNodeRepository extends INodeEntityRepository<CFileInputNode> {
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.attachments
	LEFT JOIN FETCH e.comments
	LEFT JOIN FETCH e.links
	WHERE e.id = :id
	""")
    Optional<CFileInputNode> findById(@Param("id") Long id);
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	LEFT JOIN FETCH e.attachments
	LEFT JOIN FETCH e.comments
	LEFT JOIN FETCH e.links
	WHERE e.project = :project
	ORDER BY e.id DESC
	""")
    List<CFileInputNode> listByProjectForPageView(@Param("project") CProject<?> project);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.physicalInterface = :physicalInterface AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findByPhysicalInterface(@Param("physicalInterface") String physicalInterface);
    
    @Override
    @Query("""
	SELECT DISTINCT e FROM #{#entityName} e
	LEFT JOIN FETCH e.project
	LEFT JOIN FETCH e.assignedTo
	LEFT JOIN FETCH e.createdBy
	WHERE e.isActive = true AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findActiveByProject(@Param("project") CProject<?> project);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.connectionStatus = :connectionStatus AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findByConnectionStatus(@Param("connectionStatus") String connectionStatus);
    
    @Override
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.nodeType = :nodeType AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findByNodeTypeAndProject(@Param("nodeType") String nodeType, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.physicalInterface = :physicalInterface AND e.project = :project")
    boolean existsByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT e FROM #{#entityName} e WHERE e.physicalInterface = :physicalInterface AND e.project = :project")
    Optional<CFileInputNode> findByPhysicalInterfaceAndProject(@Param("physicalInterface") String physicalInterface, @Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.isActive = true AND e.project = :project")
    long countActiveByProject(@Param("project") CProject<?> project);
    
    @Override
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.connectionStatus = :connectionStatus AND e.project = :project")
    long countByConnectionStatusAndProject(@Param("connectionStatus") String connectionStatus, @Param("project") CProject<?> project);
    
    // File input specific queries
    
    /**
     * Find file input by file path and project.
     * Ensures unique file path monitoring.
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.filePath = :filePath AND e.project = :project")
    Optional<CFileInputNode> findByFilePathAndProject(@Param("filePath") String filePath, @Param("project") CProject<?> project);
    
    /**
     * Check if file path is already monitored.
     * Critical for file path uniqueness validation.
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.filePath = :filePath AND e.project = :project")
    boolean existsByFilePathAndProject(@Param("filePath") String filePath, @Param("project") CProject<?> project);
    
    /**
     * Find file inputs by format.
     * Useful for format-specific processing grouping (JSON, XML, CSV, etc.).
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.fileFormat = :fileFormat AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findByFileFormatAndProject(@Param("fileFormat") String fileFormat, @Param("project") CProject<?> project);
    
    /**
     * Find directory watchers.
     * Useful for directory monitoring overview.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.watchDirectory = true AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findDirectoryWatchers(@Param("project") CProject<?> project);
    
    /**
     * Find file inputs with patterns.
     * Useful for pattern-based file monitoring.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.filePattern IS NOT NULL AND e.filePattern != '' AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findWithFilePattern(@Param("project") CProject<?> project);
    
    /**
     * Find file inputs by polling interval.
     * Useful for performance optimization grouping.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.pollingIntervalSeconds = :intervalSeconds AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findByPollingInterval(@Param("intervalSeconds") Integer intervalSeconds, @Param("project") CProject<?> project);
    
    /**
     * Find file inputs with auto-delete enabled.
     * Useful for cleanup configuration overview.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.autoDeleteProcessed = true AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findWithAutoDelete(@Param("project") CProject<?> project);
    
    /**
     * Find file inputs with backup enabled.
     * Useful for backup configuration management.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.backupProcessedFiles = true AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findWithBackup(@Param("project") CProject<?> project);
    
    /**
     * Find file inputs by backup directory.
     * Useful for backup location management.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.backupDirectory = :backupDirectory AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findByBackupDirectory(@Param("backupDirectory") String backupDirectory, @Param("project") CProject<?> project);
    
    /**
     * Find file inputs by max file size.
     * Useful for size-based processing optimization.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.maxFileSizeMb <= :maxSize AND e.project = :project
	ORDER BY e.maxFileSizeMb ASC
	""")
    List<CFileInputNode> findByMaxFileSizeRange(@Param("maxSize") Integer maxSize, @Param("project") CProject<?> project);
    
    /**
     * Find high-frequency polling file inputs.
     * Useful for performance monitoring (polling interval < threshold).
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.pollingIntervalSeconds < :thresholdSeconds AND e.project = :project
	ORDER BY e.pollingIntervalSeconds ASC
	""")
    List<CFileInputNode> findHighFrequencyPolling(@Param("thresholdSeconds") Integer thresholdSeconds, @Param("project") CProject<?> project);
    
    /**
     * Find file inputs monitoring parent directory.
     * Useful for checking directory conflict with specific file path.
     */
    @Query("""
	SELECT e FROM #{#entityName} e
	WHERE e.watchDirectory = true 
	AND e.filePath != :specificPath
	AND :specificPath LIKE CONCAT(e.filePath, '%')
	AND e.project = :project
	ORDER BY e.name ASC
	""")
    List<CFileInputNode> findDirectoryWatchersContaining(@Param("specificPath") String specificPath, @Param("project") CProject<?> project);
    
    /**
     * Get distinct file formats in project.
     * Useful for format filter dropdown.
     */
    @Query("SELECT DISTINCT e.fileFormat FROM #{#entityName} e WHERE e.fileFormat IS NOT NULL AND e.project = :project ORDER BY e.fileFormat")
    List<String> findDistinctFileFormatsByProject(@Param("project") CProject<?> project);
    
    /**
     * Get distinct backup directories in project.
     * Useful for backup directory management.
     */
    @Query("SELECT DISTINCT e.backupDirectory FROM #{#entityName} e WHERE e.backupDirectory IS NOT NULL AND e.project = :project ORDER BY e.backupDirectory")
    List<String> findDistinctBackupDirectoriesByProject(@Param("project") CProject<?> project);
    
    /**
     * Get distinct file patterns in project.
     * Useful for pattern reuse suggestions.
     */
    @Query("SELECT DISTINCT e.filePattern FROM #{#entityName} e WHERE e.filePattern IS NOT NULL AND e.filePattern != '' AND e.project = :project ORDER BY e.filePattern")
    List<String> findDistinctFilePatternsByProject(@Param("project") CProject<?> project);
}