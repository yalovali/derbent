package tech.derbent.plm.issues.issue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issue.view.CComponentWidgetIssue;

public class CPageServiceIssue extends CPageServiceDynamicPage<CIssue>
		implements IPageServiceHasStatusAndWorkflow<CIssue>, IComponentWidgetEntityProvider<CIssue>, ISprintItemPageService<CIssue> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceIssue.class);
	// Declare the field required by the interface
	private CProjectItemStatusService statusService;
	Long serialVersionUID = 1L;

	public CPageServiceIssue(IPageServiceImplementer<CIssue> view) {
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
		LOGGER.debug("Report action triggered for CIssue");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CIssue> gridView = (CGridViewBaseDBEntity<CIssue>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	@Override
	public CComponentWidgetEntity<CIssue> buildDataProviderComponentWidget(CIssue entity) {
		return new CComponentWidgetIssue(entity);
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }

	@Override
	public CComponentWidgetEntity<CIssue> getSprintItemWidget(CIssue entity) {
		return buildDataProviderComponentWidget(entity);
	}
}
