package tech.derbent.api.workflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.activities.service.CPageServiceActivity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;

public class CPageServiceWorkflowStatusRelation extends CPageServiceDynamicPage<CWorkflowStatusRelation> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivity.class);
	Long serialVersionUID = 1L;

	public CPageServiceWorkflowStatusRelation(IPageServiceImplementer<CWorkflowStatusRelation> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CWorkflowStatusRelation");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CWorkflowStatusRelation> gridView = (CGridViewBaseDBEntity<CWorkflowStatusRelation>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
