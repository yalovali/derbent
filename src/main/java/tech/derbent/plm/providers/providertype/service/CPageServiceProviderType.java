package tech.derbent.plm.providers.providertype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.providers.providertype.domain.CProviderType;

public class CPageServiceProviderType extends CPageServiceDynamicPage<CProviderType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProviderType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProviderType(IPageServiceImplementer<CProviderType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProviderType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CProviderType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
