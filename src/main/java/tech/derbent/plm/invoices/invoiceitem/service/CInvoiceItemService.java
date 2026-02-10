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
import tech.derbent.api.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CInvoiceItemService extends CAbstractService<CInvoiceItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CInvoiceItemService.class);
	CInvoiceItemService(final IInvoiceItemRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CInvoiceItem item) {
		return super.checkDeleteAllowed(item);
	}

	@Override
	protected void validateEntity(final CInvoiceItem entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getDescription(), "Description is required");
		Check.notNull(entity.getInvoice(), "Invoice is required");
		
		// 2. Length Checks - Use validateStringLength helper
		validateStringLength(entity.getDescription(), "Description", 500);
		validateStringLength(entity.getNotes(), "Notes", 1000);
		
		// 3. Numeric Checks - Use validateNumericField helper
		validateNumericField(entity.getItemOrder(), "Item Order", 9999);
		validateNumericField(entity.getQuantity(), "Quantity", new BigDecimal("99999999.99"));
		validateNumericField(entity.getUnitPrice(), "Unit Price", new BigDecimal("9999999999.99"));
		validateNumericField(entity.getLineTotal(), "Line Total", new BigDecimal("9999999999.99"));
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
