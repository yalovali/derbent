package tech.derbent.app.projectincomes.projectincometype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projectincomes.projectincometype.domain.CProjectIncomeType;

public class CPageServiceProjectIncomeType extends CPageServiceDynamicPage<CProjectIncomeType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectIncomeType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectIncomeType(IPageServiceImplementer<CProjectIncomeType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProjectIncomeType.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CProjectIncomeType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
