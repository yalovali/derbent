package tech.derbent.app.sprints.domain;

import java.time.LocalDate;
import com.vaadin.flow.component.icon.Icon;
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
import jakarta.validation.constraints.NotNull;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.screens.service.IOrderedEntity;
import tech.derbent.base.users.domain.CUser;

/** CSprintItem - Progress tracking entity for Sprint-ProjectItem relationships. 
 * Represents an item (Activity, Meeting, etc.) with its progress-related data (story points, dates, responsible person, progress %).
 * The business event entity (CActivity, CMeeting) contains event details while CSprintItem contains progress tracking.
 * When sprint is null, the item is in the backlog. */
@Entity
@Table (name = "csprint_items")
@AttributeOverride (name = "id", column = @Column (name = "sprint_item_id"))
public class CSprintItem extends CEntityDB<CSprintItem> implements IOrderedEntity, IHasIcon {

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
	// Transient field - loaded dynamically at runtime from itemId and itemType
	@Transient
	private ISprintableItem item;
	// Store only the ID of the project item - loaded dynamically at runtime
	@Column (name = "item_id", nullable = false)
	@NotNull (message = "Project item ID is required")
	@AMetaData (
			displayName = "Item ID", required = true, readOnly = false, description = "ID of the project item (activity, meeting, etc.)",
			hidden = false
	)
	private Long itemId;
	@Column (name = "itemOrder", nullable = false)
	@Min (value = 1, message = "Line order must be at least 1")
	@Max (value = 999, message = "Line order cannot exceed 999")
	@NotNull (message = "Item order is required")
	@AMetaData (
			displayName = "Order", required = true, readOnly = false, description = "Display order of this item in the sprint", hidden = false,
			defaultValue = "0"
	)
	private Integer itemOrder = 0;
	@Column (name = "item_type", nullable = false, length = 50)
	@NotNull (message = "Item type is required")
	@AMetaData (
			displayName = "Item Type", required = true, readOnly = false, description = "Type of the project item (CActivity, CMeeting, etc.)",
			hidden = false, maxLength = 50
	)
	private String itemType;
	@Transient
	private Long kanbanColumnId;
	
