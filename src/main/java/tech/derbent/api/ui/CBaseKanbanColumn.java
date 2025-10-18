package tech.derbent.api.ui;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.interfaces.IKanbanEntity;
import tech.derbent.api.interfaces.IKanbanStatus;
import tech.derbent.api.interfaces.IKanbanType;
import tech.derbent.api.utils.Check;

/** CBaseKanbanColumn - Abstract base class for Kanban columns with drag-and-drop functionality. Layer: Base View (MVC) Provides common functionality
 * for kanban columns including: - Status-based organization - Type-based grouping within columns - Drag and drop support for reordering between
 * columns - Status update handling
 * @param <T> the type of entity displayed in this column
 * @param <S> the type of status this column represents */
public abstract class CBaseKanbanColumn<T extends IKanbanEntity, S extends IKanbanStatus> extends Div {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CBaseKanbanColumn.class);
	protected final S status;
	protected H3 headerElement;
	protected Span countElement;
	protected VerticalLayout cardsContainer;
	protected List<T> entities;
	protected BiConsumer<T, S> statusUpdateHandler;

	/** Constructor for CBaseKanbanColumn.
	 * @param status   the status this column represents
	 * @param entities the list of entities for this status */
	protected CBaseKanbanColumn(final S status, final List<T> entities) {
		LOGGER.debug("Creating CBaseKanbanColumn for status: {} with {} entities", status != null ? status.getName() : "null",
				entities != null ? entities.size() : 0);
		Check.notNull(status, "Status cannot be null");
		this.status = status;
		this.entities = entities != null ? entities : List.of();
		initializeColumn();
		setupDropTarget();
	}

	/** Creates a draggable card wrapper for the entity.
	 * @param entity        the entity to wrap
	 * @param cardComponent the card component to wrap
	 * @return the draggable wrapper */
	protected Component createDraggableCard(final T entity, final Component cardComponent) {
		final Div wrapper = new Div();
		wrapper.add(cardComponent);
		wrapper.addClassName("kanban-card-wrapper");
		// Make it draggable
		final DragSource<Div> dragSource = DragSource.configure(wrapper);
		dragSource.setEffectAllowed(EffectAllowed.MOVE);
		dragSource.setDragData(entity);
		dragSource.addDragStartListener(_ -> {
			LOGGER.debug("Started dragging entity: {}", entity.getName());
			wrapper.addClassName("kanban-card-dragging");
		});
		dragSource.addDragEndListener(_ -> {
			LOGGER.debug("Finished dragging entity: {}", entity.getName());
			wrapper.removeClassName("kanban-card-dragging");
		});
		return wrapper;
	}

	/** Creates a card component for the given entity. Subclasses must implement this to create specific card types.
	 * @param entity the entity to create a card for
	 * @return the created card component */
	protected abstract Component createEntityCard(T entity);

	/** Creates a type section with entities for the given type.
	 * @param typeName     the entity type name for this section
	 * @param typeEntities the list of entities with this type */
	private void createTypeSection(final String typeName, final List<T> typeEntities) {
		LOGGER.debug("Creating type section for: {} with {} entities", typeName, typeEntities.size());
		// Create type section container
		final Div typeSection = new Div();
		typeSection.addClassName("kanban-type-section");
		// Create type header
		final H5 typeHeader = new H5(typeName != null ? typeName : "No Type");
		typeHeader.addClassName("kanban-type-header");
		// Create type count
		final Span typeCount = new Span("(" + typeEntities.size() + ")");
		typeCount.addClassName("kanban-type-count");
		// Create header container for type
		final Div typeHeaderContainer = new Div();
		typeHeaderContainer.addClassName("kanban-type-header-container");
		typeHeaderContainer.add(typeHeader, typeCount);
		// Create cards container for this type
		final VerticalLayout typeCardsContainer = new VerticalLayout();
		typeCardsContainer.addClassName("kanban-type-cards");
		typeCardsContainer.setSpacing(true);
		typeCardsContainer.setPadding(false);
		typeCardsContainer.setMargin(false);
		// Add entity cards for this type
		for (final T entity : typeEntities) {
			try {
				final Component card = createEntityCard(entity);
				final Component draggableCard = createDraggableCard(entity, card);
				typeCardsContainer.add(draggableCard);
				LOGGER.debug("Added card for entity: {} with type: {}", entity.getName(), typeName);
			} catch (final Exception e) {
				LOGGER.error("Error creating card for entity: {} with type: {}", entity != null ? entity.getName() : "null", typeName, e);
			}
		}
		// Add components to type section
		typeSection.add(typeHeaderContainer, typeCardsContainer);
		// Add type section to main container
		cardsContainer.add(typeSection);
	}

	/** Gets the current entities in this column.
	 * @return list of entities */
	public List<T> getEntities() { return entities; }

	/** Gets the type name for grouping purposes. Subclasses can override this if they need custom type handling.
	 * @param entity the entity to get the type name for
	 * @return the type name, or "No Type" if no type is set */
	protected String getEntityTypeName(final T entity) {
		Check.notNull(entity, "Entity cannot be null");
		final IKanbanType type = entity.getType();
		Check.notNull(type, "Entity type cannot be null");
		final String typeName = type.getName();
		return typeName != null ? typeName : "No Type";
	}

	/** Gets the status for this column.
	 * @return the status */
	public S getStatus() { return status; }

	/** Initializes the column components and layout. */
	private void initializeColumn() {
		// Set CSS class for styling
		addClassName("kanban-column");
		// Create header container
		final Div headerContainer = new Div();
		headerContainer.addClassName("kanban-column-header");
		// Create status name header
		headerElement = new H3(status.getName() != null ? status.getName() : "Unnamed Status");
		headerElement.addClassName("kanban-column-title");
		// Create count element
		countElement = new Span("(" + entities.size() + ")");
		countElement.addClassName("kanban-column-count");
		// Add header elements
		headerContainer.add(headerElement, countElement);
		// Create cards container
		cardsContainer = new VerticalLayout();
		cardsContainer.addClassName("kanban-column-cards");
		cardsContainer.setSpacing(true);
		cardsContainer.setPadding(false);
		cardsContainer.setMargin(false);
		// Populate with entity cards
		populateCards();
		// Add components to column
		add(headerContainer, cardsContainer);
	}

	/** Populates the column with entity cards grouped by type. */
	private void populateCards() {
		LOGGER.debug("Populating {} entity cards for status: {} with type grouping", entities.size(), status.getName());
		cardsContainer.removeAll();
		if (entities.isEmpty()) {
			final Div emptyMessage = new Div("No items");
			emptyMessage.addClassName("kanban-empty-message");
			cardsContainer.add(emptyMessage);
			return;
		}
		// Group entities by type
		final Map<String, List<T>> entitiesByType = entities.stream().collect(Collectors.groupingBy(this::getEntityTypeName, Collectors.toList()));
		// Create a type section for each group
		for (final Map.Entry<String, List<T>> typeEntry : entitiesByType.entrySet()) {
			final String typeName = typeEntry.getKey();
			final List<T> typeEntities = typeEntry.getValue();
			createTypeSection(typeName, typeEntities);
		}
	}

	/** Refreshes the entire column display. Useful for real-time updates. */
	public void refresh() {
		LOGGER.debug("Refreshing column for status: {}", status.getName());
		populateCards();
	}

	/** Sets the handler for status updates.
	 * @param handler the handler to call when an entity status should be updated */
	public void setStatusUpdateHandler(final BiConsumer<T, S> handler) {
		this.statusUpdateHandler = handler;
	}

	/** Sets up the drop target for this column. */
	private void setupDropTarget() {
		DropTarget.create(this).addDropListener(event -> {
			final var dragData = event.getDragData();
			if (dragData.isPresent()) {
				final Object data = dragData.get();
				if (data instanceof IKanbanEntity) {
					@SuppressWarnings ("unchecked")
					final T entity = (T) data;
					LOGGER.debug("Entity dropped on column {}: {}", status.getName(), entity.getName());
					// Only update if the status is actually different
					if (!status.equals(entity.getStatus())) {
						if (statusUpdateHandler != null) {
							statusUpdateHandler.accept(entity, status);
						}
					}
				}
			}
		});
	}

	/** Updates the column with new entities.
	 * @param newEntities the updated list of entities */
	public void updateEntities(final List<T> newEntities) {
		LOGGER.debug("Updating entities for status: {} from {} to {} entities", status.getName(), this.entities.size(),
				newEntities != null ? newEntities.size() : 0);
		this.entities = newEntities != null ? newEntities : List.of();
		// Update count
		if (countElement != null) {
			countElement.setText("(" + entities.size() + ")");
		}
		// Repopulate cards
		populateCards();
	}
}
