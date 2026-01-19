package tech.derbent.plm.components.componentversiontype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.components.componentversiontype.domain.CProjectComponentVersionType;

public class CPageServiceComponentVersionType extends CPageServiceDynamicPage<CProjectComponentVersionType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceComponentVersionType.class);
	Long serialVersionUID = 1L;

	public CPageServiceComponentVersionType(IPageServiceImplementer<CProjectComponentVersionType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(),
					CProjectComponentVersionType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CProjectComponentVersionType.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
