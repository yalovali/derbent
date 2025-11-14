package tech.derbent.api.domains;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;

/** CTypeEntity - Abstract base class for all type entities in the system. Provides common fields for type management including color, sort order, and
 * active status. Layer: Domain (MVC)
 * @author Derbent Team
 * @since 1.0 */
@MappedSuperclass
public abstract class CTypeEntity<EntityClass> extends CEntityOfProject<EntityClass> {

	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this type entity cannot be deleted by users (system configuration)", hidden = false, order = 82
	)
	private boolean attributeNonDeletable = true;
	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = "#4A90E2", colorField = true,
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
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "workflow_id", nullable = true)
	@AMetaData (
			displayName = "Workflow", required = false, readOnly = false, description = "Workflow for this type", hidden = false, order = 5,
			dataProviderBean = "CWorkflowEntityService"
	)
	private CWorkflowEntity workflow;

	/** Default constructor for JPA. */
	protected CTypeEntity() {
		super();
		// Initialize with default values for JPA
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
	}

	/** Constructor with required fields.
	 * @param name    the name of the type entity
	 * @param project the project this type belongs to */
	public CTypeEntity(final Class<EntityClass> clazz, final String name, final CProject project) {
		super(clazz, name, project);
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	/** Gets whether this type entity is non-deletable.
	 * @return true if this entity cannot be deleted */
	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	/** Gets the color code for this type.
	 * @return the hex color code */
	public String getColor() { return color; }

	/** Gets the sort order for this type.
	 * @return the sort order */
	public Integer getSortOrder() { return sortOrder; }

	/** Gets the workflow for this type.
	 * @return the workflow entity */
	public CWorkflowEntity getWorkflow() { return workflow; }

	@Override
	public int hashCode() {
		// Use the superclass hashCode method for consistency with equals method
		return super.hashCode();
	}

	/** Sets whether this type entity is non-deletable.
	 * @param attributeNonDeletable true if this entity cannot be deleted */
	public void setAttributeNonDeletable(final boolean attributeNonDeletable) {
		this.attributeNonDeletable = attributeNonDeletable;
	}

	/** Sets the color code for this type.
	 * @param color the hex color code to set */
	public void setColor(final String color) { this.color = color; }

	/** Sets the sort order for this type.
	 * @param sortOrder the sort order to set */
	public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }

	/** Sets the workflow for this type.
	 * @param workflow the workflow entity to set */
	public void setWorkflow(final CWorkflowEntity workflow) { this.workflow = workflow; }

	@Override
	public String toString() {
		return String.format("%s{id=%d, name='%s', color='%s', sortOrder=%d, nonDeletable=%s, project=%s}", getClass().getSimpleName(), getId(),
				getName(), color, sortOrder, attributeNonDeletable, getProject() != null ? getProject().getName() : "null");
	}
}
