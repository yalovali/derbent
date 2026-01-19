package tech.derbent.plm.storage.transaction.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;
import tech.derbent.plm.storage.transaction.domain.CTransactionType;

@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CStorageTransactionService extends CEntityOfCompanyService<CStorageTransaction>
        implements IEntityRegistrable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageTransactionService.class);

    public CStorageTransactionService(
            final IStorageTransactionRepository repository,
            final Clock clock,
            final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }

    @Override
    public Class<CStorageTransaction> getEntityClass() {
        return CStorageTransaction.class;
    }

    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }

    @Override
    public String checkDeleteAllowed(final CStorageTransaction entity) {
        return "Transactions cannot be deleted for audit trail integrity";
    }

    /**
     * Create a new transaction.
     */
    @Transactional
    public CStorageTransaction createTransaction(
            final CStorageItem storageItem,
            final CTransactionType transactionType,
            final BigDecimal quantity,
            final String description) {

        final CUser currentUser = sessionService.getActiveUser().orElse(null);

        final CStorageTransaction transaction = new CStorageTransaction(
                storageItem,
                transactionType,
                quantity,
                currentUser,
                description);

        transaction.setQuantityBefore(storageItem.getCurrentQuantity());

        BigDecimal quantityAfter = storageItem.getCurrentQuantity();
        switch (transactionType) {
            case STOCK_IN:
                quantityAfter = quantityAfter.add(quantity);
                break;
            case STOCK_OUT:
            case EXPIRED:
            case DAMAGED:
            case LOST:
                quantityAfter = quantityAfter.subtract(quantity);
                break;
            case ADJUSTMENT:
            case TRANSFER:
            default:
                break;
        }
        transaction.setQuantityAfter(quantityAfter);

        initializeNewEntity(transaction);
        return transaction;
    }

    /**
     * Get transactions for a storage item.
     */
    public List<CStorageTransaction> getTransactionsForItem(final CStorageItem item) {
        return ((IStorageTransactionRepository) repository).findByStorageItem(item);
    }

    /**
     * Get transactions by type for current company.
     */
    public List<CStorageTransaction> getTransactionsByType(final CTransactionType type) {
        final CCompany company = sessionService.getActiveCompany()
                .orElseThrow(() -> new IllegalStateException("No active company in session"));
        return ((IStorageTransactionRepository) repository).findByTypeAndCompany(type, company);
    }

    /**
     * Get transactions within date range for current company.
     */
    public List<CStorageTransaction> getTransactionsByDateRange(
            final LocalDateTime startDate,
            final LocalDateTime endDate) {
        final CCompany company = sessionService.getActiveCompany()
                .orElseThrow(() -> new IllegalStateException("No active company in session"));
        return ((IStorageTransactionRepository) repository).findByDateRange(company, startDate, endDate);
    }

    /**
     * Get recent transactions for current company.
     */
    public List<CStorageTransaction> getRecentTransactions(final int limit) {
        final CCompany company = sessionService.getActiveCompany()
                .orElseThrow(() -> new IllegalStateException("No active company in session"));
        final List<CStorageTransaction> transactions = ((IStorageTransactionRepository) repository)
                .findRecentTransactions(company);
        return transactions.stream().limit(limit).toList();
    }
}
