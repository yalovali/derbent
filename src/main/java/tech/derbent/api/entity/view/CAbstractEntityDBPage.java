package tech.derbent.api.entity.view;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.router.BeforeEnterEvent;
import jakarta.annotation.PostConstruct;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntity;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CMasterViewSectionBase;
import tech.derbent.api.grid.view.CMasterViewSectionGrid;
import tech.derbent.api.interfaces.ILayoutChangeListener;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.CCrudToolbar;
import tech.derbent.api.ui.component.CFlexLayout;
import tech.derbent.api.ui.component.CSearchToolbar;
import tech.derbent.api.ui.component.CVerticalLayout;
import tech.derbent.api.ui.component.ICrudToolbarOwnerPage;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CAccordionDBEntity;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.base.session.service.CLayoutService;
import tech.derbent.base.session.service.ISessionService;

public abstract class CAbstractEntityDBPage<EntityClass extends CEntityDB<EntityClass>> extends CAbstractPage
		implements ILayoutChangeListener, ICrudToolbarOwnerPage, IPageServiceImplementer<EntityClass> {

	private static final long serialVersionUID = 1L;
	ArrayList<CAccordionDBEntity<EntityClass>> AccordionList = new ArrayList<CAccordionDBEntity<EntityClass>>(); // List of accordions
	private CFlexLayout baseDetailsLayout;
	private final CEnhancedBinder<EntityClass> binder;
	protected CCrudToolbar crudToolbar;
	private EntityClass currentEntity;
	protected String currentSearchText = "";
	private final Div detailsTabLayout = new Div();
	protected final Class<EntityClass> entityClass;
	protected final CAbstractService<EntityClass> entityService;
	protected CLayoutService layoutService; // Optional injection
	protected CMasterViewSectionBase<EntityClass> masterViewSection;
	protected CProjectItemStatusService projectItemStatusService; // Optional injection
	protected CSearchToolbar searchToolbar;
	protected ISessionService sessionService;
	protected SplitLayout splitLayout = new SplitLayout();
	protected CWorkflowStatusRelationService workflowStatusRelationService; // Optional injection

	protected CAbstractEntityDBPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService,
			final ISessionService sessionService) {
		super();
		this.entityClass = entityClass;
		this.entityService = entityService;
		this.sessionService = sessionService;
		binder = new CEnhancedBinder<>(entityClass);
		// Initialize CRUD toolbar with minimal constructor and configure
		crudToolbar = new CCrudToolbar();
		crudToolbar.setPageBase(this);
		createPageContent();
	}

	public EntityClass actionCreate() {
		try {
			final EntityClass newEntity = createNewEntity();
			setCurrentEntity(newEntity);
			populateForm();
			return newEntity;
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity", e);
			showErrorNotification("Failed to create new entity");
		}
		return null;
	}

	public void actionDelete() {
		try {
			final EntityClass current = getCurrentEntity();
			if (current == null) {
				CNotificationService.showWarning("Please select an item to delete.");
				return;
			}
			// Use notification service to confirm deletion if available
			try {
				CNotificationService.showConfirmationDialog("Delete selected item?", () -> {
					try {
						entityService.delete(current.getId());
						refreshGrid();
						CNotificationService.showDeleteSuccess();
					} catch (final Exception ex) {
						LOGGER.error("Error deleting entity", ex);
						showErrorNotification("Failed to delete item");
					}
				});
			} catch (final Exception e) {
				LOGGER.error("Error showing confirmation dialog", e);
				showErrorNotification("Failed to delete item");
			}
		} catch (final Exception e) {
			LOGGER.error("Unexpected error during delete action", e);
			showErrorNotification("Failed to delete item");
		}
	}

	public void actionRefresh() {
		try {
			final EntityClass current = getCurrentEntity();
			if (current != null && current.getId() != null) {
				final EntityClass reloaded = entityService.getById(current.getId()).orElse(null);
				if (reloaded != null) {
					setCurrentEntity(reloaded);
					populateForm();
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error refreshing entity: {}", e.getMessage());
			showErrorNotification("Failed to refresh entity");
		}
	}

	public void actionSave() {
		{
			try {
				final EntityClass entity = getCurrentEntity();
				LOGGER.debug("Save action invoked, current entity: {}", entity != null ? entity.getId() : "null");
				if (entity == null) {
					LOGGER.warn("No current entity for save operation");
					return;
				}
				if (!onBeforeSaveEvent()) {
					return;
				}
				// Write form data to entity
				getBinder().writeBean(entity);
				// Validate entity before saving
				validateEntityForSave(entity);
				// Save entity
				final EntityClass savedEntity = entityService.save(entity);
				LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
				// Update current entity with saved version (includes generated ID)
				setCurrentEntity(savedEntity);
				populateForm();
				// Show success notification
				CNotificationService.showSaveSuccess();
				navigateToClass();
			} catch (final ObjectOptimisticLockingFailureException exception) {
				LOGGER.error("Optimistic locking failure during save", exception);
				CNotificationService.showOptimisticLockingError();
				throw new RuntimeException("Optimistic locking failure during save", exception);
			} catch (final ValidationException validationException) {
				LOGGER.error("Validation error during save", validationException);
				CNotificationService.showWarning("Failed to save the data. Please check that all required fields are filled and values are valid.");
				throw new RuntimeException("Validation error during save", validationException);
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
				showErrorNotification("An unexpected error occurred while saving. Please try again.");
				throw new RuntimeException("Unexpected error during save operation", exception);
			}
		}
	}

	// for details view
	protected void addAccordionPanel(final CAccordionDBEntity<EntityClass> accordion) {
		AccordionList.add(accordion);
		getBaseDetailsLayout().add(accordion);
	}

	// this method is called before the page is entered
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		LOGGER.debug("beforeEnter called for {}", getClass().getSimpleName());
		if (masterViewSection == null) {
			LOGGER.warn("Grid is null, cannot populate form");
			return;
		}
		Optional<EntityClass> lastEntity = null;
		final Optional<Long> entityID = event.getRouteParameters().get(getEntityRouteIdField()).map(Long::parseLong);
		if (entityID.isPresent()) {
			lastEntity = entityService.getById(entityID.get());
			if (lastEntity.isEmpty()) {
				LOGGER.warn("Entity with ID {} not found in database", entityID.get());
			}
		} else {
			lastEntity = entityService.getById(sessionService.getActiveId(entityClass.getSimpleName()));
		}
		// populateForm(null);
		masterViewSection.selectLastOrFirst(lastEntity.orElse(null));
	}

	protected void createButtonLayout(final Div layout) {
		LOGGER.debug("createButtonLayout called - default save/delete/cancel buttons are now in details tab");
		// Default implementation does nothing - buttons are in the tab Subclasses can
		// override this for additional custom buttons in the main content area
	}

	@PostConstruct
	private final void createDetails() throws Exception {
		createDetailsViewTab();
		createDetailsComponent();
		updateDetailsComponent();
	}

	protected abstract void createDetailsComponent() throws Exception;

	private void createDetailsSection() {
		baseDetailsLayout = CFlexLayout.forEntityPage();
		detailsTabLayout.setClassName("details-tab-layout");
		// now the content are!!!
		final Scroller detailsScroller = new Scroller();
		// FLEX LAYOUT///////////////////
		detailsScroller.setContent(baseDetailsLayout);
		detailsScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		final CVerticalLayout detailsBase = new CVerticalLayout(false, false, false);
		detailsBase.add(detailsTabLayout, detailsScroller);
		initSplitLayout(detailsBase);
		// Initial layout setup - will be updated when layout service is available
		updateLayoutOrientation();
	}

	/** Creates the default details view tab content with save/delete/cancel buttons. Subclasses can override this method to customize the tab content
	 * while maintaining consistent button placement and styling. */
	protected void createDetailsViewTab() {
		// Clear any existing content
		getDetailsTabLayout().removeAll();
		getDetailsTabLayout().add(crudToolbar);
	}

	public abstract void createGridForEntity(final CGrid<EntityClass> grid);

	protected void createGridLayout() throws Exception {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
		masterViewSection.addSelectionChangeListener(this::onSelectionChanged);
		// Create search toolbar if entity supports searching
		if (ISearchable.class.isAssignableFrom(entityClass)) {
			searchToolbar = new CSearchToolbar("Search " + entityClass.getSimpleName().replace("C", "").toLowerCase() + "...");
			searchToolbar.addSearchListener(event -> {
				try {
					currentSearchText = event.getSearchText();
					refreshGrid();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		masterViewSection.setDataProvider(getMasterQuery());
		final VerticalLayout gridContainer = new VerticalLayout();
		gridContainer.setClassName("grid-container");
		gridContainer.setPadding(false);
		gridContainer.setSpacing(false);
		if (searchToolbar != null) {
			gridContainer.add(searchToolbar);
		}
		gridContainer.add(masterViewSection);
		gridContainer.setFlexGrow(1, masterViewSection);
		splitLayout.addToPrimary(gridContainer);
	}

	@PostConstruct
	protected final void createMaster() throws Exception {
		createMasterComponent();
		updateMasterComponent();
	}

	protected abstract void createMasterComponent() throws Exception;

	protected EntityClass createNewEntity() throws Exception {
		return entityService.newEntity();
	}

	private void createPageContent() {
		createDetailsSection();
	}

	public HasComponents getBaseDetailsLayout() { return baseDetailsLayout; }

	@Override
	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	@Override
	public CCrudToolbar getCrudToolbar() { return crudToolbar; }

	@Override
	public EntityClass getCurrentEntity() { return currentEntity; }

	@Override
	public String getCurrentEntityIdString() {
		LOGGER.debug("Getting current entity ID string for entity class: {}", entityClass.getSimpleName());
		final EntityClass entity = getCurrentEntity();
		return entity != null ? entity.getId().toString() : null;
	}

	public Div getDetailsTabLayout() { return detailsTabLayout; }

	@Override
	public Class<EntityClass> getEntityClass() { return entityClass; }

	protected abstract String getEntityRouteIdField();
	// protected abstract CallbackDataProvider<EntityClass, Void> getMasterQuery();

	@Override
	public CAbstractService<EntityClass> getChildService() { return entityService; }

	protected CallbackDataProvider<EntityClass, Void> getMasterQuery() {
		return new CallbackDataProvider<>(query -> {
			// --- sort (manuel çeviri)
			final List<QuerySortOrder> sortOrders = Optional.ofNullable(query.getSortOrders()).orElse(java.util.Collections.emptyList());
			final Sort springSort = sortOrders.isEmpty() ? Sort.unsorted()
					: Sort.by(sortOrders.stream().map(so -> new Sort.Order(
							so.getDirection() == com.vaadin.flow.data.provider.SortDirection.DESCENDING ? Sort.Direction.DESC : Sort.Direction.ASC,
							so.getSorted())).toList());
			// --- paging
			final int limit = query.getLimit();
			final int offset = query.getOffset();
			final int page = (limit > 0) ? (offset / limit) : 0;
			final Pageable pageable = CPageableUtils.validateAndFix(PageRequest.of(page, Math.max(limit, 1), springSort));
			final String term = (currentSearchText == null) ? "" : currentSearchText.trim();
			// *** TEK KAYNAK: her zaman search'lü metodu kullan ***
			return entityService.list(pageable, term).stream();
		}, e -> {
			final String term = (currentSearchText == null) ? "" : currentSearchText.trim();
			final long total = entityService.list(PageRequest.of(0, 1), term).getTotalElements();
			return (int) Math.min(total, Integer.MAX_VALUE);
		});
	}

	@Override
	public abstract CPageService<EntityClass> getPageService();

	/** Gets the search toolbar component, if available.
	 * @return the search toolbar component, or null if entity doesn't support searching */
	public CSearchToolbar getSearchToolbar() {
		return searchToolbar;
	}

	@Override
	public tech.derbent.app.workflow.service.CWorkflowStatusRelationService getWorkflowStatusRelationService() {
		return workflowStatusRelationService;
	}

	@PostConstruct
	protected void initPageId() {
		// set page ID in this syntax to check with playwright tests
		final String pageid = this.getClass().getSimpleName().toLowerCase();
		super.setId("pageid-" + pageid);
		super.addClassNames("class-" + pageid);
	}

	private void initSplitLayout(final VerticalLayout detailsBase) {
		splitLayout.setSizeFull();
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.addToSecondary(detailsBase);
		add(splitLayout);
	}

	/** Navigates to the view class. Throws exception if navigation fails. */
	protected void navigateToClass() {
		final UI currentUI = UI.getCurrent();
		if (currentUI != null) {
			currentUI.navigate(getClass());
		} else {
			LOGGER.error("UI not available for navigation to {}", getClass().getSimpleName());
			throw new IllegalStateException("UI not available for navigation to " + getClass().getSimpleName());
		}
	}

	// this method is called when the page is attached to the UI
	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Register for layout change notifications if service is available
		if (layoutService != null) {
			layoutService.addLayoutChangeListener(this);
			// Update layout based on current mode
			updateLayoutOrientation();
		}
	}

	protected boolean onBeforeSaveEvent() {
		if (!entityService.onBeforeSaveEvent(currentEntity)) {
			LOGGER.warn("onBeforeSaveEvent failed for entity: {} in {}", getCurrentEntity(), this.getClass().getSimpleName());
			return false;
		}
		return true;
	}

	protected void onClonedItem(final CEntity<?> clonedItem) {
		LOGGER.debug("Cloned item callback received: {}", clonedItem);
	}

	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Unregister from layout change notifications
		if (layoutService != null) {
			layoutService.removeLayoutChangeListener(this);
		}
	}

	// Implementation of IEntityUpdateListener interface
	@Override
	public void onEntityCreated(final EntityClass entity) {
		LOGGER.debug("Entity created, populating form");
		final EntityClass newEntity = entity;
		setCurrentEntity(newEntity);
		populateForm();
	}

	@Override
	public void onEntityDeleted(final EntityClass entity) throws Exception {
		LOGGER.debug("Entity deleted, refreshing grid");
		masterViewSection.selectLastOrFirst(null);
		refreshGrid();
		CNotificationService.showDeleteSuccess();
	}

	@Override
	public void onEntitySaved(final EntityClass entity) throws Exception {
		LOGGER.debug("Entity saved, refreshing grid");
		refreshGrid();
		// Update current entity with saved version
		final EntityClass savedEntity = entity;
		setCurrentEntity(savedEntity);
		populateForm();
		// Show success notification
		CNotificationService.showSaveSuccess();
		navigateToClass();
	}

	@Override
	public void onLayoutModeChanged(final CLayoutService.LayoutMode newMode) {
		LOGGER.debug("Layout mode changed to: {} for {}", newMode, getClass().getSimpleName());
		updateLayoutOrientation();
	}

	protected void onSelectionChanged(final CMasterViewSectionBase.SelectionChangeEvent<EntityClass> event) {
		final EntityClass value = (event.getSelectedItem());
		LOGGER.debug("Grid selection changed: {}", Optional.ofNullable(value).map(Object::toString).orElse("NULL"));
		setCurrentEntity(value);
		populateForm();
		// Update CRUD toolbar with current entity
		crudToolbar.setCurrentEntity(value);
		if (value == null) {
			sessionService.setActiveId(entityClass.getClass().getSimpleName(), null);
		} else {
			sessionService.setActiveId(entityClass.getClass().getSimpleName(), value.getId());
		}
	}

	protected void populateAccordionPanels(final EntityClass entity) {
		LOGGER.debug("Populating accordion panels for entity: {}", entity != null ? entity.getId() : "null");
		// This method can be overridden by subclasses to populate accordion panels
		AccordionList.forEach(accordion -> {
			accordion.populateForm(entity);
		});
	}

	@Override
	public void populateForm() {
		final EntityClass value = getCurrentEntity();
		LOGGER.debug("Populating form for entity: {}", value != null ? value.getId() : "null");
		populateAccordionPanels(value);
		getBinder().setBean(value);
		if ((value == null) && (masterViewSection != null)) {
			masterViewSection.select(null);
		}
	}

	@Override
	public void refreshGrid() throws Exception {
		LOGGER.info("Refreshing grid for {}", getClass().getSimpleName());
		// Store the currently selected entity ID to preserve selection after refresh
		final EntityClass selectedEntity = masterViewSection.getSelectedItem();
		final Long selectedEntityId = selectedEntity != null ? selectedEntity.getId() : null;
		// Clear selection and refresh data
		masterViewSection.select(null);
		masterViewSection.refreshMasterView();
		// Restore selection if there was a previously selected entity
		if (selectedEntityId != null) {
			masterViewSection.selectLastOrFirst(entityService.getById(selectedEntityId).orElse(null));
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	public void setCurrentEntity(final CEntityDB<?> currentEntity) {
		LOGGER.debug("Setting current entity: {}", currentEntity);
		this.currentEntity = (EntityClass) currentEntity;
	}

	@Override
	public void setId(final String id) {
		throw new UnsupportedOperationException("Use initPageId instead to set page ID for testing purposes");
	}

	/** Sets the layout service. This is typically called via dependency injection or manually after construction. */
	public void setLayoutService(final CLayoutService layoutService) {
		this.layoutService = layoutService;
		// Update layout based on current mode
		updateLayoutOrientation();
	}

	@Override
	protected void setupToolbar() {}

	/** Shows an error notification. Uses CNotificationService if available, falls back to direct Vaadin call. */
	protected void showErrorNotification(final String message) {
		CNotificationService.showError(message);
	}

	/** Shows a success notification. Uses CNotificationService if available, falls back to direct Vaadin call. */
	protected void showNotification(final String message) {
		CNotificationService.showSuccess(message);
	}

	protected abstract void updateDetailsComponent()
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, Exception;

	/** Updates the split layout orientation based on the current layout mode. */
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

	protected void updateMasterComponent() throws Exception {
		if (masterViewSection == null) {
			LOGGER.warn("Grid is null, cannot add selection listener");
			return;
		}
		masterViewSection.addSelectionChangeListener(this::onSelectionChanged);
		// Create search toolbar if entity supports searching
		if (ISearchable.class.isAssignableFrom(entityClass)) {
			searchToolbar = new CSearchToolbar("Search " + entityClass.getSimpleName().replace("C", "").toLowerCase() + "...");
			searchToolbar.addSearchListener(event -> {
				try {
					currentSearchText = event.getSearchText();
					refreshGrid();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		masterViewSection.setDataProvider(getMasterQuery());
		// Create the grid container with search toolbar
		final VerticalLayout gridContainer = new VerticalLayout();
		gridContainer.setClassName("grid-container");
		gridContainer.setPadding(false);
		gridContainer.setSpacing(false);
		// Add search toolbar if available
		if (searchToolbar != null) {
			gridContainer.add(searchToolbar);
		}
		gridContainer.add(masterViewSection);
		gridContainer.setFlexGrow(1, masterViewSection);
		splitLayout.addToPrimary(gridContainer);
	}

	/** Validates an entity before saving. Subclasses can override this method to add custom validation logic beyond the standard bean validation.
	 * @param entity the entity to validate
	 * @throws IllegalArgumentException if validation fails */
	protected void validateEntityForSave(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null for validation");
	}
}
