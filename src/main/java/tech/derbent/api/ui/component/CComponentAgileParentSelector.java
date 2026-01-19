package tech.derbent.api.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.service.CActivityService;

/** Component for selecting a parent activity in the agile hierarchy. Allows selecting Epic, User Story, Feature, or other activity types as parent.
 * <p>
 * This component provides:
 * <ul>
 * <li>Filtering by project (only activities in same project)</li>
 * <li>Excluding the current entity (prevent self-parenting)</li>
 * <li>Hierarchical display with activity type indication</li>
 * <li>Clear indication of Epic → Story → Task hierarchy levels</li>
 * </ul>
 * </p>
 * <p>
 * Usage: This component can be used by any entity that implements IHasAgileParentRelation (Activities, Meetings, Issues, etc.).
 * </p>
 */
public class CComponentAgileParentSelector extends ComboBox<CActivity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentAgileParentSelector.class);
	private static final long serialVersionUID = 1L;
	private final CActivityService activityService;
	private final tech.derbent.api.domains.CAgileParentRelationService agileParentRelationService;
	private Long currentEntityId;
	private CProject<?> project;

	/** Constructor for parent selector component.
	 * @param activityService the activity service for loading activities
	 * @param agileParentRelationService the agile parent relation service for circular dependency checking */
	public CComponentAgileParentSelector(final CActivityService activityService,
			final tech.derbent.api.domains.CAgileParentRelationService agileParentRelationService) {
		super();
		Check.notNull(activityService, "Activity service cannot be null");
		Check.notNull(agileParentRelationService, "Agile parent relation service cannot be null");
		this.activityService = activityService;
		this.agileParentRelationService = agileParentRelationService;
		// Configure component
		setLabel("Parent Activity");
		setPlaceholder("Select parent activity (Epic, User Story, etc.)");
		setItemLabelGenerator(this::generateActivityLabel);
		setClearButtonVisible(true);
		setWidthFull();
		// Add help text
		setHelperText("Choose a parent activity to establish agile hierarchy (optional)");
	}

	/** Generate display label for an activity showing its hierarchy information. Format: "ActivityName (Type) - Description"
	 * @param activity the activity
	 * @return formatted label */
	private String generateActivityLabel(final CActivity activity) {
		if (activity == null) {
			return "";
		}
		final StringBuilder label = new StringBuilder();
		label.append(activity.getName());
		// Add activity type if available (cast to correct type)
		if (activity.getEntityType() != null && activity.getEntityType().getName() != null) {
			label.append(" (").append(activity.getEntityType().getName()).append(")");
		}
		// Add short description if available
		final String description = activity.getDescriptionShort();
		if (description != null && !description.isBlank()) {
			label.append(" - ").append(description);
		}
		return label.toString();
	}

	/** Get the list of selectable parent activities. Filters activities by project and excludes the current entity and its descendants.
	 * @return list of selectable activities */
	private List<CActivity> getSelectableActivities() {
		if (project == null) {
			LOGGER.warn("Cannot load activities - project is null");
			return new ArrayList<>();
		}
		try {
			// Load all activities for the project
			final List<CActivity> allActivities = activityService.listByProject(project);
			
			// Get descendants of current entity to exclude them (prevent circular dependencies)
			final java.util.Set<Long> excludedIds = new java.util.HashSet<>();
			if (currentEntityId != null) {
				excludedIds.add(currentEntityId); // Exclude self
				
				// Get current entity and its descendants
				final java.util.Optional<CActivity> currentEntity = activityService.getById(currentEntityId);
				if (currentEntity.isPresent()) {
					final java.util.List<tech.derbent.api.entityOfProject.domain.CProjectItem<?>> descendants = 
						agileParentRelationService.getAllDescendants(currentEntity.get());
					descendants.stream()
						.map(tech.derbent.api.entity.domain.CEntityDB::getId)
						.forEach(excludedIds::add);
				}
			}
			
			// Filter out excluded activities
			return allActivities.stream()
				.filter(activity -> !excludedIds.contains(activity.getId()))
				.collect(Collectors.toList());
		} catch (final Exception e) {
			LOGGER.error("Error loading activities for parent selector: {}", e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	/** Refresh the available parent activities based on current project and entity. */
	public void refresh() {
		final List<CActivity> activities = getSelectableActivities();
		setItems(activities);
		LOGGER.debug("Refreshed parent selector with {} activities for project '{}'", activities.size(),
				project != null ? project.getName() : "null");
	}

	/** Set the current entity ID being edited. This entity will be excluded from the parent selection list.
	 * @param currentEntityId the current entity ID */
	public void setCurrentEntityId(final Long currentEntityId) {
		this.currentEntityId = currentEntityId;
		refresh();
	}

	/** Set the parent activity value.
	 * @param parentActivity the parent activity to set, or null to clear */
	public void setParentActivity(final CActivity parentActivity) {
		setValue(parentActivity);
	}

	/** Set the project context for filtering activities. Only activities from the same project can be selected as parents.
	 * @param project the project context */
	public void setProject(final CProject<?> project) {
		this.project = project;
		refresh();
	}
}
