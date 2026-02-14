package tech.derbent.plm.components.component.service;

import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.components.component.domain.CProjectComponent;

public class CPageServiceProjectComponent extends CPageServiceDynamicPage<CProjectComponent>
		implements IPageServiceHasStatusAndWorkflow<CProjectComponent> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectComponent.class);
	// Declare the field required by the interface
	private CProjectItemStatusService statusService;
	Long serialVersionUID = 1L;

	public CPageServiceProjectComponent(IPageServiceImplementer<CProjectComponent> view) {
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
		LOGGER.debug("Report action triggered for CProjectComponent");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProjectComponent> gridView = (CGridViewBaseDBEntity<CProjectComponent>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}


	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }
}
