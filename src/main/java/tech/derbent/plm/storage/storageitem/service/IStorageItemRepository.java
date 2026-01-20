package tech.derbent.plm.storage.storageitem.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.storageitem.domain.CStorageItemType;

public interface IStorageItemRepository extends IEntityOfProjectRepository<CStorageItem> {

    @Query("SELECT COUNT(i) FROM #{#entityName} i WHERE i.entityType = :type")
    long countByType(@Param("type") CStorageItemType type);

    @Query("SELECT COUNT(i) FROM #{#entityName} i WHERE i.storage = :storage")
    long countByStorage(@Param("storage") CStorage storage);

    @Query("""
            SELECT i FROM CStorageItem i
            WHERE i.project = :project AND ((:sku IS NOT NULL AND i.sku = :sku) OR (:barcode IS NOT NULL AND i.barcode = :barcode))
            """)
    List<CStorageItem> findDuplicates(@Param("project") CProject<?> project, @Param("sku") String sku, @Param("barcode") String barcode);

    @Override
    @Query("""
            SELECT i FROM CStorageItem i
            LEFT JOIN FETCH i.project
            LEFT JOIN FETCH i.assignedTo
            LEFT JOIN FETCH i.createdBy
            LEFT JOIN FETCH i.status
            LEFT JOIN FETCH i.entityType et
            LEFT JOIN FETCH et.workflow
            LEFT JOIN FETCH i.storage s
            LEFT JOIN FETCH s.entityType
            LEFT JOIN FETCH i.provider
            LEFT JOIN FETCH i.responsibleUser
            LEFT JOIN FETCH i.attachments
            LEFT JOIN FETCH i.comments
            WHERE i.project = :project
            ORDER BY i.name ASC
            """)
    List<CStorageItem> listByProjectForPageView(@Param("project") CProject<?> project);

    @Override
    @Query("""
            SELECT i FROM CStorageItem i
            LEFT JOIN FETCH i.project
            LEFT JOIN FETCH i.assignedTo
            LEFT JOIN FETCH i.createdBy
            LEFT JOIN FETCH i.status
            LEFT JOIN FETCH i.entityType et
            LEFT JOIN FETCH et.workflow
            LEFT JOIN FETCH i.storage s
            LEFT JOIN FETCH s.entityType
            LEFT JOIN FETCH i.provider
            LEFT JOIN FETCH i.responsibleUser
            LEFT JOIN FETCH i.attachments
            LEFT JOIN FETCH i.comments
            WHERE i.id = :id
            """)
    Optional<CStorageItem> findById(@Param("id") Long id);
}
