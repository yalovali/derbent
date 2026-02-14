package tech.derbent.plm.deliverables.deliverable.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.deliverables.deliverable.domain.CDeliverable;

public class CPageServiceDeliverable extends CPageServiceDynamicPage<CDeliverable> implements IPageServiceHasStatusAndWorkflow<CDeliverable> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDeliverable.class);
	Long serialVersionUID = 1L;

	// Declare the field required by the interface
	private CProjectItemStatusService statusService;

	public CPageServiceDeliverable(IPageServiceImplementer<CDeliverable> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CDeliverable");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CDeliverable> gridView = (CGridViewBaseDBEntity<CDeliverable>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}


	@Override
	public CProjectItemStatusService getProjectItemStatusService() {
		return statusService;
	}
}
