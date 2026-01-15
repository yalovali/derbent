package tech.derbent.app.invoices.payment.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.invoices.invoice.domain.CInvoice;
import tech.derbent.app.invoices.payment.domain.CPayment;
import tech.derbent.app.invoices.payment.domain.CPaymentStatus;

public interface IPaymentRepository extends IAbstractRepository<CPayment> {

	/** Find all payments for an invoice.
	 * @param invoice the invoice
	 * @return list of payments ordered by payment date descending */
	@Query("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.invoice
			LEFT JOIN FETCH p.currency
			LEFT JOIN FETCH p.receivedBy
			WHERE p.invoice = :invoice
			ORDER BY p.paymentDate DESC
			""")
	List<CPayment> findByInvoice(@Param("invoice") CInvoice invoice);

	/** Find payments by status.
	 * @param status the payment status
	 * @return list of payments with the specified status */
	@Query("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.invoice
			LEFT JOIN FETCH p.currency
			LEFT JOIN FETCH p.receivedBy
			WHERE p.status = :status
			ORDER BY p.paymentDate DESC
			""")
	List<CPayment> findByStatus(@Param("status") CPaymentStatus status);

	/** Calculate total paid amount for an invoice.
	 * @param invoice the invoice
	 * @return total paid amount */
	@Query("SELECT COALESCE(SUM(p.amount), 0) FROM #{#entityName} p WHERE p.invoice = :invoice AND p.status = 'RECEIVED'")
	BigDecimal calculateTotalPaidForInvoice(@Param("invoice") CInvoice invoice);
}
