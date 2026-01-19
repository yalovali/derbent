package tech.derbent.plm.storage.storageitem.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;
import tech.derbent.plm.storage.transaction.domain.CStorageTransaction;
import tech.derbent.plm.storage.transaction.domain.CTransactionType;
import tech.derbent.plm.storage.transaction.service.CStorageTransactionService;

@Service
@PreAuthorize("isAuthenticated()")
@Menu(icon = "vaadin:package", title = "PLM.StorageItems")
@PermitAll
public class CStorageItemService extends CProjectItemService<CStorageItem>
        implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageItemService.class);
    private final CStorageItemTypeService storageItemTypeService;
    private final CStorageTransactionService transactionService;

    CStorageItemService(
            final IStorageItemRepository repository,
            final Clock clock,
            final ISessionService sessionService,
            final CStorageItemTypeService storageItemTypeService,
            final CProjectItemStatusService projectItemStatusService,
            final CStorageTransactionService transactionService) {
        super(repository, clock, sessionService, projectItemStatusService);
        this.storageItemTypeService = storageItemTypeService;
        this.transactionService = transactionService;
    }

    @Override
    public String checkDeleteAllowed(final CStorageItem entity) {
        final String superCheck = super.checkDeleteAllowed(entity);
        if (superCheck != null) {
            return superCheck;
        }

        if (entity.getCurrentQuantity() != null && entity.getCurrentQuantity().compareTo(BigDecimal.ZERO) > 0) {
            return "Cannot delete item with current stock. Please reduce quantity to zero first.";
        }

        return null;
    }

    @Override
    public Class<CStorageItem> getEntityClass() {
        return CStorageItem.class;
    }

    @Override
    public Class<?> getInitializerServiceClass() {
        return CStorageItemInitializerService.class;
    }

    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceStorageItem.class;
    }

    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }

    @SuppressWarnings("null")
    @Override
    public void initializeNewEntity(final CStorageItem entity) {
        super.initializeNewEntity(entity);
        LOGGER.debug("Initializing new storage item entity");
        final CProject<?> currentProject = sessionService.getActiveProject()
                .orElseThrow(
                        () -> new CInitializationException("No active project in session - cannot initialize storage item"));
        IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, storageItemTypeService,
                projectItemStatusService);
        LOGGER.debug("Storage item initialization complete");
    }

    @Override
    @Transactional
    protected void validateEntity(final CStorageItem entity) throws CValidationException {
        super.validateEntity(entity);

        if (entity.getStorage() == null) {
            throw new CValidationException("Storage location is required");
        }

        if (entity.getSku() != null && !entity.getSku().isBlank()) {
            final Optional<CStorageItem> existing = ((IStorageItemRepository) repository)
                    .findBySkuAndProject(entity.getSku(), entity.getProject());
            if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
                throw new CValidationException("An item with this SKU already exists in the project");
            }
        }

        if (entity.getBarcode() != null && !entity.getBarcode().isBlank()) {
            final Optional<CStorageItem> existing = ((IStorageItemRepository) repository)
                    .findByBarcodeAndProject(entity.getBarcode(), entity.getProject());
            if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
                throw new CValidationException("An item with this barcode already exists in the project");
            }
        }

        if (entity.getCurrentQuantity() == null) {
            entity.setCurrentQuantity(BigDecimal.ZERO);
        }

        if (entity.getCurrentQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new CValidationException("Current quantity cannot be negative");
        }

        if (entity.getMinimumStockLevel() != null && entity.getMinimumStockLevel().compareTo(BigDecimal.ZERO) < 0) {
            throw new CValidationException("Minimum stock level cannot be negative");
        }

        if (entity.getReorderQuantity() != null && entity.getReorderQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CValidationException("Reorder quantity must be positive");
        }

        if (entity.getMaximumStockLevel() != null) {
            if (entity.getMaximumStockLevel().compareTo(BigDecimal.ZERO) < 0) {
                throw new CValidationException("Maximum stock level cannot be negative");
            }
            if (entity.getMinimumStockLevel() != null
                    && entity.getMaximumStockLevel().compareTo(entity.getMinimumStockLevel()) < 0) {
                throw new CValidationException("Maximum stock level cannot be less than minimum stock level");
            }
        }

        if (entity.getUnitCost() != null && entity.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new CValidationException("Unit cost cannot be negative");
        }

        if (entity.getLeadTimeDays() != null && entity.getLeadTimeDays().compareTo(BigDecimal.ZERO) < 0) {
            throw new CValidationException("Lead time cannot be negative");
        }
    }

    /**
     * Add stock to an item (receive, restock).
     */
    @Transactional
    public void addStock(final CStorageItem item, final BigDecimal quantity, final String description) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        final BigDecimal newQuantity = item.getCurrentQuantity().add(quantity);
        item.setCurrentQuantity(newQuantity);
        item.setLastRestockedDate(LocalDate.now());

        save(item);

        final CStorageTransaction transaction = transactionService.createTransaction(
                item,
                CTransactionType.STOCK_IN,
                quantity,
                description != null ? description : "Stock added");
        transactionService.save(transaction);

        LOGGER.info("Added {} {} to item {} (SKU: {}). New quantity: {}",
                quantity, item.getUnitOfMeasure(), item.getName(), item.getSku(), newQuantity);
    }

    /**
     * Remove stock from an item (issue, consume).
     */
    @Transactional
    public void removeStock(final CStorageItem item, final BigDecimal quantity, final String description)
            throws CValidationException {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (item.getCurrentQuantity().compareTo(quantity) < 0) {
            throw new CValidationException(
                    String.format("Insufficient stock. Available: %s, Requested: %s",
                            item.getCurrentQuantity(), quantity));
        }

        final BigDecimal newQuantity = item.getCurrentQuantity().subtract(quantity);
        item.setCurrentQuantity(newQuantity);

        save(item);

        final CStorageTransaction transaction = transactionService.createTransaction(
                item,
                CTransactionType.STOCK_OUT,
                quantity,
                description != null ? description : "Stock removed");
        transactionService.save(transaction);

        LOGGER.info("Removed {} {} from item {} (SKU: {}). New quantity: {}",
                quantity, item.getUnitOfMeasure(), item.getName(), item.getSku(), newQuantity);
    }

    /**
     * Adjust stock to a specific quantity (inventory correction).
     */
    @Transactional
    public void adjustStock(final CStorageItem item, final BigDecimal newQuantity, final String reason)
            throws CValidationException {
        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("New quantity cannot be negative");
        }

        final BigDecimal oldQuantity = item.getCurrentQuantity();
        final BigDecimal difference = newQuantity.subtract(oldQuantity);

        item.setCurrentQuantity(newQuantity);
        save(item);

        final String description = String.format("Stock adjusted from %s to %s. Reason: %s",
                oldQuantity, newQuantity, reason != null ? reason : "Manual adjustment");

        final CStorageTransaction transaction = transactionService.createTransaction(
                item,
                CTransactionType.ADJUSTMENT,
                difference.abs(),
                description);
        transactionService.save(transaction);

        LOGGER.info("Adjusted stock for item {} (SKU: {}) from {} to {}",
                item.getName(), item.getSku(), oldQuantity, newQuantity);
    }

    /**
     * Transfer stock between storage locations.
     */
    @Transactional
    public void transferStock(final CStorageItem sourceItem, final CStorageItem targetItem,
            final BigDecimal quantity, final String description) throws CValidationException {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (sourceItem.getCurrentQuantity().compareTo(quantity) < 0) {
            throw new CValidationException("Insufficient stock in source location");
        }

        removeStock(sourceItem, quantity, "Transfer OUT: " + (description != null ? description : "Stock transfer"));
        addStock(targetItem, quantity, "Transfer IN: " + (description != null ? description : "Stock transfer"));

        LOGGER.info("Transferred {} {} from {} to {}",
                quantity, sourceItem.getUnitOfMeasure(), sourceItem.getStorage().getName(),
                targetItem.getStorage().getName());
    }

    /**
     * Get low stock items for a project.
     */
    @Transactional(readOnly = true)
    public List<CStorageItem> getLowStockItems(final CProject project) {
        return ((IStorageItemRepository) repository).findLowStockItems(project);
    }

    /**
     * Get expired items for a project.
     */
    @Transactional(readOnly = true)
    public List<CStorageItem> getExpiredItems(final CProject project) {
        return ((IStorageItemRepository) repository).findExpiredItems(project);
    }

    /**
     * Get items expiring soon (within specified days).
     */
    @Transactional(readOnly = true)
    public List<CStorageItem> getItemsExpiringSoon(final CProject project, final int days) {
        return ((IStorageItemRepository) repository).findItemsExpiringSoon(project, days);
    }
}
