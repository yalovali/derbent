package tech.derbent.plm.storage.transaction.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;
import tech.derbent.plm.storage.transaction.domain.CTransactionType;

public interface IStorageTransactionRepository extends IEntityOfCompanyRepository<CStorageTransaction> {

    @Query("""
            SELECT t FROM CStorageTransaction t
            LEFT JOIN FETCH t.company
            LEFT JOIN FETCH t.user
            LEFT JOIN FETCH t.storageItem si
            LEFT JOIN FETCH si.entityType iet
            LEFT JOIN FETCH iet.workflow
            LEFT JOIN FETCH si.storage s
            LEFT JOIN FETCH s.entityType
            WHERE t.storageItem = :storageItem
            ORDER BY t.transactionDate DESC
            """)
    List<CStorageTransaction> findByStorageItem(@Param("storageItem") CStorageItem storageItem);

    @Query("""
            SELECT t FROM CStorageTransaction t
            LEFT JOIN FETCH t.company
            LEFT JOIN FETCH t.user
            LEFT JOIN FETCH t.storageItem si
            LEFT JOIN FETCH si.entityType iet
            LEFT JOIN FETCH iet.workflow
            LEFT JOIN FETCH si.storage s
            LEFT JOIN FETCH s.entityType
            WHERE t.transactionType = :transactionType AND t.company = :company
            ORDER BY t.transactionDate DESC
            """)
    List<CStorageTransaction> findByTransactionType(@Param("transactionType") CTransactionType transactionType, @Param("company") CCompany company);

    @Query("""
            SELECT t FROM CStorageTransaction t
            LEFT JOIN FETCH t.company
            LEFT JOIN FETCH t.user
            LEFT JOIN FETCH t.storageItem si
            LEFT JOIN FETCH si.entityType iet
            LEFT JOIN FETCH iet.workflow
            LEFT JOIN FETCH si.storage s
            LEFT JOIN FETCH s.entityType
            WHERE t.transactionDate BETWEEN :startDate AND :endDate AND t.company = :company
            ORDER BY t.transactionDate DESC
            """)
    List<CStorageTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate,
            @Param("company") CCompany company);

    @Query("""
            SELECT t FROM CStorageTransaction t
            LEFT JOIN FETCH t.company
            LEFT JOIN FETCH t.user
            LEFT JOIN FETCH t.storageItem si
            LEFT JOIN FETCH si.entityType iet
            LEFT JOIN FETCH iet.workflow
            LEFT JOIN FETCH si.storage s
            LEFT JOIN FETCH s.entityType
            WHERE t.company = :company
            ORDER BY t.transactionDate DESC
            """)
    List<CStorageTransaction> findRecent(@Param("company") CCompany company);
}
