package tech.derbent.plm.orders.currency.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.orders.currency.domain.CCurrency;

public class CPageServiceCurrency extends CPageServiceDynamicPage<CCurrency> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceCurrency.class);
	Long serialVersionUID = 1L;

	public CPageServiceCurrency(IPageServiceImplementer<CCurrency> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CCurrency");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CCurrency> gridView = (CGridViewBaseDBEntity<CCurrency>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
