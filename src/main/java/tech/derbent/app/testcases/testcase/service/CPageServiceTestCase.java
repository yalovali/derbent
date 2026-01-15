package tech.derbent.app.testcases.testcase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.grid.widget.IComponentWidgetEntityProvider;
import tech.derbent.api.interfaces.ISprintItemPageService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testcase.domain.CTestCase;

public class CPageServiceTestCase extends CPageServiceDynamicPage<CTestCase>
		implements IPageServiceHasStatusAndWorkflow<CTestCase>, IComponentWidgetEntityProvider<CTestCase> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceTestCase.class);
	Long serialVersionUID = 1L;

	private CProjectItemStatusService projectItemStatusService;

	public CPageServiceTestCase(IPageServiceImplementer<CTestCase> view) {
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
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CTestCase.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CTestCase.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() {
		return projectItemStatusService;
	}

	@Override
	public CComponentWidgetEntity<CTestCase> getComponentWidget(CTestCase entity) {
		return new CComponentWidgetEntity<>(entity);
	}
}
