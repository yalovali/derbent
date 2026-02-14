package tech.derbent.plm.projectincomes.projectincometype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.projectincomes.projectincometype.domain.CProjectIncomeType;

public class CPageServiceProjectIncomeType extends CPageServiceDynamicPage<CProjectIncomeType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectIncomeType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectIncomeType(IPageServiceImplementer<CProjectIncomeType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProjectIncomeType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProjectIncomeType> gridView = (CGridViewBaseDBEntity<CProjectIncomeType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
