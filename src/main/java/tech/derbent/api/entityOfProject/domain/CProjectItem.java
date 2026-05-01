package tech.derbent.api.entityOfProject.domain;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;

/** CProjectItem - Base class for project items. Provides hierarchical structure support (via IHasParentRelation) and Gantt-specific abstract methods
 * for date handling, visual representation, and user assignments. All subclasses must implement the abstract Gantt methods. */
@MappedSuperclass
public abstract class CProjectItem<EntityClass, TypeClass extends CTypeEntity<TypeClass>>
		extends CEntityOfProject<EntityClass> {

	// Status and Priority Management
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cprojectitemstatus_id", nullable = true)
	@AMetaData (
			displayName = "Status", required = false, readOnly = false, description = "Current status of the activity", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfStatusForProjectItem", setBackgroundFromColor = true,
			useIcon = true
	)
	protected CProjectItemStatus status;

	/** Default constructor for JPA. */
	protected CProjectItem() {}

	public CProjectItem(final Class<EntityClass> clazz, final String name, final CProject<?> project) {
		super(clazz, name, project);
	}

	public abstract TypeClass getEntityType();

	public abstract void setEntityType(TypeClass entityType);

	/** Get the end date for Gantt chart display.
	 * @return the end date as LocalDate, or null if not set */
	public LocalDate getEndDate() { return null; }

	/** Get the icon identifier for Gantt chart display.
	 * @return the icon identifier */
	public String getIconString() { return "vaadin:file"; }

	/** Get the start date for Gantt chart display.
	 * @return the start date as LocalDate, or null if not set */
	public LocalDate getStartDate() { return null; }

	public CProjectItemStatus getStatus() { return status; }

	/** Checks if this entity matches the given search value in the specified fields.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, @Nullable Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true;
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

	public void setStatus(final CProjectItemStatus status) {
		Check.notNull(status, "Status cannot be null");
		Check.notNull(getProject(), "Project must be set before applying status");
		Check.isSameCompany(getProject(), status);
		this.status = status;
		updateLastModified();
	}
}
