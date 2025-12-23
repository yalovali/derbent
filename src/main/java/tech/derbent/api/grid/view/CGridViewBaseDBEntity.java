package tech.derbent.api.grid.view;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entity.view.CAbstractEntityDBPage;
import tech.derbent.base.session.service.ISessionService;

public abstract class CGridViewBaseDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CAbstractEntityDBPage<EntityClass> {

	private static final long serialVersionUID = 1L;

	public CGridViewBaseDBEntity(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService,
			final ISessionService sessionService) {
		super(entityClass, entityService, sessionService);
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		/**/
	}

	@Override
	protected void createMasterComponent() {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
	}
}
