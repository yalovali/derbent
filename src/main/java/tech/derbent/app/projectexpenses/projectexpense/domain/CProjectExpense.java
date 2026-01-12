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
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cprojectexpense\"")
@AttributeOverride (name = "id", column = @Column (name = "projectexpense_id"))
public class CProjectExpense extends CProjectItem<CProjectExpense> implements IHasStatusAndWorkflow<CProjectExpense> {

	public static final String DEFAULT_COLOR = "#A0522D"; // X11 Sienna - outgoing money (darker)
	public static final String DEFAULT_ICON = "vaadin:money-withdraw";
	public static final String ENTITY_TITLE_PLURAL = "Project Expenses";
	public static final String ENTITY_TITLE_SINGULAR = "Project Expense";
	public static final String VIEW_NAME = "Project Expense View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "ProjectExpense Type", required = false, readOnly = false, description = "Type category of the projectexpense",
			hidden = false,  dataProviderBean = "CProjectExpenseTypeService", setBackgroundFromColor = true, useIcon = true
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
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CProjectExpenseType.class, "Type entity must be an instance of CProjectExpenseType");
		Check.notNull(getProject(), "Project must be set before assigning project expense type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning project expense type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning project expense type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match project expense project company id "
						+ getProject().getCompany().getId());
		entityType = (CProjectExpenseType) typeEntity;
		updateLastModified();
	}
}
