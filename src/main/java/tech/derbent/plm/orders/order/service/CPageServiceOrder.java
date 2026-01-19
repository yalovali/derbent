package tech.derbent.plm.orders.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.orders.order.domain.COrder;

public class CPageServiceOrder extends CPageServiceDynamicPage<COrder> implements IPageServiceHasStatusAndWorkflow<COrder> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceOrder.class);
	// Declare the field required by the interface
	private CProjectItemStatusService projectItemStatusService;
	Long serialVersionUID = 1L;

	public CPageServiceOrder(IPageServiceImplementer<COrder> view) {
		super(view);
		// Initialize the service from Spring context
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	/** Handle report action - generates CSV report from grid data.
	 * @throws Exception if report generation fails */
	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for COrder");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<COrder> gridView = (CGridViewBaseDBEntity<COrder>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), COrder.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), COrder.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return projectItemStatusService; }
}
