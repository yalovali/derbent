package tech.derbent.api.domains;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;

@MappedSuperclass
public abstract class CProjectItem<EntityClass> extends CEntityOfProject<EntityClass> {

	// Hierarchical Structure Support
	@Column (name = "parent_id", nullable = true)
	@AMetaData (displayName = "Parent #", required = false, readOnly = true, description = "ID of the parent entity", hidden = true, order = 62)
	private Long parentId;
	@Column (name = "parent_type", nullable = true)
	@AMetaData (displayName = "Parent Type", required = false, readOnly = true, description = "Type of the parent entity", hidden = true, order = 61)
	private String parentType;
	// Status and Priority Management
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cprojectitemstatus_id", nullable = true)
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current status of the activity", hidden = false, order = 30,
			dataProviderBean = "CProjectItemStatusService", setBackgroundFromColor = true, useIcon = true
	)
	protected CProjectItemStatus status;
	// if you have a status, you must have a type linked to a workflow to get the status transitions
	// NOTE: This is a transient helper field. Subclasses must define their own concrete @ManyToOne field
	// and override getTypeEntity()/setTypeEntity() to use it.
	@Transient
	private CTypeEntity<EntityClass> typeEntity;

	/** Default constructor for JPA. */
	protected CProjectItem() {
		super();
	}

	public CProjectItem(final Class<EntityClass> clazz, final String name, final CProject project) {
		super(clazz, name, project);
	}

	public void clearParent() {
		parentType = null;
		parentId = null;
		updateLastModified();
	}

	// --- Plain getters / setters ---
	public Long getParentId() { return parentId; }

	public String getParentType() { return parentType; }

	public CProjectItemStatus getStatus() { return status; }

	public CTypeEntity<EntityClass> getTypeEntity() { return typeEntity; }

	public CWorkflowEntity getWorkflow() { return (typeEntity != null) ? typeEntity.getWorkflow() : null; }

	public void setParent(final CProjectItem<?> parent) {
		if (parent == null) {
			clearParent();
			return;
		}
		final Long pid = parent.getId();
		if (pid == null) {
			throw new IllegalArgumentException("Parent must be persisted (id != null)");
		}
		final String pType = parent.getClass().getSimpleName();
		// self-parent koruması: aynı tip + aynı id
		if ((getId() != null) && getId().equals(pid) && this.getClass().getSimpleName().equals(pType)) {
			throw new IllegalArgumentException("An item cannot be parent of itself");
		}
		parentType = pType; // Örn: "CActivity", "CMeeting"
		parentId = pid;
		updateLastModified();
	}

	public void setParentId(final Long parentId) { this.parentId = parentId; }

	public void setParentType(final String parentType) { this.parentType = parentType; }

	public void setStatus(final CProjectItemStatus status) {
		this.status = status;
		updateLastModified();
	}

	public void setTypeEntity(final CTypeEntity<EntityClass> typeEntity) {
		this.typeEntity = typeEntity;
		updateLastModified();
	}
}
