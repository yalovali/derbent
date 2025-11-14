package tech.derbent.app.risks.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"crisk\"") // Using quoted identifiers for PostgreSQL
@AttributeOverride (name = "id", column = @Column (name = "risk_id"))
public class CRisk extends CProjectItem<CRisk> implements IHasStatusAndWorkflow<CRisk> {

	public static final String DEFAULT_COLOR = "#003444";
	public static final String DEFAULT_ICON = "vaadin:warning";
	public static final String VIEW_NAME = "Risks View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Risk Type", required = false, readOnly = false, description = "Type category of the risk", hidden = false, order = 2,
			dataProviderBean = "CRiskTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CRiskType entityType;
	@Enumerated (EnumType.STRING)
	@Column (name = "risk_severity", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Risk Severity", required = true, readOnly = false, defaultValue = "LOW", description = "Severity level of the risk",
			hidden = false, order = 2, useRadioButtons = false
	)
	private ERiskSeverity riskSeverity;

	/** Default constructor for JPA. */
	public CRisk() {
		super();
		initializeDefaults();
		// Initialize with default values for JPA
	}

	public CRisk(final String name, final CProject project) {
		super(CRisk.class, name, project);
		initializeDefaults();
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public ERiskSeverity getRiskSeverity() { return riskSeverity; }

	@Override
	public CWorkflowEntity getWorkflow() { // TODO Auto-generated method stub
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	public void initializeAllFields() {
		// Parent class relationships (from CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		riskSeverity = ERiskSeverity.LOW;
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CRiskType.class, "Type entity must be an instance of CRiskType");
		entityType = (CRiskType) typeEntity;
		updateLastModified();
	}

	public void setRiskSeverity(final ERiskSeverity riskSeverity) { this.riskSeverity = riskSeverity; }
}
