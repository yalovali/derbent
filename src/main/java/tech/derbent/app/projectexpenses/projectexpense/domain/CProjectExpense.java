package tech.derbent.app.projectexpenses.projectexpense.domain;

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
import tech.derbent.app.projectexpenses.projectexpensetype.domain.CProjectExpenseType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cprojectexpense\"")
@AttributeOverride (name = "id", column = @Column (name = "projectexpense_id"))
public class CProjectExpense extends CProjectItem<CProjectExpense> implements IHasStatusAndWorkflow<CProjectExpense> {

	public static final String DEFAULT_COLOR = "#EF5350";
	public static final String DEFAULT_ICON = "vaadin:money-withdraw";
	public static final String VIEW_NAME = "Project Expense View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "ProjectExpense Type", required = false, readOnly = false, description = "Type category of the projectexpense",
			hidden = false, order = 2, dataProviderBean = "CProjectExpenseTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectExpenseType entityType;

	/** Default constructor for JPA. */
	public CProjectExpense() {
		super();
		initializeDefaults();
	}

	public CProjectExpense(final String name, final CProject project) {
		super(CProjectExpense.class, name, project);
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
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CProjectExpenseType.class, "Type entity must be an instance of CProjectExpenseType");
		entityType = (CProjectExpenseType) typeEntity;
		updateLastModified();
	}
}
