package tech.derbent.api.entity.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.invoices.invoicetype.domain.CInvoiceType;

public class CPageServiceInvoiceType extends CPageServiceDynamicPage<CInvoiceType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceInvoiceType.class);
	Long serialVersionUID = 1L;

	public CPageServiceInvoiceType(final IPageServiceImplementer<CInvoiceType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CInvoiceType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CInvoiceType> gridView = (CGridViewBaseDBEntity<CInvoiceType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}
}
