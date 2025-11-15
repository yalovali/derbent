package tech.derbent.app.gannt.ganntviewentity.view;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.entityOfProject.view.CProjectAwareMDPage;
import tech.derbent.api.grid.view.CMasterViewSectionBase;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.ganntitem.domain.CGanntItem;
import tech.derbent.app.gannt.ganntviewentity.view.components.CGanntGrid;
import tech.derbent.app.gannt.projectgannt.service.CPageServiceProjectGannt;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.base.session.service.ISessionService;

/* display a Gannt chart for any entity of project type */
public abstract class CGridViewBaseGannt<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CGridViewBaseGannt.class);
	private static final long serialVersionUID = 1L;
	protected final CActivityService activityService;
	protected CEnhancedBinder<CProjectItem<?>> entityBinder;
	protected final CMeetingService meetingService;
	protected final CPageEntityService pageEntityService;

	protected CGridViewBaseGannt(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final ISessionService sessionService, final CDetailSectionService screenService, final CActivityService activityService,
			final CMeetingService meetingService, final CPageEntityService pageEntityService) throws Exception {
		super(entityClass, entityService, sessionService, screenService);
		this.activityService = activityService;
		this.meetingService = meetingService;
		this.pageEntityService = pageEntityService;
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		LOGGER.debug("Creating details component for Gantt view - no detail form available.");
	}

	// override this to create a Gannt chart
	@Override
	protected void createMasterComponent() throws Exception {
		// Pass required dependencies to CMasterViewSectionGannt constructor with page entity service for navigation
		masterViewSection =
				new CMasterViewSectionGannt<EntityClass>(entityClass, this, sessionService, activityService, meetingService, pageEntityService);
	}

	/** Gets the entity binder for the actual entity (Activity or Meeting). This is needed for the page service to write binder data before saving.
	 * @return The entity binder */
	public CEnhancedBinder<CProjectItem<?>> getEntityBinder() { return entityBinder; }

	/** Gets the master view section cast to CMasterViewSectionGannt for accessing Gantt-specific functionality.
	 * @return The master view section as CMasterViewSectionGannt */
	@SuppressWarnings ("unchecked")
	protected CMasterViewSectionGannt<EntityClass> getGanttMasterViewSection() { return (CMasterViewSectionGannt<EntityClass>) masterViewSection; }

	/** Locates a CGanttItem in the grid that wraps the given actual entity. This is needed after save operations to restore selection to the saved
	 * entity.
	 * @param actualEntity The actual entity (CActivity or CMeeting) to locate
	 * @return The CGanttItem wrapper that contains this entity, or null if not found */
	protected CGanntItem locateGanttItemForEntity(final CProjectItem<?> actualEntity) {
		if (actualEntity == null || actualEntity.getId() == null) {
			return null;
		}
		try {
			final CMasterViewSectionGannt<EntityClass> ganttSection = getGanttMasterViewSection();
			if (ganttSection == null) {
				LOGGER.warn("Cannot locate Gantt item - master view section is null");
				return null;
			}
			final CGanntGrid ganttGrid = ganttSection.getGrid();
			if (ganttGrid == null) {
				LOGGER.warn("Cannot locate Gantt item - Gantt grid is null");
				return null;
			}
			// Search through all grid items to find the one that wraps our entity
			// We need to match by entity type and entity ID
			final String entityTypeName = actualEntity.getClass().getSimpleName();
			final Long entityId = actualEntity.getId();
			LOGGER.debug("Searching for CGanttItem with entityType={}, entityId={}", entityTypeName, entityId);
			// Get all items from the grid's data provider
			final var dataProvider = ganttGrid.getDataProvider();
			if (dataProvider == null) {
				LOGGER.warn("Cannot locate Gantt item - data provider is null");
				return null;
			}
			// Fetch all items and search for matching entity
			final java.util.Optional<CGanntItem> matchingItem = dataProvider.fetch(new com.vaadin.flow.data.provider.Query<>()).filter(item -> {
				return item.getEntityType().equals(entityTypeName) && item.getEntityId().equals(entityId);
			}).findFirst();
			if (matchingItem.isPresent()) {
				LOGGER.debug("Found matching CGanttItem for entity: {}", actualEntity.getName());
				return matchingItem.get();
			} else {
				LOGGER.debug("No matching CGanttItem found for entity ID {} type {}", entityId, entityTypeName);
				return null;
			}
		} catch (final Exception e) {
			LOGGER.error("Error locating Gantt item for entity: {}", e.getMessage(), e);
			return null;
		}
	}

	@Override
	public void onEntitySaved(final EntityClass entity) throws Exception {
		LOGGER.debug("Entity saved, refreshing grid");
		// Get the current actual entity from page service before refresh
		CProjectItem<?> savedActualEntity = null;
		if (getPageService() instanceof CPageServiceProjectGannt) {
			savedActualEntity = ((CPageServiceProjectGannt) getPageService()).getCurrentActualEntity();
		}
		// Refresh the grid - this will reload all data
		refreshGrid();
		// After refresh, locate and select the saved entity in the grid
		if (savedActualEntity != null) {
			final CGanntItem ganttItemToSelect = locateGanttItemForEntity(savedActualEntity);
			if (ganttItemToSelect != null) {
				// Set this as the current entity and update the form
				setCurrentEntity(ganttItemToSelect);
				populateForm();
			} else {
				LOGGER.warn("Could not locate saved entity in refreshed grid");
				// Just populate the form with current state
				populateForm();
			}
		} else {
			populateForm();
		}
		// Note: Success notification is shown in CPageServiceProjectGannt.actionSave()
		navigateToClass();
	}

	/** Override to handle CGanttItem selection - it's a DTO wrapper, not the actual entity. Selection is logged but no form editing occurs since
	 * CGanttItem is read-only. */
	@Override
	protected void onSelectionChanged(final CMasterViewSectionBase.SelectionChangeEvent<EntityClass> event) {
		// CGanttItem ganttItem=event.getSelectedItem();
		final EntityClass value = event.getSelectedItem();
		// Check if selected item is CGanttItem (DTO wrapper)
		if (value != null) {
			Check.instanceOf(value, CGanntItem.class, "Selected item is not a CGanttItem");
			setCurrentEntity(value);
			// Clear the cached entity in page service to force refresh from the selected item
			// This ensures details update correctly when switching between different gantt items
			if (getPageService() instanceof CPageServiceProjectGannt) {
				((CPageServiceProjectGannt) getPageService()).setCurrentActualEntity(null);
			}
			populateForm();
			return;
		} else {
			// Standard entity selection handling for non-DTO items
			super.onSelectionChanged(event);
		}
	}

	@Override
	public void populateForm() {
		try {
			LOGGER.debug("Populating form for entity: {}", getCurrentEntity() != null ? getCurrentEntity().getName() : "null");
			// Implementation to populate the form with current entity details
			updateDetailsComponent();
		} catch (Exception e) {
			CNotificationService.showException("Error populating form", e);
		}
	}

	@Override
	public void refreshGrid() throws Exception {
		LOGGER.info("Refreshing Gantt grid for {}", getClass().getSimpleName());
		try {
			// For Gantt view, we need to refresh the grid's data provider
			// The grid contains CGanttItem wrappers, not actual entities
			final CMasterViewSectionGannt<EntityClass> ganttSection = getGanttMasterViewSection();
			if (ganttSection == null) {
				LOGGER.warn("Cannot refresh grid - master view section is null");
				return;
			}
			final CGanntGrid ganttGrid = ganttSection.getGrid();
			if (ganttGrid == null) {
				LOGGER.warn("Cannot refresh grid - Gantt grid is null");
				return;
			}
			// Refresh the grid's data provider to reload all data from services
			LOGGER.debug("Calling refresh on Gantt grid data provider");
			ganttGrid.refresh();
		} catch (final Exception e) {
			LOGGER.error("Error refreshing Gantt grid: {}", e.getMessage(), e);
			throw e;
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	protected void updateDetailsComponent() throws Exception {
		LOGGER.debug("Updating details component for Gantt view");
		getBaseDetailsLayout().removeAll();
		// First, try to get the actual entity from the page service
		// This is necessary for new entities that haven't been wrapped in CGanttItem yet
		CProjectItem<?> ganttEntity = null;
		if (getPageService() instanceof CPageServiceProjectGannt) {
			ganttEntity = ((CPageServiceProjectGannt) getPageService()).getCurrentActualEntity();
		}
		// If we don't have an entity from page service, try to get it from the current CGanttItem
		if (ganttEntity == null) {
			if (getCurrentEntity() == null) {
				LOGGER.debug("No current entity to display in details component");
				return;
			}
			// fetch fresh entity for the gantt item
			ganttEntity = ((CGanntItem) getCurrentEntity()).getGanntItem(activityService, meetingService);
		}
		if (ganttEntity == null) {
			LOGGER.warn("Gantt item entity is null, cannot populate details form.");
			return;
		}
		entityBinder = new CEnhancedBinder<CProjectItem<?>>((Class<CProjectItem<?>>) ganttEntity.getClass());
		final Field viewNameField = ganttEntity.getClass().getField("VIEW_NAME");
		final String entityViewName = (String) viewNameField.get(null);
		buildScreen(entityViewName, entityBinder);
		// final CVerticalLayout formLayout = CFormBuilder.buildForm(ganttEntity.getClass(), entityBinder, null, this);
		// getBaseDetailsLayout().add(formLayout);
		entityBinder.readBean(ganttEntity);
		crudToolbar.setCurrentEntity(ganttEntity);
		// Update the page service with the current actual entity
		if (getPageService() instanceof CPageServiceProjectGannt) {
			((CPageServiceProjectGannt) getPageService()).setCurrentActualEntity(ganttEntity);
		}
	}
}
