package tech.derbent.app.providers.provider.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.providers.provider.domain.CProvider;

public class CPageServiceProvider extends CPageServiceDynamicPage<CProvider> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProvider.class);
	Long serialVersionUID = 1L;

	public CPageServiceProvider(IPageServiceImplementer<CProvider> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProvider.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CProvider.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
