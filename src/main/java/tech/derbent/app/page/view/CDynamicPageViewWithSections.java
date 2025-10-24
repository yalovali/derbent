package tech.derbent.app.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentDetailsMasterToolbar;
import tech.derbent.api.views.components.CCrudToolbar;
import tech.derbent.api.views.components.CFlexLayout;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** Enhanced dynamic page view that supports grid and detail sections for database-defined pages. This view displays content stored in CPageEntity
 * instances with configurable grid and detail sections. */
@PermitAll
public class CDynamicPageViewWithSections extends CDynamicPageBase {

	public static final String DEFAULT_COLOR = "#341b00";
	public static final String DEFAULT_ICON = "vaadin:database";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageViewWithSections.class);
	private static final long serialVersionUID = 1L;
	private CCrudToolbar<?> crudToolbar;
	// State tracking for performance optimization
	protected CComponentGridEntity grid;
	private final CGridEntityService gridEntityService;
	private final CVerticalLayout splitBottomLayout = new CVerticalLayout(false, false, false);
	// Layout components
	protected final SplitLayout splitLayout = new SplitLayout();

	@Autowired
	public CDynamicPageViewWithSections(final CPageEntity pageEntity, final ISessionService sessionService,
			final CDetailSectionService detailSectionService, final CGridEntityService gridEntityService) {
		super(pageEntity, sessionService, detailSectionService);
		this.gridEntityService = gridEntityService;
		try {
			initializePage();
		} catch (Exception e) {
			LOGGER.error("Failed to initialize dynamic page view with sections for: {}: {}", pageEntity.getPageTitle(), e.getMessage());
			e.printStackTrace();
		}
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected CCrudToolbar<?> createCrudToolbar() {
		try {
			LOGGER.debug("Creating CRUD toolbar for entity type: {}", entityClass != null ? entityClass.getSimpleName() : "null");
			// Use static factory method to create toolbar
			final CCrudToolbar toolbar = new CCrudToolbar(this, entityService, entityClass, currentBinder);
			toolbar.setCurrentEntity(null);
			toolbar.setNewEntitySupplier(() -> {
				try {
					return createNewEntityInstance();
				} catch (final Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return toolbar;
			});
			toolbar.setRefreshCallback((currentEntity) -> {
				refreshGrid();
				if ((currentEntity != null) && (((CEntityDB<?>) currentEntity).getId() != null)) {
					try {
						final CEntityDB<?> reloadedEntity = entityService.getById(((CEntityDB<?>) currentEntity).getId()).orElse(null);
						setCurrentEntity(reloadedEntity);
						populateForm();
					} catch (final Exception e) {
						LOGGER.error("Error reloading entity: {}", e.getMessage());
					}
				}
			});
			toolbar.addUpdateListener(this);
			return toolbar;
		} catch (final Exception e) {
			LOGGER.error("Error creating CRUD toolbar:" + e.getMessage());
			throw e;
		}
	}

	/** Create the details section. */
	private void createDetailsSection() {
		splitLayout.addToSecondary(splitBottomLayout);
		final Scroller detailsScroller = new Scroller();
		splitBottomLayout.add(detailsScroller);
		detailsScroller.setContent(baseDetailsLayout);
		detailsScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		detailsScroller.setSizeFull();
	}

	/** Create the master (grid) section. */
	private void createMasterSection() {
		try {
			// LOGGER.debug("Creating master section with grid entity");
			Check.notNull(pageEntity.getGridEntity(), "Grid entity cannot be null");
			// Create the grid component using the configured grid entity
			grid = new CComponentGridEntity(pageEntity.getGridEntity());
			// Listen for selection changes from the grid
			grid.addSelectionChangeListener(event -> {
				try {
					onEntitySelected(event);
				} catch (final Exception e) {
					LOGGER.error("Error handling entity selection", e);
				}
			});
			// Create grid layout with toolbar
			final CVerticalLayout gridLayout = new CVerticalLayout();
			gridLayout.setSizeFull();
			gridLayout.add(new CComponentDetailsMasterToolbar(grid, gridEntityService));
			gridLayout.add(grid);
			// Allow the grid layout to expand
			gridLayout.getStyle().remove("max-height");
			// Add grid to the primary (top) section
			splitLayout.addToPrimary(gridLayout);
		} catch (final Exception e) {
			LOGGER.error("Error creating master section with grid entity:" + e.getMessage());
			throw e;
		}
	}

	/** Creates a new entity instance of the current entity type.
	 * @return a new entity instance
	 * @throws Exception */
	@Override
	@SuppressWarnings ("unchecked")
	protected <T extends CEntityDB<T>> T createNewEntity() throws Exception {
		try {
			if (entityClass == null) {
				throw new IllegalStateException("Entity class not initialized");
			}
			// Create new instance using reflection
			final T newEntity = (T) entityClass.getDeclaredConstructor().newInstance();
			// Set project if the entity supports it (check for CEntityOfProject)
			if (newEntity instanceof CEntityOfProject) {
				((CEntityOfProject<?>) newEntity).setProject(sessionService.getActiveProject().orElse(null));
			}
			// Special handling for CUser entities - create project association through CUserProjectSettings
			else if (newEntity instanceof CUser) {
				final CProject activeProject = sessionService.getActiveProject().orElse(null);
				if (activeProject != null) {
					final CUser user = (CUser) newEntity;
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
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity instance for type: {} - {}", entityClass.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	protected CEntityDB<?> createNewEntityInstance() throws Exception {
		return createNewEntity();
	}

	@Override
	public CFlexLayout getBaseDetailsLayout() { return baseDetailsLayout; }

	/** Initialize the page layout and content.
	 * @throws Exception */
	@Override
	protected void initializePage() throws Exception {
		try {
			super.initializePage();
			LOGGER.debug("Initializing dynamic page view with sections for: {}", pageEntity.getPageTitle());
			setSizeFull();
			if ((pageEntity.getPageTitle() != null) && !pageEntity.getPageTitle().trim().isEmpty()) {
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
		} catch (final Exception e) {
			LOGGER.error("Error initializing dynamic page view:" + e.getMessage());
			throw e;
		}
	}

	@Override
	protected void locateItemById(Long pageItemId) {
		try {
			if (pageItemId == null) {
				return;
			}
			Check.notNull(pageItemId, "Page item ID cannot be null");
			LOGGER.debug("Locating item by ID: {}", pageItemId);
			Check.notNull(grid, "Grid component is not initialized");
			final CEntityDB<?> entity = entityService.getById(pageItemId).orElse(null);
			if (entity != null) {
				grid.selectEntity(entity);
			} else {
				LOGGER.warn("No entity found for ID: {}", pageItemId);
			}
		} catch (final Exception e) {
			LOGGER.error("Error locating item by ID {}: {}", pageItemId, e.getMessage());
			throw new IllegalStateException("Error locating item by ID " + pageItemId + ": " + e.getMessage());
		}
	}

	@Override
	public void onEntityCreated(final CEntityDB<?> entity) throws Exception {
		try {
			LOGGER.debug("Entity created notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(entity, "Created entity cannot be null");
			// Rebuild details layout for the new entity if it doesn't exist yet
			if ((currentEntityViewName == null) || (currentEntityType == null)) {
				LOGGER.debug("Rebuilding details for newly created entity");
				rebuildEntityDetails(entity.getClass());
			}
			// Set the current entity and populate form
			setCurrentEntity(entity);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error handling entity created notification:" + e.getMessage());
			throw e;
		}
	}

	// Implementation of CEntityUpdateListener
	@Override
	public void onEntityDeleted(final CEntityDB<?> entity) {
		try {
			LOGGER.debug("Entity deleted notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(grid, "Grid component is not initialized");
			// Refresh grid and clear details
			refreshGrid();
			clearEntityDetails();
			grid.selectNextItem();
		} catch (final Exception e) {
			LOGGER.error("Error handling entity deleted notification:" + e.getMessage());
			throw e;
		}
	}

	@Override
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public void onEntitySaved(final CEntityDB<?> entity) {
		try {
			LOGGER.debug("Entity saved notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(grid, "Grid component is not initialized");
			Check.notNull(entity, "Saved entity cannot be null");
			refreshGrid();
			// Reload the entity from database to ensure all lazy-loaded fields are initialized
			// This prevents BindingException when populating form with entities that have lazy relationships
			final CAbstractService service = entityService;
			final CEntityDB<?> reloadedEntity = (CEntityDB<?>) service.getById(entity.getId()).orElse(entity);
			grid.selectEntity(reloadedEntity);
		} catch (final Exception e) {
			LOGGER.error("Error handling entity saved notification:" + e.getMessage());
			throw e;
		}
	}

	/** Handle entity selection events from the grid. */
	private void onEntitySelected(final CComponentGridEntity.SelectionChangeEvent event) throws Exception {
		try {
			// LOGGER.debug("Handling entity selection event");
			Check.notNull(event, "Selection change event cannot be null");
			final CEntityDB<?> selectedEntity = event.getSelectedItem();
			setCurrentEntity(selectedEntity);
			if (selectedEntity == null) {
				// No selection - clear details
				clearEntityDetails();
				populateForm();
			} else {
				setCurrentEntity(selectedEntity);
				if ((currentEntityViewName == null) || !selectedEntity.getClass().getField("VIEW_NAME").get(null).equals(currentEntityViewName)) {
					rebuildEntityDetails(selectedEntity.getClass());
				}
				populateForm();
			}
		} catch (final Exception e) {
			LOGGER.error("Error handling entity selection:" + e.getMessage());
			throw e;
		}
	}

	/** Refresh the grid to show updated data. */
	private void refreshGrid() {
		grid.refreshGridData();
	}

	/** Reloads entity values into existing components without rebuilding the UI */
	@Override
	public void setCurrentEntity(final Object entity) {
		try {
			super.setCurrentEntity(entity);
			if (entity == null) {
				if (crudToolbar != null) {
					crudToolbar.setCurrentEntity(null);
				}
			} else {
				crudToolbar.setCurrentEntity(entity);
			}
		} catch (final Exception e) {
			LOGGER.error("Error setting current entity in toolbar:" + e.getMessage());
			throw e;
		}
	}
}
