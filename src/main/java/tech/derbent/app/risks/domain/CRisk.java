package tech.derbent.app.risks.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "\"crisk\"") // Using quoted identifiers for PostgreSQL
@AttributeOverride (name = "id", column = @Column (name = "risk_id"))
public class CRisk extends CProjectItem<CRisk> {

	public static final String DEFAULT_COLOR = "#003444";
	public static final String DEFAULT_ICON = "vaadin:cart";
	public static final String VIEW_NAME = "Risks View";
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
		// Initialize with default values for JPA
		riskSeverity = ERiskSeverity.LOW;
	}

	public CRisk(final String name, final CProject project) {
		super(CRisk.class, name, project);
	}

	public ERiskSeverity getRiskSeverity() { return riskSeverity; }

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

	public void setRiskSeverity(final ERiskSeverity riskSeverity) { this.riskSeverity = riskSeverity; }
}
