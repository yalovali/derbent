package tech.derbent.app.projectincomes.projectincome.domain;

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
import tech.derbent.app.projectincomes.projectincometype.domain.CProjectIncomeType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cprojectincome\"")
@AttributeOverride (name = "id", column = @Column (name = "projectincome_id"))
public class CProjectIncome extends CProjectItem<CProjectIncome> implements IHasStatusAndWorkflow<CProjectIncome> {

	public static final String DEFAULT_COLOR = "#9CCC65";
	public static final String DEFAULT_ICON = "vaadin:money-deposit";
	public static final String VIEW_NAME = "Project Income View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "ProjectIncome Type", required = false, readOnly = false, description = "Type category of the projectincome",
			hidden = false,  dataProviderBean = "CProjectIncomeTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectIncomeType entityType;

	/** Default constructor for JPA. */
	public CProjectIncome() {
		super();
		initializeDefaults();
	}

	public CProjectIncome(final String name, final CProject project) {
		super(CProjectIncome.class, name, project);
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
		Check.instanceOf(typeEntity, CProjectIncomeType.class, "Type entity must be an instance of CProjectIncomeType");
		entityType = (CProjectIncomeType) typeEntity;
		updateLastModified();
	}
}
