package tech.derbent.api.workflow.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.utils.Check;

/** CWorkflowEntity - Domain entity representing workflow definitions. Layer: Domain (MVC) Inherits from CWorkflowBase to provide workflow
 * functionality for companies. This entity defines status transition workflows based on user roles in a company.
 * @author Derbent Team
 * @since 1.0 */
@Entity
@Table (name = "cworkflowentity", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cworkflowentity_id"))
@AssociationOverride (name = "company", joinColumns = @JoinColumn (name = "company_id", nullable = false))
public final class CWorkflowEntity extends CWorkflowBase<CWorkflowEntity> {

	public static final String DEFAULT_COLOR = "#7A6E58"; // OpenWindows Border Darker - process flows (darker)
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
	@OneToMany (mappedBy = "workflowEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "Status Transitions", required = false, readOnly = false, description = "Status transitions for this workflow",
			hidden = false, dataProviderBean = "CWorkflowEntityService", createComponentMethod = "createWorkflowStatusRelationsComponent",
			dataProviderParamBean = "context", dataProviderParamMethod = "getValue"
	)
	private final List<CWorkflowStatusRelation> statusRelations = new ArrayList<>();

	/** Default constructor for JPA. */
	protected CWorkflowEntity() {
	}

	/** Constructor with name and company.
	 * @param name    the name of the workflow
	 * @param company the company this workflow belongs to */
	public CWorkflowEntity(final String name, final CCompany company) {
		super(CWorkflowEntity.class, name, company);
		initializeDefaults();
	}

	/** Add a status relation to this workflow and maintain bidirectional relationship.
	 * @param relation the status relation to add */
	public void addStatusRelation(final CWorkflowStatusRelation relation) {
		if (relation == null) {
			return;
		}
		if (statusRelations.contains(relation)) {
			return;
		}
		statusRelations.add(relation);
		relation.setWorkflowEntity(this);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		return !(o instanceof CWorkflowEntity) ? false : super.equals(o);
	}

	public Boolean getIsActive() { return isActive; }

	/** Gets the list of status relations for this workflow. */
	public List<CWorkflowStatusRelation> getStatusRelations() { return statusRelations; }

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), isActive);
	}

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
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
		return getName() == null ? super.toString() : getName();
	}
}
