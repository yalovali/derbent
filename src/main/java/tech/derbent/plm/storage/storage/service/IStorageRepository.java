package tech.derbent.plm.storage.storage.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;

public interface IStorageRepository extends IEntityOfProjectRepository<CStorage> {

    @Query("SELECT COUNT(s) FROM #{#entityName} s WHERE s.entityType = :type")
    long countByType(@Param("type") CStorageType type);

    @Override
    @Query("""
            SELECT s FROM CStorage s
            LEFT JOIN FETCH s.project
            LEFT JOIN FETCH s.assignedTo
            LEFT JOIN FETCH s.createdBy
            LEFT JOIN FETCH s.status
            LEFT JOIN FETCH s.entityType et
            LEFT JOIN FETCH et.workflow
            LEFT JOIN FETCH s.parentStorage
            LEFT JOIN FETCH s.responsibleUser
            LEFT JOIN FETCH s.attachments
            LEFT JOIN FETCH s.comments
            WHERE s.project = :project
            ORDER BY s.name ASC
            """)
    List<CStorage> listByProjectForPageView(@Param("project") CProject<?> project);

    @Override
    @Query("""
            SELECT s FROM CStorage s
            LEFT JOIN FETCH s.project
            LEFT JOIN FETCH s.assignedTo
            LEFT JOIN FETCH s.createdBy
            LEFT JOIN FETCH s.status
            LEFT JOIN FETCH s.entityType et
            LEFT JOIN FETCH et.workflow
            LEFT JOIN FETCH s.parentStorage
            LEFT JOIN FETCH s.responsibleUser
            LEFT JOIN FETCH s.attachments
            LEFT JOIN FETCH s.comments
            WHERE s.id = :id
            """)
    Optional<CStorage> findById(@Param("id") Long id);
}
