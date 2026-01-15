package tech.derbent.app.invoices.invoiceitem.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.invoices.invoice.domain.CInvoice;
import tech.derbent.app.invoices.invoiceitem.domain.CInvoiceItem;

public interface IInvoiceItemRepository extends IAbstractRepository<CInvoiceItem> {

	/** Find all invoice items for an invoice.
	 * @param invoice the invoice
	 * @return list of invoice items ordered by item order */
	@Query("""
			SELECT ii FROM #{#entityName} ii
			LEFT JOIN FETCH ii.invoice
			WHERE ii.invoice = :invoice
			ORDER BY ii.itemOrder ASC
			""")
	List<CInvoiceItem> findByInvoice(@Param("invoice") CInvoice invoice);

	/** Calculate subtotal for an invoice (sum of all line totals).
	 * @param invoice the invoice
	 * @return subtotal amount */
	@Query("SELECT COALESCE(SUM(ii.lineTotal), 0) FROM #{#entityName} ii WHERE ii.invoice = :invoice")
	BigDecimal calculateInvoiceSubtotal(@Param("invoice") CInvoice invoice);
}
