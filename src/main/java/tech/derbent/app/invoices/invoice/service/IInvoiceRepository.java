package tech.derbent.app.invoices.invoice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.invoices.invoice.domain.CInvoice;
import tech.derbent.app.invoices.payment.domain.CPaymentStatus;

public interface IInvoiceRepository extends IProjectItemRespository<CInvoice> {

	/** Calculate total invoice amount for a project.
	 * @param project the project
	 * @return total invoice amount */
	@Query ("SELECT COALESCE(SUM(i.totalAmount), 0) FROM #{#entityName} i WHERE i.project = :project")
	BigDecimal calculateTotalInvoiceAmount(@Param ("project") CProject project);
	/** Calculate total paid amount for a project.
	 * @param project the project
	 * @return total paid amount */
	@Query ("SELECT COALESCE(SUM(i.paidAmount), 0) FROM #{#entityName} i WHERE i.project = :project")
	BigDecimal calculateTotalPaidAmount(@Param ("project") CProject project);
	@Query ("""
			SELECT i FROM #{#entityName} i
			WHERE i.project = :project
			AND LOWER(i.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))
			ORDER BY i.invoiceDate DESC
			""")
	List<CInvoice> findByCustomerName(@Param ("project") CProject project, @Param ("customerName") String customerName);
	@Override
	@Query ("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.attachments
			LEFT JOIN FETCH i.comments
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.status
			WHERE i.id = :id
			""")
	Optional<CInvoice> findById(@Param ("id") Long id);
	@Query ("""
			SELECT i FROM #{#entityName} i
			WHERE i.project = :project
			AND i.paymentStatus = :status
			ORDER BY i.invoiceDate DESC
			""")
	List<CInvoice> findByPaymentStatus(@Param ("project") CProject project, @Param ("status") CPaymentStatus status);
	@Query ("""
			SELECT i FROM #{#entityName} i
			WHERE i.project = :project
			AND i.dueDate < :currentDate
			AND i.paymentStatus NOT IN ('PAID', 'CANCELLED')
			ORDER BY i.dueDate ASC
			""")
	List<CInvoice> findOverdueInvoices(@Param ("project") CProject project, @Param ("currentDate") LocalDate currentDate);
}
