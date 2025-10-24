package tech.derbent.app.page.view;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.interfaces.ILayoutChangeListener;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentDetailsMasterToolbar;
import tech.derbent.api.views.components.CCrudToolbar;
import tech.derbent.api.views.components.CFlexLayout;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.base.session.service.CLayoutService;
import tech.derbent.base.session.service.ISessionService;

/** Generic base class for entity management pages that provides common functionality for displaying and managing different entity types through
 * reflection and generic patterns.
 * @param <EntityClass> The entity type this page manages */
public abstract class CPageGenericEntity<EntityClass extends CEntityDB<EntityClass>> extends CPageBaseProjectAware
		implements IEntityUpdateListener, ILayoutChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageGenericEntity.class);
	private static final long serialVersionUID = 1L;
	// Current state
	protected CCrudToolbar<EntityClass> crudToolbar;
	private Class<?> currentEntityType = null;
	// State tracking for performance optimization
	private String currentEntityViewName = null;
	protected final Class<EntityClass> entityClass;
	protected final CAbstractService<EntityClass> entityService;
	protected CComponentGridEntity grid;
	// Services and Entity Information
	protected final CGridEntityService gridEntityService;
	protected final String viewName;

	/** Constructor for generic entity page */
	protected CPageGenericEntity(final ISessionService sessionService, final CDetailSectionService screenService,
			final CGridEntityService gridEntityService, final CAbstractService<EntityClass> entityService, final Class<EntityClass> entityClass,
			final String viewName) {
		super(sessionService, screenService);
		this.gridEntityService = gridEntityService;
		this.entityService = entityService;
		this.entityClass = entityClass;
		this.viewName = viewName;
		createPageContent();
	}

	/** Checks if existing components can be reused for the given entity view */
	private boolean canReuseExistingComponents(String entityViewName, Class<?> entityType) {
		return currentEntityViewName != null && currentEntityType != null && currentEntityViewName.equals(entityViewName)
				&& currentEntityType.equals(entityType) && currentBinder != null && crudToolbar != null;
	}

	/** Clears entity details and resets state */
	private void clearEntityDetails() {
		getBaseDetailsLayout().removeAll();
		currentBinder = null;
		crudToolbar = null;
		currentEntityViewName = null;
		currentEntityType = null;
	}

	CCrudToolbar<EntityClass> createCrudToolbar() {
		return crudToolbar;
	}

	/** Creates a new CCrudToolbar instance for the given entity type and binder.
	 * @param typedBinder the properly typed binder for the entity
	 * @param typedEntity the current entity instance
	 * @return a configured CCrudToolbar instance */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	protected CCrudToolbar<EntityClass> createCrudToolbar(final CEnhancedBinder<EntityClass> typedBinder, final EntityClass typedEntity) {
		// Use static factory method to create toolbar
		CCrudToolbar<EntityClass> toolbar = new CCrudToolbar(entityService, entityClass, typedBinder);
		toolbar.setCurrentEntity(typedEntity);
		toolbar.setNewEntitySupplier(this::createNewEntityInstance);
		toolbar.setRefreshCallback((currentEntity) -> {
			try {
				refreshGrid();
				if (currentEntity != null && currentEntity.getId() != null) {
					EntityClass reloadedEntity = entityService.getById(currentEntity.getId()).orElse(null);
					if (reloadedEntity != null) {
						populateEntityDetails(reloadedEntity);
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error reloading entity: {}", e.getMessage());
				e.printStackTrace();
			}
		});
		toolbar.addUpdateListener(this);
		configureCrudToolbar(toolbar);
		return toolbar;
	}

	private void createDetailsSection() {
		baseDetailsLayout = CFlexLayout.forEntityPage();
		final Scroller detailsScroller = new Scroller();
		// FLEX LAYOUT///////////////////
		detailsScroller.setContent(baseDetailsLayout);
		detailsScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		final CVerticalLayout detailsBase = new CVerticalLayout(false, false, false);
		detailsBase.add(detailsScroller);
		initSplitLayout(detailsBase);
		// Initial layout setup - will be updated when layout service is available
		updateLayoutOrientation();
	}

	protected void createMasterSection() {
		// Create and configure grid
		CGridEntity gridEntity = gridEntityService
				.findByNameAndProject(viewName,
						sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for new activity.")))
				.orElse(null);
		grid = new CComponentGridEntity(gridEntity);
		// Listen for selection changes from the grid
		grid.addSelectionChangeListener(event -> {
			try {
				onEntitySelected(event);
			} catch (Exception e) {
				LOGGER.error("Error handling entity selection", e);
			}
		});
		CVerticalLayout gridLayout = new CVerticalLayout();
		gridLayout.add(new CComponentDetailsMasterToolbar(grid, gridEntityService));
		gridLayout.add(grid);
		// Add grid to the primary (left) section
		splitLayout.addToPrimary(gridLayout);
		this.add(splitLayout);
		// Create details section with toolbar and scrollable content
	}

	/** Creates a new entity instance.
	 * @return a new entity instance of type EntityClass */
	@Override
	@SuppressWarnings ("unchecked")
	protected <T extends CEntityDB<T>> T createNewEntity() {
		return (T) createNewEntityInstance();
	}

	/** Creates a new entity instance of the specific entity type.
	 * @return a new entity instance of type EntityClass */
	protected abstract EntityClass createNewEntityInstance();

	private void createPageContent() {
		baseDetailsLayout = CFlexLayout.forEntityPage();
		createMasterSection();
		createDetailsSection();
	}

	/** Get the entity icon filename using reflection */
	public String getIconFilename() {
		try {
			return CAuxillaries.invokeStaticMethodOfStr(this.getClass(), "getStaticIconFilename");
		} catch (Exception e) {
			LOGGER.error("Error getting icon filename for {}: {}", this.getClass().getSimpleName(), e.getMessage());
			return "vaadin:question"; // Default icon
		}
	}

	private void initSplitLayout(final VerticalLayout detailsBase) {
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.addToSecondary(detailsBase);
		add(splitLayout);
	}

	/** Implementation of CEntityUpdateListener - called when an entity is deleted
	 * @throws Exception */
	@Override
	public void onEntityDeleted(CEntityDB<?> entity) throws Exception {
		LOGGER.debug("Entity deleted notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		refreshGrid();
		// Clear the details section since the entity no longer exists
		getBaseDetailsLayout().removeAll();
		// Try to select the next item in the grid or the first item if no next item
		grid.selectNextItem();
	}

	/** Implementation of CEntityUpdateListener - called when an entity is saved
	 * @throws Exception */
	@Override
	public void onEntitySaved(CEntityDB<?> entity) throws Exception {
		try {
			LOGGER.debug("Entity saved notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(entity, "Saved entity cannot be null");
			Check.notNull(grid, "Grid component is not initialized");
			refreshGrid();
			// Refresh the grid and then re-select the saved entity
			refreshGrid();
			// Try to re-select the saved entity in the grid
			grid.selectEntity(entity);
			LOGGER.debug("Re-selected saved entity in grid: {}", entity.getId());
		} catch (Exception e) {
			LOGGER.error("Error re-selecting entity after save: {}", e.getMessage());
			throw e;
		}
	}

	/** Handles entity selection events from the grid */
	private void onEntitySelected(CComponentGridEntity.SelectionChangeEvent event) throws Exception {
		CEntityDB<?> selectedEntity = event.getSelectedItem();
		populateEntityDetails(selectedEntity);
	}

	/** Implementation of CLayoutChangeListener - called when layout mode changes */
	@Override
	public void onLayoutModeChanged(final CLayoutService.LayoutMode newMode) {
		LOGGER.debug("Layout mode changed to: {} for {}", newMode, getClass().getSimpleName());
		updateLayoutOrientation();
	}

	/** Populates the entity details section with information from the selected entity */
	@SuppressWarnings ("unchecked")
	private void populateEntityDetails(CEntityDB<?> entity) throws Exception {
		if (entity == null) {
			clearEntityDetails();
			return;
		}
		Field viewNameField = entity.getClass().getField("VIEW_NAME");
		String entityViewName = (String) viewNameField.get(null);
		Check.isTrue(entityClass.isAssignableFrom(entity.getClass()),
				"Selected entity type " + entity.getClass().getSimpleName() + " does not match expected type " + entityClass.getSimpleName());
		EntityClass typedEntity = (EntityClass) entity;
		// Performance optimization: check if we can reuse existing components
		if (canReuseExistingComponents(entityViewName, entity.getClass())) {
			LOGGER.debug("Reusing existing components for entity type: {} view: {}", entity.getClass().getSimpleName(), entityViewName);
			reloadEntityValues(typedEntity);
			return;
		}
		LOGGER.debug("Rebuilding components for entity type: {} view: {}", entity.getClass().getSimpleName(), entityViewName);
		rebuildEntityDetails(typedEntity, entityViewName);
	}

	/** Rebuilds entity details from scratch */
	@SuppressWarnings ("unchecked")
	private void rebuildEntityDetails(EntityClass typedEntity, String entityViewName) throws Exception {
		// Clear existing state
		clearEntityDetails();
		CEnhancedBinder<EntityClass> typedBinder = new CEnhancedBinder<>(entityClass);
		crudToolbar = createCrudToolbar(typedBinder, typedEntity);
		getBaseDetailsLayout().add(crudToolbar);
		// Update the current binder to be the properly typed one
		CEnhancedBinder<CEntityDB<?>> genericBinder = (CEnhancedBinder<CEntityDB<?>>) (CEnhancedBinder<?>) typedBinder;
		currentBinder = genericBinder;
		currentEntityViewName = entityViewName;
		currentEntityType = typedEntity.getClass();
		buildScreen(entityViewName, typedEntity.getClass(), getBaseDetailsLayout());
		typedBinder.setBean(typedEntity);
	}

	/** Refreshes the grid to show updated data
	 * @throws Exception */
	protected void refreshGrid() throws Exception {
		Check.notNull(grid, "Grid component is not initialized");
		try {
			// Use reflection to call the private refreshGridData method
			java.lang.reflect.Method refreshMethod = grid.getClass().getDeclaredMethod("refreshGridData");
			refreshMethod.setAccessible(true);
			refreshMethod.invoke(grid);
			LOGGER.debug("Grid refreshed successfully");
		} catch (Exception e) {
			LOGGER.error("Error refreshing grid: {}", e.getMessage());
			throw e;
		}
	}

	/** Reloads entity values into existing components without rebuilding the UI */
	@SuppressWarnings ("unchecked")
	private void reloadEntityValues(EntityClass typedEntity) {
		try {
			// Update the toolbar's current entity
			crudToolbar.setCurrentEntity(typedEntity);
			// Update the binder with new entity values
			CEnhancedBinder<EntityClass> typedBinder = (CEnhancedBinder<EntityClass>) (CEnhancedBinder<?>) currentBinder;
			typedBinder.setBean(typedEntity);
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

	/** Sets the layout service for managing split layout orientation.
	 * @param layoutService the layout service */
	public void setLayoutService(final CLayoutService layoutService) {
		Check.notNull(layoutService, "Layout service cannot be null");
		this.layoutService = layoutService;
		layoutService.addLayoutChangeListener(this);
		updateLayoutOrientation();
	}

	/** Updates the split layout orientation based on the current layout mode */
	private void updateLayoutOrientation() {
		Check.notNull(splitLayout, "Split layout is not initialized");
		Check.notNull(sessionService, "Session service is not initialized");
		// If layout service is not available yet (during bean construction), use default vertical layout
		if (layoutService == null) {
			LOGGER.debug("Layout service not yet available, using default vertical orientation for {}", getClass().getSimpleName());
			splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
			splitLayout.setSplitterPosition(30.0); // Default: 30% for grid, 70% for details (same as vertical mode)
			return;
		}
		final CLayoutService.LayoutMode currentMode = layoutService.getCurrentLayoutMode();
		// LOGGER.debug("Updating layout orientation to: {} for {}", currentMode, getClass().getSimpleName());
		if (currentMode == CLayoutService.LayoutMode.HORIZONTAL) {
			splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
			// For horizontal layout, give more space to the grid (left side)
			splitLayout.setSplitterPosition(50.0); // 50% for grid, 50% for details
		} else {
			splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
			// For vertical layout, give more space to the grid (top)
			splitLayout.setSplitterPosition(30.0); // 30% for grid, 70% for details
		}
		// Force UI refresh to apply changes immediately
		getUI().ifPresent(ui -> ui.access(() -> {
			splitLayout.getElement().callJsFunction("$server.requestUpdate");
		}));
	}
}
