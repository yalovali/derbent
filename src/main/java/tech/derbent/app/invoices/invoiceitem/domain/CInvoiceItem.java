package tech.derbent.app.invoices.invoiceitem.domain;

import java.math.BigDecimal;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.app.invoices.invoice.domain.CInvoice;

/** CInvoiceItem - Line item on an invoice. */
@Entity
@Table (name = "cinvoiceitem")
@AttributeOverride (name = "id", column = @Column (name = "invoiceitem_id"))
public class CInvoiceItem extends CEntityDB<CInvoiceItem> {

	public static final String DEFAULT_COLOR = "#FFE4B5"; // Moccasin - invoice line items
	public static final String DEFAULT_ICON = "vaadin:line-chart";
	public static final String ENTITY_TITLE_PLURAL = "Invoice Items";
	public static final String ENTITY_TITLE_SINGULAR = "Invoice Item";
	public static final String VIEW_NAME = "Invoice Items View";

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "invoice_id", nullable = false)
	@AMetaData (
			displayName = "Invoice", required = true, readOnly = false,
			description = "Parent invoice", hidden = false,
			dataProviderBean = "CInvoiceService"
	)
	private CInvoice invoice;

	@Column (name = "item_order", nullable = false)
	@Min (value = 1, message = "Item order must be at least 1")
	@AMetaData (
			displayName = "Item Order", required = true, readOnly = false, defaultValue = "1",
			description = "Display order of line item", hidden = false
	)
	private Integer itemOrder = 1;

	@Column (nullable = false, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Description", required = true, readOnly = false,
			description = "Item description", hidden = false, maxLength = 500
	)
	private String description;

	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Quantity must be positive")
	@DecimalMax (value = "99999999.99", message = "Quantity cannot exceed 99999999.99")
	@AMetaData (
			displayName = "Quantity", required = false, readOnly = false, defaultValue = "1.00",
			description = "Quantity of items", hidden = false
	)
	private BigDecimal quantity = BigDecimal.ONE;

	@Column (name = "unit_price", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Unit price must be positive")
	@DecimalMax (value = "9999999999.99", message = "Unit price cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Unit Price", required = false, readOnly = false, defaultValue = "0.00",
			description = "Price per unit", hidden = false
	)
	private BigDecimal unitPrice = BigDecimal.ZERO;

	@Column (name = "line_total", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Line total must be positive")
	@DecimalMax (value = "9999999999.99", message = "Line total cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Line Total", required = false, readOnly = true, defaultValue = "0.00",
			description = "Total for this line (quantity × unit price)", hidden = false
	)
	private BigDecimal lineTotal = BigDecimal.ZERO;

	@Column (name = "notes", nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false,
			description = "Additional notes for this item", hidden = false, maxLength = 1000
	)
	private String notes;

	/** Default constructor for JPA. */
	public CInvoiceItem() {
		super();
		initializeDefaults();
	}

	public CInvoiceItem(final CInvoice invoice, final Integer itemOrder) {
		super();
		this.invoice = invoice;
		this.itemOrder = itemOrder;
		initializeDefaults();
	}

	protected void initializeDefaults() {
		super.initializeDefaults();
		if (itemOrder == null) itemOrder = 1;
		if (quantity == null) quantity = BigDecimal.ONE;
		if (unitPrice == null) unitPrice = BigDecimal.ZERO;
		if (lineTotal == null) lineTotal = BigDecimal.ZERO;
	}

	/** Calculate line total based on quantity and unit price. */
	public void calculateLineTotal() {
		if (quantity != null && unitPrice != null) {
			lineTotal = quantity.multiply(unitPrice);
		} else {
			lineTotal = BigDecimal.ZERO;
		}
	}

	public CInvoice getInvoice() { return invoice; }

	public void setInvoice(final CInvoice invoice) {
		this.invoice = invoice;
	}

	public Integer getItemOrder() { return itemOrder; }

	public void setItemOrder(final Integer itemOrder) {
		this.itemOrder = itemOrder;
	}

	public String getDescription() { return description; }

	public void setDescription(final String description) {
		this.description = description;
	}

	public BigDecimal getQuantity() { return quantity; }

	public void setQuantity(final BigDecimal quantity) {
		this.quantity = quantity;
		calculateLineTotal();
	}

	public BigDecimal getUnitPrice() { return unitPrice; }

	public void setUnitPrice(final BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
		calculateLineTotal();
	}

	public BigDecimal getLineTotal() { return lineTotal; }

	public void setLineTotal(final BigDecimal lineTotal) {
		this.lineTotal = lineTotal;
	}

	public String getNotes() { return notes; }

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	@Override
	public String toString() {
		return String.format("Item %d: %s (Qty: %s, Price: %s, Total: %s)",
			itemOrder, description, quantity, unitPrice, lineTotal);
	}
}
