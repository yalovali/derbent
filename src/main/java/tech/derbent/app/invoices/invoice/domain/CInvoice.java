package tech.derbent.app.invoices.invoice.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IFinancialEntity;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.invoices.invoiceitem.domain.CInvoiceItem;
import tech.derbent.app.invoices.payment.domain.CPayment;
import tech.derbent.app.invoices.payment.domain.CPaymentStatus;
import tech.derbent.app.milestones.milestone.domain.CMilestone;
import tech.derbent.app.orders.currency.domain.CCurrency;
import tech.derbent.base.users.domain.CUser;

/** CInvoice - Invoice entity for customer billing and income tracking.
 * Represents invoices sent to customers for project work or services. */
@Entity
@Table (name = "cinvoice")
@AttributeOverride (name = "id", column = @Column (name = "invoice_id"))
public class CInvoice extends CProjectItem<CInvoice> implements IHasAttachments, IHasComments, IFinancialEntity {

	public static final String DEFAULT_COLOR = "#FFD700"; // Gold - incoming revenue
	public static final String DEFAULT_ICON = "vaadin:invoice";
	public static final String ENTITY_TITLE_PLURAL = "Invoices";
	public static final String ENTITY_TITLE_SINGULAR = "Invoice";
	public static final String VIEW_NAME = "Invoices View";

	@Column (name = "invoice_number", nullable = false, length = 50, unique = true)
	@Size (max = 50)
	@AMetaData (
			displayName = "Invoice Number", required = true, readOnly = false,
			description = "Unique invoice identifier", hidden = false, maxLength = 50
	)
	private String invoiceNumber;

	@Column (name = "invoice_date", nullable = false)
	@AMetaData (
			displayName = "Invoice Date", required = true, readOnly = false,
			description = "Date invoice was issued", hidden = false
	)
	private LocalDate invoiceDate;

	@Column (name = "due_date", nullable = false)
	@AMetaData (
			displayName = "Due Date", required = true, readOnly = false,
			description = "Payment due date", hidden = false
	)
	private LocalDate dueDate;

	@Column (name = "customer_name", nullable = false, length = 200)
	@Size (max = 200)
	@AMetaData (
			displayName = "Customer Name", required = true, readOnly = false,
			description = "Name of customer/client", hidden = false, maxLength = 200
	)
	private String customerName;

	@Column (name = "customer_email", nullable = true, length = 150)
	@Size (max = 150)
	@AMetaData (
			displayName = "Customer Email", required = false, readOnly = false,
			description = "Customer email address", hidden = false, maxLength = 150
	)
	private String customerEmail;

	@Column (name = "customer_address", nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Customer Address", required = false, readOnly = false,
			description = "Customer billing address", hidden = false, maxLength = 500
	)
	private String customerAddress;

