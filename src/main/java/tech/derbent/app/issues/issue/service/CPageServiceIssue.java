package tech.derbent.app.issues.issue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.issues.issue.domain.CIssue;
import tech.derbent.app.issues.issue.view.CComponentWidgetIssue;

public class CPageServiceIssue extends CPageServiceDynamicPage<CIssue> 
		implements IPageServiceHasStatusAndWorkflow<CIssue>, IComponentWidgetEntityProvider<CIssue>, ISprintItemPageService<CIssue> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceIssue.class);
	Long serialVersionUID = 1L;

	// Declare the field required by the interface
	private CProjectItemStatusService projectItemStatusService;

	public CPageServiceIssue(IPageServiceImplementer<CIssue> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
/**
 * Handle report action - generates CSV report from grid data.
 * @throws Exception if report generation fails
 */
@Override
public void actionReport() throws Exception {
LOGGER.debug("Report action triggered for CIssue");
if (getView() instanceof CGridViewBaseDBEntity) {
@SuppressWarnings("unchecked")
final CGridViewBaseDBEntity<CIssue> gridView = (CGridViewBaseDBEntity<CIssue>) getView();
gridView.generateGridReport();
} else {
super.actionReport();
}
}

	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CIssue.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CIssue.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() {
		return projectItemStatusService;
	}

	@Override
	public CComponentWidgetEntity<CIssue> getComponentWidget(CIssue entity) {
		return new CComponentWidgetIssue(entity);
	}

	@Override
	public CComponentWidgetEntity<CIssue> getSprintItemWidget(CIssue entity) {
		return getComponentWidget(entity);
	}
}
