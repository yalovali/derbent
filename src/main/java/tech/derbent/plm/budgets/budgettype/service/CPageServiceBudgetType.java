package tech.derbent.plm.budgets.budgettype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.budgets.budgettype.domain.CBudgetType;

public class CPageServiceBudgetType extends CPageServiceDynamicPage<CBudgetType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceBudgetType.class);
	Long serialVersionUID = 1L;

	public CPageServiceBudgetType(IPageServiceImplementer<CBudgetType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CBudgetType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CBudgetType> gridView = (CGridViewBaseDBEntity<CBudgetType>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
