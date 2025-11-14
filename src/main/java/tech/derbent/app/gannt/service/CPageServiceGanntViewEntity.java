package tech.derbent.app.gannt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.gannt.domain.CGanntViewEntity;

public class CPageServiceGanntViewEntity extends CPageServiceDynamicPage<CGanntViewEntity> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceGanntViewEntity.class);
	Long serialVersionUID = 1L;

	public CPageServiceGanntViewEntity(IPageServiceImplementer<CGanntViewEntity> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CGanntViewEntity.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CGanntViewEntity.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
