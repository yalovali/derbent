package tech.derbent.app.budgets.budget.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.budgets.budgettype.domain.CBudgetType;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cbudget\"")
@AttributeOverride (name = "id", column = @Column (name = "budget_id"))
public class CBudget extends CProjectItem<CBudget> implements IHasStatusAndWorkflow<CBudget> {

	public static final String DEFAULT_COLOR = "#66BB6A";
	public static final String DEFAULT_ICON = "vaadin:dollar";
	public static final String VIEW_NAME = "Budget View";

	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Budget Type", required = false, readOnly = false, 
			description = "Type category of the budget", hidden = false, order = 2,
			dataProviderBean = "CBudgetTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CBudgetType entityType;

	/** Default constructor for JPA. */
	public CBudget() {
		super();
		initializeDefaults();
	}

	public CBudget(final String name, final CProject project) {
		super(CBudget.class, name, project);
		initializeDefaults();
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CBudgetType.class, "Type entity must be an instance of CBudgetType");
		entityType = (CBudgetType) typeEntity;
		updateLastModified();
	}

	@Override
	public void initializeAllFields() {
		if (getProject() != null) {
			getProject().getName();
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin();
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin();
		}
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}
}
