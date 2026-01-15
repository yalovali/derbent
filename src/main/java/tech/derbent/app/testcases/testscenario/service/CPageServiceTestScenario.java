package tech.derbent.app.testcases.testscenario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;

public class CPageServiceTestScenario extends CPageServiceDynamicPage<CTestScenario> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceTestScenario.class);
	Long serialVersionUID = 1L;

	public CPageServiceTestScenario(IPageServiceImplementer<CTestScenario> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CTestScenario.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CTestScenario.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
