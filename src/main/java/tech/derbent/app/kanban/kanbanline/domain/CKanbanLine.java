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
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.utils.Check;

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

	/** Creates a kanban line for a company with a display name. */
	public CKanbanLine(final String name, final CCompany company) {
		super(CKanbanLine.class, name, company);
		initializeDefaults();
	}

	/** Adds a column and assigns order/ownership. */
	public void addKanbanColumn(final CKanbanColumn column) {
		Check.notNull(column, "Column cannot be null");
		if (column.getItemOrder() == null || column.getItemOrder() <= 0) {
			column.setItemOrder(getNextKanbanColumnOrder());
		}
		column.setKanbanLine(this);
		kanbanColumns.add(column);
		updateLastModified();
	}

	@Override
	public CKanbanLine createClone(final CCloneOptions options) throws Exception {
		final CKanbanLine clone = super.createClone(options);
		if (options.isFullDeepClone() && kanbanColumns != null && !kanbanColumns.isEmpty()) {
			clone.kanbanColumns = new LinkedHashSet<>();
			for (final CKanbanColumn column : kanbanColumns) {
				try {
					final CKanbanColumn columnClone = column.createClone(options);
					columnClone.setKanbanLine(clone);
					clone.kanbanColumns.add(columnClone);
				} catch (final CloneNotSupportedException e) {
					throw new CloneNotSupportedException("Failed to clone kanban column: " + e.getMessage());
				}
			}
		}
		return clone;
	}

	/** Returns a self-reference for the board component binding. */
	public CKanbanLine getKanbanBoard() { return this; }

	/** Returns the column set for this line. */
	public Set<CKanbanColumn> getKanbanColumns() { return kanbanColumns; }

	/** Calculates the next column sort order. */
	private Integer getNextKanbanColumnOrder() {
		if (kanbanColumns == null || kanbanColumns.isEmpty()) {
			return 1;
		}
		return kanbanColumns.stream().map(CKanbanColumn::getItemOrder).filter(order -> order != null).mapToInt(Integer::intValue).max().orElse(0) + 1;
	}

	/** Ensures internal collections are initialized. */
	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (kanbanColumns == null) {
			kanbanColumns = new LinkedHashSet<>();
		}
	}

	/** Removes a column and clears its ownership. */
	public void removeKanbanColumn(final CKanbanColumn column) {
		Check.notNull(column, "Column cannot be null");
		if (kanbanColumns.remove(column)) {
			column.setKanbanLine(null);
			updateLastModified();
		}
	}

	/** Replaces the columns set while preserving ownership rules. */
	public void setKanbanColumns(final Set<CKanbanColumn> columns) {
		Check.notNull(columns, "Columns collection cannot be null");
		kanbanColumns.clear();
		for (final CKanbanColumn column : columns) {
			addKanbanColumn(column);
		}
	}
}
