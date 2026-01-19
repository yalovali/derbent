package tech.derbent.plm.invoices.invoiceitem.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.invoices.invoiceitem.domain.CInvoiceItem;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CInvoiceItemService extends CAbstractService<CInvoiceItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CInvoiceItemService.class);

	CInvoiceItemService(final IInvoiceItemRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CInvoiceItem> getEntityClass() {
		return CInvoiceItem.class;
	}

	/** Calculate line total for an invoice item.
	 * @param item the invoice item
	 * @return line total (quantity × unit price) */
	public BigDecimal calculateLineTotal(final CInvoiceItem item) {
		Check.notNull(item, "Invoice item cannot be null");
		if (item.getQuantity() == null || item.getUnitPrice() == null) {
			LOGGER.warn("Cannot calculate line total: quantity or unit price is null");
			return BigDecimal.ZERO;
		}
		return item.getQuantity().multiply(item.getUnitPrice());
	}
}
