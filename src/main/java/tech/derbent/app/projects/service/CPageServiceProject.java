package tech.derbent.app.projects.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;

public class CPageServiceProject extends CPageServiceDynamicPage<CProject> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceProject.class);
	Long serialVersionUID = 1L;

	public CPageServiceProject(IPageServiceImplementer<CProject> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CProject.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CProject.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}
}
