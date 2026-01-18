package tech.derbent.app.validation.validationcasetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.validation.validationcasetype.domain.CValidationCaseType;

public class CPageServiceValidationCaseType extends CPageServiceDynamicPage<CValidationCaseType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceValidationCaseType.class);
	Long serialVersionUID = 1L;

	public CPageServiceValidationCaseType(IPageServiceImplementer<CValidationCaseType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CValidationCaseType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CValidationCaseType.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
