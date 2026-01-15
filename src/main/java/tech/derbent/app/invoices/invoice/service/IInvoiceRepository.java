package tech.derbent.app.invoices.invoice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IProjectItemRespository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.invoices.invoice.domain.CInvoice;
import tech.derbent.app.invoices.payment.domain.CPaymentStatus;

public interface IInvoiceRepository extends IProjectItemRespository<CInvoice> {

	@Override
	@Query("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.currency
			LEFT JOIN FETCH i.invoiceItems
			LEFT JOIN FETCH i.payments
			LEFT JOIN FETCH i.attachments
			LEFT JOIN FETCH i.comments
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			WHERE i.id = :id
			""")
	Optional<CInvoice> findById(@Param("id") Long id);

	@Override
	@Query("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.currency
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			WHERE i.project = :project
			ORDER BY i.invoiceDate DESC
			""")
	Page<CInvoice> listByProject(@Param("project") CProject project, Pageable pageable);

	@Override
	@Query("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.createdBy
			LEFT JOIN FETCH i.currency
			LEFT JOIN FETCH i.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH i.status
			WHERE i.project = :project
			ORDER BY i.invoiceDate DESC
			""")
	List<CInvoice> listByProjectForPageView(@Param("project") CProject project);

	/** Find all overdue invoices (due date passed, not paid or cancelled).
	 * @param project the project
	 * @param currentDate the current date
	 * @return list of overdue invoices ordered by due date */
	@Query("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.currency
			WHERE i.project = :project
			AND i.dueDate < :currentDate
			AND i.paymentStatus NOT IN ('PAID', 'CANCELLED')
			ORDER BY i.dueDate ASC
			""")
	List<CInvoice> findOverdueInvoices(@Param("project") CProject project, @Param("currentDate") LocalDate currentDate);

	/** Find invoices by payment status.
	 * @param project the project
	 * @param status the payment status
	 * @return list of invoices with the specified status */
	@Query("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.currency
			WHERE i.project = :project
			AND i.paymentStatus = :status
			ORDER BY i.invoiceDate DESC
			""")
	List<CInvoice> findByPaymentStatus(@Param("project") CProject project, @Param("status") CPaymentStatus status);

	/** Find invoices by customer name (case-insensitive partial match).
	 * @param project the project
	 * @param customerName the customer name to search
	 * @return list of matching invoices */
	@Query("""
			SELECT i FROM #{#entityName} i
			LEFT JOIN FETCH i.project
			LEFT JOIN FETCH i.assignedTo
			LEFT JOIN FETCH i.currency
			WHERE i.project = :project
			AND LOWER(i.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))
			ORDER BY i.invoiceDate DESC
			""")
	List<CInvoice> findByCustomerName(@Param("project") CProject project, @Param("customerName") String customerName);

	/** Calculate total invoice amount for a project.
	 * @param project the project
	 * @return total invoice amount */
	@Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM #{#entityName} i WHERE i.project = :project")
	BigDecimal calculateTotalInvoiceAmount(@Param("project") CProject project);

	/** Calculate total paid amount for a project.
	 * @param project the project
	 * @return total paid amount */
	@Query("SELECT COALESCE(SUM(i.totalPaid), 0) FROM #{#entityName} i WHERE i.project = :project")
	BigDecimal calculateTotalPaidAmount(@Param("project") CProject project);
}
