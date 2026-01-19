package tech.derbent.plm.storage.storage.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfProject.service.IProjectItemRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;

@Repository
public interface IStorageRepository extends IProjectItemRepository<CStorage> {

    /**
     * Count storage locations by type.
     */
    long countByType(CStorageType type);

    /**
     * Find storage locations by type.
     */
    @Query("""
            SELECT s FROM CStorage s
            LEFT JOIN FETCH s.entityType
            LEFT JOIN FETCH s.parentStorage
            LEFT JOIN FETCH s.responsibleUser
            WHERE s.entityType = :type
            AND s.project = :project
            ORDER BY s.name
            """)
    List<CStorage> findByTypeAndProject(@Param("type") CStorageType type, @Param("project") CProject project);

    /**
     * Find storage locations by parent.
     */
    @Query("""
            SELECT s FROM CStorage s
            LEFT JOIN FETCH s.entityType
            LEFT JOIN FETCH s.parentStorage
            LEFT JOIN FETCH s.responsibleUser
            WHERE s.parentStorage = :parent
            ORDER BY s.name
            """)
    List<CStorage> findByParent(@Param("parent") CStorage parent);

    /**
     * Find root storage locations (no parent).
     */
    @Query("""
            SELECT s FROM CStorage s
            LEFT JOIN FETCH s.entityType
            LEFT JOIN FETCH s.parentStorage
            LEFT JOIN FETCH s.responsibleUser
            WHERE s.parentStorage IS NULL
            AND s.project = :project
            ORDER BY s.name
            """)
    List<CStorage> findRootLocations(@Param("project") CProject project);

    /**
     * Find storage by name and project (for validation).
     */
    Optional<CStorage> findByNameAndProject(String name, CProject project);

    /**
     * Find all storage locations for a project with eager loading.
     */
    @Query("""
            SELECT s FROM CStorage s
            LEFT JOIN FETCH s.entityType
            LEFT JOIN FETCH s.status
            LEFT JOIN FETCH s.parentStorage
            LEFT JOIN FETCH s.responsibleUser
            LEFT JOIN FETCH s.attachments
            LEFT JOIN FETCH s.comments
            WHERE s.project = :project
            ORDER BY s.name
            """)
    List<CStorage> listByProjectForPageView(@Param("project") CProject project);
}
