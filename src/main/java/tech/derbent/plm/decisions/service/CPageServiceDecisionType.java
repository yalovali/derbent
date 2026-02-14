package tech.derbent.plm.decisions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.decisions.domain.CDecisionType;

public class CPageServiceDecisionType extends CPageServiceDynamicPage<CDecisionType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDecisionType.class);
	Long serialVersionUID = 1L;

	public CPageServiceDecisionType(IPageServiceImplementer<CDecisionType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CDecisionType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CDecisionType> gridView = (CGridViewBaseDBEntity<CDecisionType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
