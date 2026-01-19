package tech.derbent.plm.storage.transaction.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;
import tech.derbent.plm.storage.transaction.domain.CTransactionType;

@Repository
public interface IStorageTransactionRepository extends IEntityOfCompanyRepository<CStorageTransaction> {

    /**
     * Find transactions for a specific storage item.
     */
    @Query("""
            SELECT t FROM CStorageTransaction t
            LEFT JOIN FETCH t.storageItem
            LEFT JOIN FETCH t.user
            WHERE t.storageItem = :item
            ORDER BY t.transactionDate DESC
            """)
    List<CStorageTransaction> findByStorageItem(@Param("item") CStorageItem item);

    /**
     * Find transactions by type for a company.
     */
    @Query("""
            SELECT t FROM CStorageTransaction t
            LEFT JOIN FETCH t.storageItem
            LEFT JOIN FETCH t.user
            WHERE t.company = :company
            AND t.transactionType = :type
            ORDER BY t.transactionDate DESC
            """)
    List<CStorageTransaction> findByTypeAndCompany(
            @Param("type") CTransactionType type,
            @Param("company") CCompany company);

    /**
     * Find transactions within date range for a company.
     */
    @Query("""
            SELECT t FROM CStorageTransaction t
            LEFT JOIN FETCH t.storageItem
            LEFT JOIN FETCH t.user
            WHERE t.company = :company
            AND t.transactionDate >= :startDate
            AND t.transactionDate <= :endDate
            ORDER BY t.transactionDate DESC
            """)
    List<CStorageTransaction> findByDateRange(
            @Param("company") CCompany company,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent transactions for a company.
     */
    @Query(value = """
            SELECT t FROM CStorageTransaction t
            LEFT JOIN FETCH t.storageItem
            LEFT JOIN FETCH t.user
            WHERE t.company = :company
            ORDER BY t.transactionDate DESC
            """)
    List<CStorageTransaction> findRecentTransactions(@Param("company") CCompany company);
}
