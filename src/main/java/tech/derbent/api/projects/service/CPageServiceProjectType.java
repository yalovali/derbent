package tech.derbent.api.projects.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.api.projects.domain.CProjectType;

public class CPageServiceProjectType extends CPageServiceDynamicPage<CProjectType> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProjectType.class);
	Long serialVersionUID = 1L;

	public CPageServiceProjectType(IPageServiceImplementer<CProjectType> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProjectType.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CProjectType.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
