package tech.derbent.plm.validation.validationcase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.validation.validationcase.domain.CValidationCase;

public class CPageServiceValidationCase extends CPageServiceDynamicPage<CValidationCase>
		implements IPageServiceHasStatusAndWorkflow<CValidationCase>, IComponentWidgetEntityProvider<CValidationCase> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceValidationCase.class);
	Long serialVersionUID = 1L;

	private CProjectItemStatusService statusService;

	public CPageServiceValidationCase(IPageServiceImplementer<CValidationCase> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CValidationCase");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CValidationCase> gridView = (CGridViewBaseDBEntity<CValidationCase>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}


	@Override
	public CProjectItemStatusService getProjectItemStatusService() {
		return statusService;
	}

	@Override
	public CComponentWidgetEntity<CValidationCase> getComponentWidget(CValidationCase entity) {
		return new CComponentWidgetEntity<>(entity);
	}
}
