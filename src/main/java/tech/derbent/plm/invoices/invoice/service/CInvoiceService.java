package tech.derbent.plm.invoices.invoice.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.invoiceitem.service.CInvoiceItemService;
import tech.derbent.plm.invoices.payment.domain.CPaymentStatus;
import tech.derbent.plm.invoices.payment.service.CPaymentService;
import tech.derbent.base.session.service.ISessionService;

import java.util.Optional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CInvoiceService extends CProjectItemService<CInvoice> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CInvoiceService.class);
	CInvoiceService(final IInvoiceRepository repository, final Clock clock, final ISessionService sessionService,
			final CInvoiceItemService invoiceItemService, final CPaymentService paymentService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
	}

	@Override
	public String checkDeleteAllowed(final CInvoice invoice) {
		return super.checkDeleteAllowed(invoice);
	}

	@Override
	protected void validateEntity(final CInvoice entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getCurrency(), "Currency is required");
		Check.notNull(entity.getInvoiceDate(), "Invoice Date is required");
		Check.notNull(entity.getDueDate(), "Due Date is required");
		Check.notBlank(entity.getInvoiceNumber(), "Invoice Number is required");
		Check.notBlank(entity.getCustomerName(), "Customer Name is required");
		
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		if (entity.getCustomerName().length() > 200) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Customer Name cannot exceed %d characters", 200));
		}
		if (entity.getInvoiceNumber().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Invoice Number cannot exceed %d characters", 50));
		}
		if (entity.getCustomerEmail() != null && entity.getCustomerEmail().length() > 150) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Customer Email cannot exceed %d characters", 150));
		}
		if (entity.getCustomerAddress() != null && entity.getCustomerAddress().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Customer Address cannot exceed %d characters", 500));
		}
		if (entity.getNotes() != null && entity.getNotes().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Notes cannot exceed %d characters", 2000));
		}
		if (entity.getPaymentTerms() != null && entity.getPaymentTerms().length() > 1000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Payment Terms cannot exceed %d characters", 1000));
		}
		
		// 3. Unique Checks
		// Invoice Number must be unique generally (or at least within company/project, but usually global for accounting)
		// Assuming global uniqueness per business requirement mirrored in entity unique=true (though here scoped to repo query if available)
		// For now, checking via findAll/stream or if repo has method. Assuming repo has findByInvoiceNumber
		// If not, we can rely on DB constraint or add custom check if critical.
		// Let's stick to name uniqueness within project as base pattern
		final Optional<CInvoice> existingName = ((IInvoiceRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		
		// 4. Numeric Checks
		validateNumericField(entity.getSubtotal(), "Subtotal", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getTaxAmount(), "Tax Amount", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getTotalAmount(), "Total Amount", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getPaidAmount(), "Paid Amount", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getDiscountAmount(), "Discount Amount", new BigDecimal("9999999999.99"));
		
		if (entity.getTaxRate() != null && (entity.getTaxRate().compareTo(BigDecimal.ZERO) < 0 || entity.getTaxRate().compareTo(new BigDecimal("100")) > 0)) {
			throw new IllegalArgumentException("Tax Rate must be between 0 and 100");
		}
		if (entity.getDiscountRate() != null && (entity.getDiscountRate().compareTo(BigDecimal.ZERO) < 0 || entity.getDiscountRate().compareTo(new BigDecimal("100")) > 0)) {
			throw new IllegalArgumentException("Discount Rate must be between 0 and 100");
		}
		
		// 5. Date Logic
		if (entity.getInvoiceDate() != null && entity.getDueDate() != null && entity.getDueDate().isBefore(entity.getInvoiceDate())) {
			throw new IllegalArgumentException("Due Date cannot be before Invoice Date");
		}
	}
	
	private void validateNumericField(BigDecimal value, String fieldName, BigDecimal max) {
		if (value != null) {
			if (value.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException(fieldName + " must be positive");
			}
			if (value.compareTo(max) > 0) {
				throw new IllegalArgumentException(fieldName + " cannot exceed " + max);
			}
		}
	}

	@Override
	public Class<CInvoice> getEntityClass() {
		return CInvoice.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CInvoiceInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceInvoice.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CInvoice entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new invoice entity");
		// Intrinsic defaults (dates, amounts) handled by CInvoice.initializeDefaults()
		// Contextual: set invoice number format? (Not implemented here yet)
		LOGGER.debug("Invoice initialization complete");
	}

	/** Recalculate invoice amounts (subtotal, tax, total).
	 * @param invoice the invoice to recalculate
	 * @return the updated invoice */
	public CInvoice recalculateInvoiceAmounts(final CInvoice invoice) {
		Check.notNull(invoice, "Invoice cannot be null");
		LOGGER.debug("Recalculating amounts for invoice {}", invoice.getId());
		invoice.recalculateAmounts();
		return save(invoice);
	}

	/** Update invoice payment status based on paid amounts.
	 * @param invoice the invoice to update
	 * @return the updated invoice */
	public CInvoice updatePaymentStatus(final CInvoice invoice) {
		Check.notNull(invoice, "Invoice cannot be null");
		LOGGER.debug("Updating payment status for invoice {}", invoice.getId());
		invoice.updatePaymentStatus();
		return save(invoice);
	}

	/** Get all overdue invoices for a project.
	 * @param project the project
	 * @return list of overdue invoices */
	public List<CInvoice> getOverdueInvoices(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IInvoiceRepository) repository).findOverdueInvoices(project, LocalDate.now(clock));
	}

	/** Get total invoice amount for a project.
	 * @param project the project
	 * @return total invoice amount */
	public BigDecimal getTotalInvoiceAmount(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IInvoiceRepository) repository).calculateTotalInvoiceAmount(project);
	}

	/** Get total paid amount for a project.
	 * @param project the project
	 * @return total paid amount */
	public BigDecimal getTotalPaidAmount(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IInvoiceRepository) repository).calculateTotalPaidAmount(project);
	}

	/** Find invoices by payment status.
	 * @param project the project
	 * @param status the payment status
	 * @return list of invoices */
	public List<CInvoice> findByPaymentStatus(final CProject<?> project, final CPaymentStatus status) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(status, "Payment status cannot be null");
		return ((IInvoiceRepository) repository).findByPaymentStatus(project, status);
	}

	/** Find invoices by customer name.
	 * @param project the project
	 * @param customerName the customer name to search
	 * @return list of matching invoices */
	public List<CInvoice> findByCustomerName(final CProject<?> project, final String customerName) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(customerName, "Customer name cannot be blank");
		return ((IInvoiceRepository) repository).findByCustomerName(project, customerName);
	}
}
