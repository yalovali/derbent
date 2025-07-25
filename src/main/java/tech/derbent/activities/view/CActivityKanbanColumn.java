package tech.derbent.activities.view;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityType;

/**
 * CActivityKanbanColumn - UI component representing a column in the Kanban board.
 * Layer: View (MVC)
 * 
 * Displays all activities for a specific activity type, including the type name
 * and count of activities. Contains multiple CActivityCard components.
 */
public class CActivityKanbanColumn extends Div {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivityKanbanColumn.class);

    private final CActivityType activityType;
    private H3 headerElement;
    private Span countElement;
    private VerticalLayout cardsContainer;
    private List<CActivity> activities;

    /**
     * Constructor for CActivityKanbanColumn.
     * 
     * @param activityType the activity type this column represents
     * @param activities   the list of activities for this type
     */
    public CActivityKanbanColumn(final CActivityType activityType, final List<CActivity> activities) {
        LOGGER.debug("Creating CActivityKanbanColumn for type: {} with {} activities", 
            activityType != null ? activityType.getName() : "null", 
            activities != null ? activities.size() : 0);
        
        if (activityType == null) {
            throw new IllegalArgumentException("Activity type cannot be null");
        }
        
        this.activityType = activityType;
        this.activities = activities != null ? activities : List.of();
        
        initializeColumn();
    }

    /**
     * Initializes the column components and layout.
     */
    private void initializeColumn() {
        LOGGER.debug("Initializing column layout for type: {}", activityType.getName());
        
        // Set CSS class for styling
        addClassName("kanban-column");
        
        // Create header container
        final Div headerContainer = new Div();
        headerContainer.addClassName("kanban-column-header");
        
        // Create type name header
        headerElement = new H3(activityType.getName() != null ? activityType.getName() : "Unnamed Type");
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
     * Populates the column with activity cards.
     */
    private void populateCards() {
        LOGGER.debug("Populating {} activity cards for type: {}", activities.size(), activityType.getName());
        
        cardsContainer.removeAll();
        
        if (activities.isEmpty()) {
            final Div emptyMessage = new Div("No activities");
            emptyMessage.addClassName("kanban-empty-message");
            cardsContainer.add(emptyMessage);
            return;
        }
        
        for (final CActivity activity : activities) {
            try {
                final CActivityCard card = new CActivityCard(activity);
                cardsContainer.add(card);
                LOGGER.debug("Added card for activity: {}", activity.getName());
            } catch (final Exception e) {
                LOGGER.error("Error creating card for activity: {}", 
                    activity != null ? activity.getName() : "null", e);
            }
        }
    }

    /**
     * Updates the column with new activities.
     * 
     * @param newActivities the updated list of activities
     */
    public void updateActivities(final List<CActivity> newActivities) {
        LOGGER.debug("Updating activities for type: {} from {} to {} activities", 
            activityType.getName(), 
            this.activities.size(), 
            newActivities != null ? newActivities.size() : 0);
        
        this.activities = newActivities != null ? newActivities : List.of();
        
        // Update count
        if (countElement != null) {
            countElement.setText("(" + activities.size() + ")");
        }
        
        // Repopulate cards
        populateCards();
    }

    /**
     * Gets the activity type for this column.
     * 
     * @return the activity type
     */
    public CActivityType getActivityType() {
        return activityType;
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
     * Refreshes the entire column display.
     * Useful for real-time updates.
     */
    public void refresh() {
        LOGGER.debug("Refreshing column for type: {}", activityType.getName());
        populateCards();
    }
}