package tech.derbent.plm.customers.customertype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.customers.customertype.domain.CCustomerType;

public class CPageServiceCustomerType extends CPageServiceDynamicPage<CCustomerType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceCustomerType.class);
	Long serialVersionUID = 1L;

	public CPageServiceCustomerType(IPageServiceImplementer<CCustomerType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CCustomerType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CCustomerType> gridView = (CGridViewBaseDBEntity<CCustomerType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
