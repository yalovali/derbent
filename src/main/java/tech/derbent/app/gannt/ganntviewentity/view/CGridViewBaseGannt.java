package tech.derbent.app.gannt.ganntviewentity.view;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.provider.Query;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.config.CSpringContext;
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
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.page.view.CDynamicPageRouter;
import tech.derbent.base.session.service.ISessionService;

/* display a Gannt chart for any entity of project type */
public abstract class CGridViewBaseGannt<EntityClass extends CEntityOfProject<EntityClass>>
        extends CProjectAwareMDPage<EntityClass> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(CGridViewBaseGannt.class);
    private static final long serialVersionUID = 1L;
    protected final CActivityService activityService;
    private final CDynamicPageRouter currentEntityPageRouter;
    protected CEnhancedBinder<CProjectItem<?>> entityBinder;
    protected final CMeetingService meetingService;
    protected final CPageEntityService pageEntityService;

    protected CGridViewBaseGannt(final Class<EntityClass> entityClass,
            final CEntityOfProjectService<EntityClass> entityService,
            final ISessionService sessionService, final CDetailSectionService screenService,
            final CActivityService activityService,
            final CMeetingService meetingService, final CPageEntityService pageEntityService) throws Exception {
        super(entityClass, entityService, sessionService, screenService);
        LOGGER.debug("Initializing CGridViewBaseGannt for entity class: {}", entityClass.getSimpleName());
        this.activityService = activityService;
        this.meetingService = meetingService;
        this.pageEntityService = pageEntityService;
        final CDetailSectionService detailSectionService = CSpringContext.getBean(CDetailSectionService.class);
        // CGridEntityService gridEntityService =
        // CSpringContext.getBean(CGridEntityService.class);
        currentEntityPageRouter = new CDynamicPageRouter(pageEntityService, sessionService, detailSectionService, null);
        getBaseDetailsLayout().add(currentEntityPageRouter);
        // currentEntityPageRouter.setHeight("50%");
        getBaseDetailsLayout().add(currentEntityPageRouter);
        // NO CRUD toolbar for Gantt view
        crudToolbar.setVisible(false); // no CRUD toolbar for Gantt view
        LOGGER.debug("CGridViewBaseGannt initialized for entity class: {}", entityClass.getSimpleName());
    }

    @Override
    protected void createDetailsComponent() throws Exception {
        LOGGER.debug("Creating details component for Gantt view - no detail form available.");
    }

    // override this to create a Gannt chart
    @Override
    protected void createMasterComponent() throws Exception {
        // Pass required dependencies to CMasterViewSectionGannt constructor with page
        // entity service for navigation
        masterViewSection = new CMasterViewSectionGannt<EntityClass>(entityClass, this, sessionService, activityService,
                meetingService, pageEntityService);
    }

    /**
     * Gets the entity binder for the actual entity (Activity or Meeting). This is
     * needed for the page service to write binder data before saving.
     * 
     * @return The entity binder
     */
    public CEnhancedBinder<CProjectItem<?>> getEntityBinder() {
        return entityBinder;
    }

    private CProjectItem<?> getGanntEntityFromSelectedItem() {
        LOGGER.debug("Getting Gantt entity from selected CGanttItem");
        if (getGanttMasterViewSection() == null) {
            LOGGER.warn("Gantt master view section is null - cannot get selected entity");
            return null;
        }
        if (getGanttMasterViewSection().getGrid() == null) {
            LOGGER.warn("Gantt grid is null - cannot get selected entity");
            return null;
        }
        CProjectItem<?> ganntEntity = null;
        final CGanntItem gannItem = getGanttMasterViewSection().getGrid().getSelectedEntity();
        if (gannItem != null) {
            ganntEntity = gannItem.getGanntItem(activityService, meetingService);
        }
        return ganntEntity;
    }

    /**
     * Gets the master view section cast to CMasterViewSectionGannt for accessing
     * Gantt-specific functionality.
     * 
     * @return The master view section as CMasterViewSectionGannt
     */
    protected CMasterViewSectionGannt<EntityClass> getGanttMasterViewSection() {
        return (CMasterViewSectionGannt<EntityClass>) masterViewSection;
    }

    /**
     * Locates a CGanttItem in the grid that wraps the given actual entity. This is
     * needed after save operations to restore selection to the saved
     * entity.
     * 
     * @param actualEntity The actual entity (CActivity or CMeeting) to locate
     * @return The CGanttItem wrapper that contains this entity, or null if not
     *         found
     */
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
            final Optional<CGanntItem> matchingItem = dataProvider.fetch(new Query<>()).filter(item -> {
                return item.getEntityType().equals(entityTypeName) && item.getEntityId().equals(entityId);
            }).findFirst();
            if (matchingItem.isPresent()) {
                LOGGER.debug("Found matching CGanttItem for entity: {}", actualEntity.getName());
                return matchingItem.get();
            }
            LOGGER.debug("No matching CGanttItem found for entity ID {} type {}", entityId, entityTypeName);
            return null;
        } catch (final Exception e) {
            LOGGER.error("Error locating Gantt item for entity: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onEntitySaved(final EntityClass entity) throws Exception {
        /**/}

    /**
     * Override to handle CGanttItem selection - it's a DTO wrapper, not the actual
     * entity. Selection is logged but no form editing occurs since
     * CGanttItem is read-only.
     */
    @Override
    protected void onSelectionChanged(final CMasterViewSectionBase.SelectionChangeEvent<EntityClass> event) {
        // CGanttItem ganttItem=event.getSelectedItem();
        final EntityClass value = event.getSelectedItem();
        // Check if selected item is CGanttItem (DTO wrapper)
        if (value != null) {
            Check.instanceOf(value, CGanntItem.class, "Selected item is not a CGanttItem");
            setValue(value);
            populateForm();
            return;
        }
        // Standard entity selection handling for non-DTO items
        super.onSelectionChanged(event);
    }

    @Override
    public void populateForm() {
        try {
            LOGGER.debug("Populating form for entity: {}", getValue() != null ? getValue().getName() : "null");
            // Implementation to populate the form with current entity details
            updateDetailsComponent();
        } catch (final Exception e) {
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

    @SuppressWarnings("deprecation")
    @Override
    protected void updateDetailsComponent() throws Exception {
        LOGGER.debug("Updating details component for Gantt view");
        CDynamicPageRouter.displayEntityInDynamicOnepager(getGanntEntityFromSelectedItem(), currentEntityPageRouter,
                sessionService);
    }
}
