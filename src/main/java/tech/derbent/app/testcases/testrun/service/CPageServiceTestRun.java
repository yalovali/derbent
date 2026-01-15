package tech.derbent.app.testcases.testrun.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testrun.domain.CTestRun;

public class CPageServiceTestRun extends CPageServiceDynamicPage<CTestRun> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceTestRun.class);
	Long serialVersionUID = 1L;

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
}
