package tech.derbent.plm.products.producttype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.products.producttype.domain.CProductType;

public class CPageServiceProductType extends CPageServiceDynamicPage<CProductType> {

Logger LOGGER = LoggerFactory.getLogger(CPageServiceProductType.class);
Long serialVersionUID = 1L;

public CPageServiceProductType(IPageServiceImplementer<CProductType> view) {
super(view);
}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProductType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProductType> gridView = (CGridViewBaseDBEntity<CProductType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