	@Column (name = "customer_tax_id", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Customer Tax ID", required = false, readOnly = false,
			description = "Customer tax identification number", hidden = false, maxLength = 50
	)
	private String customerTaxId;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "currency_id", nullable = false)
	@AMetaData (
			displayName = "Currency", required = true, readOnly = false,
			description = "Invoice currency", hidden = false,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;

	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Subtotal must be positive")
	@DecimalMax (value = "9999999999.99", message = "Subtotal cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Subtotal", required = false, readOnly = true, defaultValue = "0.00",
			description = "Sum of all line items before tax", hidden = false
	)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column (name = "tax_rate", nullable = true, precision = 5, scale = 2)
	@DecimalMin (value = "0.0", message = "Tax rate must be positive")
	@DecimalMax (value = "100.0", message = "Tax rate cannot exceed 100%")
	@AMetaData (
			displayName = "Tax Rate (%)", required = false, readOnly = false, defaultValue = "0.00",
			description = "Tax rate percentage", hidden = false
	)
	private BigDecimal taxRate = BigDecimal.ZERO;

	@Column (name = "tax_amount", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Tax amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Tax amount cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Tax Amount", required = false, readOnly = true, defaultValue = "0.00",
			description = "Calculated tax amount", hidden = false
	)
	private BigDecimal taxAmount = BigDecimal.ZERO;

	@Column (name = "discount_rate", nullable = true, precision = 5, scale = 2)
	@DecimalMin (value = "0.0", message = "Discount rate must be positive")
	@DecimalMax (value = "100.0", message = "Discount rate cannot exceed 100%")
	@AMetaData (
			displayName = "Discount (%)", required = false, readOnly = false, defaultValue = "0.00",
			description = "Discount percentage", hidden = false
	)
	private BigDecimal discountRate = BigDecimal.ZERO;

	@Column (name = "discount_amount", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Discount amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Discount amount cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Discount Amount", required = false, readOnly = true, defaultValue = "0.00",
			description = "Calculated discount amount", hidden = false
	)
	private BigDecimal discountAmount = BigDecimal.ZERO;

	@Column (name = "total_amount", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Total amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Total amount cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Total Amount", required = false, readOnly = true, defaultValue = "0.00",
			description = "Final total amount due", hidden = false
	)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Column (name = "paid_amount", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Paid amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Paid amount cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Paid Amount", required = false, readOnly = true, defaultValue = "0.00",
			description = "Total amount paid so far", hidden = false
	)
	private BigDecimal paidAmount = BigDecimal.ZERO;

	@Enumerated (EnumType.STRING)
	@Column (name = "payment_status", nullable = false, length = 20)
	@AMetaData (
			displayName = "Payment Status", required = false, readOnly = false,
			description = "Current payment status", hidden = false
	)
	private CPaymentStatus paymentStatus = CPaymentStatus.PENDING;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "issued_by_id", nullable = true)
	@AMetaData (
			displayName = "Issued By", required = false, readOnly = false,
			description = "User who issued the invoice", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser issuedBy;

	@Column (name = "payment_terms", nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Payment Terms", required = false, readOnly = false,
			description = "Payment terms and conditions", hidden = false, maxLength = 1000
	)
	private String paymentTerms;

	@Column (name = "notes", nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false,
			description = "Additional notes or instructions", hidden = false, maxLength = 2000
	)
	private String notes;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "milestone_id", nullable = true)
	@AMetaData (
			displayName = "Related Milestone", required = false, readOnly = false,
			description = "Project milestone this invoice is associated with (e.g., milestone acceptance payment)", hidden = false,
			dataProviderBean = "CMilestoneService"
	)
	private CMilestone relatedMilestone;

	@Column (name = "is_milestone_payment", nullable = true)
	@AMetaData (
			displayName = "Milestone Payment", required = false, readOnly = false,
			description = "Indicates if this is a milestone acceptance payment", hidden = false
	)
	private Boolean isMilestonePayment = false;

	@Column (name = "payment_plan_installments", nullable = true)
	@AMetaData (
			displayName = "Payment Plan Installments", required = false, readOnly = false,
			description = "Total number of installments if this is part of a payment plan", hidden = false
	)
	private Integer paymentPlanInstallments;

	@Column (name = "installment_number", nullable = true)
	@AMetaData (
			displayName = "Installment Number", required = false, readOnly = false,
			description = "Current installment number (e.g., 1 of 4)", hidden = false
	)
	private Integer installmentNumber;

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "invoice")
	@AMetaData (
			displayName = "Invoice Items", required = false, readOnly = false,
			description = "Line items on this invoice", hidden = false,
			dataProviderBean = "CInvoiceItemService", createComponentMethod = "createComponent"
	)
	private List<CInvoiceItem> invoiceItems = new ArrayList<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "invoice")
	@AMetaData (
			displayName = "Payments", required = false, readOnly = false,
			description = "Payments received for this invoice", hidden = false,
			dataProviderBean = "CPaymentService", createComponentMethod = "createComponent"
	)
	private List<CPayment> payments = new ArrayList<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "invoice_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false,
			description = "Invoice documents and attachments", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "invoice_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false,
			description = "Comments and discussions", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CInvoice() {
		super();
		initializeDefaults();
	}

	public CInvoice(final String name, final CProject project) {
		super(CInvoice.class, name, project);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (invoiceDate == null) {
			invoiceDate = LocalDate.now();
		}
		if (dueDate == null) {
			dueDate = invoiceDate.plusDays(30); // Default 30 days payment term
		}
		if (paymentStatus == null) {
			paymentStatus = CPaymentStatus.PENDING;
		}
		if (subtotal == null) subtotal = BigDecimal.ZERO;
		if (taxRate == null) taxRate = BigDecimal.ZERO;
		if (taxAmount == null) taxAmount = BigDecimal.ZERO;
		if (discountRate == null) discountRate = BigDecimal.ZERO;
		if (discountAmount == null) discountAmount = BigDecimal.ZERO;
		if (totalAmount == null) totalAmount = BigDecimal.ZERO;
		if (paidAmount == null) paidAmount = BigDecimal.ZERO;
	}

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
	}

	/** Recalculate invoice amounts based on line items and rates. */
	public void recalculateAmounts() {
		// Calculate subtotal from invoice items
		subtotal = invoiceItems.stream()
			.map(CInvoiceItem::getLineTotal)
			.filter(java.util.Objects::nonNull)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		// Calculate discount amount
		if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
			discountAmount = subtotal.multiply(discountRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
		} else {
			discountAmount = BigDecimal.ZERO;
		}

		// Calculate taxable amount
		BigDecimal taxableAmount = subtotal.subtract(discountAmount);

		// Calculate tax amount
		if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
			taxAmount = taxableAmount.multiply(taxRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
		} else {
			taxAmount = BigDecimal.ZERO;
		}

		// Calculate total
		totalAmount = taxableAmount.add(taxAmount);

		updateLastModified();
	}

	/** Update payment status based on paid amount. */
	public void updatePaymentStatus() {
		if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
			paymentStatus = CPaymentStatus.PENDING;
			return;
		}

		if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) == 0) {
			// Check if overdue
			if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
				paymentStatus = CPaymentStatus.LATE;
			} else if (dueDate != null && LocalDate.now().isEqual(dueDate)) {
				paymentStatus = CPaymentStatus.DUE;
			} else {
				paymentStatus = CPaymentStatus.PENDING;
			}
		} else if (paidAmount.compareTo(totalAmount) >= 0) {
			paymentStatus = CPaymentStatus.PAID;
		} else {
			paymentStatus = CPaymentStatus.PARTIAL;
		}

		updateLastModified();
	}

	/** Get remaining balance.
	 * @return amount still owed */
	public BigDecimal getRemainingBalance() {
		if (totalAmount == null) return BigDecimal.ZERO;
		if (paidAmount == null) return totalAmount;
		return totalAmount.subtract(paidAmount).max(BigDecimal.ZERO);
	}

	/** Check if invoice is overdue.
	 * @return true if payment is overdue */
	public boolean isOverdue() {
		return dueDate != null && LocalDate.now().isAfter(dueDate) &&
			paymentStatus != CPaymentStatus.PAID && paymentStatus != CPaymentStatus.CANCELLED;
	}

	/** Get days until due (negative if overdue).
	 * @return number of days */
	public long getDaysUntilDue() {
		if (dueDate == null) return 0;
		return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
	}

	// Getters and setters
	public String getInvoiceNumber() { return invoiceNumber; }

	public void setInvoiceNumber(final String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
		updateLastModified();
	}

	public LocalDate getInvoiceDate() { return invoiceDate; }

	public void setInvoiceDate(final LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
		updateLastModified();
	}

	public LocalDate getDueDate() { return dueDate; }

	public void setDueDate(final LocalDate dueDate) {
		this.dueDate = dueDate;
		updateLastModified();
	}

	public String getCustomerName() { return customerName; }

	public void setCustomerName(final String customerName) {
		this.customerName = customerName;
		updateLastModified();
	}

	public String getCustomerEmail() { return customerEmail; }

	public void setCustomerEmail(final String customerEmail) {
		this.customerEmail = customerEmail;
		updateLastModified();
	}

	public String getCustomerAddress() { return customerAddress; }

	public void setCustomerAddress(final String customerAddress) {
		this.customerAddress = customerAddress;
		updateLastModified();
	}

	public String getCustomerTaxId() { return customerTaxId; }

	public void setCustomerTaxId(final String customerTaxId) {
		this.customerTaxId = customerTaxId;
		updateLastModified();
	}

	public CCurrency getCurrency() { return currency; }

	public void setCurrency(final CCurrency currency) {
		this.currency = currency;
		updateLastModified();
	}

	public BigDecimal getSubtotal() { return subtotal; }

	public void setSubtotal(final BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getTaxRate() { return taxRate; }

	public void setTaxRate(final BigDecimal taxRate) {
		this.taxRate = taxRate;
		updateLastModified();
	}

	public BigDecimal getTaxAmount() { return taxAmount; }

	public void setTaxAmount(final BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	public BigDecimal getDiscountRate() { return discountRate; }

	public void setDiscountRate(final BigDecimal discountRate) {
		this.discountRate = discountRate;
		updateLastModified();
	}

	public BigDecimal getDiscountAmount() { return discountAmount; }

	public void setDiscountAmount(final BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	public BigDecimal getTotalAmount() { return totalAmount; }

	public void setTotalAmount(final BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public BigDecimal getAmount() { return totalAmount; }

	@Override
	public void setAmount(final BigDecimal amount) {
		setTotalAmount(amount);
	}

	public BigDecimal getPaidAmount() { return paidAmount; }

	public void setPaidAmount(final BigDecimal paidAmount) {
		this.paidAmount = paidAmount;
		updateLastModified();
	}

	public CPaymentStatus getPaymentStatus() { return paymentStatus; }

	public void setPaymentStatus(final CPaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
		updateLastModified();
	}

	public CUser getIssuedBy() { return issuedBy; }

	public void setIssuedBy(final CUser issuedBy) {
		this.issuedBy = issuedBy;
		updateLastModified();
	}

	public String getPaymentTerms() { return paymentTerms; }

	public void setPaymentTerms(final String paymentTerms) {
		this.paymentTerms = paymentTerms;
		updateLastModified();
	}

	public String getNotes() { return notes; }

	public void setNotes(final String notes) {
		this.notes = notes;
		updateLastModified();
	}

	public CMilestone getRelatedMilestone() { return relatedMilestone; }

	public void setRelatedMilestone(final CMilestone relatedMilestone) {
		this.relatedMilestone = relatedMilestone;
		updateLastModified();
	}

	public Boolean getIsMilestonePayment() { return isMilestonePayment; }

	public void setIsMilestonePayment(final Boolean isMilestonePayment) {
		this.isMilestonePayment = isMilestonePayment;
		updateLastModified();
	}

	public Integer getPaymentPlanInstallments() { return paymentPlanInstallments; }

	public void setPaymentPlanInstallments(final Integer paymentPlanInstallments) {
		this.paymentPlanInstallments = paymentPlanInstallments;
		updateLastModified();
	}

	public Integer getInstallmentNumber() { return installmentNumber; }

	public void setInstallmentNumber(final Integer installmentNumber) {
		this.installmentNumber = installmentNumber;
		updateLastModified();
	}

	public List<CInvoiceItem> getInvoiceItems() {
		if (invoiceItems == null) {
			invoiceItems = new ArrayList<>();
		}
		return invoiceItems;
	}

	public void setInvoiceItems(final List<CInvoiceItem> invoiceItems) {
		this.invoiceItems = invoiceItems;
		updateLastModified();
	}

	public List<CPayment> getPayments() {
		if (payments == null) {
			payments = new ArrayList<>();
		}
		return payments;
	}

	public void setPayments(final List<CPayment> payments) {
		this.payments = payments;
		updateLastModified();
	}

	@Override
	public CInvoice createClone(final tech.derbent.api.interfaces.CCloneOptions options) throws CloneNotSupportedException {
		final CInvoice clone = super.createClone(options);
		clone.invoiceNumber = this.invoiceNumber + " (Copy)";
		clone.customerName = this.customerName;
		clone.customerEmail = this.customerEmail;
		clone.customerAddress = this.customerAddress;
		clone.customerTaxId = this.customerTaxId;
		clone.subtotal = this.subtotal;
		clone.taxRate = this.taxRate;
		clone.taxAmount = this.taxAmount;
		clone.discountRate = this.discountRate;
		clone.discountAmount = this.discountAmount;
		clone.totalAmount = this.totalAmount;
		clone.paidAmount = BigDecimal.ZERO;
		clone.paymentStatus = CPaymentStatus.PENDING;
		clone.paymentTerms = this.paymentTerms;
		clone.notes = this.notes;
		if (!options.isResetDates()) {
			clone.invoiceDate = this.invoiceDate;
			clone.dueDate = this.dueDate;
		}
		if (!options.isResetAssignments()) {
			if (this.currency != null) {
				clone.currency = this.currency;
			}
			if (this.issuedBy != null) {
				clone.issuedBy = this.issuedBy;
			}
		}
		if (options.includesComments() && this.comments != null && !this.comments.isEmpty()) {
			clone.comments = new HashSet<>();
			for (final CComment comment : this.comments) {
				try {
					final CComment commentClone = comment.createClone(options);
					clone.comments.add(commentClone);
				} catch (final Exception e) {
					// Silently skip failed comment clones
				}
			}
		}
		if (options.includesAttachments() && this.attachments != null && !this.attachments.isEmpty()) {
			clone.attachments = new HashSet<>();
			for (final CAttachment attachment : this.attachments) {
				try {
					final CAttachment attachmentClone = attachment.createClone(options);
					clone.attachments.add(attachmentClone);
				} catch (final Exception e) {
					// Silently skip failed attachment clones
				}
			}
		}
		return clone;
	}
}
