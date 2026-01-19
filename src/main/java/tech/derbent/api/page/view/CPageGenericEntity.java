package tech.derbent.api.page.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.ILayoutChangeListener;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.ui.component.basic.CFlexLayout;
import tech.derbent.api.ui.component.basic.CScroller;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentDetailsMasterToolbar;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.CLayoutService;
import tech.derbent.base.session.service.ISessionService;

/** Generic base class for entity management pages that provides common functionality for displaying and managing different entity types through
 * reflection and generic patterns.
 * @param <EntityClass> The entity type this page manages */
public abstract class CPageGenericEntity<EntityClass extends CEntityDB<EntityClass>> extends CPageBaseProjectAware implements ILayoutChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageGenericEntity.class);
	private static final long serialVersionUID = 1L;
	// Current state
	protected CCrudToolbar crudToolbar;
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

	public EntityClass actionCreate() {
		try {
			final EntityClass newEntity = createNewEntityInstance();
			populateEntityDetails(newEntity);
			return newEntity;
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity", e);
		}
		return null;
	}

	public void actionDelete() {
		// try {
		// if (typedEntity == null || typedEntity.getId() == null) {
		// LOGGER.warn("No entity to delete");
		// return;
		// }
		// // Delete and notify
		// entityService.delete(typedEntity.getId());
		// onEntityDeleted(typedEntity);
		// } catch (final Exception e) {
		// LOGGER.error("Error deleting entity", e);
		// }
	}

	public void actionRefresh() {
		try {
			if (getValue() != null && ((CEntityDB<?>) getValue()).getId() != null) {
				final EntityClass reloaded = entityService.getById(((CEntityDB<?>) getValue()).getId()).orElse(null);
				if (reloaded != null) {
					populateEntityDetails(reloaded);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error refreshing entity: {}", e.getMessage());
		}
	}

	public void actionSave() {
		// try {
		// if (getValue() == null) {
		// LOGGER.warn("No entity to save");
		// return;
		// }
		// // Write form data to entity
		// typedBinder.writeBean(getValue());
		// // Save entity
		// final EntityClass savedEntity = entityService.save(getValue());
		// LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
		// // Notify listeners and refresh
		// onEntitySaved(savedEntity);
		// } catch (final Exception e) {
		// LOGGER.error("Error saving entity", e);
		// }
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

	CCrudToolbar createCrudToolbar() {
		return crudToolbar;
	}

	/** Creates a new CCrudToolbar instance for the given entity type and binder. All configuration is now done via setters after construction. The
	 * entity type is determined dynamically when an entity is set.
	 * @param typedBinder the properly typed binder for the entity
	 * @param typedEntity the current entity instance
	 * @return a configured CCrudToolbar instance */
	@SuppressWarnings ({})
	protected CCrudToolbar createCrudToolbar(final CEnhancedBinder<EntityClass> typedBinder, final EntityClass typedEntity) {
		final CCrudToolbar toolbar = new CCrudToolbar();
		toolbar.setValue(typedEntity);
		configureCrudToolbar(toolbar);
		return toolbar;
	}

	private void createDetailsSection() {
		baseDetailsLayout = CFlexLayout.forEntityPage();
		final CScroller detailsScroller = new CScroller();
		// FLEX LAYOUT///////////////////
		detailsScroller.setContent(baseDetailsLayout);
		final CVerticalLayout detailsBase = new CVerticalLayout(false, false, false);
		detailsBase.add(detailsScroller);
		initSplitLayout(detailsBase);
		// Initial layout setup - will be updated when layout service is available
		updateLayoutOrientation();
	}

	protected void createMasterSection() {
		// Create and configure grid
		final CGridEntity gridEntity = gridEntityService.findByNameAndProject(viewName,
				getSessionService().getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for new activity.")))
				.orElse(null);
		grid = new CComponentGridEntity(gridEntity, getSessionService());
		// Set the content owner so widget columns can access page service
		grid.setContentOwner(this);
		// Listen for selection changes from the grid
		grid.addSelectionChangeListener(event -> {
			try {
				onEntitySelected(event);
			} catch (final Exception e) {
				LOGGER.error("Error handling entity selection", e);
			}
		});
		final CVerticalLayout gridLayout = new CVerticalLayout();
		gridLayout.add(new CComponentDetailsMasterToolbar(grid, gridEntityService));
		gridLayout.add(grid);
		// Add grid to the primary (left) section
		splitLayout.addToPrimary(gridLayout);
		this.add(splitLayout);
		// Create details section with toolbar and scrollable content
	}

	/** Creates a new entity instance.
	 * @return a new entity instance of type EntityClass
	 * @throws Exception */
	@Override
	@SuppressWarnings ("unchecked")
	protected <T extends CEntityDB<T>> T createNewEntity() throws Exception {
		return (T) createNewEntityInstance();
	}

	/** Creates a new entity instance of the specific entity type.
	 * @return a new entity instance of type EntityClass
	 * @throws Exception if entity creation fails */
	@Override
	public abstract EntityClass createNewEntityInstance() throws Exception;

	private void createPageContent() {
		baseDetailsLayout = CFlexLayout.forEntityPage();
		createMasterSection();
		createDetailsSection();
	}

	/** Get the entity icon filename using reflection */
	public String getIconFilename() {
		try {
			return CAuxillaries.invokeStaticMethodOfStr(this.getClass(), "getStaticIconFilename");
		} catch (final Exception e) {
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
	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityDeleted(CEntityDB entity) throws Exception {
		LOGGER.debug("Entity deleted notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		refreshGrid();
		getBaseDetailsLayout().removeAll();
		grid.selectNextItem();
	}

	/** Implementation of CEntityUpdateListener - called when an entity is saved
	 * @throws Exception */
	@SuppressWarnings ({
			"rawtypes"
	})
	@Override
	public void onEntitySaved(CEntityDB entity) throws Exception {
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
		} catch (final Exception e) {
			LOGGER.error("Error re-selecting entity after save: {}", e.getMessage());
			throw e;
		}
	}

	/** Handles entity selection events from the grid */
	private void onEntitySelected(CComponentGridEntity.SelectionChangeEvent event) throws Exception {
		LOGGER.debug("Entity selected event received");
		final CEntityDB<?> selectedEntity = event.getSelectedItem();
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
		final Field viewNameField = entity.getClass().getField("VIEW_NAME");
		final String entityViewName = (String) viewNameField.get(null);
		Check.isTrue(entityClass.isAssignableFrom(entity.getClass()),
				"Selected entity type " + entity.getClass().getSimpleName() + " does not match expected type " + entityClass.getSimpleName());
		final EntityClass typedEntity = (EntityClass) entity;
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
		final CEnhancedBinder<EntityClass> typedBinder = new CEnhancedBinder<>(entityClass);
		crudToolbar = createCrudToolbar(typedBinder, typedEntity);
		getBaseDetailsLayout().add(crudToolbar);
		// Update the current binder to be the properly typed one
		final CEnhancedBinder<CEntityDB<?>> genericBinder = (CEnhancedBinder<CEntityDB<?>>) (CEnhancedBinder<?>) typedBinder;
		currentBinder = genericBinder;
		currentEntityViewName = entityViewName;
		currentEntityType = typedEntity.getClass();
		buildScreen(entityViewName, typedEntity.getClass(), getBaseDetailsLayout());
		typedBinder.setBean(typedEntity);
	}

	/** Refreshes the grid to show updated data
	 * @throws Exception */
	@Override
	public void refreshGrid() throws Exception {
		Check.notNull(grid, "Grid component is not initialized");
		try {
			// Use reflection to call the private refreshGridData method
			final Method refreshMethod = grid.getClass().getDeclaredMethod("refreshGridData");
			refreshMethod.setAccessible(true);
			refreshMethod.invoke(grid);
			LOGGER.debug("Grid refreshed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error refreshing grid: {}", e.getMessage());
			throw e;
		}
	}

	/** Reloads entity values into existing components without rebuilding the UI */
	@SuppressWarnings ("unchecked")
	private void reloadEntityValues(EntityClass typedEntity) {
		try {
			// Update the toolbar's current entity
			crudToolbar.setValue(typedEntity);
			// Update the binder with new entity values
			final CEnhancedBinder<EntityClass> typedBinder = (CEnhancedBinder<EntityClass>) (CEnhancedBinder<?>) currentBinder;
			typedBinder.setBean(typedEntity);
			LOGGER.debug("Successfully reloaded entity values for: {}", typedEntity.getClass().getSimpleName());
		} catch (final Exception e) {
			LOGGER.error("Error reloading entity values, falling back to rebuild: {}", e.getMessage());
			// If reloading fails, fall back to rebuilding
			try {
				rebuildEntityDetails(typedEntity, currentEntityViewName);
			} catch (final Exception rebuildException) {
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
		CLayoutService.addLayoutChangeListener(this);
		updateLayoutOrientation();
	}

	/** Updates the split layout orientation based on the current layout mode */
	private void updateLayoutOrientation() {
		Check.notNull(splitLayout, "Split layout is not initialized");
		Check.notNull(getSessionService(), "Session service is not initialized");
		// If layout service is not available yet (during bean construction), use default vertical layout
		if (layoutService == null) {
			LOGGER.debug("Layout service not yet available, using default vertical orientation for {}", getClass().getSimpleName());
			splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
			splitLayout.setSplitterPosition(30.0); // Default: 30% for grid, 70% for details (same as vertical mode)
			return;
		}
		final CLayoutService.LayoutMode currentMode = CLayoutService.getCurrentLayoutMode();
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
			splitLayout.getElement().executeJs("if (this && this.$server && this.$server.requestUpdate) { this.$server.requestUpdate(); }"
					+ " else if (this && this.requestUpdate) { this.requestUpdate(); }");
		}));
	}
}
