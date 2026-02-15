package tech.derbent.plm.activities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.activities.domain.CActivityPriority;

public class CPageServiceActivityPriority extends CPageServiceDynamicPage<CActivityPriority> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivityPriority.class);
	Long serialVersionUID = 1L;

	public CPageServiceActivityPriority(IPageServiceImplementer<CActivityPriority> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CActivityPriority");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CActivityPriority> gridView = (CGridViewBaseDBEntity<CActivityPriority>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}
}
