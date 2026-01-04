package tech.derbent.app.sprints.domain;

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

/** CSprintItem - Progress tracking component owned by CActivity/CMeeting. 
 * Stores progress-related data (story points, dates, responsible person, progress %).
 * When sprint is null, the item is in the backlog.
 * Implements IOrderedEntity for ordering within sprints/backlog. */
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
	@AMetaData (
			displayName = "Order", required = false, readOnly = false,
			description = "Display order within sprint or backlog", hidden = false
	)
	private Integer itemOrder;
	
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

	@Override
	public String getColor() { 
		return DEFAULT_COLOR;
	}

	public CComponentWidgetEntity<CSprintItem> getComponentWidget() { return componentWidget; }

	@Override
	public Icon getIcon() { 
		return new Icon(VaadinIcon.LIST_OL);
	}

	@Override
	public String getIconString() { 
		return DEFAULT_ICON;
	}

	public CSprint getSprint() { return sprint; }
	
	/** Get the parent sprintable item (CActivity/CMeeting).
	 * This is a transient back-reference set by the parent after loading.
	 * @return the parent item
	 * @throws IllegalStateException if parentItem is null (indicates architectural violation) */
	public ISprintableItem getParentItem() { 
		Check.notNull(parentItem, "parentItem is @Transient and must be set by CActivity/CMeeting "
				+ "after construction or loading from database. "
				+ "This is a composition pattern requirement where parent owns child.");
		return parentItem; 
	}
	
	/** Set the parent sprintable item (CActivity/CMeeting).
	 * Called by parent entity after it's loaded to enable widget display.
	 * @param parentItem the parent item */
	public void setParentItem(final ISprintableItem parentItem) { this.parentItem = parentItem; }

	@Override
	public void setColor(String color) {
		// Not used
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
	
	// IOrderedEntity implementation
	
	@Override
	public Integer getItemOrder() { return itemOrder; }
	
	@Override
	public void setItemOrder(final Integer itemOrder) { 
		this.itemOrder = itemOrder; 
	}
	
	// Kanban display support
	
	/** Get the kanban column ID for transient display purposes.
	 * @return the kanban column ID, or null if not set */
	public Long getKanbanColumnId() { return kanbanColumnId; }
	
	/** Set the kanban column ID for transient display purposes.
	 * Used to track which kanban column this item is displayed in.
	 * @param kanbanColumnId the kanban column ID */
	public void setKanbanColumnId(final Long kanbanColumnId) { 
		this.kanbanColumnId = kanbanColumnId; 
	}
	
	/** Get the parent item - alias for getParentItem() for compatibility.
	 * @return the parent sprintable item
	 * @throws IllegalStateException if parentItem is null (indicates architectural violation) */
	public ISprintableItem getItem() {
		Check.notNull(parentItem, "parentItem must be set by parent entity (CActivity/CMeeting) after loading. "
				+ "This is a transient back-reference in the composition pattern. "
				+ "Parent entity owns this CSprintItem via @OneToOne CASCADE.ALL.");
		return parentItem;
	}

	@Override
	public String toString() {
		return String.format("CSprintItem{id=%d, sprint=%s, storyPoint=%d, progress=%d%%}", 
				getId(), 
				sprint != null ? sprint.getName() : "backlog",
				storyPoint != null ? storyPoint : 0,
				progressPercentage != null ? progressPercentage : 0);
	}
}
