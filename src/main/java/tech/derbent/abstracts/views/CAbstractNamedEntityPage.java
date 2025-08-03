package tech.derbent.abstracts.views;

import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.interfaces.CLayoutChangeListener;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.session.service.CSessionService;

public abstract class CAbstractNamedEntityPage<EntityClass extends CEntityNamed<EntityClass>>
        extends CAbstractEntityDBPage<EntityClass> implements CLayoutChangeListener {

    private static final long serialVersionUID = 1L;

    protected CAbstractNamedEntityPage(final Class<EntityClass> entityClass,
            final CAbstractNamedEntityService<EntityClass> entityService, final CSessionService sessionService) {
        super(entityClass, entityService, sessionService);
    }

    @Override
    protected EntityClass createNewEntity() {
        final String name = "New Item";
        return ((CAbstractNamedEntityService<EntityClass>) entityService).newEntity(name);
    }
}
