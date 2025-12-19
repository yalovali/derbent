package tech.derbent.app.kanban.kanbanline.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;

@Entity
@Table (name = "ckanbancolumn")
@AttributeOverride (name = "id", column = @Column (name = "kanban_column_id"))
public class CKanbanColumn extends CEntityDB<CKanbanColumn> {
	@Column (name = "header", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@NotBlank (message = ValidationMessages.NAME_REQUIRED)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME, message = ValidationMessages.NAME_MAX_LENGTH)
	@AMetaData (
			displayName = "Column Header", required = true, readOnly = false, defaultValue = "", description = "Shown at the top of the Kanban column",
			hidden = false, maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String header;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "kanban_line_id", nullable = false)
	@AMetaData (
			displayName = "Kanban Line", required = true, readOnly = true, description = "Parent Kanban line that owns this column",
			hidden = true
	)
	private CKanbanLine kanbanLine;

	/** Default constructor for JPA. */
	public CKanbanColumn() {
		super();
	}

	public CKanbanColumn(final String header, final CKanbanLine kanbanLine) {
		super(CKanbanColumn.class);
		setHeader(header);
		setKanbanLine(kanbanLine);
	}

	public String getHeader() { return header; }

	public CKanbanLine getKanbanLine() { return kanbanLine; }

	public void setHeader(final String header) {
		Check.notBlank(header, "Column header cannot be null or empty");
		this.header = header.trim();
	}

	public void setKanbanLine(final CKanbanLine kanbanLine) {
		if (kanbanLine == null) {
			this.kanbanLine = null;
			return;
		}
		this.kanbanLine = kanbanLine;
	}

	@Override
	public String toString() {
		return "CKanbanColumn{id=%s, header='%s'}".formatted(getId(), header);
	}
}
