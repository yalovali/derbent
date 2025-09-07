package tech.derbent.decisions.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/** CDecision - Domain entity representing project decisions with comprehensive management features. Layer: Domain (MVC) Supports: - Decision type
 * categorization - Cost estimation and tracking - Team collaboration and assignments - Multi-stage approval workflow - Accountable personnel
 * management - Descriptive documentation Follows the established patterns from CActivity for consistency. */
@Entity
@Table (name = "cdecision")
@AttributeOverride (name = "id", column = @Column (name = "decision_id"))
public class CDecision extends CEntityOfProject<CDecision> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDecision.class);

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return "#dc3545"; // Red color for decision entities
	}

	public static String getIconFilename() { return "vaadin:gavel"; }

	public static Class<?> getViewClassStatic() { return CDecisionsView.class; }

	// Decision Type Classification
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "decisiontype_id", nullable = true)
	@AMetaData (
			displayName = "Decision Type", required = false, readOnly = false, description = "Category or type of the decision", hidden = false,
			order = 2
	)
	private CDecisionType decisionType;
	// Cost Estimation
	@Column (name = "estimated_cost", nullable = true, precision = 19, scale = 2)
	@DecimalMin (value = "0.0", inclusive = true)
	@AMetaData (
			displayName = "Estimated Cost", required = false, readOnly = false, description = "Estimated cost impact of the decision", hidden = false,
			order = 3, min = 0.0
	)
	private BigDecimal estimatedCost;
	// Status Management
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "decision_status_id", nullable = true)
	@AMetaData (
			displayName = "Decision Status", required = false, readOnly = false, description = "Current status of the decision", hidden = false,
			order = 4, dataProviderBean = "CDecisionStatusService"
	)
	private CDecisionStatus decisionStatus;
	// Accountable Personnel
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "accountable_user_id", nullable = true)
	@AMetaData (
			displayName = "Accountable Personnel", required = false, readOnly = false, description = "User accountable for this decision",
			hidden = false, order = 5, dataProviderBean = "CUserService"
	)
	private CUser accountableUser;
	// Decision Implementation Date
	@Column (name = "implementation_date", nullable = true)
	@AMetaData (
			displayName = "Implementation Date", required = false, readOnly = false,
			description = "Date when the decision was or will be implemented", hidden = false, order = 7
	)
	private LocalDateTime implementationDate;
	// Decision Review Date
	@Column (name = "review_date", nullable = true)
	@AMetaData (
			displayName = "Review Date", required = false, readOnly = false, description = "Date when the decision will be reviewed", hidden = false,
			order = 8
	)
	private LocalDateTime reviewDate;

	/** Default constructor for JPA. */
	public CDecision() {
		super();
	}

	public CDecision(final String name, final CProject project) {
		super(CDecision.class, name, project);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CDecision)) {
			return false;
		}
		return super.equals(o);
	}

	public CUser getAccountableUser() { return accountableUser; }

	public CDecisionStatus getDecisionStatus() { return decisionStatus; }

	public CDecisionType getDecisionType() { return decisionType; }

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	public LocalDateTime getImplementationDate() { return implementationDate; }

	public LocalDateTime getReviewDate() { return reviewDate; }

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public void setAccountableUser(final CUser accountableUser) {
		this.accountableUser = accountableUser;
		updateLastModified();
	}

	public void setDecisionStatus(final CDecisionStatus decisionStatus) {
		this.decisionStatus = decisionStatus;
		updateLastModified();
	}

	public void setDecisionType(final CDecisionType decisionType) {
		this.decisionType = decisionType;
		updateLastModified();
	}

	public void setEstimatedCost(final BigDecimal estimatedCost) {
		if ((estimatedCost != null) && (estimatedCost.compareTo(BigDecimal.ZERO) < 0)) {
			LOGGER.warn("setEstimatedCost called with negative value: {}", estimatedCost);
		}
		this.estimatedCost = estimatedCost;
		updateLastModified();
	}

	public void setImplementationDate(final LocalDateTime implementationDate) {
		this.implementationDate = implementationDate;
		updateLastModified();
	}

	public void setReviewDate(final LocalDateTime reviewDate) {
		this.reviewDate = reviewDate;
		updateLastModified();
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}
