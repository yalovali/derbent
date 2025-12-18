package tech.derbent.app.sprints.domain;

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
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.screens.service.IOrderedEntity;

/** CSprintItem - Join entity for Sprint-ProjectItem relationships. Represents an item (Activity, Meeting, etc.) included in a sprint with ordering.
 * Similar to CDetailLines pattern for ordered one-to-many relationships. Stores only the item ID and loads the actual item dynamically at runtime. */
@Entity
@Table (name = "csprint_items")
@AttributeOverride (name = "id", column = @Column (name = "sprint_item_id"))
public class CSprintItem extends CEntityDB<CSprintItem> implements IOrderedEntity {

	public static final String DEFAULT_COLOR = "#8377C5"; // CDE Active Purple - sprint items
	public static final String DEFAULT_ICON = "vaadin:list-ol";
	public static final String ENTITY_TITLE_PLURAL = "Sprint Items";
	public static final String ENTITY_TITLE_SINGULAR = "Sprint Item";
	public static final String VIEW_NAME = "Sprint Items View";
	@AMetaData (
			displayName = "Component Widget", required = false, readOnly = false, description = "Component Widget for item", hidden = false,
			dataProviderBean = "view", dataProviderMethod = "getComponentWidget"
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
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "sprint_id", nullable = false)
	@NotNull (message = "Sprint reference is requ`ired")
	@AMetaData (
			displayName = "Sprint", required = true, readOnly = false, description = "The sprint this item belongs to", hidden = false,
			dataProviderBean = "CSprintService"
	)
	private CSprint sprint;
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
		this.itemId = item.getId();
		this.itemType = item.getClass().getSimpleName();
		this.item = item;
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

	public CComponentWidgetEntity<CSprintItem> getComponentWidget() { return componentWidget; }

	/** Get the project item. This is a transient field that must be loaded at runtime. Use CSprintItemService.loadItem() to populate this field.
	 * @return the project item, or null if not loaded */
	public ISprintableItem getItem() { return item; }

	public Long getItemId() { return itemId; }

	@Override
	public Integer getItemOrder() { return itemOrder; }

	public String getItemType() { return itemType; }

	public CSprint getSprint() { return sprint; }

	public CProjectItemStatus getStatus() {
		if (item == null) {
			return null;
		}
		return item.getStatus();
	}

	/** Set the project item. This also updates itemId and itemType.
	 * @param item the project item */
	public void setItem(final ISprintableItem item) {
		this.item = item;
		if (item != null) {
			this.itemId = item.getId();
			this.itemType = item.getClass().getSimpleName();
		}
	}

	public void setItemId(final Long itemId) { this.itemId = itemId; }

	@Override
	public void setItemOrder(final Integer itemOrder) { this.itemOrder = itemOrder; }

	public void setItemType(final String itemType) { this.itemType = itemType; }

	public void setSprint(final CSprint sprint) { this.sprint = sprint; }

	@Override
	public String toString() {
		return String.format("CSprintItem{id=%d, sprint=%s, itemId=%d, itemType=%s, order=%d}", getId(), sprint != null ? sprint.getName() : "null",
				itemId, itemType, itemOrder);
	}
}
