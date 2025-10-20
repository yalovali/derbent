package tech.derbent.app.workflow.domain;

import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.projects.domain.CProject;

/** CWorkflowEntity - Domain entity representing workflow definitions. Layer: Domain (MVC) Inherits from CWorkflowBase to provide workflow
 * functionality for projects. This entity defines status transition workflows based on user roles in a project.
 * @author Derbent Team
 * @since 1.0 */
@Entity
@Table (name = "cworkflowentity", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cworkflowentity_id"))
public class CWorkflowEntity extends CWorkflowBase<CWorkflowEntity> {

	public static final String DEFAULT_COLOR = "#6c757d";
	public static final String VIEW_NAME = "Workflow View";
	@Column (name = "is_active", nullable = false)
	@AMetaData (
			displayName = "Is Active", required = true, readOnly = false, defaultValue = "true",
			description = "Indicates if this workflow is currently active", hidden = false, order = 3
	)
	private Boolean isActive = Boolean.TRUE;

	/** Default constructor for JPA. */
	public CWorkflowEntity() {
		super();
		isActive = Boolean.TRUE;
	}

	/** Constructor with name and project.
	 * @param name    the name of the workflow
	 * @param project the project this workflow belongs to */
	public CWorkflowEntity(final String name, final CProject project) {
		super(CWorkflowEntity.class, name, project);
		isActive = Boolean.TRUE;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CWorkflowEntity)) {
			return false;
		}
		return super.equals(o);
	}

	public Boolean getIsActive() { return isActive; }

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), isActive);
	}

	public void setIsActive(final Boolean isActive) { this.isActive = isActive; }

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}
