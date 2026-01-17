package tech.derbent.api.entityOfProject.domain;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;

/** CProjectItem - Base class for project items that can be displayed in Gantt charts. Provides hierarchical structure support and Gantt-specific
 * abstract methods for date handling, visual representation, and user assignments. All subclasses must implement the abstract Gantt methods. */
@MappedSuperclass
public abstract class CProjectItem<EntityClass> extends CEntityOfProject<EntityClass> {

	// Hierarchical Structure Support
	@Column (name = "parent_id", nullable = true)
	@AMetaData (displayName = "Parent #", required = false, readOnly = true, description = "ID of the parent entity", hidden = true)
	private Long parentId;
	@Column (name = "parent_type", nullable = true)
	@AMetaData (displayName = "Parent Type", required = false, readOnly = true, description = "Type of the parent entity", hidden = true)
	private String parentType;
	// Status and Priority Management
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cprojectitemstatus_id", nullable = true)
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current status of the activity", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getAvailableStatusesForProjectItem", setBackgroundFromColor = true, useIcon = true
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

	/** Creates a clone of this project item with the specified options. This implementation clones parent relationships and status. Subclasses must
	 * override to add their specific fields.
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the entity with cloned data
	 * @throws CloneNotSupportedException if cloning fails
	 * @throws Exception */
	@Override
	public EntityClass createClone(final CCloneOptions options) throws Exception {
		// Get parent's clone (CEntityOfProject -> CEntityNamed -> CEntityDB)
		final EntityClass clone = super.createClone(options);
		if (clone instanceof CProjectItem) {
			final CProjectItem<?> cloneItem = (CProjectItem<?>) clone;
			// Clone parent relationships if requested
			if (options.includesRelations()) {
				cloneItem.parentId = this.getParentId();
				cloneItem.parentType = this.getParentType();
			}
			// Clone status if requested
			if (options.isCloneStatus() && this.getStatus() != null) {
				cloneItem.status = this.getStatus();
			}
			// If not cloning status, leave it null (will be set by service initialization)
		}
		return clone;
	}

	/** Get the end date for Gantt chart display. Subclasses should override this to return the appropriate end date field (e.g., dueDate for
	 * activities, endDate for meetings, reviewDate for decisions). Default implementation returns null.
	 * @return the end date as LocalDate, or null if not set */
	@SuppressWarnings ("static-method")
	public LocalDate getEndDate() { return null; }

	/** Get the icon identifier for Gantt chart display. Subclasses should override this to return their specific icon (e.g., "vaadin:tasks" for
	 * activities). Default implementation returns a generic icon.
	 * @return the icon identifier */
	@SuppressWarnings ("static-method")
	public String getIconString() { return "vaadin:file"; }

	// --- Plain getters / setters ---
	public Long getParentId() { return parentId; }

	public String getParentType() { return parentType; }

	/** Get the start date for Gantt chart display. Subclasses should override this to return the appropriate start date field (e.g., startDate for
	 * activities, meetingDate for meetings, implementationDate for decisions). Default implementation returns null.
	 * @return the start date as LocalDate, or null if not set */
	@SuppressWarnings ("static-method")
	public LocalDate getStartDate() { return null; }

	public CProjectItemStatus getStatus() { return status; }

	/** Check if this item has a parent.
	 * @return true if this item has a parent assigned */
	public boolean hasParent() {
		return parentId != null && parentType != null;
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CEntityOfProject to also search in
	 * status field. For the status field, only the status name is searched.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: "id",
	 *                    "active", "name", "description", "project", "assignedTo", "createdBy", "status"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, @Nullable Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		if (fieldNames.remove("status") && getStatus() != null && getStatus().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
	}
	// ========================================================================
	// Gantt Chart Display Methods - Override in subclasses that need Gantt display
	// ========================================================================

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
		if (getId() != null && getId().equals(pid) && this.getClass().getSimpleName().equals(pType)) {
			throw new IllegalArgumentException("An item cannot be parent of itself");
		}
		parentType = pType; // Örn: "CActivity", "CMeeting"
		parentId = pid;
		updateLastModified();
	}

	public void setParentId(final Long parentId) { this.parentId = parentId; }

	public void setParentType(final String parentType) { this.parentType = parentType; }

	public void setStatus(final CProjectItemStatus status) {
		Check.notNull(status, "Status cannot be null");
		Check.notNull(getProject(), "Project must be set before applying status");
		Check.isSameCompany(getProject(), status);
		this.status = status;
		updateLastModified();
	}
}
