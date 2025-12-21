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
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.screens.service.IOrderedEntity;
import tech.derbent.api.utils.Check;

@Entity
@Table (name = "ckanbancolumn")
@AttributeOverride (name = "id", column = @Column (name = "kanban_column_id"))
public class CKanbanColumn extends CEntityNamed<CKanbanColumn> implements IOrderedEntity {

	public static final String DEFAULT_COLOR = "#FFD54F";
	public static final String DEFAULT_ICON = "vaadin:columns";
	public static final String ENTITY_TITLE_PLURAL = "Kanban Columns";
	public static final String ENTITY_TITLE_SINGULAR = "Kanban Column";
	public static final String VIEW_NAME = "Kanban Columns View";
	@Column (name = "item_order", nullable = false)
	@AMetaData (
			displayName = "Order", required = false, readOnly = false, defaultValue = "1", description = "Sort order for kanban columns",
			hidden = false
	)
	private Integer itemOrder = 1;
	@Column (name = "default_column", nullable = false)
	@AMetaData (
			displayName = "Default Column", required = false, readOnly = false, defaultValue = "false",
			description = "When enabled, this column handles items without explicit status mapping", hidden = false
	)
	private boolean defaultColumn = false;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "kanban_line_id", nullable = false)
	@AMetaData (
			displayName = "Kanban Line", required = true, readOnly = true, description = "Parent Kanban line that owns this column", hidden = true
	)
	private CKanbanLine kanbanLine;
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

	/** Default constructor for JPA. */
	public CKanbanColumn() {
		super();
	}

	public CKanbanColumn(final String header, final CKanbanLine kanbanLine) {
		super(CKanbanColumn.class, header);
		setKanbanLine(kanbanLine);
	}

	public List<CProjectItemStatus> getIncludedStatuses() { return includedStatuses; }

	public boolean getDefaultColumn() { return defaultColumn; }

	@Override
	public Integer getItemOrder() { return itemOrder; }

	public CKanbanLine getKanbanLine() { return kanbanLine; }

	public void setIncludedStatuses(final List<CProjectItemStatus> includedStatuses) {
		Check.notNull(includedStatuses, "Included statuses cannot be null");
		this.includedStatuses = new ArrayList<>(includedStatuses);
	}

	public void setDefaultColumn(final boolean defaultColumn) { this.defaultColumn = defaultColumn; }

	@Override
	public void setItemOrder(final Integer itemOrder) { this.itemOrder = itemOrder; }

	public void setKanbanLine(final CKanbanLine kanbanLine) {
		if (kanbanLine == null) {
			this.kanbanLine = null;
			return;
		}
		this.kanbanLine = kanbanLine;
	}
}
