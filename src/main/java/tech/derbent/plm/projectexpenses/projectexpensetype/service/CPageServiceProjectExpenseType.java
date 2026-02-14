package tech.derbent.plm.projectexpenses.projectexpensetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.projectexpenses.projectexpensetype.domain.CProjectExpenseType;

public class CPageServiceProjectExpenseType extends CPageServiceDynamicPage<CProjectExpenseType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectExpenseType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectExpenseType(IPageServiceImplementer<CProjectExpenseType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProjectExpenseType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProjectExpenseType> gridView = (CGridViewBaseDBEntity<CProjectExpenseType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
