package tech.derbent.app.risks.risk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.risks.risk.domain.CRisk;

public class CPageServiceRisk extends CPageServiceDynamicPage<CRisk> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceRisk.class);
	Long serialVersionUID = 1L;

	public CPageServiceRisk(IPageServiceImplementer<CRisk> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CRisk.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CRisk.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