	// Sprint reference - nullable to support backlog items (sprint = null means in backlog)
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "sprint_id", nullable = true)
	@AMetaData (
			displayName = "Sprint", required = false, readOnly = false, 
			description = "The sprint this item belongs to (null = backlog)", hidden = false,
			dataProviderBean = "CSprintService"
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
			displayName = "Progress %", required = false, readOnly = false, defaultValue = "0", 
			description = "Completion percentage (0-100)", hidden = false
	)
	private Integer progressPercentage = 0;
	
	@Column (name = "start_date", nullable = true)
	@AMetaData (
			displayName = "Start Date", required = false, readOnly = false, 
			description = "Planned or actual start date", hidden = false
	)
	private LocalDate startDate;
	
	@Column (name = "due_date", nullable = true)
	@AMetaData (
			displayName = "Due Date", required = false, readOnly = false, 
			description = "Expected completion date", hidden = false
	)
	private LocalDate dueDate;
	
	@Column (name = "completion_date", nullable = true)
	@AMetaData (
			displayName = "Completion Date", required = false, readOnly = true, 
			description = "Actual completion date", hidden = false
	)
	private LocalDate completionDate;
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "responsible_id", nullable = true)
	@AMetaData (
			displayName = "Responsible", required = false, readOnly = false,
			description = "Person responsible for completing this work", hidden = false, 
			dataProviderBean = "CUserService"
	)
	private CUser responsible;
	
	@Transient
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current status of the item", hidden = false,
			setBackgroundFromColor = true, useIcon = true
	)
	protected CProjectItemStatus status;

	/** Default constructor for JPA. */
	public CSprintItem() {
		super();
	}

	/** Constructor with sprint and item (for backward compatibility).
	 * @param sprint the sprint
	 * @param item   the project item */
	public CSprintItem(final CSprint sprint, final ISprintableItem item) {
		super();
		this.sprint = sprint;
		itemId = item.getId();
		itemType = item.getClass().getSimpleName();
		this.item = item;
		status = item.getStatus();
		item.setSprintItem(this);
	}

	/** Constructor with sprint, item, and order (for backward compatibility).
	 * @param sprint    the sprint
	 * @param item      the project item
	 * @param itemOrder the display order */
	public CSprintItem(final CSprint sprint, final ISprintableItem item, final Integer itemOrder) {
		this(sprint, item);
		this.itemOrder = itemOrder;
	}

	@Override
	public String getColor() { // TODO Auto-generated method stub
		return ((IHasIcon) getItem()).getColor();
	}

	public CComponentWidgetEntity<CSprintItem> getComponentWidget() { return componentWidget; }

	@Override
	public Icon getIcon() { return ((IHasIcon) getItem()).getIcon(); }

	@Override
	public String getIconString() { // TODO Auto-generated method stub
		return ((IHasIcon) getItem()).getIconString();
	}

	/** Get the project item. This is a transient field that must be loaded at runtime. Use CSprintItemService.loadItem() to populate this field.
	 * @return the project item, or null if not loaded */
	public ISprintableItem getItem() { return item; }

	public Long getItemId() { return itemId; }

	@Override
	public Integer getItemOrder() { return itemOrder; }

	public String getItemType() { return itemType; }

	/** Returns the assigned kanban column id for UI placement. */
	public Long getKanbanColumnId() { return kanbanColumnId; }

	public CSprint getSprint() { return sprint; }

	public CProjectItemStatus getStatus() {
		if (item == null) {
			return null;
		}
		return item.getStatus();
	}

	@Override
	public void setColor(String color) {
		// TODO Auto-generated method stub
	}

	/** Set the project item. This also updates itemId and itemType.
	 * @param item the project item */
	public void setItem(final ISprintableItem item) {
		this.item = item;
		if (item != null) {
			itemId = item.getId();
			itemType = item.getClass().getSimpleName();
		}
	}

	public void setItemId(final Long itemId) { this.itemId = itemId; }

	@Override
	public void setItemOrder(final Integer itemOrder) { this.itemOrder = itemOrder; }

	public void setItemType(final String itemType) { this.itemType = itemType; }

	/** Sets the assigned kanban column id for UI placement. */
	public void setKanbanColumnId(final Long kanbanColumnId) {
		this.kanbanColumnId = kanbanColumnId;
	}

	public void setSprint(final CSprint sprint) { this.sprint = sprint; }
	
	public Long getStoryPoint() { return storyPoint; }
	
	public void setStoryPoint(final Long storyPoint) { 
		this.storyPoint = storyPoint; 
	}
	
	public Integer getProgressPercentage() { return progressPercentage != null ? progressPercentage : 0; }
	
	public void setProgressPercentage(final Integer progressPercentage) {
		this.progressPercentage = progressPercentage != null ? progressPercentage : 0;
		// Auto-set completion date if progress reaches 100%
		if (progressPercentage != null && progressPercentage >= 100 && completionDate == null) {
			completionDate = LocalDate.now();
		}
	}
	
	public LocalDate getStartDate() { return startDate; }
	
	public void setStartDate(final LocalDate startDate) { 
		this.startDate = startDate; 
	}
	
	public LocalDate getDueDate() { return dueDate; }
	
	public void setDueDate(final LocalDate dueDate) { 
		this.dueDate = dueDate; 
	}
	
	public LocalDate getCompletionDate() { return completionDate; }
	
	public void setCompletionDate(final LocalDate completionDate) {
		this.completionDate = completionDate;
		if (completionDate != null && progressPercentage != null && progressPercentage < 100) {
			progressPercentage = 100;
		}
	}
	
	public CUser getResponsible() { return responsible; }
	
	public void setResponsible(final CUser responsible) { 
		this.responsible = responsible; 
	}

	@Override
	public String toString() {
		return String.format("CSprintItem{id=%d, sprint=%s, itemId=%d, itemType=%s, order=%d}", getId(), sprint != null ? sprint.getName() : "null",
				itemId, itemType, itemOrder);
	}
}
