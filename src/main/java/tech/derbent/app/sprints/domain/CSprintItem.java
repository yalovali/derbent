package tech.derbent.app.sprints.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.domain.CProjectItem;

/**
 * CSprintItem - Join entity for Sprint-ProjectItem relationships.
 * Represents an item (Activity, Meeting, etc.) included in a sprint with ordering.
 * Similar to CDetailLines pattern for ordered one-to-many relationships.
 * Stores only the item ID and loads the actual item dynamically at runtime.
 */
@Entity
@Table(name = "csprint_items")
@AttributeOverride(name = "id", column = @Column(name = "sprint_item_id"))
public class CSprintItem extends CEntityDB<CSprintItem> {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:list-ol";
	public static final String VIEW_NAME = "Sprint Items View";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sprint_id", nullable = false)
	@NotNull(message = "Sprint reference is required")
	@AMetaData(
			displayName = "Sprint", required = true, readOnly = false,
			description = "The sprint this item belongs to", hidden = false, order = 1,
			dataProviderBean = "CSprintService"
	)
	private CSprint sprint;

	// Store only the ID of the project item - loaded dynamically at runtime
	@Column(name = "item_id", nullable = false)
	@NotNull(message = "Project item ID is required")
	@AMetaData(
			displayName = "Item ID", required = true, readOnly = false,
			description = "ID of the project item (activity, meeting, etc.)", hidden = false, order = 2
	)
	private Long itemId;

	@Column(name = "item_order", nullable = false)
	@NotNull(message = "Item order is required")
	@AMetaData(
			displayName = "Order", required = true, readOnly = false,
			description = "Display order of this item in the sprint", hidden = false, order = 3,
			defaultValue = "0"
	)
	private Integer itemOrder = 0;

	@Column(name = "item_type", nullable = false, length = 50)
	@NotNull(message = "Item type is required")
	@AMetaData(
			displayName = "Item Type", required = true, readOnly = false,
			description = "Type of the project item (CActivity, CMeeting, etc.)", hidden = false, order = 4,
			maxLength = 50
	)
	private String itemType;

	// Transient field - loaded dynamically at runtime from itemId and itemType
	@Transient
	private CProjectItem<?> item;

	/** Default constructor for JPA. */
	public CSprintItem() {
		super();
	}

	/** Constructor with sprint, item ID, and item type.
	 * @param sprint the sprint
	 * @param itemId the project item ID
	 * @param itemType the project item type */
	public CSprintItem(final CSprint sprint, final Long itemId, final String itemType) {
		super();
		this.sprint = sprint;
		this.itemId = itemId;
		this.itemType = itemType;
	}

	/** Constructor with sprint, item ID, item type, and order.
	 * @param sprint the sprint
	 * @param itemId the project item ID
	 * @param itemType the project item type
	 * @param itemOrder the display order */
	public CSprintItem(final CSprint sprint, final Long itemId, final String itemType, final Integer itemOrder) {
		this(sprint, itemId, itemType);
		this.itemOrder = itemOrder;
	}

	/** Constructor with sprint and item (for backward compatibility).
	 * @param sprint the sprint
	 * @param item the project item */
	public CSprintItem(final CSprint sprint, final CProjectItem<?> item) {
		super();
		this.sprint = sprint;
		if (item != null) {
			this.itemId = item.getId();
			this.itemType = item.getClass().getSimpleName();
			this.item = item;
		}
	}

	/** Constructor with sprint, item, and order (for backward compatibility).
	 * @param sprint the sprint
	 * @param item the project item
	 * @param itemOrder the display order */
	public CSprintItem(final CSprint sprint, final CProjectItem<?> item, final Integer itemOrder) {
		this(sprint, item);
		this.itemOrder = itemOrder;
	}

	public CSprint getSprint() {
		return sprint;
	}

	public void setSprint(final CSprint sprint) {
		this.sprint = sprint;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(final Long itemId) {
		this.itemId = itemId;
	}

	/**
	 * Get the project item. This is a transient field that must be loaded at runtime.
	 * Use CSprintItemService.loadItem() to populate this field.
	 * @return the project item, or null if not loaded
	 */
	public CProjectItem<?> getItem() {
		return item;
	}

	/**
	 * Set the project item. This also updates itemId and itemType.
	 * @param item the project item
	 */
	public void setItem(final CProjectItem<?> item) {
		this.item = item;
		if (item != null) {
			this.itemId = item.getId();
			this.itemType = item.getClass().getSimpleName();
		}
	}

	public Integer getItemOrder() {
		return itemOrder;
	}

	public void setItemOrder(final Integer itemOrder) {
		this.itemOrder = itemOrder;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(final String itemType) {
		this.itemType = itemType;
	}

	@Override
	public String toString() {
		return String.format("CSprintItem{id=%d, sprint=%s, itemId=%d, itemType=%s, order=%d}",
				getId(),
				sprint != null ? sprint.getName() : "null",
				itemId,
				itemType,
				itemOrder);
	}
}
