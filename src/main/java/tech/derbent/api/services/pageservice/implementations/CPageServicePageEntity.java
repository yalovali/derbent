package tech.derbent.api.services.pageservice.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.view.CDynamicPageBase;

public class CPageServicePageEntity extends CPageServiceDynamicPage<CPageEntity> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServicePageEntity.class);
	Long serialVersionUID = 1L;

	public CPageServicePageEntity(CDynamicPageBase view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CPageEntity.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(),
					CPageEntity.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
