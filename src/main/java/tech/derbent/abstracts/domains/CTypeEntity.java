package tech.derbent.abstracts.domains;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.projects.domain.CProject;

/** CTypeEntity - Abstract base class for all type entities in the system. Provides common fields for type management including color, sort order, and
 * active status. Layer: Domain (MVC)
 * @author Derbent Team
 * @since 1.0 */
@MappedSuperclass
public abstract class CTypeEntity<EntityType> extends CEntityOfProject<EntityType> {

	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = "#4A90E2",
			description = "Hex color code for type visualization (e.g., #4A90E2)", hidden = false, order = 3, maxLength = 7
	)
	private String color = "#4A90E2";
	@Column (name = "sort_order", nullable = false)
	@NotNull
	@AMetaData (
			displayName = "Sort Order", required = true, readOnly = false, defaultValue = "100", description = "Display order for type sorting",
			hidden = false, order = 4
	)
	private Integer sortOrder = 100;

	/** Default constructor for JPA. */
	protected CTypeEntity() {
		super();
		// Initialize with default values for JPA
		this.color = "#4A90E2";
		this.sortOrder = 100;
	}

	/** Constructor with required fields.
	 * @param name    the name of the type entity
	 * @param project the project this type belongs to */
	public CTypeEntity(final Class<EntityType> clazz, final String name, final CProject project) {
		super(clazz, name, project);
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	/** Gets the color code for this type.
	 * @return the hex color code */
	public String getColor() { return color; }

	/** Gets the sort order for this type.
	 * @return the sort order */
	public Integer getSortOrder() { return sortOrder; }

	@Override
	public int hashCode() {
		// Use the superclass hashCode method for consistency with equals method
		return super.hashCode();
	}

	/** Sets the color code for this type.
	 * @param color the hex color code to set */
	public void setColor(final String color) { this.color = color; }

	/** Sets the sort order for this type.
	 * @param sortOrder the sort order to set */
	public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }

	@Override
	public String toString() {
		return String.format("%s{id=%d, name='%s', color='%s', sortOrder=%d, project=%s}", getClass().getSimpleName(), getId(), getName(), color,
				sortOrder, getProject() != null ? getProject().getName() : "null");
	}
}
