package tech.derbent.app.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.ui.component.basic.CFlexLayout;
import tech.derbent.api.ui.component.basic.CScroller;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentDetailsMasterToolbar;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** Enhanced dynamic page view that supports grid and detail sections for database-defined pages. This view displays content stored in CPageEntity
 * instances with configurable grid and detail sections. */
@PermitAll
public class CDynamicPageViewWithSections extends CDynamicPageViewForEntityEdit {
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageViewWithSections.class);
	private static final long serialVersionUID = 1L;
	// State tracking for performance optimization
	protected CComponentGridEntity grid;
	private final CGridEntityService gridEntityService;
	private final CVerticalLayout splitBottomLayout = new CVerticalLayout(false, false, false);
	// Layout components
	protected final SplitLayout splitLayout = new SplitLayout();

	@Autowired
	public CDynamicPageViewWithSections(final CPageEntity pageEntity, final ISessionService sessionService,
			final CDetailSectionService detailSectionService, final CGridEntityService gridEntityService) throws Exception {
		super(pageEntity, sessionService, detailSectionService);
		this.gridEntityService = gridEntityService;
		try {
			initializePage();
		} catch (final Exception e) {
			CNotificationService.showException("Failed to initialize dynamic page view with sections for: " + pageEntity.getPageTitle(), e);
		}
	}

	/** Create the details section. */
	private void createDetailsSection() {
		splitLayout.addToSecondary(splitBottomLayout);
		final CScroller detailsScroller = new CScroller();
		splitBottomLayout.add(detailsScroller);
		detailsScroller.setContent(baseDetailsLayout);
	}

	/** Create the master (grid) section. */
	private void createMasterSection() {
		try {
			// LOGGER.debug("Creating master section with grid entity");
			Check.notNull(pageEntity.getGridEntity(), "Grid entity cannot be null");
			// Create the grid component using the configured grid entity
			grid = new CComponentGridEntity(pageEntity.getGridEntity(), getSessionService());
			// Set the content owner so widget columns can access page service
			grid.setContentOwner(this);
			// Listen for selection changes from the grid
			grid.addSelectionChangeListener(event -> {
				try {
					onEntitySelected(event);
				} catch (final Exception e) {
					CNotificationService.showException("Error handling entity selection from grid", e);
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
				((CEntityOfProject<?>) newEntity).setProject(getSessionService().getActiveProject().orElse(null));
			}
			// Special handling for CUser entities - create project association through CUserProjectSettings
			else if (newEntity instanceof CUser) {
				final CProject activeProject = getSessionService().getActiveProject().orElse(null);
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

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		return createNewEntity();
	}

	@Override
	public CFlexLayout getBaseDetailsLayout() { return baseDetailsLayout; }

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
			// Create toolbar with minimal constructor and configure
			crudToolbar = new CCrudToolbar();
			crudToolbar.setPageBase(this);
			configureCrudToolbar(crudToolbar);
			splitBottomLayout.addComponentAsFirst(crudToolbar);
			grid.selectNextItem();
		} catch (final Exception e) {
			LOGGER.error("Error initializing dynamic page view:" + e.getMessage());
			throw e;
		}
	}

	@Override
	protected void locateItemById(final Long pageItemId) {
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

	// Implementation of CEntityUpdateListener
	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityDeleted(final CEntityDB entity) {
		try {
			LOGGER.debug("Entity deleted notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(grid, "Grid component is not initialized");
			// Refresh grid and clear details
			refreshGrid();
			clearEntityDetails();
			grid.selectNextItem();
			CNotificationService.showDeleteSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error handling entity deleted notification:" + e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntitySaved(final CEntityDB entity) {
		try {
			LOGGER.debug("Entity saved notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(grid, "Grid component is not initialized");
			Check.notNull(entity, "Saved entity cannot be null");
			refreshGrid();
			// Select the saved entity in the grid to maintain selection after save
			// Only select if not already selected to avoid triggering selection change loop
			final CEntityDB<?> currentSelection = grid.getSelectedItem();
			if ((currentSelection == null) || !entity.getId().equals(currentSelection.getId())) {
				grid.selectEntity(entity);
				LOGGER.debug("Selected saved entity in grid: {}", entity.getId());
			} else {
				LOGGER.debug("Entity already selected in grid, skipping selection to avoid loop");
			}
		} catch (final Exception e) {
			LOGGER.error("Error handling entity saved notification:" + e.getMessage());
			throw e;
		}
	}

	/** Handle entity selection events from the grid. */
	private void onEntitySelected(final CComponentGridEntity.SelectionChangeEvent event) throws Exception {
		LOGGER.debug("Entity selection changed event received: {}", event);
		Check.notNull(event, "Selection change event cannot be null");
		final CEntityDB<?> selectedEntity = event.getSelectedItem();
		onEntitySelected(selectedEntity);
	}

	/** Refresh the grid to show updated data. */
	@Override
	public void refreshGrid() {
		grid.refreshGridData();
	}

	/** Select the first item in the grid. Used after discarding unsaved new entities. */
	@Override
	public void selectFirstInGrid() {
		grid.selectFirstItem();
	}

	/** Reloads entity values into existing components without rebuilding the UI */
	@Override
	public void setCurrentEntity(final CEntityDB<?> entity) {
		LOGGER.debug("Setting current entity in dynamic page view with sections: {}", entity);
		try {
			super.setCurrentEntity(entity);
			if (entity == null) {
				if (crudToolbar != null) {
					crudToolbar.setCurrentEntity(null);
				}
			} else if (crudToolbar != null) {
				crudToolbar.setCurrentEntity(entity);
			}
		} catch (final Exception e) {
			LOGGER.error("Error setting current entity in toolbar:" + e.getMessage());
			throw e;
		}
	}
}
