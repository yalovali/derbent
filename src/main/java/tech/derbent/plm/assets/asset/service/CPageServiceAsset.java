package tech.derbent.plm.assets.asset.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.assets.asset.domain.CAsset;

public class CPageServiceAsset extends CPageServiceDynamicPage<CAsset> implements IPageServiceHasStatusAndWorkflow<CAsset> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceAsset.class);
	Long serialVersionUID = 1L;
	
	// Declare the field required by the interface
	private CProjectItemStatusService statusService;

	public CPageServiceAsset(IPageServiceImplementer<CAsset> view) {
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
		LOGGER.debug("Report action triggered for CAsset");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CAsset> gridView = (CGridViewBaseDBEntity<CAsset>) getView();
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