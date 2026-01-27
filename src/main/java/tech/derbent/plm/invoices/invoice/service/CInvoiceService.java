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
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.invoiceitem.service.CInvoiceItemService;
import tech.derbent.plm.invoices.payment.domain.CPaymentStatus;
import tech.derbent.plm.invoices.payment.service.CPaymentService;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CInvoiceService extends CProjectItemService<CInvoice> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CInvoiceService.class);

	CInvoiceService(final IInvoiceRepository repository, final Clock clock, final ISessionService sessionService,
			@SuppressWarnings ("unused") final CInvoiceItemService invoiceItemService,
			@SuppressWarnings ("unused") final CPaymentService paymentService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
	}

	@Override
	public String checkDeleteAllowed(final CInvoice invoice) {
		return super.checkDeleteAllowed(invoice);
	}

	/** Find invoices by customer name.
	 * @param project      the project
	 * @param customerName the customer name to search
	 * @return list of matching invoices */
	public List<CInvoice> findByCustomerName(final CProject<?> project, final String customerName) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(customerName, "Customer name cannot be blank");
		return ((IInvoiceRepository) repository).findByCustomerName(project, customerName);
	}

	/** Find invoices by payment status.
	 * @param project the project
	 * @param status  the payment status
	 * @return list of invoices */
	public List<CInvoice> findByPaymentStatus(final CProject<?> project, final CPaymentStatus status) {
		Check.notNull(project, "Project cannot be null");
		Check.notNull(status, "Payment status cannot be null");
		return ((IInvoiceRepository) repository).findByPaymentStatus(project, status);
	}

	@Override
	public Class<CInvoice> getEntityClass() { return CInvoice.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CInvoiceInitializerService.class; }

	/** Get all overdue invoices for a project.
	 * @param project the project
	 * @return list of overdue invoices */
	public List<CInvoice> getOverdueInvoices(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IInvoiceRepository) repository).findOverdueInvoices(project, LocalDate.now(clock));
	}

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceInvoice.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

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

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
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
		// 2. Length Checks - USE STATIC HELPER
		validateStringLength(entity.getCustomerName(), "Customer Name", 200);
		validateStringLength(entity.getInvoiceNumber(), "Invoice Number", 50);
		validateStringLength(entity.getCustomerEmail(), "Customer Email", 150);
		validateStringLength(entity.getCustomerAddress(), "Customer Address", 500);
		validateStringLength(entity.getNotes(), "Notes", 2000);
		validateStringLength(entity.getPaymentTerms(), "Payment Terms", 1000);
		
		// 3. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IInvoiceRepository) repository, entity, entity.getName(), entity.getProject());
		
		// 4. Numeric Checks - USE STATIC HELPER
		validateNumericField(entity.getSubtotal(), "Subtotal", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getTaxAmount(), "Tax Amount", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getTotalAmount(), "Total Amount", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getPaidAmount(), "Paid Amount", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getDiscountAmount(), "Discount Amount", new BigDecimal("9999999999.99"));
		// Tax Rate and Discount Rate ranges
		validateNumericRange(entity.getTaxRate(), "Tax Rate", new BigDecimal("0"), new BigDecimal("100"));
		validateNumericRange(entity.getDiscountRate(), "Discount Rate", new BigDecimal("0"), new BigDecimal("100"));
		// 5. Date Logic
		if (entity.getInvoiceDate() != null && entity.getDueDate() != null && entity.getDueDate().isBefore(entity.getInvoiceDate())) {
			throw new IllegalArgumentException("Due Date cannot be before Invoice Date");
		}
	}
}
