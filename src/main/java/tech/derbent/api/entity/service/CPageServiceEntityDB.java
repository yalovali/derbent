package tech.derbent.api.entity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;

public class CPageServiceEntityDB<EntityClass extends CEntityDB<EntityClass>> extends CPageServiceDynamicPage<EntityClass> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceEntityDB.class);
	Long serialVersionUID = 1L;

	public CPageServiceEntityDB(IPageServiceImplementer<EntityClass> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
		} catch (Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity: {}", this.getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}
}
