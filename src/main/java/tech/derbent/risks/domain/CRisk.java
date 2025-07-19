package tech.derbent.risks.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;

@Entity
@Table(name = "crisk") // table name for the entity as the default is the class name in lowercase
public class CRisk extends CEntityDB {

	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
	@Size(max = MAX_LENGTH_NAME)
	@MetaData(displayName = "Risk Name", required = true, readOnly = false, defaultValue = "-", description = "Name of the risk", hidden = false)
	private String name;
	@Enumerated(EnumType.STRING)
	@Column(name = "risk_severity", nullable = false, length = 20)
	@MetaData(displayName = "Risk Severity", required = true, readOnly = false, defaultValue = "LOW", description = "Severity of the risk", hidden = false)
	private ERiskSeverity riskSeverity;
	// Many risks belong to one project
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private CProject project;

	public CRisk() {
		// Default constructor - project will be set later
	}

	public CRisk(final String name, final ERiskSeverity riskSeverity, final CProject project) {
		this.name = name;
		this.riskSeverity = riskSeverity;
		this.project = project;
	}

	public String getName() { return name; }

	public CProject getProject() { return project; }

	public ERiskSeverity getRiskSeverity() { return riskSeverity; }

	public void setName(final String name) { this.name = name; }

	public void setProject(final CProject project) { this.project = project; }

	public void setRiskSeverity(final ERiskSeverity riskSeverity) { this.riskSeverity = riskSeverity; }
}