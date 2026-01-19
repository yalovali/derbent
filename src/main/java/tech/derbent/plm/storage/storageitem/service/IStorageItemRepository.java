package tech.derbent.plm.storage.storageitem.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfProject.service.IProjectItemRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.storage.storage.domain.CStorage;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;

@Repository
public interface IStorageItemRepository extends IProjectItemRepository<CStorageItem> {

    /**
     * Count storage items by type.
     */
    long countByType(CStorageItemType type);

    /**
     * Find storage items by storage location.
     */
    @Query("""
            SELECT si FROM CStorageItem si
            LEFT JOIN FETCH si.entityType
            LEFT JOIN FETCH si.storage
            LEFT JOIN FETCH si.supplier
            LEFT JOIN FETCH si.responsibleUser
            WHERE si.storage = :storage
            ORDER BY si.name
            """)
    List<CStorageItem> findByStorage(@Param("storage") CStorage storage);

    /**
     * Find storage items by SKU.
     */
    Optional<CStorageItem> findBySkuAndProject(String sku, CProject project);

    /**
     * Find storage items by barcode.
     */
    Optional<CStorageItem> findByBarcodeAndProject(String barcode, CProject project);

    /**
     * Find low stock items (current quantity <= minimum level).
     */
    @Query("""
            SELECT si FROM CStorageItem si
            LEFT JOIN FETCH si.entityType
            LEFT JOIN FETCH si.storage
            WHERE si.project = :project
            AND si.isActive = true
            AND si.minimumStockLevel IS NOT NULL
            AND si.currentQuantity <= si.minimumStockLevel
            ORDER BY si.currentQuantity ASC
            """)
    List<CStorageItem> findLowStockItems(@Param("project") CProject project);

    /**
     * Find expired items.
     */
    @Query("""
            SELECT si FROM CStorageItem si
            LEFT JOIN FETCH si.entityType
            LEFT JOIN FETCH si.storage
            WHERE si.project = :project
            AND si.isActive = true
            AND si.trackExpiration = true
            AND si.expirationDate < CURRENT_DATE
            ORDER BY si.expirationDate ASC
            """)
    List<CStorageItem> findExpiredItems(@Param("project") CProject project);

    /**
     * Find items expiring soon (within days).
     */
    @Query("""
            SELECT si FROM CStorageItem si
            LEFT JOIN FETCH si.entityType
            LEFT JOIN FETCH si.storage
            WHERE si.project = :project
            AND si.isActive = true
            AND si.trackExpiration = true
            AND si.expirationDate >= CURRENT_DATE
            AND si.expirationDate <= CURRENT_DATE + :days
            ORDER BY si.expirationDate ASC
            """)
    List<CStorageItem> findItemsExpiringSoon(@Param("project") CProject project, @Param("days") int days);

    /**
     * Find all storage items for a project with eager loading.
     */
    @Query("""
            SELECT si FROM CStorageItem si
            LEFT JOIN FETCH si.entityType
            LEFT JOIN FETCH si.status
            LEFT JOIN FETCH si.storage
            LEFT JOIN FETCH si.supplier
            LEFT JOIN FETCH si.responsibleUser
            LEFT JOIN FETCH si.attachments
            LEFT JOIN FETCH si.comments
            WHERE si.project = :project
            ORDER BY si.name
            """)
    List<CStorageItem> listByProjectForPageView(@Param("project") CProject project);
}
