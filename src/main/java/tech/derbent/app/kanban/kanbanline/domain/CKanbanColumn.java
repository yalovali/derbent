package tech.derbent.app.kanban.kanbanline.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import java.util.ArrayList;
import java.util.List;
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

@Entity
@Table (name = "ckanbancolumn")
@AttributeOverride (name = "id", column = @Column (name = "kanban_column_id"))
public class CKanbanColumn extends CEntityNamed<CKanbanColumn> implements IOrderedEntity {

	public static final String DEFAULT_COLOR = "#FFD54F";
	public static final String DEFAULT_ICON = "vaadin:columns";
	public static final String ENTITY_TITLE_PLURAL = "Kanban Columns";
	public static final String ENTITY_TITLE_SINGULAR = "Kanban Column";

	@Column (name = "item_order", nullable = false)
	@AMetaData (
			displayName = "Order", required = false, readOnly = true, defaultValue = "1", description = "Sort order for kanban columns",
			hidden = false
	)
	private Integer itemOrder = 1;

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "kanban_line_id", nullable = false)
	@AMetaData (
			displayName = "Kanban Line", required = true, readOnly = true, description = "Parent Kanban line that owns this column", hidden = true
	)
	private CKanbanLine kanbanLine;

	@ManyToMany (fetch = FetchType.EAGER)
	@JoinTable (
			name = "ckanbancolumn_statuses", joinColumns = @JoinColumn (name = "kanban_column_id"),
			inverseJoinColumns = @JoinColumn (name = "cprojectitemstatus_id")
	)
	@AMetaData (
			displayName = "Statuses", required = false, readOnly = false, description = "Company statuses included in this column", hidden = false,
			setBackgroundFromColor = true, useIcon = true, dataProviderBean = "CProjectItemStatusService", useGridSelection = true
	)
	private List<CProjectItemStatus> statuses = new ArrayList<>();

	/** Default constructor for JPA. */
	public CKanbanColumn() {
		super();
	}

	public CKanbanColumn(final String header, final CKanbanLine kanbanLine) {
		super(CKanbanColumn.class, header);
		setKanbanLine(kanbanLine);
	}

	@Override
	public Integer getItemOrder() { return itemOrder; }

	public CKanbanLine getKanbanLine() { return kanbanLine; }

	public List<CProjectItemStatus> getStatuses() { return statuses; }

	@Override
	public void setItemOrder(final Integer itemOrder) { this.itemOrder = itemOrder; }

	public void setKanbanLine(final CKanbanLine kanbanLine) {
		if (kanbanLine == null) {
			this.kanbanLine = null;
			return;
		}
		this.kanbanLine = kanbanLine;
	}

	public void setStatuses(final List<CProjectItemStatus> statuses) {
		this.statuses = statuses != null ? statuses : new ArrayList<>();
	}
}
