package tech.derbent.api.entity.service;

import tech.derbent.api.utils.Check;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

public class CPageServiceEntityDB<EntityClass extends CEntityDB<EntityClass>> extends CPageServiceDynamicPage<EntityClass> {

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceEntityDB.class);
	Long serialVersionUID = 1L;

	public CPageServiceEntityDB(IPageServiceImplementer<EntityClass> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity: {}", this.getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for EntityClass");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<EntityClass> gridView = (CGridViewBaseDBEntity<EntityClass>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

}
