package tech.derbent.plm.risklevel.risklevel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.risklevel.risklevel.domain.CRiskLevel;

public class CPageServiceRiskLevel extends CPageServiceDynamicPage<CRiskLevel> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceRiskLevel.class);
	Long serialVersionUID = 1L;

	public CPageServiceRiskLevel(IPageServiceImplementer<CRiskLevel> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CRiskLevel");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CRiskLevel> gridView = (CGridViewBaseDBEntity<CRiskLevel>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
