package tech.derbent.abstracts.views.grids;

import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

public abstract class CGridViewBaseProject<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {

	private static final long serialVersionUID = 1L;

	protected CGridViewBaseProject(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final CSessionService sessionService, final CDetailSectionService screenService) {
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
