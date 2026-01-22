package tech.derbent.plm.sprints.domain;

import java.time.LocalDate;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.screens.service.IOrderedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.activities.domain.CActivity;

/** CSprintItem - Progress tracking component owned by CActivity/CMeeting.
 * <p>
 * <strong>OWNERSHIP AND LIFECYCLE:</strong>
 * </p>
 * <p>
 * Sprint items are OWNED by their parent entities (CActivity/CMeeting) via @OneToOne with CASCADE.ALL and orphanRemoval=true. This means:
 * </p>
 * <ul>
 * <li>Sprint items are created ONCE when Activity/Meeting is created</li>
 * <li>Sprint items are NEVER deleted independently - only when parent is deleted</li>
 * <li>Sprint items are NEVER replaced - only their properties are modified</li>
 * <li>Deleting a sprint item will CASCADE DELETE its parent entity</li>
 * </ul>
 * <p>
 * <strong>BACKLOG SEMANTICS:</strong>
 * </p>
 * <p>
 * The sprint field determines whether an item is in backlog or assigned to a sprint:
 * </p>
 * <ul>
 * <li><strong>sprint = NULL</strong>: Item is in the backlog (not assigned to any sprint)</li>
 * <li><strong>sprint = CSprint</strong>: Item is assigned to that specific sprint</li>
 * </ul>
 * <p>
 * <strong>CORRECT USAGE PATTERNS:</strong>
 * </p>
 *
 * <pre>
 * // ✅ CORRECT: Modify sprint reference to move between backlog and sprint
 * activity.getSprintItem().setSprint(targetSprint); // Add to sprint
 * activity.getSprintItem().setSprint(null); // Move to backlog
 * // ❌ WRONG: These patterns violate ownership and cause data loss
 * activity.setSprintItem(new CSprintItem()); // Creates orphaned sprint item
 * sprintItemService.delete(sprintItem); // Deletes parent Activity/Meeting
 * activity.setSprintItem(null); // Orphans sprint item, causes constraint violation
 * </pre>
 * <p>
 * <strong>DRAG-DROP OPERATIONS:</strong>
 * </p>
 * <p>
 * All drag-drop operations that move items between backlog and sprint MUST:
 * </p>
 * <ul>
 * <li>Use CSprintItemDragDropService for unified handling</li>
 * <li>Set sprint field to NULL for backlog, or target sprint for sprint assignment</li>
 * <li>NEVER delete sprint items during drag-drop</li>
 * <li>NEVER call item.setSprintItem() to replace the sprint item</li>
 * </ul>
 * <p>
 * <strong>DATA STORAGE:</strong>
 * </p>
 * <p>
 * Stores progress-related data (story points, dates, responsible person, progress %). Implements IOrderedEntity for ordering within sprints/backlog.
 * </p>
 * @see CActivity
 * @see tech.derbent.plm.meetings.domain.CMeeting
 * @see tech.derbent.plm.sprints.service.CSprintItemDragDropService */
@Entity
@Table (name = "csprint_items")
@AttributeOverride (name = "id", column = @Column (name = "sprint_item_id"))
public class CSprintItem extends CEntityDB<CSprintItem> implements IHasIcon, IOrderedEntity {

