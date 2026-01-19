package tech.derbent.plm.invoices.payment.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.interfaces.IChildEntityRepository;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.payment.domain.CPayment;
import tech.derbent.plm.invoices.payment.domain.CPaymentStatus;

public interface IPaymentRepository extends IChildEntityRepository<CPayment, CInvoice> {

	@Override
	@Query("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.invoice
			LEFT JOIN FETCH p.currency
			LEFT JOIN FETCH p.receivedBy
			WHERE p.invoice = :master
			ORDER BY p.paymentDate DESC
			""")
	List<CPayment> findByMaster(@Param("master") CInvoice master);

	@Override
	@Query("""
			SELECT p FROM #{#entityName} p
			LEFT JOIN FETCH p.invoice
			LEFT JOIN FETCH p.currency
			LEFT JOIN FETCH p.receivedBy
			WHERE p.invoice.id = :masterId
			ORDER BY p.paymentDate DESC
			""")
	List<CPayment> findByMasterId(@Param("masterId") Long masterId);

	@Override
	@Query("SELECT COUNT(p) FROM #{#entityName} p WHERE p.invoice = :master")
	Long countByMaster(@Param("master") CInvoice master);

	@Override
	@Query("SELECT COALESCE(MAX(p.id), 0) + 1 FROM #{#entityName} p WHERE p.invoice = :master")
	Integer getNextItemOrder(@Param("master") CInvoice master);

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
