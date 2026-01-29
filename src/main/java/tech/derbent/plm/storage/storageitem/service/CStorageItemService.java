package tech.derbent.plm.storage.storageitem.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.transaction.domain.CTransactionType;
import tech.derbent.plm.storage.transaction.service.CStorageTransactionService;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CStorageItemService extends CProjectItemService<CStorageItem> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CStorageItemService.class);
	private final CStorageTransactionService transactionService;
	private final CStorageItemTypeService typeService;

	public CStorageItemService(final IStorageItemRepository repository, final Clock clock, final ISessionService sessionService,
			final CStorageItemTypeService storageItemTypeService, final CStorageTransactionService transactionService,
			final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = storageItemTypeService;
		this.transactionService = transactionService;
	}

	@Transactional
	public void addStock(final CStorageItem item, final BigDecimal quantity, final String description) {
		Check.notNull(item, "Item cannot be null");
		Check.notNull(quantity, "Quantity cannot be null");
		Check.isTrue(quantity.signum() > 0, "Quantity must be positive");
		final BigDecimal before = item.getCurrentQuantity();
		final BigDecimal after = before.add(quantity);
		item.setCurrentQuantity(after);
		item.setLastRestockedDate(LocalDate.now(clock));
		save(item);
		transactionService.createTransaction(item, CTransactionType.STOCK_IN, quantity, before, after, description, null);
	}

	@Transactional
	public void adjustStock(final CStorageItem item, final BigDecimal newQuantity, final String reason) {
		Check.notNull(item, "Item cannot be null");
		Check.notNull(newQuantity, "New quantity cannot be null");
		final BigDecimal before = item.getCurrentQuantity();
		item.setCurrentQuantity(newQuantity);
		save(item);
		transactionService.createTransaction(item, CTransactionType.ADJUSTMENT, newQuantity.subtract(before), before, newQuantity, reason, null);
	}

	@Override
	public String checkDeleteAllowed(final CStorageItem entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CStorageItem> getEntityClass() { return CStorageItem.class; }

	public java.util.List<CStorageItem> getExpiredItems(final CProject<?> project) {
		final LocalDate today = LocalDate.now(clock);
		return ((IStorageItemRepository) repository).listByProjectForPageView(project).stream()
				.filter(i -> Boolean.TRUE.equals(i.getTrackExpiration()) && i.getExpirationDate() != null && i.getExpirationDate().isBefore(today))
				.toList();
	}

	@Override
	public Class<?> getInitializerServiceClass() { return CStorageItemInitializerService.class; }

	public java.util.List<CStorageItem> getItemsExpiringSoon(final CProject<?> project, final int days) {
		final LocalDate threshold = LocalDate.now(clock).plusDays(days);
		return ((IStorageItemRepository) repository).listByProjectForPageView(project).stream()
				.filter(i -> Boolean.TRUE.equals(i.getTrackExpiration()) && i.getExpirationDate() != null && !i.getExpirationDate().isAfter(threshold)
						&& !i.getExpirationDate().isBefore(LocalDate.now(clock)))
				.toList();
	}

	public java.util.List<CStorageItem> getLowStockItems(final CProject<?> project) {
		return ((IStorageItemRepository) repository).listByProjectForPageView(project).stream().filter(CStorageItem::isLowStock).toList();
	}

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceStorageItem.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	@Transactional
	public void removeStock(final CStorageItem item, final BigDecimal quantity, final CTransactionType type, final String description) {
		Check.notNull(item, "Item cannot be null");
		Check.notNull(quantity, "Quantity cannot be null");
		Check.isTrue(quantity.signum() > 0, "Quantity must be positive");
		final BigDecimal before = item.getCurrentQuantity();
		if (before.compareTo(quantity) < 0) {
			throw new CValidationException("Insufficient stock for removal.");
		}
		final BigDecimal after = before.subtract(quantity);
		item.setCurrentQuantity(after);
		save(item);
		transactionService.createTransaction(item, type, quantity, before, after, description, null);
	}

	@Transactional
	public void transferStock(final CStorageItem sourceItem, final CStorageItem targetItem, final BigDecimal quantity, final String description) {
		Check.notNull(sourceItem, "Source item cannot be null");
		Check.notNull(targetItem, "Target item cannot be null");
		Check.notNull(quantity, "Quantity cannot be null");
		Check.isTrue(quantity.signum() > 0, "Quantity must be positive");
		if (sourceItem.getStorage() == null || targetItem.getStorage() == null) {
			throw new CValidationException("Both items must belong to a storage location.");
		}
		if (sourceItem.getSku() != null && targetItem.getSku() != null && !sourceItem.getSku().equals(targetItem.getSku())) {
			throw new CValidationException("Transfer requires matching SKU between source and target.");
		}
		removeStock(sourceItem, quantity, CTransactionType.TRANSFER, description);
		addStock(targetItem, quantity, description);
	}

	/**
	 * Service-level method to copy CStorageItem-specific fields.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CStorageItem source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CStorageItem)) {
			return;
		}
		final CStorageItem targetItem = (CStorageItem) target;
		
		// Copy string fields
		targetItem.setBarcode(source.getBarcode());
		targetItem.setBatchNumber(source.getBatchNumber());
		targetItem.setCurrency(source.getCurrency());
		targetItem.setHandlingInstructions(source.getHandlingInstructions());
		targetItem.setManufacturer(source.getManufacturer());
		targetItem.setModelNumber(source.getModelNumber());
		targetItem.setUnitOfMeasure(source.getUnitOfMeasure());
		
		// Make SKU unique
		if (source.getSku() != null) {
			targetItem.setSku(source.getSku() + "-copy");
		}
		
		// Copy numeric fields
		targetItem.setCurrentQuantity(source.getCurrentQuantity());
		targetItem.setLeadTimeDays(source.getLeadTimeDays());
		targetItem.setMaximumStockLevel(source.getMaximumStockLevel());
		targetItem.setMinimumStockLevel(source.getMinimumStockLevel());
		targetItem.setReorderQuantity(source.getReorderQuantity());
		targetItem.setUnitCost(source.getUnitCost());
		
		// Copy boolean flags
		targetItem.setIsConsumable(source.getIsConsumable());
		targetItem.setRequiresSpecialHandling(source.getRequiresSpecialHandling());
		targetItem.setTrackExpiration(source.getTrackExpiration());
		
		// Copy type
		targetItem.setEntityType(source.getEntityType());
		
		// Handle dates conditionally
		if (!options.isResetDates()) {
			targetItem.setExpirationDate(source.getExpirationDate());
			targetItem.setLastRestockedDate(source.getLastRestockedDate());
		}
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetItem.setStorage(source.getStorage());
			targetItem.setProvider(source.getProvider());
			targetItem.setResponsibleUser(source.getResponsibleUser());
		}
		
		LOGGER.debug("Copied CStorageItem '{}' with options: {}", source.getName(), options);
	}

	@Override
	protected void validateEntity(final CStorageItem entity) throws CValidationException {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getStorage(), "Storage is required");
		Check.notNull(entity.getCurrentQuantity(), "Current Quantity is required");
		
		// 2. String Length Checks - USE STATIC HELPER
		validateStringLength(entity.getSku(), "SKU", 100);
		validateStringLength(entity.getBarcode(), "Barcode", 100);
		validateStringLength(entity.getManufacturer(), "Manufacturer", 255);
		validateStringLength(entity.getModelNumber(), "Model Number", 255);
		validateStringLength(entity.getUnitOfMeasure(), "Unit of Measure", 50);
		validateStringLength(entity.getCurrency(), "Currency", 10);
		validateStringLength(entity.getBatchNumber(), "Batch Number", 100);
		validateStringLength(entity.getHandlingInstructions(), "Handling Instructions", 500);
		
		// 3. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IStorageItemRepository) repository, entity, entity.getName().trim(), entity.getProject());
		
		// 4. SKU/Barcode Uniqueness Check (domain-specific)
		final var duplicates = ((IStorageItemRepository) repository).findDuplicates(entity.getProject(), entity.getSku(), entity.getBarcode());
		final boolean conflict = duplicates.stream().anyMatch(it -> !it.getId().equals(entity.getId()));
		if (conflict) {
			throw new IllegalArgumentException("Duplicate SKU or barcode within the same project.");
		}
	}
}
