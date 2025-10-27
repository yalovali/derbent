package tech.derbent.api.domains;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;

/** CProjectItem - Base class for project items that can be displayed in Gantt charts. Provides hierarchical structure support and Gantt-specific
 * abstract methods for date handling, visual representation, and user assignments. All subclasses must implement the abstract Gantt methods. */
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
	// ========================================================================
	// Gantt Chart Display Methods - Override in subclasses that need Gantt display
	// ========================================================================

	/** Get the end date for Gantt chart display. Subclasses should override this to return the appropriate end date field (e.g., dueDate for
	 * activities, endDate for meetings, reviewDate for decisions). Default implementation returns null.
	 * @return the end date as LocalDate, or null if not set */
	public LocalDate getEndDate() { return null; }

	/** Get the icon identifier for Gantt chart display. Subclasses should override this to return their specific icon (e.g., "vaadin:tasks" for
	 * activities). Default implementation returns a generic icon.
	 * @return the icon identifier */
	public String getIcon() { return "vaadin:file"; }

	/** Get the user responsible for this item in Gantt chart display. Subclasses should override this to return the appropriate user field (e.g.,
	 * assignedTo for activities, responsible for meetings, accountableUser for decisions). Default implementation returns the assignedTo user.
	 * @return the responsible user, or null if not assigned */
	public CUser getResponsible() { return getAssignedTo(); }

	/** Get the start date for Gantt chart display. Subclasses should override this to return the appropriate start date field (e.g., startDate for
	 * activities, meetingDate for meetings, implementationDate for decisions). Default implementation returns null.
	 * @return the start date as LocalDate, or null if not set */
	public LocalDate getStartDate() { return null; }
}
