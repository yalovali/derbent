package tech.derbent.app.kanban.kanbanline.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
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
	@OneToMany (mappedBy = "kanbanLine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderColumn (name = "column_order")
	@AMetaData (
			displayName = "Columns", required = false, readOnly = false, defaultValue = "", description = "Columns that belong to this Kanban line",
			hidden = false
	)
	private List<CKanbanColumn> kanbanColumns = new ArrayList<>();

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
		column.setKanbanLine(this);
		kanbanColumns.add(column);
		updateLastModified();
	}

	public List<CKanbanColumn> getKanbanColumns() { return kanbanColumns; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (kanbanColumns == null) {
			kanbanColumns = new ArrayList<>();
		}
	}

	public void removeKanbanColumn(final CKanbanColumn column) {
		if ((column == null) || kanbanColumns.isEmpty()) {
			return;
		}
		if (kanbanColumns.remove(column)) {
			column.setKanbanLine(null);
			updateLastModified();
		}
	}

	public void setKanbanColumns(final List<CKanbanColumn> columns) {
		Check.notNull(columns, "Columns collection cannot be null");
		this.kanbanColumns.clear();
		for (final CKanbanColumn column : columns) {
			addKanbanColumn(column);
		}
	}
}
