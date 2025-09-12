package tech.derbent.page.view;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.abstracts.components.CCrudToolbar;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CEntityUpdateListener;
import tech.derbent.abstracts.interfaces.CLayoutChangeListener;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.view.CComponentGridEntity;
import tech.derbent.session.service.CLayoutService;
import tech.derbent.session.service.CSessionService;

/** Generic base class for entity management pages that provides common functionality for displaying and managing different entity types through
 * reflection and generic patterns.
 * @param <EntityType> The entity type this page manages */
public abstract class CPageGenericEntity<EntityType extends CEntityDB<EntityType>> extends CPageBaseProjectAware
		implements CEntityUpdateListener, CLayoutChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageGenericEntity.class);
	private static final long serialVersionUID = 1L;
	// Current state
	protected CCrudToolbar<EntityType> crudToolbar;
	protected FlexLayout detailsContainer;
	protected Scroller detailsScroller;
	protected final Class<EntityType> entityClass;
	protected final CAbstractService<EntityType> entityService;
	// UI Components
	protected CComponentGridEntity grid;
	// Services and Entity Information
	protected final CGridEntityService gridEntityService;
	protected SplitLayout splitLayout;
	protected final String viewName;
	protected CLayoutService layoutService;

	/** Constructor for generic entity page */
	protected CPageGenericEntity(final CSessionService sessionService, final CDetailSectionService screenService,
			final CGridEntityService gridEntityService, final CAbstractService<EntityType> entityService, final Class<EntityType> entityClass,
			final String viewName) {
		super(sessionService, screenService);
		this.gridEntityService = gridEntityService;
		this.entityService = entityService;
		this.entityClass = entityClass;
		this.viewName = viewName;
		createPageContent();
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

	/** Implementation of CLayoutChangeListener - called when layout mode changes */
	@Override
	public void onLayoutModeChanged(final CLayoutService.LayoutMode newMode) {
		LOGGER.debug("Layout mode changed to: {} for {}", newMode, getClass().getSimpleName());
		updateLayoutOrientation();
	}

	/** Updates the split layout orientation based on the current layout mode */
	private void updateLayoutOrientation() {
		if ((layoutService != null) && (splitLayout != null)) {
			final CLayoutService.LayoutMode currentMode = layoutService.getCurrentLayoutMode();
			if (currentMode == CLayoutService.LayoutMode.HORIZONTAL) {
				splitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
				splitLayout.setSplitterPosition(50.0); // 50% for grid, 50% for details
			} else {
				splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
				splitLayout.setSplitterPosition(60.0); // 60% for grid, 40% for details
			}
		}
	}

	private void createDetailsSection() {
		// Create the main details container using FlexLayout for proper accordion filling
		detailsContainer = new FlexLayout();
		detailsContainer.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		detailsContainer.setSizeFull();
		// Create scrollable content area for field details
		divDetails = new CDiv();
		detailsScroller = new Scroller();
		detailsScroller.setContent(divDetails);
		detailsScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		detailsScroller.setSizeFull();
		// Add scroller to details container (toolbar will be added at the top by buildScreen)
		detailsContainer.add(detailsScroller);
		detailsContainer.setFlexGrow(1, detailsScroller); // Scroller takes remaining space after toolbar
		// Add details container to the secondary (right) section of split layout
		splitLayout.addToSecondary(detailsContainer);
	}

	/** Abstract method to create a new entity instance with project set */
	protected abstract EntityType createNewEntity();

	/** Hook method for subclasses to configure the CRUD toolbar with specific behavior like dependency checking */
	protected void configureCrudToolbar(CCrudToolbar<EntityType> toolbar) {
		// Default implementation does nothing - subclasses can override to add specific configuration
	}

	private void createPageContent() {
		// Create SplitLayout - vertical by default as requested
		splitLayout = new SplitLayout();
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.setSplitterPosition(60.0); // 60% for grid, 40% for details
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
		// Create details section with toolbar and scrollable content
		createDetailsSection();
		// Add split layout to the page
		this.add(splitLayout);
		// Add project info below
		add(new Div("Entity management page for " + entityClass.getSimpleName() + ". Project: "
				+ (sessionService.getActiveProject().get() != null ? sessionService.getActiveProject().get().getName() : "None")));
	}

	/** Abstract method to get the entity color code for UI styling */
	public abstract String getEntityColorCode();
	/** Abstract method to get the entity icon filename */
	public abstract String getIconFilename();

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

	/** Handles entity selection events from the grid */
	private void onEntitySelected(CComponentGridEntity.SelectionChangeEvent event) throws Exception {
		CEntityDB<?> selectedEntity = event.getSelectedItem();
		populateEntityDetails(selectedEntity);
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
			EntityType typedEntity = (EntityType) entity;
			// Build the screen structure first to get the binder
			buildScreen(entityViewName, entity.getClass());
			// Create CRUD toolbar with the binder
			if (getCurrentBinder() != null) {
				CCrudToolbar<EntityType> toolbar =
						new CCrudToolbar<>(
								(tech.derbent.abstracts.components.CEnhancedBinder<
										EntityType>) (tech.derbent.abstracts.components.CEnhancedBinder<?>) getCurrentBinder(),
								entityService, entityClass);
				// Configure toolbar callbacks
				toolbar.setNewEntitySupplier(this::createNewEntity);
				toolbar.setRefreshCallback((currentEntity) -> {
					refreshGrid();
					if (currentEntity != null && currentEntity.getId() != null) {
						// Reload entity from database
						try {
							EntityType reloadedEntity = entityService.getById(currentEntity.getId()).orElse(null);
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
}
