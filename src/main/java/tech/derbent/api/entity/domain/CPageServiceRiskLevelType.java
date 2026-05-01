package tech.derbent.api.entity.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.risklevel.riskleveltype.domain.CRiskLevelType;

public class CPageServiceRiskLevelType extends CPageServiceDynamicPage<CRiskLevelType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceRiskLevelType.class);
	Long serialVersionUID = 1L;

	public CPageServiceRiskLevelType(final IPageServiceImplementer<CRiskLevelType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CRiskLevelType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CRiskLevelType> gridView = (CGridViewBaseDBEntity<CRiskLevelType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}
}
