package tech.derbent.abstracts.views;

import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.interfaces.CLayoutChangeListener;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.abstracts.services.CDetailsBuilder;
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

	@Override
	protected EntityClass createNewEntity() {
		final String name = "New Item";
		return ((CAbstractNamedEntityService<EntityClass>) entityService).newEntity(name);
	}
}
