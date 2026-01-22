package tech.derbent.plm.storage.storageitem.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.transaction.domain.CTransactionType;
import tech.derbent.plm.storage.transaction.service.CStorageTransactionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:archive", title = "Storage.StorageItems")
@PermitAll
public class CStorageItemService extends CProjectItemService<CStorageItem> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CStorageItemService.class);
	private final CStorageItemTypeService storageItemTypeService;
	private final CStorageTransactionService transactionService;

	public CStorageItemService(final IStorageItemRepository repository, final Clock clock, final ISessionService sessionService,
			final CStorageItemTypeService storageItemTypeService, final CStorageTransactionService transactionService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.storageItemTypeService = storageItemTypeService;
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

	@SuppressWarnings ("null")
	@Override
	public void initializeNewEntity(final CStorageItem entity) {
		super.initializeNewEntity(entity);
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize storage item"));
		entity.initializeDefaults_IHasStatusAndWorkflow(currentProject, storageItemTypeService, projectItemStatusService);
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

	@Override
	protected void validateEntity(final CStorageItem entity) throws CValidationException {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getStorage(), "Storage is required");
		Check.notNull(entity.getCurrentQuantity(), "Current Quantity is required");
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(
					ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getSku() != null && entity.getSku().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("SKU cannot exceed %d characters", 100));
		}
		if (entity.getBarcode() != null && entity.getBarcode().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Barcode cannot exceed %d characters", 100));
		}
		if (entity.getManufacturer() != null && entity.getManufacturer().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Manufacturer cannot exceed %d characters", 255));
		}
		if (entity.getModelNumber() != null && entity.getModelNumber().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Model Number cannot exceed %d characters", 255));
		}
		if (entity.getUnitOfMeasure() != null && entity.getUnitOfMeasure().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Unit of Measure cannot exceed %d characters", 50));
		}
		if (entity.getCurrency() != null && entity.getCurrency().length() > 10) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Currency cannot exceed %d characters", 10));
		}
		if (entity.getBatchNumber() != null && entity.getBatchNumber().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Batch Number cannot exceed %d characters", 100));
		}
		if (entity.getHandlingInstructions() != null && entity.getHandlingInstructions().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Handling Instructions cannot exceed %d characters", 500));
		}
		// 3. Unique Checks
		final Optional<CStorageItem> existingName = ((IStorageItemRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		// SKU/Barcode uniqueness check
		final var duplicates = ((IStorageItemRepository) repository).findDuplicates(entity.getProject(), entity.getSku(), entity.getBarcode());
		final boolean conflict = duplicates.stream().anyMatch(it -> !it.getId().equals(entity.getId()));
		if (conflict) {
			throw new IllegalArgumentException("Duplicate SKU or barcode within the same project.");
		}
		// 4. Numeric Checks
		// Note: currentQuantity can be negative in some inventory systems (backorders), but typically 0 min
		// Assuming non-negative based on domain logic unless specific requirement
		// Keeping flexible for now but alerting if null (checked above)
	}
}
