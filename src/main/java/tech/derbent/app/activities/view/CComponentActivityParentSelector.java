package tech.derbent.app.activities.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityType;
import tech.derbent.app.activities.service.CActivityService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Component for selecting a parent activity in the agile hierarchy.
 * Allows selecting Epic, User Story, Feature, or other activity types as parent.
 * <p>
 * This component provides:
 * <ul>
 * <li>Filtering by project (only activities in same project)</li>
 * <li>Excluding the current activity (prevent self-parenting)</li>
 * <li>Hierarchical display with activity type indication</li>
 * <li>Clear indication of Epic → Story → Task hierarchy levels</li>
 * </ul>
 * </p>
 */
public class CComponentActivityParentSelector extends ComboBox<CActivity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CComponentActivityParentSelector.class);
    
    private final CActivityService activityService;
    private CProject project;
    private CActivity currentActivity;

    /**
     * Constructor for parent selector component.
     * 
     * @param activityService the activity service for loading activities
     */
    public CComponentActivityParentSelector(final CActivityService activityService) {
        super();
        Check.notNull(activityService, "Activity service cannot be null");
        this.activityService = activityService;
        
        // Configure component
        setLabel("Parent Activity");
        setPlaceholder("Select parent activity (Epic, User Story, etc.)");
        setItemLabelGenerator(this::generateActivityLabel);
        setClearButtonVisible(true);
        setWidthFull();
        
        // Add help text
        setHelperText("Choose a parent activity to establish agile hierarchy (optional)");
    }

    /**
     * Generate display label for an activity showing its hierarchy information.
     * Format: "ActivityName (Type) - Description"
     * 
     * @param activity the activity
     * @return formatted label
     */
    private String generateActivityLabel(final CActivity activity) {
        if (activity == null) {
            return "";
        }
        
        final StringBuilder label = new StringBuilder();
        label.append(activity.getName());
        
        // Add activity type if available
        final CActivityType type = activity.getEntityType();
        if (type != null && type.getName() != null) {
            label.append(" (").append(type.getName()).append(")");
        }
        
        // Add short description if available
        final String description = activity.getDescriptionShort();
        if (description != null && !description.isBlank()) {
            label.append(" - ").append(description);
        }
        
        return label.toString();
    }

    /**
     * Get the list of selectable parent activities.
     * Filters activities by project and excludes the current activity.
     * 
     * @return list of selectable activities
     */
    private List<CActivity> getSelectableActivities() {
        if (project == null) {
            LOGGER.warn("Cannot load activities - project is null");
            return new ArrayList<>();
        }
        
        try {
            // Load all activities for the project
            final List<CActivity> allActivities = activityService.getAllByProject(project);
            
            // Filter out current activity and descendants (to prevent circular dependencies)
            return allActivities.stream()
                .filter(activity -> {
                    // Exclude current activity (prevent self-parenting)
                    if (currentActivity != null && activity.getId().equals(currentActivity.getId())) {
                        return false;
                    }
                    
                    // TODO: Exclude descendants of current activity (prevent circular dependencies)
                    // This would require calling parentRelationService.getAllDescendants(currentActivity)
                    // For now, service-side validation will catch circular dependencies
                    
                    return true;
                })
                .collect(Collectors.toList());
                
        } catch (final Exception e) {
            LOGGER.error("Error loading activities for parent selector: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Refresh the available parent activities based on current project and activity.
     */
    public void refresh() {
        final List<CActivity> activities = getSelectableActivities();
        final ListDataProvider<CActivity> dataProvider = DataProvider.ofCollection(activities);
        setDataProvider(dataProvider);
        
        LOGGER.debug("Refreshed parent selector with {} activities for project '{}'", 
            activities.size(), project != null ? project.getName() : "null");
    }

    /**
     * Set the current activity being edited.
     * This activity will be excluded from the parent selection list.
     * 
     * @param currentActivity the current activity
     */
    public void setCurrentActivity(final CActivity currentActivity) {
        this.currentActivity = currentActivity;
        refresh();
    }

    /**
     * Set the parent activity value.
     * 
     * @param parentActivity the parent activity to set, or null to clear
     */
    public void setParentActivity(final CActivity parentActivity) {
        setValue(parentActivity);
    }

    /**
     * Set the project context for filtering activities.
     * Only activities from the same project can be selected as parents.
     * 
     * @param project the project context
     */
    public void setProject(final CProject project) {
        this.project = project;
        refresh();
    }
}
