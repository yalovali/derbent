package tech.derbent.plm.projectincomes.projectincome.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.projectincomes.projectincome.domain.CProjectIncome;

public class CPageServiceProjectIncome extends CPageServiceDynamicPage<CProjectIncome> implements IPageServiceHasStatusAndWorkflow<CProjectIncome> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectIncome.class);
	// Declare the field required by the interface
	private CProjectItemStatusService statusService;
	Long serialVersionUID = 1L;

	public CPageServiceProjectIncome(IPageServiceImplementer<CProjectIncome> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProjectIncome");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProjectIncome> gridView = (CGridViewBaseDBEntity<CProjectIncome>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}


	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }
}
