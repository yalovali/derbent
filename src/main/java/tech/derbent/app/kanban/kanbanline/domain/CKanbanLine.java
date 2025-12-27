package tech.derbent.app.kanban.kanbanline.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;

@Entity
@Table (name = "ckanbanline")
@AttributeOverride (name = "id", column = @Column (name = "kanban_line_id"))
public class CKanbanLine extends CEntityOfCompany<CKanbanLine> {
	public static final String DEFAULT_COLOR = "#4DB6AC"; // Bold teal for Kanban headers
	public static final String DEFAULT_ICON = "vaadin:barcode";
	public static final String ENTITY_TITLE_PLURAL = "Kanban Lines";
	public static final String ENTITY_TITLE_SINGULAR = "Kanban Line";
	public static final String VIEW_NAME = "Kanban Lines View";
	@Transient
	@AMetaData (
			displayName = "Kanban Board", required = true, readOnly = false, description = "Kanban Board", hidden = false,
			createComponentMethod = "createKanbanBoardComponent", dataProviderBean = "pageservice", captionVisible = false
	)
	private final CKanbanLine kanbanBoard = null;
	@OneToMany (mappedBy = "kanbanLine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy ("itemOrder ASC")
	@AMetaData (
			displayName = "Columns", required = false, readOnly = false, defaultValue = "", description = "Columns that belong to this Kanban line",
			hidden = false, createComponentMethod = "createKanbanColumnsComponent", dataProviderBean = "pageservice", captionVisible = false
	)
	private Set<CKanbanColumn> kanbanColumns = new LinkedHashSet<>();

	/** Default constructor for JPA */
	public CKanbanLine() {
		super();
		initializeDefaults();
	}

	public CKanbanLine(final String name, final CCompany company) {
		super(CKanbanLine.class, name, company);
		initializeDefaults();
	}

	public void addKanbanColumn(final CKanbanColumn column) {
		Check.notNull(column, "Column cannot be null");
		if ((column.getItemOrder() == null) || (column.getItemOrder() <= 0)) {
			column.setItemOrder(getNextKanbanColumnOrder());
		}
		column.setKanbanLine(this);
		kanbanColumns.add(column);
		updateLastModified();
	}

	public CKanbanLine getKanbanBoard() { return this; }

	public Set<CKanbanColumn> getKanbanColumns() { return kanbanColumns; }

	private Integer getNextKanbanColumnOrder() {
		if ((kanbanColumns == null) || kanbanColumns.isEmpty()) {
			return 1;
		}
		return kanbanColumns.stream().map(CKanbanColumn::getItemOrder).filter(order -> order != null).mapToInt(Integer::intValue).max().orElse(0) + 1;
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (kanbanColumns == null) {
			kanbanColumns = new LinkedHashSet<>();
		}
	}

	public void removeKanbanColumn(final CKanbanColumn column) {
		Check.notNull(column, "Column cannot be null");
		if (kanbanColumns.remove(column)) {
			column.setKanbanLine(null);
			updateLastModified();
		}
	}

	public void setKanbanColumns(final Set<CKanbanColumn> columns) {
		Check.notNull(columns, "Columns collection cannot be null");
		kanbanColumns.clear();
		for (final CKanbanColumn column : columns) {
			addKanbanColumn(column);
		}
	}
}
