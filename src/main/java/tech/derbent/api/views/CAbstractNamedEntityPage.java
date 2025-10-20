package tech.derbent.api.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.ILayoutChangeListener;
import tech.derbent.api.services.CEntityNamedService;
import tech.derbent.api.services.CDetailsBuilder;
import tech.derbent.api.utils.Check;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.base.session.service.ISessionService;

public abstract class CAbstractNamedEntityPage<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractEntityDBPage<EntityClass>
		implements ILayoutChangeListener {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CAbstractNamedEntityPage.class);
	private static final long serialVersionUID = 1L;
	protected final CDetailsBuilder detailsBuilder = new CDetailsBuilder();
	protected final CDetailSectionService screenService;

	protected CAbstractNamedEntityPage(final Class<EntityClass> entityClass, final CEntityNamedService<EntityClass> entityService,
			final ISessionService sessionService, final CDetailSectionService screenService) {
		super(entityClass, entityService, sessionService);
		this.screenService = screenService;
	}

	protected void buildScreen(final String baseViewName) {
		try {
			final CDetailSection screen = screenService.findByNameAndProject(sessionService.getActiveProject().orElse(null), baseViewName);
			Check.notNull(screenService, "Screen service cannot be null");
			detailsBuilder.buildDetails(this, screen, getBinder(), getBaseDetailsLayout());
		} catch (final Exception e) {
			final String errorMsg = "Error building details layout for screen: " + baseViewName;
			LOGGER.error("Error building details layout for screen '{}': {}", baseViewName, e.getMessage());
			throw new RuntimeException(errorMsg, e);
		}
	}

	@Override
	protected EntityClass createNewEntity() {
		final String name = "New Item";
		return ((CEntityNamedService<EntityClass>) entityService).newEntity(name);
	}

	@Override
	public String getPageTitle() { return entityClass.getSimpleName() + " Management"; }

	@Override
	protected boolean onBeforeSaveEvent() {
		if (super.onBeforeSaveEvent() == false) {
			return false; // If the base class validation fails, do not proceed
		}
		return true; // Default implementation allows save
	}
}
