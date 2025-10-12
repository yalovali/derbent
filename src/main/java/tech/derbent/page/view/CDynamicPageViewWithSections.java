package tech.derbent.page.view;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.interfaces.IPageTitleProvider;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentDetailsMasterToolbar;
import tech.derbent.api.views.components.CCrudToolbar;
import tech.derbent.api.views.components.CFlexLayout;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.view.CComponentGridEntity;
import tech.derbent.session.service.ISessionService;
import tech.derbent.users.domain.CUser;

/** Enhanced dynamic page view that supports grid and detail sections for database-defined pages. This view displays content stored in CPageEntity
 * instances with configurable grid and detail sections. */
@PermitAll
public class CDynamicPageViewWithSections extends CPageBaseProjectAware implements BeforeEnterObserver, IEntityUpdateListener, IPageTitleProvider {

	public static final String DEFAULT_COLOR = "#341b00";
	public static final String DEFAULT_ICON = "vaadin:database";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageViewWithSections.class);
	private static final long serialVersionUID = 1L;
	protected final ApplicationContext applicationContext;
	private CCrudToolbar<?> crudToolbar;
	private Class<?> currentEntityType = null;
	// State tracking for performance optimization
	private String currentEntityViewName = null;
	protected Class<?> entityClass;
	// Services for dynamic entity management
	protected CAbstractService<?> entityService;
	protected CComponentGridEntity grid;
	private final CPageEntity pageEntity;
	private final CVerticalLayout splitBottomLayout = new CVerticalLayout(false, false, false);;
	// Layout components
	protected final SplitLayout splitLayout = new SplitLayout();

	@Autowired
	public CDynamicPageViewWithSections(final CPageEntity pageEntity, final ISessionService sessionService,
			final CDetailSectionService detailSectionService, final CGridEntityService gridEntityService,
			final ApplicationContext applicationContext) {
		super(sessionService, detailSectionService);
		this.pageEntity = pageEntity;
		this.applicationContext = applicationContext;
		initializePage();
		LOGGER.debug("Creating dynamic page view with sections for: {}", pageEntity.getPageTitle());
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// Security check
		if (pageEntity.getRequiresAuthentication() && sessionService.getActiveUser().isEmpty()) {
			event.rerouteToError(IllegalAccessException.class, "Authentication required");
			return;
		}
		// Check if page is active
		if (!pageEntity.getIsActive()) {
			event.rerouteToError(IllegalStateException.class, "Page not available");
			return;
		}
		LOGGER.debug("User accessing page: {}", pageEntity.getPageTitle());
	}

	/** Clear entity details and reset state. */
	private void clearEntityDetails() {
		if (baseDetailsLayout != null) {
			baseDetailsLayout.removeAll();
		}
		currentBinder = null;
		currentEntityViewName = null;
		currentEntityType = null;
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected CCrudToolbar<?> createCrudToolbar() {
		LOGGER.debug("Creating CRUD toolbar for entity type: {}", entityClass != null ? entityClass.getSimpleName() : "null");
		// Use static factory method to create toolbar
		CCrudToolbar toolbar = new CCrudToolbar(entityService, entityClass);
		toolbar.setCurrentEntity(null);
		toolbar.setNewEntitySupplier(() -> createNewEntityInstance());
		toolbar.setRefreshCallback((currentEntity) -> {
			refreshGrid();
			if (currentEntity != null && ((CEntityDB<?>) currentEntity).getId() != null) {
				try {
					CEntityDB<?> reloadedEntity = entityService.getById(((CEntityDB<?>) currentEntity).getId()).orElse(null);
					setCurrentEntity(reloadedEntity);
					populateForm();
				} catch (Exception e) {
					LOGGER.error("Error reloading entity: {}", e.getMessage());
				}
			}
		});
		toolbar.addUpdateListener(this);
		return toolbar;
	}

	/** Create the details section. */
	private void createDetailsSection() {
		splitLayout.addToSecondary(splitBottomLayout);
		final Scroller detailsScroller = new Scroller();
		splitBottomLayout.add(detailsScroller);
		detailsScroller.setContent(baseDetailsLayout);
		baseDetailsLayout.setSizeFull();
		detailsScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		detailsScroller.setSizeFull();
	}

	/** Create the master (grid) section. */
	private void createMasterSection() {
		LOGGER.debug("Creating master section with grid entity");
		Check.notNull(pageEntity.getGridEntity(), "Grid entity cannot be null");
		// Create the grid component using the configured grid entity
		grid = new CComponentGridEntity(pageEntity.getGridEntity());
		// Listen for selection changes from the grid
		grid.addSelectionChangeListener(event -> {
			try {
				onEntitySelected(event);
			} catch (Exception e) {
				LOGGER.error("Error handling entity selection", e);
			}
		});
		// Create grid layout with toolbar
		CVerticalLayout gridLayout = new CVerticalLayout();
		gridLayout.setSizeFull();
		gridLayout.add(new CComponentDetailsMasterToolbar(grid));
		gridLayout.add(grid);
		// Allow the grid layout to expand
		gridLayout.getStyle().remove("max-height");
		// Add grid to the primary (top) section
		splitLayout.addToPrimary(gridLayout);
	}

	/** Creates a new entity instance of the current entity type.
	 * @return a new entity instance */
	@Override
	@SuppressWarnings ("unchecked")
	protected <T extends CEntityDB<T>> T createNewEntity() {
		if (entityClass == null) {
			throw new IllegalStateException("Entity class not initialized");
		}
		try {
			// Create new instance using reflection
			T newEntity = (T) entityClass.getDeclaredConstructor().newInstance();
			// Set project if the entity supports it (check for CEntityOfProject)
			if (newEntity instanceof CEntityOfProject) {
				((CEntityOfProject<?>) newEntity).setProject(sessionService.getActiveProject().orElse(null));
			}
			// Special handling for CUser entities - create project association through CUserProjectSettings
			else if (newEntity instanceof CUser) {
				CProject activeProject = sessionService.getActiveProject().orElse(null);
				if (activeProject != null) {
					CUser user = (CUser) newEntity;
					// Initialize project settings list to establish project context for display
					if (user.getProjectSettings() == null) {
						user.setProjectSettings(new java.util.ArrayList<>());
					}
					// Note: The actual CUserProjectSettings creation will be handled when the user is saved
					// This just ensures the user has the project context for dynamic page display
					LOGGER.debug("CUser entity created in context of project: {}", activeProject.getName());
				}
			}
			return newEntity;
		} catch (Exception e) {
			LOGGER.error("Error creating new entity instance for type: {}", entityClass.getSimpleName(), e);
			throw new RuntimeException("Failed to create new entity instance", e);
		}
	}

	protected CEntityDB<?> createNewEntityInstance() {
		return createNewEntity();
	}

	@Override
	public CFlexLayout getBaseDetailsLayout() { return baseDetailsLayout; }

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
			CGridEntity gridEntity = pageEntity.getGridEntity();
			Check.notNull(gridEntity, "Grid entity cannot be null");
			Check.notBlank(gridEntity.getDataServiceBeanName(), "Data service bean name cannot be blank");
			// Get the service bean from the application context
			Object serviceBean = applicationContext.getBean(gridEntity.getDataServiceBeanName());
			Check.notNull(serviceBean, "Service bean not found: " + gridEntity.getDataServiceBeanName());
			Check.instanceOf(serviceBean, CAbstractService.class, "Service bean is not an instance of CAbstractService: " + serviceBean.getClass());
			entityService = (CAbstractService<?>) serviceBean;
			// Get the entity class from the detail section
			CDetailSection detailSection = pageEntity.getDetailSection();
			Check.notNull(detailSection, "Detail section cannot be null");
			Check.notBlank(detailSection.getEntityType(), "Entity type cannot be blank");
			entityClass = CAuxillaries.getEntityClass(detailSection.getEntityType());
			Check.notNull(entityClass, "Entity class not found for type: " + detailSection.getEntityType());
			Check.isTrue(CEntityDB.class.isAssignableFrom(entityClass), "Entity class does not extend CEntityDB: " + entityClass);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize entity service for entity type", e);
			throw new RuntimeException("Failed to initialize entity service", e);
		}
	}

	/** Initialize the page layout and content. */
	protected void initializePage() {
		try {
			LOGGER.debug("Initializing dynamic page view with sections for: {}", pageEntity.getPageTitle());
			setSizeFull();
			if (pageEntity.getPageTitle() != null && !pageEntity.getPageTitle().trim().isEmpty()) {
				getElement().executeJs("document.title = $0", pageEntity.getPageTitle());
			}
			initializeEntityService();
			splitLayout.setSizeFull();
			splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
			splitLayout.setSplitterPosition(30.0); // 50% for grid, 50% for details
			add(splitLayout);
			createMasterSection();
			createDetailsSection();
			crudToolbar = createCrudToolbar();
			splitBottomLayout.addComponentAsFirst(crudToolbar);
			grid.selectNextItem();
		} catch (Exception e) {
			LOGGER.error("Error initializing dynamic page view", e);
			throw e;
		}
	}

	// Implementation of CEntityUpdateListener
	@Override
	public void onEntityDeleted(CEntityDB<?> entity) {
		try {
			LOGGER.debug("Entity deleted notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(grid, "Grid component is not initialized");
			// Refresh grid and clear details
			refreshGrid();
			clearEntityDetails();
			grid.selectNextItem();
		} catch (Exception e) {
			LOGGER.error("Error handling entity deleted notification", e);
			throw e;
		}
	}

	@Override
	public void onEntitySaved(CEntityDB<?> entity) {
		try {
			LOGGER.debug("Entity saved notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(grid, "Grid component is not initialized");
			Check.notNull(entity, "Saved entity cannot be null");
			refreshGrid();
			grid.selectEntity(entity);
		} catch (Exception e) {
			LOGGER.error("Error handling entity saved notification", e);
			throw e;
		}
	}

	@Override
	public void onEntityCreated(CEntityDB<?> entity) {
		try {
			LOGGER.debug("Entity created notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(grid, "Grid component is not initialized");
			Check.notNull(entity, "Created entity cannot be null");
			refreshGrid();
			grid.selectEntity(entity);
		} catch (Exception e) {
			LOGGER.error("Error handling entity created notification", e);
			throw e;
		}
	}

	/** Handle entity selection events from the grid. */
	private void onEntitySelected(CComponentGridEntity.SelectionChangeEvent event) throws Exception {
		try {
			LOGGER.debug("Handling entity selection event");
			Check.notNull(event, "Selection change event cannot be null");
			CEntityDB<?> selectedEntity = event.getSelectedItem();
			if (selectedEntity == null) {
				// No selection - clear details
				clearEntityDetails();
				setCurrentEntity(null);
				populateForm();
			} else {
				LOGGER.debug("Entity selected: {}", selectedEntity != null ? selectedEntity.toString() + " ID: " + selectedEntity.getId() : "null");
				// Performance optimization: check if we can reuse existing components
				if (currentEntityViewName == null || !selectedEntity.getClass().getField("VIEW_NAME").equals(currentEntityViewName)) {
					rebuildEntityDetails(selectedEntity.getClass());
				}
				setCurrentEntity(selectedEntity);
				populateForm();
			}
		} catch (Exception e) {
			LOGGER.error("Error handling entity selection", e);
			throw e;
		}
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void rebuildEntityDetails(Class<?> clazz) throws Exception {
		try {
			Field viewNameField = clazz.getField("VIEW_NAME");
			String entityViewName = (String) viewNameField.get(null);
			LOGGER.debug("Rebuilding entity details for view: {}", entityViewName);
			clearEntityDetails();
			currentEntityViewName = entityViewName;
			buildScreen(entityViewName, (Class) entityClass, baseDetailsLayout);
		} catch (Exception e) {
			LOGGER.error("Error rebuilding entity details for view '{}': {}", clazz.getField("VIEW_NAME"), e.getMessage());
			throw e;
		}
	}

	/** Refresh the grid to show updated data. */
	private void refreshGrid() {
		grid.refreshGridData();
	}

	/** Reloads entity values into existing components without rebuilding the UI */
	@Override
	public void setCurrentEntity(Object entity) {
		try {
			super.setCurrentEntity(entity);
			if (entity == null) {
				currentEntityType = null;
				if (crudToolbar != null) {
					crudToolbar.setCurrentEntity(null);
				}
			} else {
				currentEntityType = entity.getClass();
				crudToolbar.setCurrentEntity(entity);
			}
		} catch (Exception e) {
			LOGGER.error("Error setting current entity in toolbar", e);
			throw e;
		}
	}
}
