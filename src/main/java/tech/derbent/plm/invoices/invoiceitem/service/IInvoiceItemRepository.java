package tech.derbent.plm.invoices.invoiceitem.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.interfaces.IChildEntityRepository;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.invoiceitem.domain.CInvoiceItem;

public interface IInvoiceItemRepository extends IChildEntityRepository<CInvoiceItem, CInvoice> {

	@Override
	@Query("""
			SELECT ii FROM #{#entityName} ii
			LEFT JOIN FETCH ii.invoice
			WHERE ii.invoice = :master
			ORDER BY ii.itemOrder ASC
			""")
	List<CInvoiceItem> findByMaster(@Param("master") CInvoice master);

	@Override
	@Query("""
			SELECT ii FROM #{#entityName} ii
			LEFT JOIN FETCH ii.invoice
			WHERE ii.invoice.id = :masterId
			ORDER BY ii.itemOrder ASC
			""")
	List<CInvoiceItem> findByMasterId(@Param("masterId") Long masterId);

	@Override
	@Query("SELECT COUNT(ii) FROM #{#entityName} ii WHERE ii.invoice = :master")
	Long countByMaster(@Param("master") CInvoice master);

	@Override
	@Query("SELECT COALESCE(MAX(ii.itemOrder), 0) + 1 FROM #{#entityName} ii WHERE ii.invoice = :master")
	Integer getNextItemOrder(@Param("master") CInvoice master);

	/** Calculate subtotal for an invoice (sum of all line totals).
	 * @param invoice the invoice
	 * @return subtotal amount */
	@Query("SELECT COALESCE(SUM(ii.lineTotal), 0) FROM #{#entityName} ii WHERE ii.invoice = :invoice")
	BigDecimal calculateInvoiceSubtotal(@Param("invoice") CInvoice invoice);
}
