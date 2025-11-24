package tech.derbent.app.sprints.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.domain.CProjectItem;

/**
 * CSprintItem - Join entity for Sprint-ProjectItem relationships.
 * Represents an item (Activity, Meeting, etc.) included in a sprint with ordering.
 * Similar to CDetailLines pattern for ordered one-to-many relationships.
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
			description = "The sprint this item belongs to", hidden = false, order = 1
	)
	private CSprint sprint;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false)
	@NotNull(message = "Project item is required")
	@AMetaData(
			displayName = "Item", required = true, readOnly = false,
			description = "The project item (activity, meeting, etc.)", hidden = false, order = 2,
			dataProviderBean = "CSprintService", dataProviderMethod = "getAllProjectItemsForCurrentProject"
	)
	private CProjectItem<?> item;

	@Column(name = "item_order", nullable = false)
	@AMetaData(
			displayName = "Order", required = true, readOnly = false,
			description = "Display order of this item in the sprint", hidden = false, order = 3,
			defaultValue = "0"
	)
	private Integer itemOrder = 0;

	@Column(name = "item_type", nullable = false, length = 50)
	@AMetaData(
			displayName = "Item Type", required = true, readOnly = true,
			description = "Type of the project item (Activity, Meeting, etc.)", hidden = false, order = 4
	)
	private String itemType;

	/** Default constructor for JPA. */
	public CSprintItem() {
		super();
	}

	/** Constructor with sprint and item.
	 * @param sprint the sprint
	 * @param item the project item */
	public CSprintItem(final CSprint sprint, final CProjectItem<?> item) {
		super();
		this.sprint = sprint;
		this.item = item;
		if (item != null) {
			this.itemType = item.getClass().getSimpleName();
		}
	}

	/** Constructor with sprint, item, and order.
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

	public CProjectItem<?> getItem() {
		return item;
	}

	public void setItem(final CProjectItem<?> item) {
		this.item = item;
		if (item != null) {
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
		return String.format("CSprintItem{id=%d, sprint=%s, item=%s, order=%d, type=%s}",
				getId(),
				sprint != null ? sprint.getName() : "null",
				item != null ? item.getName() : "null",
				itemOrder,
				itemType);
	}
}
