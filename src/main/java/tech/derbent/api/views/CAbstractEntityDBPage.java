package tech.derbent.api.views;

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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.router.BeforeEnterEvent;
import jakarta.annotation.PostConstruct;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.components.CSearchToolbar;
import tech.derbent.api.domains.CEntity;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.CLayoutChangeListener;
import tech.derbent.api.interfaces.CSearchable;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.api.views.components.CFlexLayout;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.api.views.dialogs.CDialogClone;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.api.views.grids.CMasterViewSectionBase;
import tech.derbent.api.views.grids.CMasterViewSectionGrid;
import tech.derbent.session.service.CLayoutService;
import tech.derbent.session.service.ISessionService;

public abstract class CAbstractEntityDBPage<EntityClass extends CEntityDB<EntityClass>> extends CAbstractPage
		implements CLayoutChangeListener, IContentOwner {

	private static final long serialVersionUID = 1L;
	ArrayList<CAccordionDBEntity<EntityClass>> AccordionList = new ArrayList<CAccordionDBEntity<EntityClass>>(); // List of accordions
	private CFlexLayout baseDetailsLayout;
	private final CEnhancedBinder<EntityClass> binder;
	private EntityClass currentEntity;
	protected String currentSearchText = "";
	// private final VerticalLayout baseDetailsLayout = new VerticalLayout();
	private final Div detailsTabLayout = new Div();
	protected final Class<EntityClass> entityClass;
	protected final CAbstractService<EntityClass> entityService;
	protected CLayoutService layoutService; // Optional injection
	protected CMasterViewSectionBase<EntityClass> masterViewSection;
	protected CNotificationService notificationService; // Optional injection
	// Search functionality
	protected CSearchToolbar searchToolbar;
	protected ISessionService sessionService;
	// divide screen into two parts
	protected SplitLayout splitLayout = new SplitLayout();

	protected CAbstractEntityDBPage(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService,
			final ISessionService sessionService) {
		super();
		this.entityClass = entityClass;
		this.entityService = entityService;
		this.sessionService = sessionService;
		binder = new CEnhancedBinder<>(entityClass);
		createPageContent();
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

	protected CButton createCancelButton(final String buttonText) {
		final CButton cancel = CButton.createCancelButton(buttonText, e -> {
			final Long lastSelectedId = sessionService.getActiveId(getClass().getSimpleName());
			masterViewSection.selectLastOrFirst(entityService.getById(lastSelectedId).orElse(null));
		});
		return cancel;
	}

	protected CButton createCloneButton(final String buttonText) {
		final CButton cloneButton = CButton.createCloneButton(buttonText, e -> {
			LOGGER.debug("Clone button clicked");
			try {
				if (currentEntity == null) {
					new CWarningDialog("Please select an item to clone.").open();
					return;
				}
				final EntityClass selectedEntity = masterViewSection.getSelectedItem();
				final CDialogClone<EntityClass> dialog = new CDialogClone<EntityClass>(selectedEntity, this::onClonedItem);
				dialog.open();
			} catch (final Exception exception) {
				LOGGER.error("Error cloning entity", exception);
				new CWarningDialog("Failed to clone the item. Please try again.").open();
				throw new RuntimeException("Error cloning entity", exception);
			}
		});
		return cloneButton;
	}

	protected CButton createDeleteButton(final String buttonText) {
		final CButton delete = CButton.createDeleteButton(buttonText, null);
		delete.addClickListener(e -> {
			if (currentEntity == null) {
				new CWarningDialog("Please select an item to delete.").open();
				return;
			}
			// Show confirmation dialog for delete operation
			final String confirmMessage = String.format("Are you sure you want to delete this %s? This action cannot be undone.",
					entityClass.getSimpleName().replace("C", "").toLowerCase());
			new CConfirmationDialog(confirmMessage, () -> {
				try {
					LOGGER.info("Deleting entity: {} with ID: {}", entityClass.getSimpleName(), currentEntity.getId());
					entityService.delete(currentEntity);
					masterViewSection.selectLastOrFirst(null);
					if (notificationService != null) {
						notificationService.showDeleteSuccess();
					} else {
						showNotification("Item deleted successfully");
					}
				} catch (final Exception exception) {
					LOGGER.error("Error deleting entity", exception);
					new CWarningDialog("Failed to delete the item. Please try again.").open();
					throw new RuntimeException("Error deleting entity", exception);
				}
			}).open();
		});
		return delete;
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

	/** Creates the button layout for the details tab. Contains new, save, cancel, and delete buttons with consistent styling.
	 * @return HorizontalLayout with action buttons */
	protected HorizontalLayout createDetailsTabButtonLayout() {
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("details-tab-button-layout");
		buttonLayout.setSpacing(true);
		buttonLayout.add(createNewButton("New"), createCloneButton("Clone"), createSaveButton("Save"), createCancelButton("Cancel"),
				createDeleteButton("Delete"));
		return buttonLayout;
	}

	/** Creates the left content of the details tab. Subclasses can override this to provide custom tab content.
	 * @return Component for the left side of the tab */
	protected Div createDetailsTabLeftContent() {
		final Div detailsTabLabel = new Div();
		detailsTabLabel.setText("Details");
		detailsTabLabel.setClassName("details-tab-label");
		return detailsTabLabel;
	}

	/** Creates the default details view tab content with save/delete/cancel buttons. Subclasses can override this method to customize the tab content
	 * while maintaining consistent button placement and styling. */
	protected void createDetailsViewTab() {
		// Clear any existing content
		getDetailsTabLayout().removeAll();
		// Create a horizontal layout for the tab content
		final HorizontalLayout tabContent = new HorizontalLayout();
		tabContent.setWidthFull();
		tabContent.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
		tabContent.setPadding(true);
		tabContent.setSpacing(true);
		tabContent.setClassName("details-tab-content");
		// Left side: Tab label or custom content (can be overridden by subclasses)
		final Div leftContent = createDetailsTabLeftContent();
		// Right side: Action buttons
		final HorizontalLayout buttonLayout = createDetailsTabButtonLayout();
		tabContent.add(leftContent, buttonLayout);
		getDetailsTabLayout().add(tabContent);
	}

	public abstract void createGridForEntity(final CGrid<EntityClass> grid);

	protected void createGridLayout() {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
		masterViewSection.addSelectionChangeListener(this::onSelectionChanged);
		// Create search toolbar if entity supports searching
		if (CSearchable.class.isAssignableFrom(entityClass)) {
			searchToolbar = new CSearchToolbar("Search " + entityClass.getSimpleName().replace("C", "").toLowerCase() + "...");
			searchToolbar.addSearchListener(event -> {
				currentSearchText = event.getSearchText();
				refreshGrid();
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

	@PostConstruct
	protected final void createMaster() throws Exception {
		createMasterComponent();
		updateMasterComponent();
	}

	protected abstract void createMasterComponent();

	protected CButton createNewButton(final String buttonText) {
		final CButton newButton = CButton.createNewButton(buttonText, e -> {
			LOGGER.debug("New button clicked, emptying bound fields");
			try {
				// Empty the bound fields by creating new entity and binding it
				final EntityClass newEntityInstance = createNewEntity();
				setCurrentEntity(newEntityInstance);
				populateForm(newEntityInstance);
				LOGGER.debug("Bound fields emptied for new entity: {}", newEntityInstance.getClass().getSimpleName());
			} catch (final Exception exception) {
				LOGGER.error("Error emptying bound fields", exception);
				new CWarningDialog("Failed to empty fields. Please try again.").open();
				throw new RuntimeException("Error emptying bound fields", exception);
			}
		});
		return newButton;
	}

	protected EntityClass createNewEntity() {
		return entityService.newEntity();
	}

	private void createPageContent() {
		createDetailsSection();
	}

	protected CButton createSaveButton(final String buttonText) {
		final CButton save = CButton.createSaveButton(buttonText, e -> {
			try {
				// Ensure we have an entity to save
				LOGGER.debug("Save button clicked, current entity: {}", currentEntity != null ? currentEntity.getId() : "null");
				if (currentEntity == null) {
					LOGGER.warn("No current entity for save operation, creating new entity");
					currentEntity = createNewEntity();
					populateForm(currentEntity);
				}
				if (!onBeforeSaveEvent()) {
					return;
				}
				// Write form data to entity
				getBinder().writeBean(currentEntity);
				// Validate entity before saving
				validateEntityForSave(currentEntity);
				// Save entity
				final EntityClass savedEntity = entityService.save(currentEntity);
				LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
				// Update current entity with saved version (includes generated ID)
				setCurrentEntity(savedEntity);
				// Clear form and refresh grid
				refreshGrid();
				// Show success notification
				if (notificationService != null) {
					notificationService.showSaveSuccess();
				} else {
					showNotification("Data saved successfully");
				}
				// Navigate back to the current view (list mode)
				navigateToClass();
			} catch (final ObjectOptimisticLockingFailureException exception) {
				LOGGER.error("Optimistic locking failure during save", exception);
				if (notificationService != null) {
					notificationService.showOptimisticLockingError();
				} else {
					showErrorNotification("Error updating the data. Somebody else has updated the record while you were making changes.");
				}
				throw new RuntimeException("Optimistic locking failure during save", exception);
			} catch (final ValidationException validationException) {
				LOGGER.error("Validation error during save", validationException);
				new CWarningDialog("Failed to save the data. Please check that all required fields are filled and values are valid.").open();
				throw new RuntimeException("Validation error during save", validationException);
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
				new CWarningDialog("An unexpected error occurred while saving. Please try again.").open();
				throw new RuntimeException("Unexpected error during save operation", exception);
			}
		});
		return save;
	}

	public HasComponents getBaseDetailsLayout() { return baseDetailsLayout; }

	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	@Override
	public EntityClass getCurrentEntity() { return currentEntity; }

	public Div getDetailsTabLayout() { return detailsTabLayout; }

	public Class<EntityClass> getEntityClass() { return entityClass; }

	protected abstract String getEntityRouteIdField();
	// protected abstract CallbackDataProvider<EntityClass, Void> getMasterQuery();

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
		}, query -> {
			final String term = (currentSearchText == null) ? "" : currentSearchText.trim();
			final long total = entityService.list(PageRequest.of(0, 1), term).getTotalElements();
			return (int) Math.min(total, Integer.MAX_VALUE);
		});
	}

	/** Gets the search toolbar component, if available.
	 * @return the search toolbar component, or null if entity doesn't support searching */
	public CSearchToolbar getSearchToolbar() {
		return searchToolbar;
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

	@Override
	public void onLayoutModeChanged(final CLayoutService.LayoutMode newMode) {
		LOGGER.debug("Layout mode changed to: {} for {}", newMode, getClass().getSimpleName());
		updateLayoutOrientation();
	}

	protected void onSelectionChanged(final CMasterViewSectionGrid.SelectionChangeEvent<EntityClass> event) {
		final EntityClass value = (event.getSelectedItem());
		LOGGER.debug("Grid selection changed: {}", Optional.ofNullable(value).map(Object::toString).orElse("NULL"));
		populateForm(value);
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

	protected void populateForm(final EntityClass value) {
		LOGGER.debug("Populating form for entity: {}", value != null ? value.getId() : "null");
		currentEntity = value;
		// not needed anymore, handled in onSelectionChanged
		// sessionService.setActiveId(entityClass.getSimpleName(), value == null ? null : value.getId());
		populateAccordionPanels(value);
		// getBinder().readBean(value);
		getBinder().setBean(value);
		if ((value == null) && (masterViewSection != null)) {
			masterViewSection.select(null);
		}
	}

	protected void refreshGrid() {
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

	/** Shows an error notification. Uses CNotificationService if available, falls back to direct Vaadin call. */
	protected void showErrorNotification(final String message) {
		if (notificationService != null) {
			notificationService.showError(message);
		} else {
			// Fallback to direct Vaadin call if service not injected
			final Notification notification = Notification.show(message);
			notification.setPosition(Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			LOGGER.debug("Shown error notification (fallback): {}", message);
		}
	}

	/** Shows a success notification. Uses CNotificationService if available, falls back to direct Vaadin call. */
	protected void showNotification(final String message) {
		if (notificationService != null) {
			notificationService.showSuccess(message);
		} else {
			// Fallback to direct Vaadin call if service not injected
			final Notification notification = Notification.show(message);
			notification.setPosition(Position.BOTTOM_START);
			LOGGER.debug("Shown notification (fallback): {}", message);
		}
	}

	public void setCurrentEntity(final EntityClass currentEntity) {
		LOGGER.debug("Setting current entity: {}", currentEntity);
		this.currentEntity = currentEntity;
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

	/** Sets the notification service. This is typically called via dependency injection or manually after construction. */
	public void setNotificationService(final CNotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Override
	protected void setupToolbar() {}

	/** Populates the form with entity data - public wrapper for testing.
	 * @param entity the entity to populate the form with */
	public void testPopulateForm(final EntityClass entity) {
		populateForm(entity);
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
		if (CSearchable.class.isAssignableFrom(entityClass)) {
			searchToolbar = new CSearchToolbar("Search " + entityClass.getSimpleName().replace("C", "").toLowerCase() + "...");
			searchToolbar.addSearchListener(event -> {
				currentSearchText = event.getSearchText();
				refreshGrid();
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
