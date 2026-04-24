package tech.derbent.plm.risks.risk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.parentrelation.service.CHierarchyPageSupport;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.risks.risk.domain.CRisk;

public class CPageServiceRisk extends CPageServiceDynamicPage<CRisk> implements IPageServiceHasStatusAndWorkflow<CRisk> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceRisk.class);
	private CProjectItemStatusService statusService;
	Long serialVersionUID = 1L;

	public CPageServiceRisk(IPageServiceImplementer<CRisk> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated reason={}", e.getMessage());
		}
	}

	/** Handle report action - generates CSV report from grid data.
	 * @throws Exception if report generation fails */
	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CRisk");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CRisk> gridView = (CGridViewBaseDBEntity<CRisk>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	public Component createComponentParent() {
		// Reuse the shared hierarchy selector so requirement-style levels work for every project item page.
		return CHierarchyPageSupport.createParentComponent();
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }
}
