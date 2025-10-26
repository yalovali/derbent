package tech.derbent.app.decisions.domain;

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
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.domains.IHasStatusAndWorkflow;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.base.users.domain.CUser;

/** CDecision - Domain entity representing project decisions with comprehensive management features. Layer: Domain (MVC) Supports: - Decision type
 * categorization - Cost estimation and tracking - Team collaboration and assignments - Multi-stage approval workflow - Accountable personnel
 * management - Descriptive documentation Follows the established patterns from CActivity for consistency. */
@Entity
@Table (name = "cdecision")
@AttributeOverride (name = "id", column = @Column (name = "decision_id"))
public class CDecision extends CProjectItem<CDecision> implements IHasStatusAndWorkflow<CDecision> {

	public static final String DEFAULT_COLOR = "#e83e8c";
	public static final String DEFAULT_ICON = "vaadin:gavel";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDecision.class);
	public static final String VIEW_NAME = "Decisions View";
	// Accountable Personnel
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "accountable_user_id", nullable = true)
	@AMetaData (
			displayName = "Accountable Personnel", required = false, readOnly = false, description = "User accountable for this decision",
			hidden = false, order = 5, dataProviderBean = "CUserService"
	)
	private CUser accountableUser;
	// Decision Type Classification
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Decision Type", required = false, readOnly = false, description = "Category or type of the decision", hidden = false,
			order = 2
	)
	private CDecisionType entityType;
	// Cost Estimation
	@Column (name = "estimated_cost", nullable = true, precision = 19, scale = 2)
	@DecimalMin (value = "0.0", inclusive = true)
	@AMetaData (
			displayName = "Estimated Cost", required = false, readOnly = false, description = "Estimated cost impact of the decision", hidden = false,
			order = 3, min = 0.0
	)
	private BigDecimal estimatedCost;
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

	@Override
	public CDecisionType getEntityType() { return entityType; }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	public LocalDateTime getImplementationDate() { return implementationDate; }

	public LocalDateTime getReviewDate() { return reviewDate; }

	@Override
	public CWorkflowEntity getWorkflow() { // TODO Auto-generated method stub
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships
		if (accountableUser != null) {
			accountableUser.getLogin(); // Trigger accountable user loading
		}
		if (entityType != null) {
			entityType.getName(); // Trigger type loading
		}
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

	public void setAccountableUser(final CUser accountableUser) {
		this.accountableUser = accountableUser;
		updateLastModified();
	}

	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CDecisionType.class, "Type entity must be an instance of CDecisionType");
		this.entityType = (CDecisionType) typeEntity;
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
