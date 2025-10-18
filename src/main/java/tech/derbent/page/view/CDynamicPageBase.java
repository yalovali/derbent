package tech.derbent.page.view;

import java.lang.reflect.Field;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.interfaces.IPageTitleProvider;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.ISessionService;

public abstract class CDynamicPageBase extends CPageBaseProjectAware implements BeforeEnterObserver, IEntityUpdateListener, IPageTitleProvider {

	private static final long serialVersionUID = 1L;
	protected final ApplicationContext applicationContext;
	protected Class<?> currentEntityType = null;
	protected String currentEntityViewName = null;
	protected Class<?> entityClass;
	// Services for dynamic entity management
	protected CAbstractService<?> entityService;
	protected final CPageEntity pageEntity;

	public CDynamicPageBase(CPageEntity pageEntity, ISessionService sessionService, CDetailSectionService detailSectionService,
			ApplicationContext applicationContext) {
		super(sessionService, detailSectionService);
		this.applicationContext = applicationContext;
		this.pageEntity = pageEntity;
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		LOGGER.debug("Before enter event for dynamic page: {}", pageEntity.getPageTitle());
		// Security check
		if (pageEntity.getRequiresAuthentication() && sessionService.getActiveUser().isEmpty()) {
			event.rerouteToError(IllegalAccessException.class, "Authentication required");
			return;
		}
		// Check if page is active
		if (!pageEntity.getActive()) {
			event.rerouteToError(IllegalStateException.class, "Page not available");
			return;
		}
	}

	/** Clear entity details and reset state. */
	protected void clearEntityDetails() {
		if (baseDetailsLayout != null) {
			baseDetailsLayout.removeAll();
		}
		currentBinder = null;
		currentEntityViewName = null;
		currentEntityType = null;
	}

	/** Get the page entity this view represents. */
	public CPageEntity getPageEntity() { return pageEntity; }

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
			final Object serviceBean = applicationContext.getBean(gridEntity.getDataServiceBeanName());
			Check.notNull(serviceBean, "Service bean not found: " + gridEntity.getDataServiceBeanName());
			Check.instanceOf(serviceBean, CAbstractService.class, "Service bean is not an instance of CAbstractService: " + serviceBean.getClass());
			entityService = (CAbstractService<?>) serviceBean;
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

	@SuppressWarnings ({
			"unchecked", "rawtypes"
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

	@Override
	public void setCurrentEntity(final Object entity) {
		super.setCurrentEntity(entity);
		if (entity == null) {
			currentEntityType = null;
		} else {
			currentEntityType = entity.getClass();
		}
	}
}
