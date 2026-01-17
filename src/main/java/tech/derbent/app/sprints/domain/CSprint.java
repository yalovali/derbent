package tech.derbent.app.sprints.domain;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.gannt.ganntitem.service.IGanntEntityItem;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.activities.service.IActivityRepository;
import tech.derbent.app.meetings.service.IMeetingRepository;
import org.slf4j.LoggerFactory;


// @AssociationOverride (name = "status", joinColumns = @JoinColumn (name = "sprint_status_id"))
@Entity
@Table (name = "csprint")
@AttributeOverride (name = "id", column = @Column (name = "sprint_id"))
public class CSprint extends CProjectItem<CSprint>
		implements IHasStatusAndWorkflow<CSprint>, IGanntEntityItem, IHasIcon, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#8377C5"; // CDE Active Purple - time-boxed work
	public static final String DEFAULT_ICON = "vaadin:calendar-clock";
	public static final String ENTITY_TITLE_PLURAL = "Sprints";
	public static final String ENTITY_TITLE_SINGULAR = "Sprint";
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CSprint.class);
	public static final String VIEW_NAME = "Sprints View";

	private static boolean isSameSprintable(final ISprintableItem item, final CSprintItem sprintItem) {
		return sprintItem.getParentItem() != null && sprintItem.getParentItem().equals(item);
	}

	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "sprint_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Sprint documentation and files", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "sprint_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this sprint", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Transient
	@AMetaData (
			displayName = "Item Detail", required = false, readOnly = false, description = "Item fields", hidden = false,
			createComponentMethod = "createSpritBacklogComponent", dataProviderBean = "pageservice", captionVisible = false
	)
	private final List<CSprintItem> backlogItems = new ArrayList<>();
	@Column (nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = DEFAULT_COLOR,
			description = "Color code for sprint visualization (hex format)", hidden = false, colorField = true
	)
	private String color = DEFAULT_COLOR;
	@Transient
	@AMetaData (
			displayName = "Component Widget", required = false, readOnly = false, description = "Component Widget for item", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComponentWidget"
	)
	private final CComponentWidgetEntity<CSprint> componentWidget = null;
	// Sprint Basic Information
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, defaultValue = "",
			description = "Detailed description of the sprint goals and objectives", hidden = false, maxLength = 2000
	)
	private String description;
	@Column (nullable = true)
	@AMetaData (displayName = "End Date", required = true, readOnly = false, description = "Sprint end date", hidden = false)
	private LocalDate endDate;
	// Type Management - using CSprintType
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Sprint Type", required = false, readOnly = false, description = "Type/category of the sprint", hidden = false,
			dataProviderBean = "CSprintTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CSprintType entityType;
	// Calculated field for display - populated automatically after entity load via @PostLoad
	// Service callback: CSprintService.getItemCount(CSprint)
	@Transient
	@AMetaData (
			displayName = "Item Count", required = false, readOnly = true, description = "Total number of items in this sprint", hidden = false,
			dataProviderBean = "CSprintService", dataProviderMethod = "getItemCount", autoCalculate = true, dataProviderParamMethod = "this"
	)
	private Integer itemCount;
	@Transient
	@AMetaData (
			displayName = "Backlog Items", required = false, readOnly = false, description = "Items (activities, meetings, etc.", hidden = false,
			createComponentMethod = "createItemDetailsComponent", dataProviderBean = "pageservice", captionVisible = false
	)
	private final int itemDetails = 0;
	// Sprint Items - Collection of progress tracking items for activities/meetings in this sprint
	// Sprint items are owned by CActivity/CMeeting, sprint is just a reference
	// Query items via: SELECT a FROM CActivity a WHERE a.sprintItem.sprint = :sprint
	@OneToMany (mappedBy = "sprint", cascade = {
			CascadeType.PERSIST, CascadeType.MERGE
	}, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "Sprint Items", required = false, readOnly = false,
			description = "Progress tracking items for activities/meetings in this sprint", hidden = false,
			createComponentMethod = "createSpritActivitiesComponent", dataProviderBean = "pageservice", captionVisible = false
	)
	private List<CSprintItem> sprintItems = new ArrayList<>();
	@Column (nullable = true)
	@AMetaData (
			displayName = "Start Date", required = false, readOnly = false, description = "Planned or actual start date of the sprint", hidden = false
	)
	private LocalDate startDate;
	// Scrum Guide 2020 - Core Sprint Artifacts
	@Column (nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Sprint Goal", required = false, readOnly = false,
			description = "The single objective for the Sprint - Scrum Guide 2020 core artifact", hidden = false, maxLength = 500
	)
	private String sprintGoal;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Definition of Done", required = false, readOnly = false,
			description = "Shared understanding of what it means for work to be complete - Scrum Guide 2020", hidden = false, maxLength = 2000
	)
	private String definitionOfDone;
	@Column (nullable = true)
	@AMetaData (
			displayName = "Velocity", required = false, readOnly = true, description = "Story points completed in this sprint - Agile metric",
			hidden = false
	)
	private Integer velocity;
	@Column (nullable = true, length = 4000)
	@Size (max = 4000)
	@AMetaData (
			displayName = "Retrospective Notes", required = false, readOnly = false,
			description = "What went well, what needs improvement, action items - Scrum Guide 2020", hidden = false, maxLength = 4000
	)
	private String retrospectiveNotes;
	// Calculated field for total story points - populated automatically after entity load via @PostLoad
	// Service callback: CSprintService.getTotalStoryPoints(CSprint)
	@Transient
	@AMetaData (
			displayName = "Total Story Points", required = false, readOnly = true, description = "Sum of story points for all items in this sprint",
			hidden = false, dataProviderBean = "CSprintService", dataProviderMethod = "getTotalStoryPoints", autoCalculate = true,
			dataProviderParamMethod = "this"
	)
	private Long totalStoryPoints;

	/** Default constructor for JPA. */
	public CSprint() {
		super();
		initializeDefaults();
	}

	/** Constructor with name and project.
	 * @param name    the name of the sprint - must not be null
	 * @param project the project this sprint belongs to - must not be null */
	public CSprint(final String name, final CProject project) {
		super(CSprint.class, name, project);
		initializeDefaults();
	}

	/** Constructor with name, project, and dates.
	 * @param name      the name of the sprint - must not be null
	 * @param project   the project this sprint belongs to - must not be null
	 * @param startDate the sprint start date
	 * @param endDate   the sprint end date */
	public CSprint(final String name, final CProject project, final LocalDate startDate, final LocalDate endDate) {
		super(CSprint.class, name, project);
		setStartDate(startDate);
		this.endDate = endDate;
		initializeDefaults();
	}

	/** Add a project item to this sprint by setting its sprintItem.sprint reference.
	 * @param item the project item (CActivity/CMeeting) to add to sprint */
	public void addItem(final ISprintableItem item) {
		if (item == null || item.getSprintItem() == null) {
			return;
		}
		// Check if already in sprint
		if (sprintItems != null) {
			final boolean alreadyPresent = sprintItems.stream().anyMatch(si -> isSameSprintable(item, si));
			if (alreadyPresent) {
				return;
			}
		}
		// Set sprint reference - item remains owned by parent
		item.getSprintItem().setSprint(this);
		updateLastModified();
	}

	/** Add a sprint item to this sprint.
	 * @param sprintItem the sprint item to add */
	public void addSprintItem(final CSprintItem sprintItem) {
		if (sprintItem != null) {
			sprintItem.setSprint(this);
			updateLastModified();
		}
	}

	/** Calculate velocity from completed sprint items (Scrum Guide 2020 metric). Velocity is the sum of story points for items that have reached a
	 * final status. This method should be called at sprint completion to record historical velocity. */
	public void calculateVelocity() {
		if (sprintItems == null) {
			velocity = 0;
			return;
		}
		velocity = sprintItems.stream().filter(item -> item.getParentItem() != null).filter(item -> {
			final ISprintableItem parent = item.getParentItem();
			if (parent.getStatus() != null && parent.getStatus().getFinalStatus()) {
				return true;
			}
			return false;
		}).map(CSprintItem::getStoryPoint).filter(sp -> sp != null).mapToInt(Long::intValue).sum();
		updateLastModified();
	}

	/** Gets the activities in this sprint.
	 * @return list of activities */
	public List<CActivity> getActivities() {
		final List<CActivity> activities = new ArrayList<>();
		if (sprintItems != null) {
			for (final CSprintItem sprintItem : sprintItems) {
				if (sprintItem.getParentItem() instanceof CActivity) {
					activities.add((CActivity) sprintItem.getParentItem());
				}
			}
		}
		return activities;
	}

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public String getColor() { return color; }

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	public CComponentWidgetEntity<CSprint> getComponentWidget() { return componentWidget; }

	public String getDefinitionOfDone() { return definitionOfDone; }

	@Override
	public String getDescription() { return description; }

	@Override
	public LocalDate getEndDate() { return endDate; }

	/** Gets the sprint type.
	 * @return the sprint type */
	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	/** Gets the icon for Gantt chart display.
	 * @return the sprint icon identifier */
	@Override
	public String getIconString() { return DEFAULT_ICON; }

	/** Get the total number of items in this sprint. This is a calculated field for UI display.
	 * @return total count of sprint items */
	public Integer getItemCount() { return sprintItems != null ? sprintItems.size() : 0; }

	/** Get all sprint items (activities and meetings combined) as a list. This is a convenience method for backward compatibility.
	 * @return combined list of all sprint items */
	public List<ISprintableItem> getItems() {
		final List<ISprintableItem> allItems = new ArrayList<>();
		if (sprintItems != null) {
			for (final CSprintItem sprintItem : sprintItems) {
				if (sprintItem.getParentItem() != null) {
					allItems.add(sprintItem.getParentItem());
				}
			}
		}
		return allItems;
	}

	/** Gets the meetings in this sprint.
	 * @return list of meetings */
	public List<CMeeting> getMeetings() {
		final List<CMeeting> meetings = new ArrayList<>();
		if (sprintItems != null) {
			for (final CSprintItem sprintItem : sprintItems) {
				if (sprintItem.getParentItem() instanceof CMeeting) {
					meetings.add((CMeeting) sprintItem.getParentItem());
				}
			}
		}
		return meetings;
	}

	@Override
	public Integer getProgressPercentage() {
		// Calculate progress based on completed activities
		final List<CActivity> activities = getActivities();
		if (activities == null || activities.isEmpty()) {
			return 0;
		}
		final long completedCount = activities.stream().filter(activity -> {
			if (activity.getStatus() != null && activity.getStatus().getFinalStatus()) {
				return true;
			}
			return false;
		}).count();
		return (int) (completedCount * 100 / activities.size());
	}

	public String getRetrospectiveNotes() { return retrospectiveNotes; }

	public String getSprintGoal() { return sprintGoal; }

	/** Gets the sprint items collection.
	 * @return list of sprint items */
	public List<CSprintItem> getSprintItems() { return sprintItems != null ? sprintItems : new ArrayList<>(); }

	@Override
	public LocalDate getStartDate() { return startDate; }

	/** Get the total story points for all items in this sprint. This is a calculated field for UI display.
	 * @return total story points, or 0 if no items have story points */
	public Long getTotalStoryPoints() {
		if (sprintItems == null || sprintItems.isEmpty()) {
			return 0L;
		}
		long total = 0L;
		for (final CSprintItem sprintItem : sprintItems) {
			final Long itemStoryPoint = sprintItem.getStoryPoint();
			if (itemStoryPoint != null) {
				total += itemStoryPoint;
			}
		}
		return total;
	}

	public Integer getVelocity() { return velocity; }

	@Override
	public CWorkflowEntity getWorkflow() { return getEntityType().getWorkflow(); }

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships
		if (getEntityType() != null) {
			getEntityType().getName(); // Trigger sprint type loading
		}
		if (status != null) {
			status.getName(); // Trigger status loading
		}
		// Parent class relationships (from CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
		// Note: sprint items collection will be initialized if accessed
	}

	/** Initialize default values for the sprint. */
	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (color == null || color.isEmpty()) {
			color = DEFAULT_COLOR;
		}
		if (sprintItems == null) {
			sprintItems = new ArrayList<>();
		}
		if (getStartDate() == null) {
			setStartDate(LocalDate.now());
		}
		if (endDate == null) {
			endDate = LocalDate.now().plusWeeks(2); // Default to 2-week sprint
		}
	}
	// Getters and Setters

	/** Check if the sprint is active (current date is between start and end dates).
	 * @return true if the sprint is currently active */
	@Transient
	public boolean isActive() {
		if (getStartDate() == null || endDate == null) {
			return false;
		}
		final LocalDate now = LocalDate.now();
		return !now.isBefore(getStartDate()) && !now.isAfter(endDate);
	}

	/** Check if the sprint is completed (end date has passed).
	 * @return true if the sprint has ended */
	@Transient
	public boolean isCompleted() {
		if (endDate == null) {
			return false;
		}
		return LocalDate.now().isAfter(endDate);
	}

	/** JPA lifecycle callback: Populates transient calculated fields after entity is loaded from database. This method automatically discovers fields
	 * with @AMetaData(autoCalculate=true) annotations and invokes the corresponding service methods using the data provider pattern. This approach
	 * combines JPA lifecycle callbacks with service-layer business logic.
	 * @throws Exception */
	@PostLoad
	protected void postLoadEntity() throws Exception {
		try {
			final CDataProviderResolver resolver = CSpringContext.getBean(CDataProviderResolver.class);
			final List<Field> fields = CEntityFieldService.getAllFields(this.getClass()).stream()
					.filter(field -> field.isAnnotationPresent(AMetaData.class) && field.getAnnotation(AMetaData.class).autoCalculate()).toList();
			for (final Field field : fields) {
				final AMetaData metadata = field.getAnnotation(AMetaData.class);
				final Object value = resolver.resolveMethodAnnotations(this, null, CEntityFieldService.createFieldInfo(metadata));
				field.setAccessible(true);
				field.set(this, value);
			}
			if (sprintItems != null && !sprintItems.isEmpty()) {
				final IActivityRepository activityRepo =
						CSpringContext.getBean(IActivityRepository.class);
				final IMeetingRepository meetingRepo =
						CSpringContext.getBean(IMeetingRepository.class);
				for (final CSprintItem item : sprintItems) {
					if (item.getId() == null) {
						continue;
					}
					final var activity = activityRepo.findBySprintItemId(item.getId());
					if (activity.isPresent()) {
						item.setParentItem(activity.get());
						continue;
					}
					final var meeting = meetingRepo.findBySprintItemId(item.getId());
					meeting.ifPresent(item::setParentItem);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error in @PostLoad calculateTransientFields: {}", e.getMessage());
			throw e;
		}
	}

	/** Remove an activity from this sprint by setting its sprintItem.sprint to null.
	 * @param activity the activity to remove */
	public void removeActivity(final CActivity activity) {
		if (activity != null && activity.getSprintItem() != null) {
			activity.getSprintItem().setSprint(null);
			updateLastModified();
		}
	}

	/** Remove a project item from this sprint by setting its sprintItem.sprint to null.
	 * @param item the project item to remove */
	public void removeItem(final ISprintableItem item) {
		if (item == null || item.getSprintItem() == null) {
			return;
		}
		item.getSprintItem().setSprint(null);
		updateLastModified();
	}

	/** Sets the activities in this sprint.
	 * @param activities the activities to set */
	public void setActivities(final List<CActivity> activities) {
		// Remove all current activities from sprint
		if (sprintItems != null) {
			for (final CSprintItem si : new ArrayList<>(sprintItems)) {
				if (si.getParentItem() instanceof CActivity) {
					si.setSprint(null);
				}
			}
		}
		// Add new activities
		if (activities != null) {
			for (final CActivity activity : activities) {
				if (activity.getSprintItem() != null) {
					activity.getSprintItem().setSprint(this);
				}
			}
		}
		updateLastModified();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setColor(final String color) {
		this.color = color;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDefinitionOfDone(final String definitionOfDone) {
		this.definitionOfDone = definitionOfDone;
		updateLastModified();
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}
	// IGanntEntityItem implementation

	public void setEndDate(final LocalDate endDate) {
		this.endDate = endDate;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CSprintType.class, "Type entity must be an instance of CSprintType");
		Check.notNull(getProject(), "Project must be set before assigning sprint type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning sprint type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning sprint type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match sprint project company id " + getProject().getCompany().getId());
		entityType = (CSprintType) typeEntity;
		updateLastModified();
	}
	// Scrum Guide 2020 - Getters/Setters

	public void setItemCount(final Integer itemCount) { this.itemCount = itemCount; }
	// IHasStatusAndWorkflow implementation

	public void setItems(final List<ISprintableItem> items) {
		// Remove all current items from sprint
		if (sprintItems != null) {
			for (final CSprintItem si : new ArrayList<>(sprintItems)) {
				si.setSprint(null);
			}
		}
		// Add new items
		if (items != null) {
			for (final ISprintableItem item : items) {
				if (item.getSprintItem() != null) {
					item.getSprintItem().setSprint(this);
				}
			}
		}
		updateLastModified();
	}

	/** Sets the meetings in this sprint.
	 * @param meetings the meetings to set */
	public void setMeetings(final List<CMeeting> meetings) {
		// Remove all current meetings from sprint
		if (sprintItems != null) {
			for (final CSprintItem si : new ArrayList<>(sprintItems)) {
				if (si.getParentItem() instanceof CMeeting) {
					si.setSprint(null);
				}
			}
		}
		// Add new meetings
		if (meetings != null) {
			for (final CMeeting meeting : meetings) {
				if (meeting.getSprintItem() != null) {
					meeting.getSprintItem().setSprint(this);
				}
			}
		}
		updateLastModified();
	}

	public void setRetrospectiveNotes(final String retrospectiveNotes) {
		this.retrospectiveNotes = retrospectiveNotes;
		updateLastModified();
	}

	public void setSprintGoal(final String sprintGoal) {
		this.sprintGoal = sprintGoal;
		updateLastModified();
	}

	/** Sets the sprint items collection.
	 * @param sprintItems the sprint items to set */
	public void setSprintItems(final List<CSprintItem> sprintItems) {
		this.sprintItems = sprintItems != null ? sprintItems : new ArrayList<>();
		updateLastModified();
	}

	public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

	/** Sets the total story points. This is populated automatically via @PostLoad after entity is loaded.
	 * @param totalStoryPoints the total story points value */
	public void setTotalStoryPoints(final Long totalStoryPoints) {
		this.totalStoryPoints = totalStoryPoints;
	}

	public void setVelocity(final Integer velocity) {
		this.velocity = velocity;
		updateLastModified();
	}

	/**
	 * Creates a clone of this sprint with the specified options.
	 * This implementation demonstrates the recursive cloning pattern:
	 * 1. Calls parent's createClone() to handle inherited fields
	 * 2. Clones sprint-specific fields based on options
	 * 3. Returns the fully cloned sprint
	 * 
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the sprint with cloned data
	 * @throws CloneNotSupportedException if cloning fails
	 */
	@Override
	public CSprint createClone(final CCloneOptions options) throws Exception {
		// Get parent's clone (CProjectItem -> CEntityOfProject -> CEntityNamed -> CEntityDB)
		final CSprint clone = super.createClone(options);

		// Clone basic sprint fields (always included)
		clone.description = this.description;
		clone.color = this.color;
		clone.sprintGoal = this.sprintGoal;
		clone.definitionOfDone = this.definitionOfDone;
		clone.retrospectiveNotes = this.retrospectiveNotes;
		
		// Clone sprint type (not a date or assignment)
		clone.entityType = this.entityType;
		
		// Clone workflow if requested
		if (options.isCloneWorkflow() && this.getWorkflow() != null) {
			// Workflow is already handled by entityType, no additional cloning needed
		}
		
		// Handle date fields based on options
		if (!options.isResetDates()) {
			clone.startDate = this.startDate;
			clone.endDate = this.endDate;
		}
		// If resetDates is true, leave dates null
		
		// Clone velocity if dates are preserved (velocity is date-related metric)
		if (!options.isResetDates()) {
			clone.velocity = this.velocity;
		}
		
		// Clone comments if requested
		if (options.includesComments() && this.comments != null && !this.comments.isEmpty()) {
			clone.comments = new HashSet<>();
			for (final CComment comment : this.comments) {
				try {
					final CComment commentClone = comment.createClone(options);
					clone.comments.add(commentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone comment: {}", e.getMessage());
				}
			}
		}
		
		// Clone attachments if requested
		if (options.includesAttachments() && this.attachments != null && !this.attachments.isEmpty()) {
			clone.attachments = new HashSet<>();
			for (final CAttachment attachment : this.attachments) {
				try {
					final CAttachment attachmentClone = attachment.createClone(options);
					clone.attachments.add(attachmentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone attachment: {}", e.getMessage());
				}
			}
		}
		
		// Note: Sprint items (sprintItems collection) are NOT cloned
		// Sprint items are owned by activities/meetings, not the sprint
		// Clone starts with empty sprint items collection
		
		LOGGER.debug("Successfully cloned sprint '{}' with options: {}", this.getName(), options);
		return clone;
	}
}
