package tech.derbent.api.services.pageservice;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;

public abstract class CPageServiceDynamicPage<EntityClass extends CEntityDB<EntityClass>> extends CPageService<EntityClass> {

	public CPageServiceDynamicPage(final IPageServiceImplementer<EntityClass> view) {
		super(view);
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
