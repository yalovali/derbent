package tech.derbent.abstracts.domains;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.projects.domain.CProject;

/**
 * CTypeEntity - Abstract base class for all type entities in the system. Provides common
 * fields for type management including color, sort order, and active status. Layer:
 * Domain (MVC)
 * @author Derbent Team
 * @since 1.0
 */
@MappedSuperclass
public abstract class CTypeEntity<EntityType> extends CEntityOfProject<EntityType> {

	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@MetaData (
		displayName = "Color", required = false, readOnly = false,
		defaultValue = "#4A90E2",
		description = "Hex color code for type visualization (e.g., #4A90E2)",
		hidden = false, order = 3, maxLength = 7
	)
	private String color = "#4A90E2";

	@Column (name = "sort_order", nullable = false)
	@NotNull
	@MetaData (
		displayName = "Sort Order", required = true, readOnly = false,
		defaultValue = "100", description = "Display order for type sorting",
		hidden = false, order = 4
	)
	private Integer sortOrder = 100;

	@Column (name = "is_active", nullable = false)
	@NotNull
	@MetaData (
		displayName = "Is Active", required = true, readOnly = false,
		defaultValue = "true",
		description = "Indicates if this type is currently active and available",
		hidden = false, order = 6
	)
	private Boolean isActive = true;

	/**
	 * Default constructor for JPA.
	 */
	protected CTypeEntity() {
		super();
		// Initialize with default values for JPA
		this.color = "#4A90E2";
		this.sortOrder = 100;
		this.isActive = true;
	}

	/**
	 * Constructor with required fields.
	 * @param name    the name of the type entity
	 * @param project the project this type belongs to
	 */
	public CTypeEntity(final Class<EntityType> clazz, final String name,
		final CProject project) {
		super(clazz, name, project);
	}

	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		if (!super.equals(obj)) {
			return false;
		}

		if (!(obj instanceof CTypeEntity)) {
			return false;
		}
		final CTypeEntity<EntityType> other = (CTypeEntity<EntityType>) obj;
		return Objects.equals(getName(), other.getName())
			&& Objects.equals(getProject(), other.getProject());
	}

	/**
	 * Gets the color code for this type.
	 * @return the hex color code
	 */
	public String getColor() { return color; }

	/**
	 * Gets the active status of this type.
	 * @return true if active, false otherwise
	 */
	public Boolean getIsActive() { return isActive; }

	/**
	 * Gets the sort order for this type.
	 * @return the sort order
	 */
	public Integer getSortOrder() { return sortOrder; }

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getName(), getProject());
	}

	/**
	 * Convenience method to check if this type is active.
	 * @return true if active, false otherwise
	 */
	public boolean isActive() { return Boolean.TRUE.equals(isActive); }

	/**
	 * Sets the color code for this type.
	 * @param color the hex color code to set
	 */
	public void setColor(final String color) { this.color = color; }

	/**
	 * Sets the active status of this type.
	 * @param isActive the active status to set
	 */
	public void setIsActive(final Boolean isActive) { this.isActive = isActive; }

	/**
	 * Sets the sort order for this type.
	 * @param sortOrder the sort order to set
	 */
	public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }

	@Override
	public String toString() {
		return String.format(
			"%s{id=%d, name='%s', color='%s', sortOrder=%d, isActive=%s, project=%s}",
			getClass().getSimpleName(), getId(), getName(), color, sortOrder, isActive,
			getProject() != null ? getProject().getName() : "null");
	}
}