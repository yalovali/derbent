package tech.derbent.plm.milestones.milestone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.milestones.milestone.domain.CMilestone;

public class CPageServiceMilestone extends CPageServiceDynamicPage<CMilestone> implements IPageServiceHasStatusAndWorkflow<CMilestone> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceMilestone.class);
	// Declare the field required by the interface
	private CProjectItemStatusService statusService;
	Long serialVersionUID = 1L;

	public CPageServiceMilestone(IPageServiceImplementer<CMilestone> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	/** Handle report action - generates CSV report from grid data.
	 * @throws Exception if report generation fails */
	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CMilestone");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CMilestone> gridView = (CGridViewBaseDBEntity<CMilestone>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }
}
