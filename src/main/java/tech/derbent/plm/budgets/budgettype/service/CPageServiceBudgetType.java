package tech.derbent.plm.budgets.budgettype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.budgets.budgettype.domain.CBudgetType;

public class CPageServiceBudgetType extends CPageServiceDynamicPage<CBudgetType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceBudgetType.class);
	Long serialVersionUID = 1L;

	public CPageServiceBudgetType(IPageServiceImplementer<CBudgetType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CBudgetType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CBudgetType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
