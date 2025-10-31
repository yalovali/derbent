package tech.derbent.app.page.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.orderedlayout.Scroller;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CCrudToolbar;
import tech.derbent.api.views.components.CFlexLayout;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.base.session.service.ISessionService;

/** Single entity dynamic page view for displaying pageEntity without grid. This page is used for displaying settings, user's single company, etc.
 * where there is only one item per user or per project or per application wide. Only works with pageEntity.getGridEntity().getAttributeNone() ==
 * true */
@PermitAll
public class CDynamicSingleEntityPageView extends CDynamicPageViewWithSections {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicSingleEntityPageView.class);
	private static final long serialVersionUID = 1L;
	// Configuration options for CRUD toolbar
	private boolean enableDeleteButton = false;
	private boolean enableNewButton = false;
	private boolean enableReloadButton = true;
	private boolean enableSaveButton = true;

	public CDynamicSingleEntityPageView(final CPageEntity pageEntity, final ISessionService sessionService,
			final CDetailSectionService detailSectionService, final CGridEntityService gridEntityService) {
		super(pageEntity, sessionService, detailSectionService, gridEntityService);
		// Validate that this page entity is configured for single entity display
		validateSingleEntityConfiguration();
		LOGGER.debug("Creating single entity dynamic page view for: {}", pageEntity.getPageTitle());
	}

	/** Configure CRUD toolbar buttons.
	 * @param enableDelete Enable/disable delete button
	 * @param enableNew    Enable/disable new button
	 * @param enableSave   Enable/disable save button
	 * @param enableReload Enable/disable reload/cancel button */
	public void configureCrudToolbar(boolean enableDelete, boolean enableNew, boolean enableSave, boolean enableReload) {
		enableDeleteButton = enableDelete;
		enableNewButton = enableNew;
		enableSaveButton = enableSave;
		enableReloadButton = enableReload;
		LOGGER.debug("CRUD toolbar configured - Delete: {}, New: {}, Save: {}, Reload: {}", enableDelete, enableNew, enableSave, enableReload);
	}

	@Override
	protected CCrudToolbar<?> createCrudToolbar(IContentOwner parentPage, Class<?> entityClass, CEnhancedBinder<?> currentBinder) {
		// Create the base toolbar using parent implementation
		CCrudToolbar<?> toolbar = super.createCrudToolbar(this, currentEntityType, currentBinder);
		// Configure button visibility based on our settings
		if (toolbar != null) {
			try {
				// Access private button fields and set visibility
				setButtonVisibility(toolbar, "createButton", enableNewButton);
				setButtonVisibility(toolbar, "deleteButton", enableDeleteButton);
				setButtonVisibility(toolbar, "saveButton", enableSaveButton);
				setButtonVisibility(toolbar, "refreshButton", enableReloadButton);
				LOGGER.debug("CRUD toolbar buttons configured for single entity view - Delete: {}, New: {}, Save: {}, Reload: {}", enableDeleteButton,
						enableNewButton, enableSaveButton, enableReloadButton);
			} catch (Exception e) {
				LOGGER.warn("Could not configure toolbar button visibility: {}", e.getMessage());
			}
		}
		return toolbar;
	}

	/** Create only the details section in full view (no grid) */
	private void createDetailsSection() {
		Check.notNull(getPageEntity().getDetailSection(), "Detail section cannot be null");
		// Create details layout that takes full space
		baseDetailsLayout = CFlexLayout.forEntityPage();
		baseDetailsLayout.setSizeFull();
		final Scroller detailsScroller = new Scroller();
		detailsScroller.setContent(baseDetailsLayout);
		detailsScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		detailsScroller.setSizeFull();
		final CVerticalLayout detailsBase = new CVerticalLayout(false, false, false);
		detailsBase.add(detailsScroller);
		detailsBase.setSizeFull();
		// Add details directly to this component (no split layout)
		add(detailsBase);
		LOGGER.debug("Created single entity details section with detail section: {}", getPageEntity().getDetailSection().getName());
	}
	// Helper methods are no longer needed since we can access protected fields/methods directly

	/** Override to create only details section without grid/master section */
	private void createSingleEntityLayout() {
		try {
			// Initialize the entity service for the configured entity type
			initializeEntityService();
			// Create only the details section, no grid
			createDetailsSection();
			// Load and display the single entity
			loadAndDisplaySingleEntity();
		} catch (Exception e) {
			LOGGER.error("Failed to create single entity layout for page: {}", getPageEntity().getPageTitle(), e);
			throw e;
		}
	}

	/** Override the parent's createGridAndDetailSections to create only details section */
	@Override
	protected void initializePage() {
		setSizeFull();
		// Set page title for browser tab only if pageTitle is not empty
		if (getPageEntity().getPageTitle() != null && !getPageEntity().getPageTitle().trim().isEmpty()) {
			getElement().executeJs("document.title = $0", getPageEntity().getPageTitle());
		}
		// Create single entity layout instead of grid and details
		createSingleEntityLayout();
		LOGGER.debug("Single entity dynamic page view initialized for: {}", getPageEntity().getPageTitle());
	}

	/** Loads the single entity from the data source and displays it. Shows warning if more than 1 item is returned and displays the first item. */
	private void loadAndDisplaySingleEntity() {
		try {
			// Get the entity service and load all entities
			if (entityService == null) {
				LOGGER.error("Entity service is null, cannot load single entity");
				return;
			}
			List<? extends CEntityDB<?>> entities = entityService.findAll();
			if (entities == null || entities.isEmpty()) {
				LOGGER.warn("No entities found for single entity page: {}", getPageEntity().getPageTitle());
				return;
			}
			if (entities.size() > 1) {
				LOGGER.warn("Single entity page {} has {} entities in data source, showing first item only", getPageEntity().getPageTitle(),
						entities.size());
			}
			// Always show the first entity
			CEntityDB<?> firstEntity = entities.get(0);
			setCurrentEntity(firstEntity);
			LOGGER.debug("Displaying single entity: {} with ID: {}", firstEntity.getClass().getSimpleName(), firstEntity.getId());
			// Populate the details with the first entity
			populateForm();
		} catch (Exception e) {
			LOGGER.error("Error loading single entity for page: {}", getPageEntity().getPageTitle(), e);
		}
	}

	/** Helper method to set button visibility using reflection */
	private void setButtonVisibility(CCrudToolbar<?> toolbar, String buttonFieldName, boolean visible) {
		try {
			java.lang.reflect.Field field = CCrudToolbar.class.getDeclaredField(buttonFieldName);
			field.setAccessible(true);
			Object button = field.get(toolbar);
			if (button != null && button instanceof com.vaadin.flow.component.Component) {
				((com.vaadin.flow.component.Component) button).setVisible(visible);
			}
		} catch (Exception e) {
			LOGGER.warn("Could not set visibility for button {}: {}", buttonFieldName, e.getMessage());
		}
	}

	/** Validates that the page entity is properly configured for single entity display. Throws exception if
	 * pageEntity.getGridEntity().getAttributeNone() != true */
	private void validateSingleEntityConfiguration() {
		Check.notNull(getPageEntity(), "pageEntity cannot be null");
		Check.notNull(getPageEntity().getGridEntity(), "pageEntity.getGridEntity() cannot be null");
		if (!getPageEntity().getGridEntity().getAttributeNone()) {
			throw new IllegalArgumentException(
					"CDynamicSingleEntityPageView can only be used with pageEntity where gridEntity.attributeNone is true. " + "Current value: "
							+ getPageEntity().getGridEntity().getAttributeNone() + " for page: " + getPageEntity().getPageTitle());
		}
		LOGGER.debug("Single entity configuration validated for page: {}", getPageEntity().getPageTitle());
	}
}
