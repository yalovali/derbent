package tech.derbent.plm.storage.transaction.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;
import tech.derbent.plm.storage.transaction.domain.CTransactionType;

@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CStorageTransactionService extends CEntityOfCompanyService<CStorageTransaction> implements IEntityRegistrable, IEntityWithView {

    public CStorageTransactionService(final IStorageTransactionRepository repository, final Clock clock, final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }

    @Override
    public Class<CStorageTransaction> getEntityClass() { return CStorageTransaction.class; }

    @Override
    public Class<?> getInitializerServiceClass() { return CStorageTransactionInitializerService.class; }

    @Override
    public Class<?> getPageServiceClass() { return CPageServiceStorageTransaction.class; }

    @Override
    public Class<?> getServiceClass() { return this.getClass(); }

    @Transactional
    public CStorageTransaction createTransaction(final CStorageItem item, final CTransactionType type, final java.math.BigDecimal quantity,
            final java.math.BigDecimal before, final java.math.BigDecimal after, final String description, final String reference) {
        Check.notNull(item, "Storage item is required");
        Check.notNull(type, "Transaction type is required");
        Check.notNull(quantity, "Quantity is required");
        Check.isTrue(quantity.signum() != 0, "Quantity cannot be zero");
        Check.notNull(item.getProject(), "Storage item must belong to a project");
        final CCompany company = sessionService.getActiveCompany().orElseGet(() -> item.getProject().getCompany());
        final CStorageTransaction tx = new CStorageTransaction();
        tx.setName(item.getName() + " - " + type.name());
        tx.setCompany(company);
        tx.setStorageItem(item);
        tx.setTransactionType(type);
        tx.setQuantity(quantity);
        tx.setQuantityBefore(before);
        tx.setQuantityAfter(after);
        tx.setTransactionDate(LocalDateTime.now(clock));
        tx.setUser(sessionService.getActiveUser().orElse(null));
        tx.setDescription(description);
        tx.setReference(reference);
        return repository.save(tx);
    }

    public List<CStorageTransaction> getTransactionsForItem(final CStorageItem item) {
        return ((IStorageTransactionRepository) repository).findByStorageItem(item);
    }

    public List<CStorageTransaction> getTransactionsByType(final CTransactionType type) {
        final CCompany company = sessionService.getActiveCompany().orElseThrow();
        return ((IStorageTransactionRepository) repository).findByTransactionType(type, company);
    }

    public List<CStorageTransaction> getTransactionsByDateRange(final LocalDateTime start, final LocalDateTime end) {
        final CCompany company = sessionService.getActiveCompany().orElseThrow();
        return ((IStorageTransactionRepository) repository).findByDateRange(start, end, company);
    }

    public List<CStorageTransaction> getRecentTransactions(final int limit) {
        final CCompany company = sessionService.getActiveCompany().orElseThrow();
        final List<CStorageTransaction> all = ((IStorageTransactionRepository) repository).findRecent(company);
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    @Override
    protected void validateEntity(final CStorageTransaction entity) throws tech.derbent.api.exceptions.CValidationException {
        super.validateEntity(entity);
        Check.notNull(entity.getStorageItem(), "Storage item is required");
        Check.notNull(entity.getTransactionType(), "Transaction type is required");
        Check.notNull(entity.getQuantity(), "Quantity is required");
        Check.isTrue(entity.getQuantity().signum() != 0, "Quantity cannot be zero");
        Check.notNull(entity.getTransactionDate(), "Transaction date is required");
    }

    @Override
    public String checkDeleteAllowed(final CStorageTransaction entity) {
        return "Transactions are immutable and cannot be deleted.";
    }
}