	public static final String DEFAULT_COLOR = "#8377C5"; // CDE Active Purple - sprint items
	public static final String DEFAULT_ICON = "vaadin:list-ol";
	public static final String ENTITY_TITLE_PLURAL = "Sprint Items";
	public static final String ENTITY_TITLE_SINGULAR = "Sprint Item";
	public static final String VIEW_NAME = "Sprint Items View";
	@AMetaData (
			displayName = "Component Widget", required = false, readOnly = false, description = "Component Widget for item", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComponentWidget"
	)
	private final CComponentWidgetEntity<CSprintItem> componentWidget = null;
	// Transient back-reference to parent entity (CActivity/CMeeting)
	// Set by parent after loading to enable display in widgets/grids
	@Transient
	private ISprintableItem parentItem;
	// Transient field for kanban board display - temporary column assignment
	@Transient
	private Long kanbanColumnId;
	// Item order within sprint or backlog
	@Column (name = "item_order", nullable = true)
	@AMetaData (displayName = "Order", required = false, readOnly = false, description = "Display order within sprint or backlog", hidden = false)
	private Integer itemOrder;
	// Sprint reference - nullable to support backlog items (sprint = null means in
	// backlog)
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "sprint_id", nullable = true)
	@AMetaData (
			displayName = "Sprint", required = false, readOnly = false, description = "The sprint this item belongs to (null = backlog)",
			hidden = false, dataProviderBean = "CSprintService"
	)
	private CSprint sprint;
	// Progress tracking fields - moved from CActivity/CMeeting
	@Column (nullable = true)
	@AMetaData (
			displayName = "Story Points", required = false, readOnly = false, defaultValue = "0",
			description = "Estimated effort or complexity in story points", hidden = false
	)
	private Long storyPoint;
	@Column (nullable = true)
	@Min (value = 0, message = "Progress percentage must be between 0 and 100")
	@Max (value = 100, message = "Progress percentage must be between 0 and 100")
	@AMetaData (
			displayName = "Progress %", required = false, readOnly = false, defaultValue = "0", description = "Completion percentage (0-100)",
			hidden = false
	)
	private Integer progressPercentage = 0;
	@Column (name = "start_date", nullable = true)
	@AMetaData (displayName = "Start Date", required = false, readOnly = false, description = "Planned or actual start date", hidden = false)
	private LocalDate startDate;
	@Column (name = "due_date", nullable = true)
	@AMetaData (displayName = "Due Date", required = false, readOnly = false, description = "Expected completion date", hidden = false)
	private LocalDate dueDate;
	@Column (name = "completion_date", nullable = true)
	@AMetaData (displayName = "Completion Date", required = false, readOnly = true, description = "Actual completion date", hidden = false)
	private LocalDate completionDate;
	@Transient
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current status of the item", hidden = false,
			setBackgroundFromColor = true, useIcon = true
	)
	protected CProjectItemStatus status;

	/** Default constructor for JPA. */
	public CSprintItem() {
		super();
		initializeDefaults();
	}

	public CUser getAssignedTo() { return parentItem.getAssignedTo(); }

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	public LocalDate getCompletionDate() { return completionDate; }

	public CComponentWidgetEntity<CSprintItem> getComponentWidget() { return componentWidget; }

	public LocalDate getDueDate() { return dueDate; }

	@Override
	public Icon getIcon() { return new Icon(VaadinIcon.LIST_OL); }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	public Long getItemId() { return parentItem.getId(); }

	@Override
	public Integer getItemOrder() { return itemOrder; }

	/** Get the kanban column ID for transient display purposes.
	 * @return the kanban column ID, or null if not set */
	public Long getKanbanColumnId() { return kanbanColumnId; }

	/** Get the parent sprintable item (CActivity/CMeeting).
	 * @return the parent item
	 * @throws IllegalStateException if parentItem is null */
	public ISprintableItem getParentItem() {
		Check.notNull(parentItem, "parentItem must be set by parent entity after loading");
		return parentItem;
	}

	public Integer getProgressPercentage() { return progressPercentage != null ? progressPercentage : 0; }

	public CSprint getSprint() { return sprint; }

	public LocalDate getStartDate() { return startDate; }

	public Long getStoryPoint() { return storyPoint; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		progressPercentage = 0;
		storyPoint = 0L;
		startDate = LocalDate.now();
		dueDate = LocalDate.now().plusDays(7); // Default to 1 week
		setSprint(null);
		setItemOrder(1); // Default order
	}

	@Override
	public void setColor(String color) {
		// Not used
	}

	public void setCompletionDate(final LocalDate completionDate) {
		this.completionDate = completionDate;
		if (completionDate != null && progressPercentage != null && progressPercentage < 100) {
			progressPercentage = 100;
		}
	}

	public void setDueDate(final LocalDate dueDate) { this.dueDate = dueDate; }

	@Override
	public void setItemOrder(final Integer itemOrder) { this.itemOrder = itemOrder; }

	/** Set the kanban column ID for transient display purposes. Used to track which kanban column this item is displayed in.
	 * @param kanbanColumnId the kanban column ID */
	public void setKanbanColumnId(final Long kanbanColumnId) { this.kanbanColumnId = kanbanColumnId; }
	// IOrderedEntity implementation

	/** Set the parent sprintable item (CActivity/CMeeting).
	 * @param parentItem the parent item */
	public void setParentItem(final ISprintableItem parentItem) { this.parentItem = parentItem; }

	public void setProgressPercentage(final Integer progressPercentage) {
		this.progressPercentage = progressPercentage != null ? progressPercentage : 0;
		// Auto-set completion date if progress reaches 100%
		if (progressPercentage != null && progressPercentage >= 100 && completionDate == null) {
			completionDate = LocalDate.now();
		}
	}
	// Kanban display support

	public void setSprint(final CSprint sprint) { this.sprint = sprint; }

	public void setStartDate(final LocalDate startDate) { this.startDate = startDate; }

	public void setStoryPoint(final Long storyPoint) { this.storyPoint = storyPoint; }

	@Override
	public String toString() {
		return String.format("CSprintItem{id=%d, sprint=%s, storyPoint=%d, progress=%d%%}", getId(), sprint != null ? sprint.getName() : "backlog",
				storyPoint != null ? storyPoint : 0, progressPercentage != null ? progressPercentage : 0);
	}
}
