package tech.derbent.plm.invoices.payment.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.payment.domain.CPayment;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CPaymentService extends CAbstractService<CPayment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPaymentService.class);

	CPaymentService(final IPaymentRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CPayment payment) {
		return super.checkDeleteAllowed(payment);
	}

	@Override
	public Class<CPayment> getEntityClass() { return CPayment.class; }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	/** Record a payment against an invoice and update invoice payment status.
	 * @param invoice the invoice
	 * @param payment the payment to record
	 * @return the saved payment */
	public CPayment recordPayment(final CInvoice invoice, final CPayment payment) {
		Check.notNull(invoice, "Invoice cannot be null");
		Check.notNull(payment, "Payment cannot be null");
		LOGGER.debug("Recording payment for invoice {}", invoice.getId());
		payment.setInvoice(invoice);
		final CPayment savedPayment = save(payment);
		invoice.updatePaymentStatus();
		LOGGER.debug("Payment recorded and invoice payment status updated");
		return savedPayment;
	}

	@Override
	protected void validateEntity(final CPayment entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notNull(entity.getInvoice(), "Invoice is required");
		Check.notNull(entity.getPaymentDate(), "Payment Date is required");
		Check.notNull(entity.getStatus(), "Payment Status is required");
		// 2. Length Checks - USE STATIC HELPER
		validateStringLength(entity.getPaymentMethod(), "Payment Method", 100);
		validateStringLength(entity.getReferenceNumber(), "Reference Number", 100);
		validateStringLength(entity.getNotes(), "Notes", 2000);
		// 3. Numeric Checks - USE STATIC HELPER (inherited from CAbstractService)
		validateNumericField(entity.getAmount(), "Amount", new BigDecimal("9999999999.99"));
	}
}
