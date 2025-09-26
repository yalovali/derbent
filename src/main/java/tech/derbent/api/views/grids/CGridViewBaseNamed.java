package tech.derbent.api.views.grids;

import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.services.CAbstractNamedEntityService;
import tech.derbent.api.views.CAbstractNamedEntityPage;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

public abstract class CGridViewBaseNamed<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractNamedEntityPage<EntityClass> {

	private static final long serialVersionUID = 1L;

	public CGridViewBaseNamed(final Class<EntityClass> entityClass, final CAbstractNamedEntityService<EntityClass> entityService,
			final CSessionService sessionService, final CDetailSectionService screenService) {
		super(entityClass, entityService, sessionService, screenService);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void createMasterComponent() {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
	}
}
