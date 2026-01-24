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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;

@Entity
@Table (name = "cstoragetransaction")
@AttributeOverride (name = "id", column = @Column (name = "transaction_id"))
public class CStorageTransaction extends CEntityOfCompany<CStorageTransaction> implements IHasIcon {

	public static final String DEFAULT_COLOR = "#444444";
	public static final String DEFAULT_ICON = "vaadin:records";
	public static final String ENTITY_TITLE_PLURAL = "Storage Transactions";
	public static final String ENTITY_TITLE_SINGULAR = "Storage Transaction";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CStorageTransaction.class);
	public static final String VIEW_NAME = "Storage Transaction View";
	@Column (name = "color", length = 20)
	@AMetaData (displayName = "Color", required = false, readOnly = false, description = "Display color", hidden = true, maxLength = 20)
	private String color = DEFAULT_COLOR;
	@Column (name = "quantity", nullable = false, precision = 19, scale = 2)
	@NotNull
	@AMetaData (displayName = "Quantity", required = true, description = "Quantity moved")
	private BigDecimal quantity = BigDecimal.ZERO;
	@Column (name = "quantity_after", precision = 19, scale = 2)
	@AMetaData (displayName = "Quantity After", required = false, description = "Quantity after transaction")
	private BigDecimal quantityAfter;
	@Column (name = "quantity_before", precision = 19, scale = 2)
	@AMetaData (displayName = "Quantity Before", required = false, description = "Quantity before transaction")
	private BigDecimal quantityBefore;
	@Column (name = "reference", length = 255)
	@Size (max = 255)
	@AMetaData (displayName = "Reference", required = false, description = "External reference", maxLength = 255)
	private String reference;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "storage_item_id", nullable = false)
	@NotNull
	@AMetaData (displayName = "Storage Item", required = true, description = "Storage item", dataProviderBean = "CStorageItemService")
	private CStorageItem storageItem;
	@Column (name = "transaction_date", nullable = false)
	@NotNull
	@AMetaData (displayName = "Transaction Date", required = true, description = "Date of transaction")
	private LocalDateTime transactionDate;
	@Enumerated (EnumType.STRING)
	@Column (name = "transaction_type", nullable = false, length = 50)
	@NotNull
	@AMetaData (displayName = "Transaction Type", required = true, description = "Transaction type")
	private CTransactionType transactionType;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "user_id")
	@AMetaData (displayName = "User", required = false, description = "User performing transaction", dataProviderBean = "CUserService")
	private CUser user;
	/** Default constructor for JPA. */
	public CStorageTransaction() {
		super();
	}

	@Override
	public String getColor() { return color; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	public BigDecimal getQuantity() { return quantity; }

	public BigDecimal getQuantityAfter() { return quantityAfter; }

	public BigDecimal getQuantityBefore() { return quantityBefore; }

	public String getReference() { return reference; }

	public CStorageItem getStorageItem() { return storageItem; }

	public LocalDateTime getTransactionDate() { return transactionDate; }

	public CTransactionType getTransactionType() { return transactionType; }

	public CUser getUser() { return user; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setColor(final String color) { this.color = color; }

	public void setQuantity(final BigDecimal quantity) { this.quantity = quantity; }

	public void setQuantityAfter(final BigDecimal quantityAfter) { this.quantityAfter = quantityAfter; }

	public void setQuantityBefore(final BigDecimal quantityBefore) { this.quantityBefore = quantityBefore; }

	public void setReference(final String reference) { this.reference = reference; }

	public void setStorageItem(final CStorageItem storageItem) { this.storageItem = storageItem; }

	public void setTransactionDate(final LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

	public void setTransactionType(final CTransactionType transactionType) { this.transactionType = transactionType; }

	public void setUser(final CUser user) { this.user = user; }
}
