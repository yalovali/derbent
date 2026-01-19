package tech.derbent.plm.storage.transaction.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;

/**
 * CStorageTransaction - Transaction record for storage item movements.
 * Follows comment pattern - unidirectional relationship from parent (CStorageItem).
 * Tracks all inventory movements with timestamp, user, type, and quantity.
 */
@Entity
@Table(name = "cstoragetransaction")
@AttributeOverride(name = "id", column = @Column(name = "transaction_id"))
public class CStorageTransaction extends CEntityOfCompany<CStorageTransaction> {

    public static final String DEFAULT_COLOR = "#FF8C00"; // X11 DarkOrange - transactions
    public static final String DEFAULT_ICON = "vaadin:exchange";
    public static final String ENTITY_TITLE_PLURAL = "Storage Transactions";
    public static final String ENTITY_TITLE_SINGULAR = "Storage Transaction";
    private static final Logger LOGGER = LoggerFactory.getLogger(CStorageTransaction.class);
    public static final String VIEW_NAME = "Storage Transaction View";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storage_item_id", nullable = false)
    @AMetaData(
            displayName = "Storage Item",
            required = true,
            readOnly = true,
            description = "Item involved in this transaction",
            hidden = false,
            dataProviderBean = "CStorageItemService")
    private CStorageItem storageItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    @AMetaData(
            displayName = "Transaction Type",
            required = true,
            readOnly = true,
            description = "Type of inventory movement",
            hidden = false)
    private CTransactionType transactionType;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Quantity",
            required = true,
            readOnly = true,
            description = "Quantity moved (always positive)",
            hidden = false)
    private BigDecimal quantity;

    @Column(name = "quantity_before", nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Quantity Before",
            required = false,
            readOnly = true,
            description = "Stock level before transaction",
            hidden = false)
    private BigDecimal quantityBefore;

    @Column(name = "quantity_after", nullable = true, precision = 15, scale = 2)
    @AMetaData(
            displayName = "Quantity After",
            required = false,
            readOnly = true,
            description = "Stock level after transaction",
            hidden = false)
    private BigDecimal quantityAfter;

    @Column(name = "transaction_date", nullable = false)
    @AMetaData(
            displayName = "Transaction Date",
            required = true,
            readOnly = true,
            description = "Date and time of transaction",
            hidden = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    @AMetaData(
            displayName = "User",
            required = false,
            readOnly = true,
            description = "User who performed the transaction",
            hidden = false,
            dataProviderBean = "CUserService")
    private CUser user;

    @Column(name = "description", nullable = true, length = 1000)
    @Size(max = 1000)
    @AMetaData(
            displayName = "Description",
            required = false,
            readOnly = false,
            description = "Transaction description or notes",
            hidden = false,
            maxLength = 1000)
    private String description;

    @Column(name = "reference", nullable = true, length = 255)
    @Size(max = 255)
    @AMetaData(
            displayName = "Reference",
            required = false,
            readOnly = false,
            description = "External reference (order number, ticket, etc.)",
            hidden = false,
            maxLength = 255)
    private String reference;

    /** Default constructor for JPA. */
    public CStorageTransaction() {
        super();
    }

    public CStorageTransaction(
            final CStorageItem storageItem,
            final CTransactionType transactionType,
            final BigDecimal quantity,
            final CUser user,
            final String description) {
        super(CStorageTransaction.class, "transaction", storageItem != null ? storageItem.getCompany() : null);
        this.storageItem = storageItem;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.user = user;
        this.description = description;
        this.transactionDate = LocalDateTime.now();

        if (storageItem != null) {
            this.quantityBefore = storageItem.getCurrentQuantity();
        }
    }

    // Getters and Setters
    public CStorageItem getStorageItem() {
        return storageItem;
    }

    public void setStorageItem(final CStorageItem storageItem) {
        this.storageItem = storageItem;
    }

    public CTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(final CTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(final BigDecimal quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public BigDecimal getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(final BigDecimal quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(final LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public CUser getUser() {
        return user;
    }

    public void setUser(final CUser user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public String getUserName() {
        if (user == null) {
            return "System";
        }
        try {
            return user.getName();
        } catch (final org.hibernate.LazyInitializationException e) {
            LOGGER.debug("LazyInitializationException accessing user name", e);
            return "User#" + (user.getId() != null ? user.getId() : "unknown");
        }
    }

    @Override
    public String toString() {
        return String.format("CStorageTransaction{id=%d, type=%s, quantity=%s, date=%s, item=%s}",
                getId(), transactionType, quantity, transactionDate,
                storageItem != null ? storageItem.getName() : "none");
    }
}
