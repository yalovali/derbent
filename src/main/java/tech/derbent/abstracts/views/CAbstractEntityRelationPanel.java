package tech.derbent.abstracts.views;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.base.ui.dialogs.CConfirmationDialog;
import tech.derbent.base.ui.dialogs.CWarningDialog;

/**
 * Abstract base class for managing entity-to-entity relationships. This class provides common functionality for panels
 * that manage relationships between entities.
 * 
 * @param <ParentEntity>
 *            The parent entity type (e.g., CUser, CProject)
 * @param <RelationEntity>
 *            The relationship entity type (e.g., CUserProjectSettings)
 */
public abstract class CAbstractEntityRelationPanel<ParentEntity extends CEntityDB<ParentEntity>, RelationEntity extends CEntityDB<RelationEntity>>
        extends CAccordionDBEntity<ParentEntity> {

    private static final long serialVersionUID = 1L;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected final Grid<RelationEntity> grid = new Grid<>(getRelationEntityClass(), false);

    protected Supplier<List<RelationEntity>> getRelations;

    protected Consumer<List<RelationEntity>> setRelations;

    protected Runnable saveEntity;

    /**
     * Constructor for the relation panel
     */
    public CAbstractEntityRelationPanel(final String title, final ParentEntity currentEntity,
            final CEnhancedBinder<ParentEntity> beanValidationBinder, final Class<ParentEntity> entityClass,
            final CAbstractService<ParentEntity> entityService) {
        super(title, currentEntity, beanValidationBinder, entityClass, entityService);
        setupGrid();
        setupButtons();
        closePanel();
    }

    /**
     * Abstract method to create delete confirmation message
     */
    protected abstract String createDeleteConfirmationMessage(final RelationEntity selected);

    /**
     * Helper method to create a simple text span
     */
    protected Span createTextSpan(final String text) {
        return new Span(text != null ? text : "");
    }

    /**
     * Deletes the selected relationship
     */
    protected void deleteSelected() {
        final RelationEntity selected = grid.asSingleSelect().getValue();

        if (selected == null) {
            new CWarningDialog("Please select a relationship to delete.").open();
            return;
        }

        if ((getRelations == null) || (setRelations == null)) {
            new CWarningDialog("Relationship handlers are not available. Please refresh the page.").open();
            return;
        }
        final String confirmMessage = createDeleteConfirmationMessage(selected);
        new CConfirmationDialog(confirmMessage, () -> {
            final List<RelationEntity> relations = getRelations.get();
            relations.remove(selected);
            setRelations.accept(relations);

            if (saveEntity != null) {
                saveEntity.run();
            }
            refresh();
        }).open();
    }

    /**
     * Abstract method to get the relationship entity class
     */
    protected abstract Class<RelationEntity> getRelationEntityClass();

    /**
     * Gets the currently selected relation entity
     */
    protected RelationEntity getSelectedRelation() {
        return grid.asSingleSelect().getValue();
    }

    /**
     * Abstract method to handle relation save events
     */
    protected abstract void onRelationSaved(final RelationEntity relation);

    /**
     * Abstract method to open the add dialog
     */
    protected abstract void openAddDialog();

    /**
     * Abstract method to open the edit dialog
     */
    protected abstract void openEditDialog();

    /**
     * Refreshes the grid data
     */
    public void refresh() {
        LOGGER.debug("Refreshing relation grid data for {}", getClass().getSimpleName());

        if (getRelations != null) {
            grid.setItems(getRelations.get());
        }
    }

    /**
     * Sets the relation accessors (getters, setters, save callback)
     */
    public void setRelationAccessors(final Supplier<List<RelationEntity>> getRelations,
            final Consumer<List<RelationEntity>> setRelations, final Runnable saveEntity) {
        LOGGER.debug("Setting relation accessors for {}", getClass().getSimpleName());
        this.getRelations = getRelations;
        this.setRelations = setRelations;
        this.saveEntity = saveEntity;
        refresh();
    }

    /**
     * Sets up the action buttons (Add, Edit, Delete)
     */
    private void setupButtons() {
        final CButton addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> openAddDialog());
        final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> openEditDialog());
        editButton.setEnabled(false);
        final CButton deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> deleteSelected());
        deleteButton.setEnabled(false);
        // Enable/disable edit and delete buttons based on selection
        grid.addSelectionListener(selection -> {
            final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
        final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
        buttonLayout.setSpacing(true);
        addToContent(buttonLayout);
    }

    /**
     * Abstract method to setup grid columns
     */
    protected abstract void setupGrid();

    @Override
    protected void updatePanelEntityFields() {
        setEntityFields(List.of(""));
    }
}