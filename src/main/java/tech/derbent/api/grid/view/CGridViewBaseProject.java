package tech.derbent.api.grid.view;

import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.entityOfProject.view.CProjectAwareMDPage;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.base.session.service.ISessionService;

public abstract class CGridViewBaseProject<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {

	private static final long serialVersionUID = 1L;

	protected CGridViewBaseProject(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final ISessionService sessionService, final CDetailSectionService screenService) throws Exception {
		super(entityClass, entityService, sessionService, screenService);
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		/***/
	}

	@Override
	protected void createMasterComponent() {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
	}
}
