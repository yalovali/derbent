package tech.derbent.plm.storage.transaction.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;
import tech.derbent.plm.storage.transaction.domain.CTransactionType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CStorageTransactionService extends CEntityOfCompanyService<CStorageTransaction> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CStorageTransactionService.class);

	public CStorageTransactionService(final IStorageTransactionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CStorageTransaction entity) {
		return "Transactions are immutable and cannot be deleted.";
	}

	/**
	 * Service-level method to copy CStorageTransaction-specific fields.
	 * Note: Transactions are immutable, so copying creates an audit record.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CStorageTransaction source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CStorageTransaction)) {
			return;
		}
		final CStorageTransaction targetTransaction = (CStorageTransaction) target;
		
		// Copy transaction fields (immutable record)
		targetTransaction.setQuantity(source.getQuantity());
		targetTransaction.setQuantityBefore(source.getQuantityBefore());
		targetTransaction.setQuantityAfter(source.getQuantityAfter());
		targetTransaction.setReference(source.getReference());
		targetTransaction.setTransactionType(source.getTransactionType());
		
		// Handle dates conditionally
		if (!options.isResetDates()) {
			targetTransaction.setTransactionDate(source.getTransactionDate());
		}
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetTransaction.setStorageItem(source.getStorageItem());
			targetTransaction.setUser(source.getUser());
		}
		
		LOGGER.debug("Copied CStorageTransaction '{}' with options: {}", source.getName(), options);
	}

	@Transactional
	public CStorageTransaction createTransaction(final CStorageItem item, final CTransactionType type, final java.math.BigDecimal quantity,
			final java.math.BigDecimal before, final java.math.BigDecimal after, final String description, final String reference) {
		Check.notNull(item, "Storage item is required");
		Check.notNull(type, "Transaction type is required");
		Check.notNull(quantity, "Quantity is required");
		Check.isTrue(quantity.signum() != 0, "Quantity cannot be zero");
		Check.notNull(item.getProject(), "Storage item must belong to a project");
		final CCompany company = sessionService.getActiveCompany().orElseGet(() -> item.getProject().getCompany());
		final CStorageTransaction tx = new CStorageTransaction(item.getName() + " - " + type.name(), company);
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

	@Override
	public Class<CStorageTransaction> getEntityClass() { return CStorageTransaction.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CStorageTransactionInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceStorageTransaction.class; }

	public List<CStorageTransaction> getRecentTransactions(final int limit) {
		final CCompany company = sessionService.getActiveCompany().orElseThrow();
		final List<CStorageTransaction> all = ((IStorageTransactionRepository) repository).findRecent(company);
		return all.size() > limit ? all.subList(0, limit) : all;
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	public List<CStorageTransaction> getTransactionsByDateRange(final LocalDateTime start, final LocalDateTime end) {
		final CCompany company = sessionService.getActiveCompany().orElseThrow();
		return ((IStorageTransactionRepository) repository).findByDateRange(start, end, company);
	}

	public List<CStorageTransaction> getTransactionsByType(final CTransactionType type) {
		final CCompany company = sessionService.getActiveCompany().orElseThrow();
		return ((IStorageTransactionRepository) repository).findByTransactionType(type, company);
	}

	public List<CStorageTransaction> getTransactionsForItem(final CStorageItem item) {
		return ((IStorageTransactionRepository) repository).findByStorageItem(item);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CStorageTransaction entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notNull(entity.getStorageItem(), "Storage item is required");
		Check.notNull(entity.getTransactionType(), "Transaction type is required");
		Check.notNull(entity.getQuantity(), "Quantity is required");
		Check.notNull(entity.getTransactionDate(), "Transaction date is required");
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getDescription(), "Description", 1000);
		validateStringLength(entity.getReference(), "Reference", 255);
		
		// 3. Numeric Checks - Use validateNumericField helper for positive validation
		if (entity.getQuantity().signum() == 0) {
			throw new CValidationException("Quantity cannot be zero");
		}
	}
}
