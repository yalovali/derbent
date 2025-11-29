package tech.derbent.app.workflow.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.utils.Check;
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

	public static final String DEFAULT_COLOR = "#C3B79F"; // OpenWindows 3D Shadow - process flows
	public static final String DEFAULT_ICON = "vaadin:automation";
	public static final String ENTITY_TITLE_PLURAL = "Workflows";
	public static final String ENTITY_TITLE_SINGULAR = "Workflow";
	public static final String VIEW_NAME = "Workflow View";
	@Column (name = "is_active", nullable = false)
	@AMetaData (
			displayName = "Is Active", required = true, readOnly = false, defaultValue = "true",
			description = "Indicates if this workflow is currently active", hidden = false
	)
	private Boolean isActive = Boolean.TRUE;
	// lets keep it layzily loaded to avoid loading all status relations at once
	@OneToMany (mappedBy = "workflowentity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "Status Transitions", required = false, readOnly = false, description = "Status transitions for this workflow",
			hidden = false, dataProviderBean = "CWorkflowEntityService", createComponentMethod = "createWorkflowStatusRelationsComponent",
			dataProviderParamBean = "context", dataProviderParamMethod = "getCurrentEntity"
	)
	private final List<CWorkflowStatusRelation> statusRelations = new ArrayList<>();

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

	/** Add a status relation to this workflow and maintain bidirectional relationship.
	 * @param relation the status relation to add */
	public void addStatusRelation(final CWorkflowStatusRelation relation) {
		if (relation == null) {
			return;
		}
		if (!statusRelations.contains(relation)) {
			statusRelations.add(relation);
			relation.setWorkflowEntity(this);
		}
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

	/** Gets the list of status relations for this workflow. */
	public List<CWorkflowStatusRelation> getStatusRelations() { return statusRelations; }

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), isActive);
	}

	/** Remove a status relation from this workflow and maintain bidirectional relationship.
	 * @param relation the status relation to remove */
	public void removeStatusRelation(final CWorkflowStatusRelation relation) {
		Check.notNull(relation, "Status relation cannot be null");
		if (statusRelations.remove(relation)) {
			relation.setWorkflowEntity(null);
		}
	}

	public void setIsActive(final Boolean isActive) { this.isActive = isActive; }

	@Override
	public String toString() {
		if (getName() == null) {
			return super.toString();
		}
		return getName();
	}
}
