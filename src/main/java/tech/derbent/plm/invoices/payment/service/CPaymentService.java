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

import java.math.BigDecimal;
import tech.derbent.api.validation.ValidationMessages;

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
	
	@Override
	public String checkDeleteAllowed(final CPayment payment) {
		return super.checkDeleteAllowed(payment);
	}

	@Override
	protected void validateEntity(final CPayment entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notNull(entity.getInvoice(), "Invoice is required");
		Check.notNull(entity.getPaymentDate(), "Payment Date is required");
		Check.notNull(entity.getStatus(), "Payment Status is required");
		
		// 2. Length Checks
		if (entity.getPaymentMethod() != null && entity.getPaymentMethod().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Payment Method cannot exceed %d characters", 100));
		}
		if (entity.getReferenceNumber() != null && entity.getReferenceNumber().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Reference Number cannot exceed %d characters", 100));
		}
		if (entity.getNotes() != null && entity.getNotes().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Notes cannot exceed %d characters", 2000));
		}
		
		// 3. Numeric Checks
		validateNumericField(entity.getAmount(), "Amount", new BigDecimal("9999999999.99"));
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
