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
import tech.derbent.api.components.CComponentDetailsMasterToolbar;
import tech.derbent.api.components.CCrudToolbar;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.interfaces.IPageTitleProvider;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CFlexLayout;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.view.CComponentGridEntity;
import tech.derbent.session.service.CSessionService;
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
	// Layout components
	protected SplitLayout splitLayout;
	private Object currentEntity;

	@Autowired
	public CDynamicPageViewWithSections(final CPageEntity pageEntity, final CSessionService sessionService,
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

	/** Checks if existing components can be reused for the given entity view */
	private boolean canReuseExistingComponents(String entityViewName, Class<?> entityType) {
		return currentEntityViewName != null && currentEntityType != null && currentEntityViewName.equals(entityViewName)
				&& currentEntityType.equals(entityType) && currentBinder != null && crudToolbar != null;
	}

	/** Clear entity details and reset state. */
	private void clearEntityDetails() {
		if (baseDetailsLayout != null) {
			baseDetailsLayout.removeAll();
		}
		currentBinder = null;
		crudToolbar = null;
		currentEntityViewName = null;
		currentEntityType = null;
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected CCrudToolbar<?> createCrudToolbar(final CEnhancedBinder<?> typedBinder, final CEntityDB<?> typedEntity) {
		// Use static factory method to create toolbar
		CCrudToolbar toolbar = new CCrudToolbar(typedBinder, entityService, entityClass);
		toolbar.setCurrentEntity(typedEntity);
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
		baseDetailsLayout = CFlexLayout.forEntityPage();
		baseDetailsLayout.setSizeFull();
		final Scroller detailsScroller = new Scroller();
		detailsScroller.setContent(baseDetailsLayout);
		detailsScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		detailsScroller.setSizeFull();
		final CVerticalLayout detailsBase = new CVerticalLayout(false, false, false);
		detailsBase.add(detailsScroller);
		splitLayout.addToSecondary(detailsBase);
		LOGGER.debug("Created details section with detail section: {}", pageEntity.getDetailSection().getName());
	}

	/** Create grid and detail sections similar to CPageGenericEntity. */
	private void createGridAndDetailSections() {
		try {
			// Initialize the entity service for the configured entity type
			initializeEntityService();
			initSplitLayout();
			createMasterSection();
			createDetailsSection();
		} catch (Exception e) {
			LOGGER.error("Failed to create grid and detail sections for page: {}", pageEntity.getPageTitle(), e);
			throw e;
		}
	}

	/** Create the master (grid) section. */
	private void createMasterSection() {
		Check.notNull(pageEntity.getGridEntity(), "Grid entity cannot be null");
		// Create the grid component using the configured grid entity
		grid = new CComponentGridEntity(pageEntity.getGridEntity());
		// Listen for selection changes from the grid
		grid.addSelectionChangeListener(event -> {
			try {
				LOGGER.debug("Grid selection changed: {}", event.getSelectedItem().toString());
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
		LOGGER.debug("Created master section with grid entity: {}", pageEntity.getGridEntity().getName());
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
			// Try to get the service bean from the configured grid entity
			CGridEntity gridEntity = pageEntity.getGridEntity();
			Check.notNull(gridEntity, "Grid entity cannot be null");
			Check.notBlank(gridEntity.getDataServiceBeanName(), "Data service bean name cannot be blank");
			// Get the service bean from the application context
			Object serviceBean = applicationContext.getBean(gridEntity.getDataServiceBeanName());
			Check.notNull(serviceBean, "Service bean not found: " + gridEntity.getDataServiceBeanName());
			Check.isTrue(serviceBean instanceof CAbstractService<?>,
					"Service bean is not an instance of CAbstractService: " + serviceBean.getClass());
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
		setSizeFull();
		// setPadding(true);
		// setSpacing(true);
		// Set page title for browser tab only if pageTitle is not empty
		if (pageEntity.getPageTitle() != null && !pageEntity.getPageTitle().trim().isEmpty()) {
			getElement().executeJs("document.title = $0", pageEntity.getPageTitle());
		}
		createGridAndDetailSections();
		LOGGER.debug("Dynamic page view with sections initialized for: {}", pageEntity.getPageTitle());
	}

	private void initSplitLayout() {
		splitLayout = new SplitLayout();
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		// splitLayout.setSplitterPosition(30.0); // 30% for grid, 70% for details
		// Remove height constraints to allow primary section to expand
		// splitLayout.getStyle().remove("max-height");
		// splitLayout.getPrimaryComponent().getElement().getStyle().remove("max-height");
		add(splitLayout);
	}

	// Implementation of CEntityUpdateListener
	@Override
	public void onEntityDeleted(CEntityDB<?> entity) {
		LOGGER.debug("Entity deleted notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		Check.notNull(grid, "Grid component is not initialized");
		// Refresh grid and clear details
		refreshGrid();
		clearEntityDetails();
		grid.selectNextItem();
	}

	@Override
	public void onEntitySaved(CEntityDB<?> entity) {
		LOGGER.debug("Entity saved notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		Check.notNull(grid, "Grid component is not initialized");
		Check.notNull(entity, "Saved entity cannot be null");
		// Refresh grid and re-select entity
		refreshGrid();
		grid.selectEntity(entity);
	}

	/** Handle entity selection events from the grid. */
	private void onEntitySelected(CComponentGridEntity.SelectionChangeEvent event) throws Exception {
		CEntityDB<?> selectedEntity = event.getSelectedItem();
		LOGGER.debug("Entity selected: {}", selectedEntity != null ? selectedEntity.toString() + " ID: " + selectedEntity.getId() : "null");
		setCurrentEntity(selectedEntity);
		populateForm();
	}

	/** Populate the entity details section with information from the selected entity.
	 * @throws SecurityException
	 * @throws NoSuchFieldException */
	@Override
	public void populateForm() throws Exception {
		CEntityDB<?> entity = (CEntityDB<?>) getCurrentEntity();
		LOGGER.debug("Populating entity details for: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		Check.notNull(baseDetailsLayout, "Base details layout is not initialized");
		Check.notNull(pageEntity.getDetailSection(), "Detail section cannot be null");
		// Clear existing content
		if (entity == null) {
			// setEmptyEntityDetailsMessage();
			getCurrentBinder().setBean(null);
			return;
		}
		Field viewNameField = entity.getClass().getField("VIEW_NAME");
		String entityViewName = (String) viewNameField.get(null);
		// Performance optimization: check if we can reuse existing components
		if (canReuseExistingComponents(entityViewName, entity.getClass())) {
			// LOGGER.debug("Reusing existing components for entity type: {} view: {}", entity.getClass().getSimpleName(), entityViewName);
			reloadEntityValues(entity);
			detailsBuilder.populateForm();
			return;
		}
		LOGGER.debug("Rebuilding components for entity type: {} view: {}", entity.getClass().getSimpleName(), entityViewName);
		rebuildEntityDetails(entity, entityViewName);
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void rebuildEntityDetails(CEntityDB<?> typedEntity, String entityViewName) throws Exception {
		LOGGER.debug("Rebuilding entity details for type: {} view: {}", typedEntity.getClass().getSimpleName(), entityViewName);
		// Clear existing state
		clearEntityDetails();
		// Create a properly typed binder for this specific entity type
		CEnhancedBinder typedBinder = new CEnhancedBinder(entityClass);
		// Create and configure toolbar using the factory method
		CCrudToolbar<?> toolbar = createCrudToolbar(typedBinder, typedEntity);
		crudToolbar = toolbar;
		// Update the current binder to be the properly typed one
		currentBinder = typedBinder;
		// Update state tracking
		currentEntityViewName = entityViewName;
		currentEntityType = typedEntity.getClass();
		// Build screen with toolbar - cast to appropriate type to bypass generic constraints
		buildScreen(entityViewName, (Class) entityClass, toolbar);
		typedBinder.setBean(typedEntity);
		populateForm();
	}

	/** Refresh the grid to show updated data. */
	private void refreshGrid() {
		grid.refreshGridData();
	}

	/** Reloads entity values into existing components without rebuilding the UI */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void reloadEntityValues(CEntityDB<?> typedEntity) {
		try {
			// Update the toolbar's current entity
			if (crudToolbar != null) {
				((CCrudToolbar) crudToolbar).setCurrentEntity(typedEntity);
			}
			// Update the binder with new entity values
			if (currentBinder != null) {
				currentBinder.setBean(typedEntity);
			}
			LOGGER.debug("Successfully reloaded entity values for: {}", typedEntity.getClass().getSimpleName());
		} catch (Exception e) {
			LOGGER.error("Error reloading entity values, falling back to rebuild: {}", e.getMessage());
			// If reloading fails, fall back to rebuilding
			try {
				rebuildEntityDetails(typedEntity, currentEntityViewName);
			} catch (Exception rebuildException) {
				LOGGER.error("Error rebuilding entity details: {}", rebuildException.getMessage());
				clearEntityDetails();
			}
		}
	}

	@Override
	public Object getCurrentEntity() { // TODO Auto-generated method stub
		return currentEntity;
	}

	@Override
	public void setCurrentEntity(Object entity) { currentEntity = entity; }
}
