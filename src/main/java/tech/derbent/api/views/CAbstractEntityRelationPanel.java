package tech.derbent.api.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/** Abstract base class for managing entity-to-entity relationships. This class provides common functionality for panels that manage relationships
 * between entities.
 * @param <ParentEntity>   The parent entity type (e.g., CUser, CProject)
 * @param <RelationEntity> The relationship entity type (e.g., CUserProjectSettings) */
public abstract class CAbstractEntityRelationPanel<ParentEntity extends CEntityDB<ParentEntity>, RelationEntity extends CEntityDB<RelationEntity>>
		extends CAccordionDBEntity<ParentEntity> {

	private static final long serialVersionUID = 1L;

	/** Helper method to create a simple text span */
	protected static Span createTextSpan(final String text) {
		return new Span(text != null ? text : "");
	}

	protected Supplier<List<RelationEntity>> getRelations;
	protected final Grid<RelationEntity> grid = new Grid<>(getRelationEntityClass(), false);
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected Runnable saveEntity;
	protected Consumer<List<RelationEntity>> setRelations;

	/** Constructor for the relation panel */
	public CAbstractEntityRelationPanel(final String title, final IContentOwner parentContent,
			final CEnhancedBinder<ParentEntity> beanValidationBinder, final Class<ParentEntity> entityClass,
			final CAbstractService<ParentEntity> entityService) {
		super(title, parentContent, beanValidationBinder, entityClass, entityService);
		initializeGrid();
		setupGrid();
		setupButtons();
		closePanel();
	}

	/** Abstract method to create delete confirmation message */
	protected abstract String createDeleteConfirmationMessage(final RelationEntity selected);

	/** Deletes the selected relationship
	 * @throws Exception
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException */
	protected void deleteSelected() throws Exception {
		final RelationEntity selected = grid.asSingleSelect().getValue();
		Check.notNull(selected, "Selected relation cannot be null");
		Check.notNull(getRelations, "Get relations supplier cannot be null");
		Check.notNull(setRelations, "Set relations consumer cannot be null");
		final String confirmMessage = createDeleteConfirmationMessage(selected);
		CNotificationService.showConfirmationDialog(confirmMessage, () -> {
			final List<RelationEntity> relations = getRelations.get();
			relations.remove(selected);
			setRelations.accept(relations);
			if (saveEntity != null) {
				saveEntity.run();
			}
			refresh();
		});
	}

	/** Abstract method to get the relationship entity class */
	protected abstract Class<RelationEntity> getRelationEntityClass();

	/** Gets the currently selected relation entity */
	protected RelationEntity getSelectedRelation() { return grid.asSingleSelect().getValue(); }

	/** Initialize grid with common settings */
	private void initializeGrid() {
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		final GridSingleSelectionModel<RelationEntity> sm =
				(GridSingleSelectionModel<RelationEntity>) grid.getSelectionModel();
		sm.setDeselectAllowed(false);
	}

	/** Abstract method to open the add dialog */
	protected abstract void on_actionOpenAddDialog();
	/** Abstract method to open the edit dialog */
	protected abstract void on_actionOpenEditDialog();
	/** Abstract method to handle relation save events */
	protected abstract void onRelationSaved(final RelationEntity relation);

	/** Refreshes the grid data */
	public void refresh() {
		LOGGER.debug("Refreshing relation grid data for {}", getClass().getSimpleName());
		if (getRelations != null) {
			grid.setItems(getRelations.get());
		}
	}

	/** Sets the relation accessors (getters, setters, save callback) */
	public void setRelationAccessors(final Supplier<List<RelationEntity>> getRelations, final Consumer<List<RelationEntity>> setRelations,
			final Runnable saveEntity) {
		LOGGER.debug("Setting relation accessors for {}", getClass().getSimpleName());
		this.getRelations = getRelations;
		this.setRelations = setRelations;
		this.saveEntity = saveEntity;
		refresh();
	}

	/** Sets up the action buttons (Add, Edit, Delete) */
	private void setupButtons() {
		final CButton addButton = CButton.createPrimary("Add", VaadinIcon.PLUS.create(), e -> on_actionOpenAddDialog());
		final CButton editButton = new CButton("Edit", VaadinIcon.EDIT.create(), e -> on_actionOpenEditDialog());
		editButton.setEnabled(false);
		final CButton deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> {
			try {
				deleteSelected();
			} catch (final Exception e1) {
				CNotificationService.showException("Error deleting selected relation.", e1);
			}
		});
		deleteButton.setEnabled(false);
		// Enable/disable edit and delete buttons based on selection
		grid.addSelectionListener(selection -> {
			final boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
			editButton.setEnabled(hasSelection);
			deleteButton.setEnabled(hasSelection);
		});
		// Add double-click listener to open edit dialog
		grid.addItemDoubleClickListener(e -> {
			if (e.getItem() != null) {
				on_actionOpenEditDialog();
			}
		});
		final HorizontalLayout buttonLayout = new HorizontalLayout(addButton, editButton, deleteButton);
		buttonLayout.setSpacing(true);
		addToContent(buttonLayout);
	}

	/** Abstract method to setup grid columns */
	protected abstract void setupGrid();

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(new ArrayList<>());
	}
}
