package tech.derbent.app.page.view;

import java.lang.reflect.Field;
import com.vaadin.flow.router.BeforeEnterEvent;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.services.pageservice.service.CPageServiceUtility;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.base.session.service.ISessionService;

@SuppressWarnings ("rawtypes")
public abstract class CDynamicPageBase extends CPageBaseProjectAware implements IPageServiceImplementer {

	private static final long serialVersionUID = 1L;
	protected Class<?> currentEntityType = null;
	protected String currentEntityViewName = null;
	protected Class<?> entityClass;
	protected CAbstractService<?> entityService;
	protected final CPageEntity pageEntity;
	protected final CPageService<?> pageService;

	public CDynamicPageBase(CPageEntity pageEntity, ISessionService sessionService, CDetailSectionService detailSectionService) throws Exception {
		super(sessionService, detailSectionService);
		Check.notNull(pageEntity, "Page entity cannot be null");
		this.pageEntity = pageEntity;
		pageService = getPageService();
		Check.notNull(pageService, "Page service cannot be null for dynamic page: " + pageEntity.getPageTitle());
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		LOGGER.debug("Before enter event for dynamic page: {}", pageEntity.getPageTitle());
		// Security check
		if (pageEntity.getRequiresAuthentication() && getSessionService().getActiveUser().isEmpty()) {
			event.rerouteToError(IllegalAccessException.class, "Authentication required");
			return;
		}
		// Check if page is active
		if (!pageEntity.getActive()) {
			event.rerouteToError(IllegalStateException.class, "Page not available");
			return;
		}
		if (pageService != null) {
			pageService.bind();
		}
	}

	/** Clear entity details and reset state. */
	public void clearEntityDetails() {
		if (baseDetailsLayout != null) {
			baseDetailsLayout.removeAll();
		}
		currentBinder = null;
		currentEntityViewName = null;
		currentEntityType = null;
	}

	@Override
	public Class<?> getEntityClass() { return entityClass; }

	@Override
	public CAbstractService getEntityService() { return entityService; }

	/** Get the page entity this view represents. */
	public CPageEntity getPageEntity() { return pageEntity; }

	public CPageService<?> getPageService() {
		// this creates a page service instance per page. this may be memory inefficient.
		try {
			Check.notNull(pageEntity, "Page entity cannot be null");
			Class<?> clazz = CPageServiceUtility.getPageServiceClassByName(pageEntity.getPageService());
			var constructor = clazz.getDeclaredConstructor(CDynamicPageBase.class);
			CPageService<?> page = (CPageService<?>) constructor.newInstance(this);
			Check.notNull(page, "Page service instance cannot be null for page: " + pageEntity.getPageTitle());
			return page;
		} catch (final Exception e) {
			LOGGER.error("Failed to get CPageService bean: {}", e.getMessage());
			return null;
		}
	}

	/** Implementation of IPageTitleProvider - provides the page title from the CPageEntity */
	@Override
	public String getPageTitle() { return pageEntity != null ? pageEntity.getPageTitle() : null; }

	/** Initialize the entity service based on the configured entity type. */
	protected void initializeEntityService() {
		try {
			LOGGER.debug("Initializing entity service for page: {}", pageEntity.getPageTitle());
			// Try to get the service bean from the configured grid entity
			final CGridEntity gridEntity = pageEntity.getGridEntity();
			Check.notNull(gridEntity, "Grid entity cannot be null");
			Check.notBlank(gridEntity.getDataServiceBeanName(), "Data service bean name cannot be blank");
			// Get the service bean from the application context
			entityService = CSpringContext.<CAbstractService<?>>getBean(gridEntity.getDataServiceBeanName());
			// Get the entity class from the detail section
			final CDetailSection detailSection = pageEntity.getDetailSection();
			Check.notNull(detailSection, "Detail section cannot be null");
			Check.notBlank(detailSection.getEntityType(), "Entity type cannot be blank");
			entityClass = CAuxillaries.getEntityClass(detailSection.getEntityType());
			Check.notNull(entityClass, "Entity class not found for type: " + detailSection.getEntityType());
			Check.isTrue(CEntityDB.class.isAssignableFrom(entityClass), "Entity class does not extend CEntityDB: " + entityClass);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize entity service for entity type: {}", e.getMessage());
			throw e;
		}
	}

	protected void initializePage() throws Exception {
		// TODO Auto-generated method stub
	}

	protected abstract void locateItemById(Long pageItemId);

	@SuppressWarnings ({
			"unchecked"
	})
	protected void rebuildEntityDetails(final Class<?> clazz) throws Exception {
		try {
			final Field viewNameField = clazz.getField("VIEW_NAME");
			final String entityViewName = (String) viewNameField.get(null);
			LOGGER.debug("Rebuilding entity details for view: {}", entityViewName);
			clearEntityDetails();
			currentEntityViewName = entityViewName;
			buildScreen(entityViewName, (Class) entityClass, baseDetailsLayout);
		} catch (final Exception e) {
			LOGGER.error("Error rebuilding entity details for view '{}': {}", clazz.getField("VIEW_NAME"), e.getMessage());
			throw e;
		}
	}

	/** Select the first item in grid. Subclasses with grids should override this. */
	@Override
	public void selectFirstInGrid() {
		// Default implementation - subclasses with grids should override
		LOGGER.debug("selectFirstInGrid called on base class (no-op)");
	}

	@Override
	public void setCurrentEntity(final CEntityDB<?> entity) {
		super.setCurrentEntity(entity);
		if (entity == null) {
			currentEntityType = null;
		} else {
			currentEntityType = entity.getClass();
		}
	}
}
