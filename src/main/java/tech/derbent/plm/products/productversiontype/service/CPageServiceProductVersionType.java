package tech.derbent.plm.products.productversiontype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.products.productversiontype.domain.CProductVersionType;

public class CPageServiceProductVersionType extends CPageServiceDynamicPage<CProductVersionType> {

Logger LOGGER = LoggerFactory.getLogger(CPageServiceProductVersionType.class);
Long serialVersionUID = 1L;

public CPageServiceProductVersionType(IPageServiceImplementer<CProductVersionType> view) {
super(view);
}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProductVersionType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProductVersionType> gridView = (CGridViewBaseDBEntity<CProductVersionType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
