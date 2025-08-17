package tech.derbent.kanban.view;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.view.CActivityCard;

/**
 * CActivityKanbanColumn - UI component representing a column in the Kanban board. Layer: View (MVC) Displays all
 * activities for a specific activity status, including the status name and count of activities. Groups activities by
 * type within the column for better organization. Contains multiple CActivityCard components organized by activity
 * type.
 */
public class CActivityKanbanColumn extends Div {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityKanbanColumn.class);

    private final CActivityStatus activityStatus;

    private H3 headerElement;

    private Span countElement;

    private VerticalLayout cardsContainer;

    private List<CActivity> activities;

    private Consumer<CActivity> onActivityDropped;

    /**
     * Constructor for CActivityKanbanColumn.
     * 
     * @param activityStatus
     *            the activity status this column represents
     * @param activities
     *            the list of activities for this status
     */
    public CActivityKanbanColumn(final CActivityStatus activityStatus, final List<CActivity> activities) {
        this(activityStatus, activities, null);
    }

    /**
     * Constructor for CActivityKanbanColumn with drop handler.
     * 
     * @param activityStatus
     *            the activity status this column represents
     * @param activities
     *            the list of activities for this status
     * @param onActivityDropped
     *            callback function to handle dropped activities
     */
    public CActivityKanbanColumn(final CActivityStatus activityStatus, final List<CActivity> activities,
            final Consumer<CActivity> onActivityDropped) {
        LOGGER.debug("Creating CActivityKanbanColumn for status: {} with {} activities",
                activityStatus != null ? activityStatus.getName() : "null", activities != null ? activities.size() : 0);

        if (activityStatus == null) {
            throw new IllegalArgumentException("Activity status cannot be null");
        }
        this.activityStatus = activityStatus;
        this.activities = activities != null ? activities : List.of();
        this.onActivityDropped = onActivityDropped;
        initializeColumn();
    }

    /**
     * Creates a type section with activities for the given type.
     * 
     * @param typeName
     *            the activity type name for this section
     * @param typeActivities
     *            the list of activities with this type
     */
    private void createTypeSection(final String typeName, final List<CActivity> typeActivities) {
        LOGGER.debug("Creating type section for: {} with {} activities", typeName, typeActivities.size());
        // Create type section container
        final Div typeSection = new Div();
        typeSection.addClassName("kanban-type-section");
        // Create type header
        final H5 typeHeader = new H5(typeName != null ? typeName : "No Type");
        typeHeader.addClassName("kanban-type-header");
        // Create type count
        final Span typeCount = new Span("(" + typeActivities.size() + ")");
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

        // Add activity cards for this type
        for (final CActivity activity : typeActivities) {

            try {
                final CActivityCard card = new CActivityCard(activity);
                typeCardsContainer.add(card);
                LOGGER.debug("Added card for activity: {} with type: {}", activity.getName(), typeName);
            } catch (final Exception e) {
                LOGGER.error("Error creating card for activity: {} with type: {}",
                        activity != null ? activity.getName() : "null", typeName, e);
            }
        }
        // Add components to type section
        typeSection.add(typeHeaderContainer, typeCardsContainer);
        // Add type section to main container
        cardsContainer.add(typeSection);
    }

    /**
     * Gets the current activities in this column.
     * 
     * @return list of activities
     */
    public List<CActivity> getActivities() {
        return activities;
    }

    /**
     * Gets the activity status for this column.
     * 
     * @return the activity status
     */
    public CActivityStatus getActivityStatus() {
        return activityStatus;
    }

    /**
     * Initializes the column components and layout.
     */
    private void initializeColumn() {
        LOGGER.debug("Initializing column layout for status: {}", activityStatus.getName());
        // Set CSS class for styling
        addClassName("kanban-column");

        // Configure drag and drop
        setupDropTarget();

        // Create header container
        final Div headerContainer = new Div();
        headerContainer.addClassName("kanban-column-header");
        // Create status name header
        headerElement = new H3(activityStatus.getName() != null ? activityStatus.getName() : "Unnamed Status");
        headerElement.addClassName("kanban-column-title");
        // Create count element
        countElement = new Span("(" + activities.size() + ")");
        countElement.addClassName("kanban-column-count");
        // Add header elements
        headerContainer.add(headerElement, countElement);
        // Create cards container
        cardsContainer = new VerticalLayout();
        cardsContainer.addClassName("kanban-column-cards");
        cardsContainer.setSpacing(true);
        cardsContainer.setPadding(false);
        cardsContainer.setMargin(false);
        // Populate with activity cards
        populateCards();
        // Add components to column
        add(headerContainer, cardsContainer);
    }

    /**
     * Sets up drop target functionality for the kanban column.
     */
    private void setupDropTarget() {
        LOGGER.debug("Setting up drop target for column: {}", activityStatus.getName());

        final DropTarget<CActivityKanbanColumn> dropTarget = DropTarget.create(this);
        dropTarget.setDropEffect(DropEffect.MOVE);

        // Add drop listener to handle the actual drop
        dropTarget.addDropListener(event -> {
            LOGGER.debug("Drop event in column: {}", activityStatus.getName());

            // Get the dragged activity from the drag source
            if (event.getDragSourceComponent().isPresent()
                    && event.getDragSourceComponent().get() instanceof CActivityCard) {

                final CActivityCard draggedCard = (CActivityCard) event.getDragSourceComponent().get();
                final CActivity droppedActivity = draggedCard.getActivity();

                LOGGER.info("Activity dropped: {} into status: {}", droppedActivity.getName(),
                        activityStatus.getName());

                // Only process if the activity is being dropped into a different status
                if (!activityStatus.equals(droppedActivity.getStatus())) {
                    droppedActivity.setStatus(activityStatus);

                    // Call the callback if provided
                    if (onActivityDropped != null) {
                        onActivityDropped.accept(droppedActivity);
                    }
                }
            }
        });
    }

    /**
     * Populates the column with activity cards grouped by type.
     */
    private void populateCards() {
        LOGGER.debug("Populating {} activity cards for status: {} with type grouping", activities.size(),
                activityStatus.getName());
        cardsContainer.removeAll();

        if (activities.isEmpty()) {
            final Div emptyMessage = new Div("No activities");
            emptyMessage.addClassName("kanban-empty-message");
            cardsContainer.add(emptyMessage);
            return;
        }
        // Group activities by type
        final Map<String, List<CActivity>> activitiesByType = activities.stream()
                .collect(Collectors.groupingBy(activity -> {
                    if (activity == null) {
                        return "No Type";
                    }
                    if (activity.getActivityType() == null) {
                        return "No Type";
                    }
                    final String typeName = activity.getActivityType().getName();
                    return typeName != null ? typeName : "No Type";
                }, Collectors.toList()));

        // Create a type section for each group
        for (final Map.Entry<String, List<CActivity>> typeEntry : activitiesByType.entrySet()) {
            final String typeName = typeEntry.getKey();
            final List<CActivity> typeActivities = typeEntry.getValue();
            createTypeSection(typeName, typeActivities);
        }
    }

    /**
     * Refreshes the entire column display. Useful for real-time updates.
     */
    public void refresh() {
        LOGGER.debug("Refreshing column for status: {}", activityStatus.getName());
        populateCards();
    }

    /**
     * Updates the column with new activities.
     * 
     * @param newActivities
     *            the updated list of activities
     */
    public void updateActivities(final List<CActivity> newActivities) {
        LOGGER.debug("Updating activities for status: {} from {} to {} activities", activityStatus.getName(),
                this.activities.size(), newActivities != null ? newActivities.size() : 0);
        this.activities = newActivities != null ? newActivities : List.of();

        // Update count
        if (countElement != null) {
            countElement.setText("(" + activities.size() + ")");
        }
        // Repopulate cards
        populateCards();
    }
}