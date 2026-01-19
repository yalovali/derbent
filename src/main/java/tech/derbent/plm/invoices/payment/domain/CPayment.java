package tech.derbent.plm.invoices.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IFinancialEntity;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.base.users.domain.CUser;

/** CPayment - Payment received against an invoice. */
@Entity
@Table (name = "cpayment")
@AttributeOverride (name = "id", column = @Column (name = "payment_id"))
public class CPayment extends CEntityDB<CPayment> implements IFinancialEntity {

	public static final String DEFAULT_COLOR = "#90EE90"; // LightGreen - payments received
	public static final String DEFAULT_ICON = "vaadin:money";
	public static final String ENTITY_TITLE_PLURAL = "Payments";
	public static final String ENTITY_TITLE_SINGULAR = "Payment";
	public static final String VIEW_NAME = "Payments View";
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Amount cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Amount", required = false, readOnly = false, defaultValue = "0.00", description = "Payment amount received", hidden = false
	)
	private BigDecimal amount = BigDecimal.ZERO;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "currency_id", nullable = true)
	@AMetaData (
			displayName = "Currency", required = false, readOnly = false, description = "Payment currency", hidden = false,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "invoice_id", nullable = false)
	@AMetaData (
			displayName = "Invoice", required = true, readOnly = false, description = "Invoice this payment applies to", hidden = false,
			dataProviderBean = "CInvoiceService"
	)
	private CInvoice invoice;
	@Column (name = "notes", nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, description = "Payment notes or additional information", hidden = false,
			maxLength = 2000
	)
	private String notes;
	@Column (name = "payment_date", nullable = false)
	@AMetaData (displayName = "Payment Date", required = true, readOnly = false, description = "Date payment was received", hidden = false)
	private LocalDate paymentDate;
	@Column (name = "payment_method", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Payment Method", required = false, readOnly = false,
			description = "Method of payment (bank transfer, credit card, check, etc.)", hidden = false, maxLength = 100
	)
	private String paymentMethod;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "received_by_id", nullable = true)
	@AMetaData (
			displayName = "Received By", required = false, readOnly = false, description = "User who recorded the payment", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser receivedBy;
	@Column (name = "reference_number", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Reference Number", required = false, readOnly = false, description = "Transaction or check reference number",
			hidden = false, maxLength = 100
	)
	private String referenceNumber;
	@Enumerated (EnumType.STRING)
	@Column (name = "status", nullable = false, length = 20)
	@AMetaData (displayName = "Status", required = false, readOnly = false, description = "Payment processing status", hidden = false)
	private CPaymentStatus status = CPaymentStatus.PENDING;

	/** Default constructor for JPA. */
	public CPayment() {
		super();
		initializeDefaults();
	}

	public CPayment(final CInvoice invoice, final BigDecimal amount) {
		super();
		this.invoice = invoice;
		this.amount = amount;
		initializeDefaults();
	}

	@Override
	public BigDecimal getAmount() { return amount; }

	@Override
	public CCurrency getCurrency() { return currency; }

	public CInvoice getInvoice() { return invoice; }

	public String getNotes() { return notes; }

	public LocalDate getPaymentDate() { return paymentDate; }

	public String getPaymentMethod() { return paymentMethod; }

	public CUser getReceivedBy() { return receivedBy; }

	public String getReferenceNumber() { return referenceNumber; }

	public CPaymentStatus getStatus() { return status; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (paymentDate == null) {
			paymentDate = LocalDate.now();
		}
		if (status == null) {
			status = CPaymentStatus.PENDING;
		}
		if (amount == null) {
			amount = BigDecimal.ZERO;
		}
	}

	@Override
	public void setAmount(final BigDecimal amount) { this.amount = amount; }

	@Override
	public void setCurrency(final CCurrency currency) { this.currency = currency; }

	public void setInvoice(final CInvoice invoice) { this.invoice = invoice; }

	public void setNotes(final String notes) { this.notes = notes; }

	public void setPaymentDate(final LocalDate paymentDate) { this.paymentDate = paymentDate; }

	public void setPaymentMethod(final String paymentMethod) { this.paymentMethod = paymentMethod; }

	public void setReceivedBy(final CUser receivedBy) { this.receivedBy = receivedBy; }

	public void setReferenceNumber(final String referenceNumber) { this.referenceNumber = referenceNumber; }

	public void setStatus(final CPaymentStatus status) { this.status = status; }

	@Override
	public String toString() {
		return String.format("Payment: %s on %s (Ref: %s)", amount, paymentDate, referenceNumber);
	}
}
