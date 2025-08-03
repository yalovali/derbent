package tech.derbent.risks.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "crisk") // table name for the entity as the default is the class name in
						// lowercase
@AttributeOverride (name = "id", column = @Column (name = "risk_id"))
public class CRisk extends CEntityOfProject<CRisk> {

	@Enumerated (EnumType.STRING)
	@Column (name = "risk_severity", nullable = false, length = 20)
	@MetaData (
		displayName = "Risk Severity", required = true, readOnly = false,
		defaultValue = "LOW", description = "Severity level of the risk", hidden = false,
		order = 2, useRadioButtons = false
	)
	private ERiskSeverity riskSeverity;

	/**
	 * Default constructor for JPA.
	 */
	public CRisk() {
		super();
		// Initialize with default values for JPA
		this.riskSeverity = ERiskSeverity.LOW;
	}

	public CRisk(final String name, final CProject project,
		final ERiskSeverity riskSeverity) {
		super(CRisk.class, name, project);
		this.riskSeverity = riskSeverity;
	}

	public ERiskSeverity getRiskSeverity() { return riskSeverity; }

	public void setRiskSeverity(final ERiskSeverity riskSeverity) {
		this.riskSeverity = riskSeverity;
	}
}