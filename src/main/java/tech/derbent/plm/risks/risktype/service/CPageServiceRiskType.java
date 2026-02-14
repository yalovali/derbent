package tech.derbent.plm.risks.risktype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.risks.risktype.domain.CRiskType;

public class CPageServiceRiskType extends CPageServiceDynamicPage<CRiskType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceRiskType.class);
	Long serialVersionUID = 1L;

	public CPageServiceRiskType(IPageServiceImplementer<CRiskType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CRiskType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CRiskType> gridView = (CGridViewBaseDBEntity<CRiskType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
