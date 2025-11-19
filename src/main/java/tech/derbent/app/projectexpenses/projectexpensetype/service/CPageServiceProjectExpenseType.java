package tech.derbent.app.projectexpenses.projectexpensetype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projectexpenses.projectexpensetype.domain.CProjectExpenseType;

public class CPageServiceProjectExpenseType extends CPageServiceDynamicPage<CProjectExpenseType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectExpenseType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectExpenseType(IPageServiceImplementer<CProjectExpenseType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProjectExpenseType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CProjectExpenseType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
