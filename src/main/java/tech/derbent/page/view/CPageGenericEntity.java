package tech.derbent.page.view;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.abstracts.components.CCrudToolbar;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CEntityUpdateListener;
import tech.derbent.abstracts.interfaces.CLayoutChangeListener;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.abstracts.views.components.CFlexLayout;
import tech.derbent.abstracts.views.components.CVerticalLayout;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.view.CComponentGridEntity;
import tech.derbent.session.service.CLayoutService;
import tech.derbent.session.service.CSessionService;

/** Generic base class for entity management pages that provides common functionality for displaying and managing different entity types through
 * reflection and generic patterns.
 * @param <EntityClass> The entity type this page manages */
public abstract class CPageGenericEntity<EntityClass extends CEntityDB<EntityClass>> extends CPageBaseProjectAware
		implements CEntityUpdateListener, CLayoutChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageGenericEntity.class);
	private static final long serialVersionUID = 1L;
	// Current state
	protected CCrudToolbar<EntityClass> crudToolbar;
	protected final Class<EntityClass> entityClass;
	protected final CAbstractService<EntityClass> entityService;
	protected CComponentGridEntity grid;
	// Services and Entity Information
	protected final CGridEntityService gridEntityService;
	protected final String viewName;

	/** Constructor for generic entity page */
	protected CPageGenericEntity(final CSessionService sessionService, final CDetailSectionService screenService,
			final CGridEntityService gridEntityService, final CAbstractService<EntityClass> entityService, final Class<EntityClass> entityClass,
			final String viewName) {
		super(sessionService, screenService);
		this.gridEntityService = gridEntityService;
		this.entityService = entityService;
		this.entityClass = entityClass;
		this.viewName = viewName;
		createPageContent();
	}

	/** Hook method for subclasses to configure the CRUD toolbar with specific behavior like dependency checking */
	protected void configureCrudToolbar(CCrudToolbar<EntityClass> toolbar) {
		// Default implementation does nothing - subclasses can override to add specific configuration
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
		CGridEntity gridEntity = gridEntityService.findByNameAndProject(viewName, sessionService.getActiveProject().orElseThrow()).orElse(null);
		grid = new CComponentGridEntity(gridEntity);
		// Listen for selection changes from the grid
		grid.addSelectionChangeListener(event -> {
			try {
				onEntitySelected(event);
			} catch (Exception e) {
				LOGGER.error("Error handling entity selection", e);
			}
		});
		// Add grid to the primary (left) section
		splitLayout.addToPrimary(grid);
		this.add(splitLayout);
		// Create details section with toolbar and scrollable content
	}

	/** Abstract method to create a new entity instance with project set */
	protected abstract EntityClass createNewEntity();

	private void createPageContent() {
		baseDetailsLayout = CFlexLayout.forEntityPage();
		createMasterSection();
		createDetailsSection();
	}

	/** Abstract method to get the entity color code for UI styling */
	public abstract String getEntityColorCode();
	/** Abstract method to get the entity icon filename */
	public abstract String getIconFilename();

	private void initSplitLayout(final VerticalLayout detailsBase) {
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.addToSecondary(detailsBase);
		add(splitLayout);
	}

	/** Implementation of CEntityUpdateListener - called when an entity is deleted */
	@Override
	public void onEntityDeleted(CEntityDB<?> entity) {
		LOGGER.debug("Entity deleted notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		refreshGrid();
		// Clear the details section since the entity no longer exists
		getBaseDetailsLayout().removeAll();
		// Try to select the next item in the grid or the first item if no next item
		selectNextItemInGrid();
	}

	/** Implementation of CEntityUpdateListener - called when an entity is saved */
	@Override
	public void onEntitySaved(CEntityDB<?> entity) {
		LOGGER.debug("Entity saved notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		refreshGrid();
		// Keep the same item selected in the grid after save
		if (entity != null && grid != null) {
			try {
				// Refresh the grid and then re-select the saved entity
				java.lang.reflect.Method refreshMethod = grid.getClass().getDeclaredMethod("refreshGridData");
				refreshMethod.setAccessible(true);
				refreshMethod.invoke(grid);
				// Try to re-select the saved entity in the grid
				selectEntityInGrid(entity);
			} catch (Exception e) {
				LOGGER.warn("Error re-selecting entity after save: {}", e.getMessage());
			}
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
			// Clear the details when no entity is selected
			getBaseDetailsLayout().removeAll();
			currentBinder = null;
			crudToolbar = null;
			return;
		}
		Class<? extends CAbstractEntityDBPage<?>> entityViewClass = entity.getViewClass();
		Check.notNull(entityViewClass, "Entity view class cannot be null for entity: " + entity.getClass().getSimpleName());
		// Get view name by invoking static field named VIEW_NAME of entityViewClass
		Field viewNameField = entityViewClass.getField("VIEW_NAME");
		String entityViewName = (String) viewNameField.get(null);
		// Check if the selected entity is of the expected type
		if (entityClass.isInstance(entity)) {
			EntityClass typedEntity = (EntityClass) entity;
			// Build the screen structure first to get the binder
			buildScreen(entityViewName, entity.getClass());
			// Create CRUD toolbar with the binder
			if (getCurrentBinder() != null) {
				CCrudToolbar<EntityClass> toolbar =
						new CCrudToolbar<>(
								(tech.derbent.abstracts.components.CEnhancedBinder<
										EntityClass>) (tech.derbent.abstracts.components.CEnhancedBinder<?>) getCurrentBinder(),
								entityService, entityClass);
				// Configure toolbar callbacks
				toolbar.setNewEntitySupplier(this::createNewEntity);
				toolbar.setRefreshCallback((currentEntity) -> {
					refreshGrid();
					if (currentEntity != null && currentEntity.getId() != null) {
						// Reload entity from database
						try {
							EntityClass reloadedEntity = entityService.getById(currentEntity.getId()).orElse(null);
							if (reloadedEntity != null) {
								populateEntityDetails(reloadedEntity);
							}
						} catch (Exception e) {
							LOGGER.warn("Error reloading entity: {}", e.getMessage());
						}
					}
				});
				// Register this page as listener for CRUD operations
				toolbar.addUpdateListener(this);
				// Configure toolbar with subclass-specific settings
				configureCrudToolbar(toolbar);
				// Set current entity
				toolbar.setCurrentEntity(typedEntity);
				crudToolbar = toolbar;
				// Rebuild screen with toolbar
				buildScreen(entityViewName, entity.getClass(), toolbar);
			}
		} else {
			// For entities not of the expected type, build screen without toolbar
			buildScreen(entityViewName, entity.getClass());
		}
		// Bind the entity data to the form if binder is available
		if (getCurrentBinder() != null) {
			try {
				getCurrentBinder().setBean(entity);
				LOGGER.debug("Entity data bound to form: {}", entity.getClass().getSimpleName() + " ID: " + entity.getId());
			} catch (Exception e) {
				LOGGER.warn("Error binding entity data to form: {}", e.getMessage());
				getBaseDetailsLayout().add(new CDiv("Error loading entity data: " + e.getMessage()));
			}
		} else {
			LOGGER.warn("No binder available for data binding");
		}
	}

	/** Refreshes the grid to show updated data */
	protected void refreshGrid() {
		if (grid != null) {
			try {
				// Use reflection to call the private refreshGridData method
				java.lang.reflect.Method refreshMethod = grid.getClass().getDeclaredMethod("refreshGridData");
				refreshMethod.setAccessible(true);
				refreshMethod.invoke(grid);
				LOGGER.debug("Grid refreshed successfully");
			} catch (Exception e) {
				LOGGER.warn("Error refreshing grid: {}", e.getMessage());
			}
		}
	}

	/** Selects a specific entity in the grid */
	private void selectEntityInGrid(CEntityDB<?> entity) {
		if (grid != null && entity != null) {
			try {
				// Use reflection to access the grid's selection mechanism if available
				java.lang.reflect.Method selectMethod = grid.getClass().getDeclaredMethod("selectEntity", CEntityDB.class);
				selectMethod.setAccessible(true);
				selectMethod.invoke(grid, entity);
			} catch (Exception e) {
				LOGGER.debug("Could not select entity in grid: {}", e.getMessage());
			}
		}
	}

	/** Selects the next item in the grid after deletion */
	private void selectNextItemInGrid() {
		if (grid != null) {
			try {
				// Use reflection to access grid's selection mechanism for next item
				java.lang.reflect.Method selectNextMethod = grid.getClass().getDeclaredMethod("selectNextItem");
				selectNextMethod.setAccessible(true);
				selectNextMethod.invoke(grid);
			} catch (Exception e) {
				LOGGER.debug("Could not select next item in grid: {}", e.getMessage());
				// Fallback: try to select the first item
				try {
					java.lang.reflect.Method selectFirstMethod = grid.getClass().getDeclaredMethod("selectFirstItem");
					selectFirstMethod.setAccessible(true);
					selectFirstMethod.invoke(grid);
				} catch (Exception e2) {
					LOGGER.debug("Could not select first item in grid: {}", e2.getMessage());
				}
			}
		}
	}

	/** Sets the layout service for managing split layout orientation.
	 * @param layoutService the layout service */
	public void setLayoutService(final CLayoutService layoutService) {
		this.layoutService = layoutService;
		if (layoutService != null) {
			layoutService.addLayoutChangeListener(this);
			updateLayoutOrientation();
		}
	}

	/** Updates the split layout orientation based on the current layout mode */
	private void updateLayoutOrientation() {
		if ((layoutService != null) && (splitLayout != null)) {
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
		} else {
			// Default fallback when no layout service is available
			splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
			splitLayout.setSplitterPosition(30.0);
		}
	}
}
