package tech.derbent.plm.validation.validationsuite.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.validation.validationsuite.domain.CValidationSuite;

public class CPageServiceValidationSuite extends CPageServiceDynamicPage<CValidationSuite> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceValidationSuite.class);
	Long serialVersionUID = 1L;

	public CPageServiceValidationSuite(IPageServiceImplementer<CValidationSuite> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CValidationSuite.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CValidationSuite.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
