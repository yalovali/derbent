package tech.derbent.app.invoices.invoice.service;

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
import tech.derbent.app.invoices.invoice.domain.CInvoice;
import tech.derbent.app.invoices.invoiceitem.service.CInvoiceItemService;
import tech.derbent.app.invoices.payment.domain.CPaymentStatus;
import tech.derbent.app.invoices.payment.service.CPaymentService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CInvoiceService extends CProjectItemService<CInvoice> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CInvoiceService.class);
	private final CInvoiceItemService invoiceItemService;
	private final CPaymentService paymentService;

	CInvoiceService(final IInvoiceRepository repository, final Clock clock, final ISessionService sessionService,
			final CInvoiceItemService invoiceItemService, final CPaymentService paymentService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.invoiceItemService = invoiceItemService;
		this.paymentService = paymentService;
	}

	@Override
	public String checkDeleteAllowed(final CInvoice invoice) {
		return super.checkDeleteAllowed(invoice);
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
		entity.setInvoiceDate(LocalDate.now(clock));
		entity.setDueDate(LocalDate.now(clock).plusDays(30));
		LOGGER.debug("Invoice initialization complete with current date and 30-day due date");
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
