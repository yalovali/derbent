package tech.derbent.app.tickets.ticket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.tickets.ticket.domain.CTicket;

public class CPageServiceTicket extends CPageServiceDynamicPage<CTicket> implements IPageServiceHasStatusAndWorkflow<CTicket> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceTicket.class);
	Long serialVersionUID = 1L;
	
	// Declare the field required by the interface
	private CProjectItemStatusService projectItemStatusService;

	public CPageServiceTicket(IPageServiceImplementer<CTicket> view) {
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
LOGGER.debug("Report action triggered for CTicket");
if (getView() instanceof CGridViewBaseDBEntity) {
@SuppressWarnings("unchecked")
final CGridViewBaseDBEntity<CTicket> gridView = (CGridViewBaseDBEntity<CTicket>) getView();
gridView.generateGridReport();
} else {
super.actionReport();
}
}

	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CTicket.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CTicket.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
	
	@Override
	public CProjectItemStatusService getProjectItemStatusService() {
		return projectItemStatusService;
	}
}