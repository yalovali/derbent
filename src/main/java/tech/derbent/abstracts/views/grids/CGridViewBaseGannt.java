package tech.derbent.abstracts.views.grids;

import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.gannt.view.CMasterViewSectionGannt;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/* display a Gannt chart for any entity of project type */
public abstract class CGridViewBaseGannt<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {

	private static final long serialVersionUID = 1L;

	protected CGridViewBaseGannt(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final CSessionService sessionService, final CScreenService screenService) {
		super(entityClass, entityService, sessionService, screenService);
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// TODO Auto-generated method stub
	}

	// override this to create a Gannt chart
	@Override
	protected void createMasterComponent() {
		masterViewSection = new CMasterViewSectionGannt<EntityClass>(entityClass, this);
	}
}
