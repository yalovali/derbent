package tech.derbent.plm.activities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.activities.domain.CActivityType;

public class CPageServiceActivityType extends CPageServiceDynamicPage<CActivityType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivityType.class);
	Long serialVersionUID = 1L;

	public CPageServiceActivityType(final IPageServiceImplementer<CActivityType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CActivityType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CActivityType> gridView = (CGridViewBaseDBEntity<CActivityType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}
}
