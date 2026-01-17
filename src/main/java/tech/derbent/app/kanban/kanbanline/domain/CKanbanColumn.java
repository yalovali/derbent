package tech.derbent.app.kanban.kanbanline.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.interfaces.IHasColor;
import tech.derbent.api.screens.service.IOrderedEntity;
import tech.derbent.api.utils.Check;

@Entity
@Table (name = "ckanbancolumn")
@AttributeOverride (name = "id", column = @Column (name = "kanban_column_id"))
public class CKanbanColumn extends CEntityNamed<CKanbanColumn> implements IOrderedEntity, IHasColor {

	public static final String DEFAULT_COLOR = "#FFD54F";
	public static final String DEFAULT_ICON = "vaadin:columns";
	public static final String ENTITY_TITLE_PLURAL = "Kanban Columns";
	public static final String ENTITY_TITLE_SINGULAR = "Kanban Column";
	public static final String VIEW_NAME = "Kanban Columns View";
	@Column (nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = DEFAULT_COLOR,
			description = "Background color for this Kanban column", hidden = false, colorField = true
	)
	private String color = DEFAULT_COLOR;
	@Column (name = "default_column", nullable = false)
	@AMetaData (
			displayName = "Default Column", required = false, readOnly = false, defaultValue = "false",
			description = "When enabled, this column handles items without explicit status mapping", hidden = false
	)
	private boolean defaultColumn = false;
	@ManyToMany (fetch = FetchType.EAGER)
	@JoinTable (
			name = "ckanbancolumn_included_status", joinColumns = @JoinColumn (name = "kanban_column_id"),
			inverseJoinColumns = @JoinColumn (name = "status_id")
	)
	@AMetaData (
			displayName = "Included Statuses", required = false, readOnly = false,
			description = "Statuses covered by this kanban column (items with these statuses appear in this column)", hidden = false,
			setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CProjectItemStatusService", useGridSelection = true
	)
	private List<CProjectItemStatus> includedStatuses = new ArrayList<>();
	@Column (name = "item_order", nullable = false)
	@AMetaData (
			displayName = "Order", required = false, readOnly = false, defaultValue = "1", description = "Sort order for kanban columns",
			hidden = false
	)
	private Integer itemOrder = 1;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "kanban_line_id", nullable = false)
	@AMetaData (
			displayName = "Kanban Line", required = true, readOnly = true, description = "Parent Kanban line that owns this column", hidden = true
	)
	private CKanbanLine kanbanLine;
	// Kanban Method (David J. Anderson, 2010) - WIP Limits & Flow Management
	@Column (name = "wip_limit", nullable = true)
	@AMetaData (
			displayName = "WIP Limit", required = false, readOnly = false,
			description = "Work In Progress limit - maximum items allowed in this column (Kanban Method)", hidden = false
	)
	private Integer wipLimit;
	@Column (name = "wip_limit_enabled", nullable = false)
	@AMetaData (
			displayName = "Enforce WIP Limit", required = false, readOnly = false, defaultValue = "false",
			description = "Block adding items when WIP limit is reached (Kanban Method explicit policy)", hidden = false
	)
	private Boolean wipLimitEnabled = false;
	@jakarta.persistence.Enumerated (jakarta.persistence.EnumType.STRING)
	@Column (name = "service_class", nullable = true, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Class of Service", required = false, readOnly = false,
			description = "Priority policy for items in this column - Kanban Method (Expedite, Fixed Date, Standard, Intangible)", hidden = false,
			useRadioButtons = false
	)
	private EServiceClass serviceClass;

	/** Default constructor for JPA. */
	public CKanbanColumn() {
		super();
		color = DEFAULT_COLOR;
	}

	/** Creates a column with a header and parent line. */
	public CKanbanColumn(final String header, final CKanbanLine kanbanLine) {
		super(CKanbanColumn.class, header);
		color = DEFAULT_COLOR;
		setKanbanLine(kanbanLine);
	}

	/** Returns the column background color. */
	@Override
	public String getColor() { return color; }

	/** Get current Work In Progress count for this column (Kanban Method metric). This should be implemented by counting items with statuses in this
	 * column.
	 * @return Current number of items in this column, or null if not calculated */
	@jakarta.persistence.Transient
	public Integer getCurrentWIP() {
		// This would typically be calculated by the service layer
		// by querying items that have statuses in this column's includedStatuses list
		return null; // Placeholder - implement in CKanbanColumnService
	}

	/** Returns true when this column is the fallback/default bucket. */
	public boolean getDefaultColumn() { return defaultColumn; }

	/** Returns the statuses mapped to this column. */
	public List<CProjectItemStatus> getIncludedStatuses() { return includedStatuses; }

	/** Returns the sort order for this column. */
	@Override
	public Integer getItemOrder() { return itemOrder; }

	/** Returns the owning kanban line. */
	public CKanbanLine getKanbanLine() { return kanbanLine; }

	public EServiceClass getServiceClass() { return serviceClass; }

	/** Get WIP limit status display string.
	 * @return Display string like "3/5" (current/limit) or "5" (no limit) */
	@jakarta.persistence.Transient
	public String getWIPDisplay() {
		final Integer current = getCurrentWIP();
		if (wipLimit != null && getWipLimitEnabled()) {
			return (current != null ? current : 0) + "/" + wipLimit;
		}
		return current != null ? current.toString() : "0";
	}

	public Integer getWipLimit() { return wipLimit; }

	public Boolean getWipLimitEnabled() { return wipLimitEnabled != null ? wipLimitEnabled : false; }
	// Kanban Method (David J. Anderson) - Getters/Setters

	/** Check if WIP limit is exceeded (Kanban Method explicit limit policy).
	 * @return true if WIP limit is enabled and current WIP meets or exceeds the limit */
	@jakarta.persistence.Transient
	public boolean isWIPLimitExceeded() {
		if (!getWipLimitEnabled() || wipLimit == null) {
			return false;
		}
		final Integer currentWIP = getCurrentWIP();
		return currentWIP != null && currentWIP >= wipLimit;
	}

	/** Sets the background color, defaulting when blank. */
	@Override
	public void setColor(final String color) { this.color = color == null || color.isBlank() ? DEFAULT_COLOR : color; }

	/** Sets whether this column is the fallback/default bucket. */
	public void setDefaultColumn(final boolean defaultColumn) {
		this.defaultColumn = defaultColumn;
	}

	/** Replaces the included status list defensively. */
	public void setIncludedStatuses(final List<CProjectItemStatus> includedStatuses) {
		Check.notNull(includedStatuses, "Included statuses cannot be null");
		this.includedStatuses = new ArrayList<>(includedStatuses);
	}

	/** Sets the sort order for this column. */
	@Override
	public void setItemOrder(final Integer itemOrder) { this.itemOrder = itemOrder; }

	/** Sets the owning kanban line. */
	public void setKanbanLine(final CKanbanLine kanbanLine) {
		if (kanbanLine == null) {
			this.kanbanLine = null;
			return;
		}
		this.kanbanLine = kanbanLine;
	}

	public void setServiceClass(final EServiceClass serviceClass) { this.serviceClass = serviceClass; }

	public void setWipLimit(final Integer wipLimit) { this.wipLimit = wipLimit; }

	public void setWipLimitEnabled(final Boolean wipLimitEnabled) { this.wipLimitEnabled = wipLimitEnabled; }
}
