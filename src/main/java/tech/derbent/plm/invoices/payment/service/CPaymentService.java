package tech.derbent.plm.invoices.payment.service;

import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.payment.domain.CPayment;
import tech.derbent.plm.invoices.payment.domain.CPaymentStatus;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CPaymentService extends CAbstractService<CPayment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPaymentService.class);

	CPaymentService(final IPaymentRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CPayment> getEntityClass() {
		return CPayment.class;
	}

	@Override
	public void initializeNewEntity(final CPayment entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new payment entity");
		entity.setPaymentDate(LocalDate.now(clock));
		entity.setStatus(CPaymentStatus.PENDING);
		LOGGER.debug("Payment initialization complete with current date and PENDING status");
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
}
