package tech.derbent.risks.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.annotations.MetaData;

@Entity
@Table(name = "crisk") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "risk_id")) // Override the default column name for the ID field
public class CRisk extends CEntityDB {

    @Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Risk Name", required = true, readOnly = false, defaultValue = "-", description = "Name of the risk", hidden = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_severity", nullable = false, length = 20)
    @MetaData(displayName = "Risk Severity", required = true, readOnly = false, defaultValue = "LOW", description = "Severity of the risk", hidden = false)
    private ERiskSeverity riskSeverity;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ERiskSeverity getRiskSeverity() {
        return riskSeverity;
    }

    public void setRiskSeverity(final ERiskSeverity riskSeverity) {
        this.riskSeverity = riskSeverity;
    }
}