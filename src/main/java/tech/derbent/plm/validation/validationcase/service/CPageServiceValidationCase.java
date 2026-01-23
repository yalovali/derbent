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
import tech.derbent.api.utils.Check;
import tech.derbent.plm.validation.validationcase.domain.CValidationCase;

public class CPageServiceValidationCase extends CPageServiceDynamicPage<CValidationCase>
		implements IPageServiceHasStatusAndWorkflow<CValidationCase>, IComponentWidgetEntityProvider<CValidationCase> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceValidationCase.class);
	Long serialVersionUID = 1L;

	private CProjectItemStatusService projectItemStatusService;

	public CPageServiceValidationCase(IPageServiceImplementer<CValidationCase> view) {
		super(view);
		try {
			projectItemStatusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CValidationCase.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CValidationCase.class.getSimpleName(), e.getMessage());
			throw e;
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
		return projectItemStatusService;
	}

	@Override
	public CComponentWidgetEntity<CValidationCase> getComponentWidget(CValidationCase entity) {
		return new CComponentWidgetEntity<>(entity);
	}
}
