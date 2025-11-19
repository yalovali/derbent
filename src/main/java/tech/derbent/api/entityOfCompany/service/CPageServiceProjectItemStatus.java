package tech.derbent.api.entityOfCompany.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;

public class CPageServiceProjectItemStatus extends CPageServiceDynamicPage<CProjectItemStatus> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectItemStatus.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectItemStatus(final IPageServiceImplementer<CProjectItemStatus> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProjectItemStatus.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CProjectItemStatus.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
