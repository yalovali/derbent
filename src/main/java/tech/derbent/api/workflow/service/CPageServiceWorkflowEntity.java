package tech.derbent.api.workflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.workflow.domain.CWorkflowEntity;

public class CPageServiceWorkflowEntity extends CPageServiceDynamicPage<CWorkflowEntity> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceWorkflowEntity.class);
	Long serialVersionUID = 1L;

	public CPageServiceWorkflowEntity(IPageServiceImplementer<CWorkflowEntity> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CWorkflowEntity");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CWorkflowEntity> gridView = (CGridViewBaseDBEntity<CWorkflowEntity>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
