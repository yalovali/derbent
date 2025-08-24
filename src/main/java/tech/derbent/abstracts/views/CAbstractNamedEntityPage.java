package tech.derbent.abstracts.views;

import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.interfaces.CLayoutChangeListener;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.abstracts.services.CDetailsBuilder;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

public abstract class CAbstractNamedEntityPage<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractEntityDBPage<EntityClass>
		implements CLayoutChangeListener {
	private static final long serialVersionUID = 1L;
	protected final CDetailsBuilder detailsBuilder = new CDetailsBuilder();
	protected final CScreenService screenService;

	protected CAbstractNamedEntityPage(final Class<EntityClass> entityClass, final CAbstractNamedEntityService<EntityClass> entityService,
			final CSessionService sessionService, final CScreenService screenService) {
		super(entityClass, entityService, sessionService);
		this.screenService = screenService;
	}

	protected void buildScreen(final String baseViewName) {
		try {
			final CScreen screen = screenService.findByNameAndProject(sessionService.getActiveProject().orElse(null), baseViewName);
			if (screen == null) {
				final String errorMsg = "Screen not found: " + baseViewName + " for project: "
						+ sessionService.getActiveProject().map(CProject::getName).orElse("No Project");
				getBaseDetailsLayout().add(new CDiv(errorMsg));
				return;
			}
			detailsBuilder.buildDetails(screen, getBinder(), getBaseDetailsLayout());
		} catch (final Exception e) {
			final String errorMsg = "Error building details layout for screen: " + baseViewName;
			e.printStackTrace();
			getBaseDetailsLayout().add(new CDiv(errorMsg));
		}
	}

	@Override
	protected EntityClass createNewEntity() {
		final String name = "New Item";
		return ((CAbstractNamedEntityService<EntityClass>) entityService).newEntity(name);
	}
}
