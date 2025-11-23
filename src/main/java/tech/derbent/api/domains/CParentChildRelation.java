package tech.derbent.api.domains;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.derbent.api.entity.domain.CEntityDB;

@Entity
@Table (name = "cparentchildrelation", uniqueConstraints = {
		@UniqueConstraint (name = "uk_parentchild", columnNames = {
				"child_id", "child_type", "parent_id", "parent_type"
		})
})
public class CParentChildRelation extends CEntityDB<CParentChildRelation> {

	public static final String DEFAULT_COLOR = "#6c757d";
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String VIEW_NAME = "Parent-Child Relation View";
	@NotNull
	@Column (name = "child_id", nullable = false)
	private Long childId;
	@NotBlank
	@Column (name = "child_type", nullable = false, length = 32)
	private String childType;
	@NotNull
	@Column (name = "parent_id", nullable = false)
	private Long parentId;
	@NotBlank
	@Column (name = "parent_type", nullable = false, length = 32)
	private String parentType;

	// --- Constructors ---
	public CParentChildRelation() {
		super(CParentChildRelation.class);
	}

	public CParentChildRelation(final Long childId, final String childType, final Long parentId, final String parentType) {
		super(CParentChildRelation.class);
		this.childId = childId;
		this.childType = childType;
		this.parentId = parentId;
		this.parentType = parentType;
	}

	// --- Getters / Setters ---
	public Long getChildId() { return childId; }

	public String getChildType() { return childType; }

	public Long getParentId() { return parentId; }

	public String getParentType() { return parentType; }

	public void setChildId(final Long childId) { this.childId = childId; }

	public void setChildType(final String childType) { this.childType = childType; }

	public void setParentId(final Long parentId) { this.parentId = parentId; }

	public void setParentType(final String parentType) { this.parentType = parentType; }

	@Override
	public String toString() {
		return "CParentChildRelation{" + "id=" + getId() + ", childId=" + childId + ", childType='" + childType + '\'' + ", parentId=" + parentId
				+ ", parentType='" + parentType + '\'' + '}';
	}
}
