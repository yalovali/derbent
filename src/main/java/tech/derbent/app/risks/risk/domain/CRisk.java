package tech.derbent.app.risks.risk.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.risks.risktype.domain.CRiskType;

@Entity
@Table (name = "\"crisk\"") // Using quoted identifiers for PostgreSQL
@AttributeOverride (name = "id", column = @Column (name = "risk_id"))
public class CRisk extends CProjectItem<CRisk> implements IHasStatusAndWorkflow<CRisk>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#91856C"; // OpenWindows Border Dark - caution
	public static final String DEFAULT_ICON = "vaadin:warning";
	public static final String ENTITY_TITLE_PLURAL = "Risks";
	public static final String ENTITY_TITLE_SINGULAR = "Risk";
	private static final Logger LOGGER = LoggerFactory.getLogger(CRisk.class);
	public static final String VIEW_NAME = "Risks View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "risk_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this risk", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "risk_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Discussion comments for this risk", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
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
			displayName = "Mitigation Strategy", required = false, readOnly = false,
			description = "Strategy to mitigate or reduce the risk - ISO 31000", hidden = false, maxLength = 2000
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
	// ISO 31000:2018 Risk Management - Quantitative Risk Assessment
	@Column (nullable = true)
	@AMetaData (
			displayName = "Probability (1-10)", required = false, readOnly = false,
			description = "Quantitative likelihood of risk occurrence (1=Very Low, 10=Very High) - ISO 31000", hidden = false
	)
	private Integer probability;
	@Column (nullable = true)
	@AMetaData (
			displayName = "Impact Score (1-10)", required = false, readOnly = false,
			description = "Quantitative magnitude of consequences if risk occurs (1=Minimal, 10=Catastrophic) - ISO 31000", hidden = false
	)
	private Integer impactScore;
	// ISO 31000:2018 - Risk Treatment Strategy
	@Enumerated (EnumType.STRING)
	@Column (name = "risk_response_strategy", nullable = true, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Response Strategy", required = false, readOnly = false,
			description = "Risk treatment approach per ISO 31000 (Avoid, Transfer, Mitigate, Accept, Escalate)", hidden = false,
			useRadioButtons = false
	)
	private ERiskResponseStrategy riskResponseStrategy;
	// ISO 31000:2018 - Residual Risk Assessment
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Residual Risk", required = false, readOnly = false,
			description = "Remaining risk after mitigation measures are applied - ISO 31000", hidden = false, maxLength = 2000
	)
	private String residualRisk;

	/** Default constructor for JPA. */
	public CRisk() {
		super();
		initializeDefaults();
	}

	public CRisk(final String name, final CProject project) {
		super(CRisk.class, name, project);
		initializeDefaults();
	}

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	public String getCause() { return cause; }

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public String getImpact() { return impact; }

	public Integer getImpactScore() { return impactScore; }

	public String getMitigation() { return mitigation; }

	public String getPlan() { return plan; }

	public Integer getProbability() { return probability; }

	public String getResidualRisk() { return residualRisk; }

	public String getResult() { return result; }

	public ERiskCriticality getRiskCriticality() { return riskCriticality; }

	public ERiskLikelihood getRiskLikelihood() { return riskLikelihood; }

	/** Get Risk Matrix Category based on calculated risk score (ISO 31000 risk matrix).
	 * @return Risk category: Critical (75+), High (50-74), Medium (25-49), Low (1-24), or "Not Assessed" */
	@jakarta.persistence.Transient
	public String getRiskMatrixCategory() {
		final Integer score = getRiskScore();
		if (score == null) {
			return "Not Assessed";
		}
		if (score >= 75) {
			return "Critical";
		}
		if (score >= 50) {
			return "High";
		}
		if (score >= 25) {
			return "Medium";
		}
		return "Low";
	}

	public ERiskResponseStrategy getRiskResponseStrategy() { return riskResponseStrategy; }

	/** Calculate Risk Score as Probability × Impact (ISO 31000 quantitative assessment).
	 * @return Risk score value from 1-100, or null if probability or impact not set */
	@jakarta.persistence.Transient
	public Integer getRiskScore() {
		if (probability != null && impactScore != null) {
			return probability * impactScore;
		}
		return null;
	}

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
		if (searchValue == null || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check entity field
		if (fieldNames.remove("entityType") && getEntityType() != null && getEntityType().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setCause(final String cause) {
		this.cause = cause;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CRiskType.class, "Type entity must be an instance of CRiskType");
		Check.notNull(getProject(), "Project must be set before assigning risk type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning risk type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning risk type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match risk project company id " + getProject().getCompany().getId());
		entityType = (CRiskType) typeEntity;
		updateLastModified();
	}

	public void setImpact(final String impact) {
		this.impact = impact;
		updateLastModified();
	}
	// ISO 31000:2018 - Quantitative Risk Assessment Getters/Setters

	public void setImpactScore(final Integer impactScore) {
		this.impactScore = impactScore;
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

	public void setProbability(final Integer probability) {
		this.probability = probability;
		updateLastModified();
	}

	public void setResidualRisk(final String residualRisk) {
		this.residualRisk = residualRisk;
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

	public void setRiskResponseStrategy(final ERiskResponseStrategy riskResponseStrategy) {
		this.riskResponseStrategy = riskResponseStrategy;
		updateLastModified();
	}

	public void setRiskSeverity(final ERiskSeverity riskSeverity) {
		this.riskSeverity = riskSeverity;
		updateLastModified();
	}

	/**
	 * Creates a clone of this risk with the specified options.
	 * This implementation follows the recursive cloning pattern:
	 * 1. Calls parent's createClone() to handle inherited fields (CProjectItem)
	 * 2. Clones risk-specific fields based on options
	 * 3. Recursively clones collections (comments, attachments) if requested
	 * 
	 * Cloning behavior:
	 * - Basic fields (strings, numbers, enums) are always cloned
	 * - Date fields are cloned only if !options.isResetDates()
	 * - Workflow field is cloned only if options.isCloneWorkflow()
	 * - Comments collection is recursively cloned if options.includesComments()
	 * - Attachments collection is recursively cloned if options.includesAttachments()
	 * 
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the risk with cloned data
	 * @throws CloneNotSupportedException if cloning fails
	 */
	@Override
	public CRisk createClone(final CCloneOptions options) throws Exception {
		// Get parent's clone (CProjectItem -> CEntityOfProject -> CEntityNamed -> CEntityDB)
		final CRisk clone = super.createClone(options);

		// Clone basic risk fields (always included)
		clone.cause = this.cause;
		clone.impact = this.impact;
		clone.mitigation = this.mitigation;
		clone.plan = this.plan;
		clone.result = this.result;
		clone.residualRisk = this.residualRisk;
		
		// Clone numeric fields
		clone.probability = this.probability;
		clone.impactScore = this.impactScore;
		
		// Clone enum fields
		clone.riskCriticality = this.riskCriticality;
		clone.riskLikelihood = this.riskLikelihood;
		clone.riskSeverity = this.riskSeverity;
		clone.riskResponseStrategy = this.riskResponseStrategy;
		
		// Clone entity type (risk type)
		clone.entityType = this.entityType;
		
		// Clone workflow if requested
		if (options.isCloneWorkflow() && this.getWorkflow() != null) {
			// Workflow is obtained via entityType.getWorkflow() - already cloned via entityType
		}
		
		// Clone comments if requested
		if (options.includesComments() && this.comments != null && !this.comments.isEmpty()) {
			clone.comments = new HashSet<>();
			for (final CComment comment : this.comments) {
				try {
					final CComment commentClone = comment.createClone(options);
					clone.comments.add(commentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone comment: {}", e.getMessage());
				}
			}
		}
		
		// Clone attachments if requested
		if (options.includesAttachments() && this.attachments != null && !this.attachments.isEmpty()) {
			clone.attachments = new HashSet<>();
			for (final CAttachment attachment : this.attachments) {
				try {
					final CAttachment attachmentClone = attachment.createClone(options);
					clone.attachments.add(attachmentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone attachment: {}", e.getMessage());
				}
			}
		}
		
		LOGGER.debug("Successfully cloned risk '{}' with options: {}", this.getName(), options);
		return clone;
	}
}
