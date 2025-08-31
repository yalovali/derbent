package tech.derbent.abstracts.views.grids;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.IDisplayEntity;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.session.service.CSessionService;

public abstract class CGridViewBaseDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CAbstractEntityDBPage<EntityClass>
		implements IDisplayEntity {
	private static final long serialVersionUID = 1L;

	public CGridViewBaseDBEntity(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService,
			final CSessionService sessionService) {
		super(entityClass, entityService, sessionService);
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected void createMasterComponent() {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
	}
}
