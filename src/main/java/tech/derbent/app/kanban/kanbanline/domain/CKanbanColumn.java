package tech.derbent.app.kanban.kanbanline.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;

@Entity
@Table (name = "ckanbancolumn")
@AttributeOverride (name = "id", column = @Column (name = "kanban_column_id"))
public class CKanbanColumn extends CEntityNamed<CKanbanColumn> {

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "kanban_line_id", nullable = false)
	@AMetaData (
			displayName = "Kanban Line", required = true, readOnly = true, description = "Parent Kanban line that owns this column", hidden = true
	)
	private CKanbanLine kanbanLine;

	/** Default constructor for JPA. */
	public CKanbanColumn() {
		super();
	}

	public CKanbanColumn(final String header, final CKanbanLine kanbanLine) {
		super(CKanbanColumn.class, header);
		setKanbanLine(kanbanLine);
	}

	public CKanbanLine getKanbanLine() { return kanbanLine; }

	public void setKanbanLine(final CKanbanLine kanbanLine) {
		if (kanbanLine == null) {
			this.kanbanLine = null;
			return;
		}
		this.kanbanLine = kanbanLine;
	}
}
