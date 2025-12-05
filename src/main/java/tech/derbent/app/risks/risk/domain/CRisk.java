package tech.derbent.app.risks.risk.domain;

import java.util.Arrays;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.risks.risktype.domain.CRiskType;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"crisk\"") // Using quoted identifiers for PostgreSQL
@AttributeOverride (name = "id", column = @Column (name = "risk_id"))
public class CRisk extends CProjectItem<CRisk> implements IHasStatusAndWorkflow<CRisk> {

	public static final String DEFAULT_COLOR = "#91856C"; // OpenWindows Border Dark - caution
	public static final String DEFAULT_ICON = "vaadin:warning";
	public static final String ENTITY_TITLE_PLURAL = "Risks";
	public static final String ENTITY_TITLE_SINGULAR = "Risk";
	public static final String VIEW_NAME = "Risks View";
	@Column (nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Cause", required = false, readOnly = false, description = "Root cause or source of the risk", hidden = false,
			maxLength = 1000
	)
	private String cause;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Risk Type", required = false, readOnly = false, description = "Type category of the risk", hidden = false,
			dataProviderBean = "CRiskTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CRiskType entityType;
	@Column (nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Impact", required = false, readOnly = false, description = "Potential impact if risk occurs", hidden = false,
			maxLength = 1000
	)
	private String impact;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Mitigation", required = false, readOnly = false, description = "Strategy to mitigate or reduce the risk", hidden = false,
			maxLength = 2000
	)
	private String mitigation;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Action Plan", required = false, readOnly = false, description = "Detailed action plan to address the risk", hidden = false,
			maxLength = 2000
	)
	private String plan;
	@Column (nullable = true, length = 1000)
	@Size (max = 1000)
	@AMetaData (
			displayName = "Result", required = false, readOnly = false, description = "Outcome or result of risk management", hidden = false,
			maxLength = 1000
	)
	private String result;
	@Enumerated (EnumType.STRING)
	@Column (name = "risk_criticality", nullable = true, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Criticality", required = false, readOnly = false, defaultValue = "MODERATE",
			description = "Overall importance/criticality of the risk", hidden = false, useRadioButtons = false
	)
	private ERiskCriticality riskCriticality;
	@Enumerated (EnumType.STRING)
	@Column (name = "risk_likelihood", nullable = true, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Likelihood", required = false, readOnly = false, defaultValue = "POSSIBLE", description = "Probability of risk occurring",
			hidden = false, useRadioButtons = false
	)
	private ERiskLikelihood riskLikelihood;
	@Enumerated (EnumType.STRING)
	@Column (name = "risk_severity", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Severity", required = true, readOnly = false, defaultValue = "LOW", description = "Severity level of the risk",
			hidden = false, useRadioButtons = false
	)
	private ERiskSeverity riskSeverity;

	/** Default constructor for JPA. */
	public CRisk() {
		super();
		initializeDefaults();
	}

	public CRisk(final String name, final CProject project) {
		super(CRisk.class, name, project);
		initializeDefaults();
	}

	public String getCause() { return cause; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public String getImpact() { return impact; }

	public String getMitigation() { return mitigation; }

	public String getPlan() { return plan; }

	public String getResult() { return result; }

	public ERiskCriticality getRiskCriticality() { return riskCriticality; }

	public ERiskLikelihood getRiskLikelihood() { return riskLikelihood; }

	public ERiskSeverity getRiskSeverity() { return riskSeverity; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (riskSeverity == null) {
			riskSeverity = ERiskSeverity.LOW;
		}
		if (riskLikelihood == null) {
			riskLikelihood = ERiskLikelihood.POSSIBLE;
		}
		if (riskCriticality == null) {
			riskCriticality = ERiskCriticality.MODERATE;
		}
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CProjectItem to also search in
	 * risk-specific entity fields.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: all parent
	 *                    fields plus "entityType"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, final java.util.Collection<String> fieldNames) {
		if ((searchValue == null) || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check entity field
		if (fieldNames.remove("entityType") && (getEntityType() != null) && getEntityType().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
	}

	public void setCause(final String cause) {
		this.cause = cause;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CRiskType.class, "Type entity must be an instance of CRiskType");
		entityType = (CRiskType) typeEntity;
		updateLastModified();
	}

	public void setImpact(final String impact) {
		this.impact = impact;
		updateLastModified();
	}

	public void setMitigation(final String mitigation) {
		this.mitigation = mitigation;
		updateLastModified();
	}

	public void setPlan(final String plan) {
		this.plan = plan;
		updateLastModified();
	}

	public void setResult(final String result) {
		this.result = result;
		updateLastModified();
	}

	public void setRiskCriticality(final ERiskCriticality riskCriticality) {
		this.riskCriticality = riskCriticality;
		updateLastModified();
	}

	public void setRiskLikelihood(final ERiskLikelihood riskLikelihood) {
		this.riskLikelihood = riskLikelihood;
		updateLastModified();
	}

	public void setRiskSeverity(final ERiskSeverity riskSeverity) {
		this.riskSeverity = riskSeverity;
		updateLastModified();
	}
}
