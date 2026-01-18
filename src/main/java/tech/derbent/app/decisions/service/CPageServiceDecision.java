package tech.derbent.app.decisions.service;

import tech.derbent.api.utils.Check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.app.decisions.domain.CDecision;

public class CPageServiceDecision extends CPageServiceDynamicPage<CDecision> implements IPageServiceHasStatusAndWorkflow<CDecision> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceDecision.class);
	// Declare the field required by the interface
	private CProjectItemStatusService projectItemStatusService;
	Long serialVersionUID = 1L;

	public CPageServiceDecision(IPageServiceImplementer<CDecision> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
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
LOGGER.debug("Report action triggered for CDecision");
if (getView() instanceof CGridViewBaseDBEntity) {
@SuppressWarnings("unchecked")
final CGridViewBaseDBEntity<CDecision> gridView = (CGridViewBaseDBEntity<CDecision>) getView();
gridView.generateGridReport();
} else {
super.actionReport();
}
}

	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CDecision.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CDecision.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }
}
