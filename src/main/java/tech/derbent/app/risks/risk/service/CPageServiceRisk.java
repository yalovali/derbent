package tech.derbent.app.risks.risk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.risks.risk.domain.CRisk;

public class CPageServiceRisk extends CPageServiceDynamicPage<CRisk> implements IPageServiceHasStatusAndWorkflow<CRisk> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceRisk.class);
	Long serialVersionUID = 1L;

	private CProjectItemStatusService projectItemStatusService;

	public CPageServiceRisk(IPageServiceImplementer<CRisk> view) {
		super(view);
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	/**
	 * Handle report action - generates CSV report from grid data.
	 * @throws Exception if report generation fails
	 */
	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CRisk");
		if (getView() instanceof CGridViewBaseDBEntity) {
			@SuppressWarnings("unchecked")
			final CGridViewBaseDBEntity<CRisk> gridView = (CGridViewBaseDBEntity<CRisk>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CRisk.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CRisk.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() {
		return projectItemStatusService;
	}
}
