package tech.derbent.abstracts.views;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;

import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.PageableUtils;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;

/**
 * Abstract project-aware MD page that filters entities by the currently active project. Implements
 * CProjectChangeListener to receive immediate notifications when the active project changes.
 */
public abstract class CProjectAwareMDPage<EntityClass extends CEntityOfProject<EntityClass>>
        extends CAbstractNamedEntityPage<EntityClass> implements CProjectChangeListener {

    private static final long serialVersionUID = 1L;

    protected final CSessionService sessionService;

    protected CProjectAwareMDPage(final Class<EntityClass> entityClass,
            final CAbstractNamedEntityService<EntityClass> entityService, final CSessionService sessionService) {
        super(entityClass, entityService, sessionService);
        this.sessionService = sessionService;
        // Now that sessionService is set, we can populate the grid
        refreshProjectAwareGrid();
    }

    @Override
    protected void createGridLayout() {
        // LOGGER.debug("Creating grid layout for project-aware MD page");
        grid = new CGrid<>(entityClass, false);
        grid.getColumns().forEach(grid::removeColumn);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        // Initially set empty items - will be populated after view is fully initialized
        grid.setItems(Collections.emptyList());
        grid.addIdColumn(entity -> entity.getId().toString(), "ID", "id");
        // Add selection listener to the grid
        grid.asSingleSelect().addValueChangeListener(event -> {
            populateForm(event.getValue());
        });
        final Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        wrapper.add(grid);
        splitLayout.addToPrimary(wrapper);
    }

    @Override
    protected EntityClass createNewEntity() {
        final String name = "New Item";
        final CProject project = sessionService.getActiveProject()
                .orElseThrow(() -> new IllegalStateException("No current project set in session"));
        return ((CEntityOfProjectService<EntityClass>) entityService).newEntity(name, project);
    }

    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Register this component to receive project change notifications
        sessionService.addProjectChangeListener(this);
        LOGGER.debug("Registered project change listener for: {}", getClass().getSimpleName());
    }

    /**
     * Called when the component is detached from the UI. Unregisters the project change listener to prevent memory
     * leaks.
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Unregister this component to prevent memory leaks
        sessionService.removeProjectChangeListener(this);
        LOGGER.debug("Unregistered project change listener for: {}", getClass().getSimpleName());
    }

    /**
     * Implementation of CProjectChangeListener interface. Called when the active project changes via the
     * SessionService.
     * 
     * @param newProject
     *            The newly selected project
     */
    @Override
    public void onProjectChanged(final CProject newProject) {
        LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
        refreshProjectAwareGrid();
    }

    @Override
    protected void refreshGrid() {
        LOGGER.debug("Refreshing project-aware grid for {}", getClass().getSimpleName());
        // Store the currently selected entity ID to preserve selection after refresh
        final EntityClass selectedEntity = grid.asSingleSelect().getValue();
        final Long selectedEntityId = selectedEntity != null ? selectedEntity.getId() : null;
        LOGGER.debug("Currently selected entity ID before project-aware refresh: {}", selectedEntityId);
        // Clear selection and refresh with project-aware data
        grid.select(null);
        refreshProjectAwareGrid();

        // Restore selection if there was a previously selected entity
        if (selectedEntityId != null) {
            restoreGridSelection(selectedEntityId);
        }
    }

    /**
     * Refreshes the grid with project-aware data.
     */
    protected void refreshProjectAwareGrid() {
        LOGGER.debug("Refreshing project-aware grid");

        if ((sessionService == null) || (grid == null)) {
            // Not fully initialized yet
            return;
        }
        final Optional<CProject> activeProject = sessionService.getActiveProject();

        if (activeProject.isPresent()) {
            LOGGER.debug("Loading entities for active project: {}", activeProject.get().getName());
            List<EntityClass> entities;

            // Check if the entity service is for CEntityOfProject entities
            if (entityService instanceof CEntityOfProjectService) {
                final CEntityOfProjectService<EntityClass> projectService = (CEntityOfProjectService<EntityClass>) entityService;
                entities = projectService.findEntitiesByProject(activeProject.get(), PageableUtils.createSafe(0, 10));
            } else {
                // For non-project entities, show all entities (they don't have project
                // filtering)
                LOGGER.debug("Entity service is not project-aware, showing all entities");
                entities = entityService.list(PageableUtils.createSafe(0, 10));
            }
            grid.setItems(entities);
        } else {
            // If no active project, show empty grid
            LOGGER.debug("No active project found, clearing grid items");
            grid.setItems(Collections.emptyList());
        }
    }

    @Override
    protected void selectFirstItemIfAvailable() {

        if (grid == null) {
            LOGGER.warn("Grid is null, cannot select first item");
            return;
        }

        if (!sessionService.getActiveProject().isPresent()) {
            LOGGER.warn("No active project available for first item selection");
            return;
        }
        final Pageable pageable = PageableUtils.createSafe(0, 1); // first page, 1 item
        List<EntityClass> result;

        // Check if the entity service is for CEntityOfProject entities
        if (entityService instanceof CEntityOfProjectService) {
            final CEntityOfProjectService<EntityClass> projectService = (CEntityOfProjectService<EntityClass>) entityService;
            result = projectService.findEntitiesByProject(sessionService.getActiveProject().get(), pageable);
        } else {
            // For non-project entities, just get the first entity from all entities
            LOGGER.debug("Entity service is not project-aware, selecting from all entities");
            result = entityService.list(pageable);
        }
        LOGGER.debug("Fetched {} entities for project", result.size());

        if (!result.isEmpty()) {
            final EntityClass firstEntity = result.get(0);
            grid.select(firstEntity);
        }
    }

    /**
     * Sets the project for the entity.
     */
    protected abstract void setProjectForEntity(EntityClass entity, CProject project);
}