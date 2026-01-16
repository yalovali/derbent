package tech.derbent.app.testcases.testrun.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Component;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
import tech.derbent.app.testcases.testrun.view.CComponentTestExecution;
import tech.derbent.base.session.service.ISessionService;

public class CPageServiceTestRun extends CPageServiceDynamicPage<CTestRun> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceTestRun.class);
	Long serialVersionUID = 1L;

	@Autowired
	private CTestRunService testRunService;

	public CPageServiceTestRun(IPageServiceImplementer<CTestRun> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CTestRun.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CTestRun.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	/** Creates test execution component for running tests.
	 * @return test execution component instance */
	public Component createTestExecutionComponent() {
		try {
			LOGGER.debug("Creating test execution component");
			Check.notNull(testRunService, "TestRunService must be injected");
			final CComponentTestExecution component = new CComponentTestExecution(testRunService);
			component.registerWithPageService(this);
			LOGGER.debug("Test execution component created and registered");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create test execution component: {}", e.getMessage(), e);
			throw e;
		}
	}
}
