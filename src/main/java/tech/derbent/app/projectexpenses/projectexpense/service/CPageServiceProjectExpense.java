package tech.derbent.app.projectexpenses.projectexpense.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projectexpenses.projectexpense.domain.CProjectExpense;

public class CPageServiceProjectExpense extends CPageServiceDynamicPage<CProjectExpense> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectExpense.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectExpense(IPageServiceImplementer<CProjectExpense> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProjectExpense.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CProjectExpense.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
