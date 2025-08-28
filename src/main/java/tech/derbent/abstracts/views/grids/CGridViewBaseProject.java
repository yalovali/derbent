package tech.derbent.abstracts.views.grids;

import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

public abstract class CGridViewBaseProject<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {
	private static final long serialVersionUID = 1L;

	protected CGridViewBaseProject(final Class<EntityClass> entityClass, final CAbstractNamedEntityService<EntityClass> entityService,
			final CSessionService sessionService, final CScreenService screenService) {
		super(entityClass, entityService, sessionService, screenService);
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
