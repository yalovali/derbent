package tech.derbent.app.budgets.budget.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.budgets.budget.domain.CBudget;

public class CPageServiceBudget extends CPageServiceDynamicPage<CBudget> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceBudget.class);
	Long serialVersionUID = 1L;

	public CPageServiceBudget(IPageServiceImplementer<CBudget> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CBudget.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CBudget.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
