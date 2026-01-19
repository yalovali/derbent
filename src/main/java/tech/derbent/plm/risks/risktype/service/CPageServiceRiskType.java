package tech.derbent.plm.risks.risktype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.risks.risktype.domain.CRiskType;

public class CPageServiceRiskType extends CPageServiceDynamicPage<CRiskType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceRiskType.class);
	Long serialVersionUID = 1L;

	public CPageServiceRiskType(IPageServiceImplementer<CRiskType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CRiskType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CRiskType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
