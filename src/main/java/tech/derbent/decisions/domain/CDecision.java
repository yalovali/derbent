package tech.derbent.decisions.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * CDecision - Domain entity representing project decisions with comprehensive management
 * features. Layer: Domain (MVC) Supports: - Decision type categorization - Cost
 * estimation and tracking - Team collaboration and assignments - Multi-stage approval
 * workflow - Accountable personnel management - Descriptive documentation Follows the
 * established patterns from CActivity for consistency.
 */
@Entity
@Table (name = "cdecision")
@AttributeOverride (name = "id", column = @Column (name = "decision_id"))
public class CDecision extends CEntityOfProject {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDecision.class);

	// Decision Type Classification
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "decisiontype_id", nullable = true)
	@MetaData (
		displayName = "Decision Type", required = false, readOnly = false,
		description = "Category or type of the decision", hidden = false, order = 2,
		dataProviderBean = "CDecisionTypeService"
	)
	private CDecisionType decisionType;

	// Cost Estimation
	@Column (name = "estimated_cost", nullable = true, precision = 19, scale = 2)
	@DecimalMin (value = "0.0", inclusive = true)
	@MetaData (
		displayName = "Estimated Cost", required = false, readOnly = false,
		description = "Estimated cost impact of the decision", hidden = false, order = 3,
		min = 0.0
	)
	private BigDecimal estimatedCost;

	// Status Management
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "decision_status_id", nullable = true)
	@MetaData (
		displayName = "Decision Status", required = false, readOnly = false,
		description = "Current status of the decision", hidden = false, order = 4,
		dataProviderBean = "CDecisionStatusService"
	)
	private CDecisionStatus decisionStatus;

	// Accountable Personnel
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "accountable_user_id", nullable = true)
	@MetaData (
		displayName = "Accountable Personnel", required = false, readOnly = false,
		description = "User accountable for this decision", hidden = false, order = 5,
		dataProviderBean = "CUserService"
	)
	private CUser accountableUser;

	// Team Members
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (
		name = "cdecision_team_members", joinColumns = @JoinColumn (name = "decision_id"),
		inverseJoinColumns = @JoinColumn (name = "user_id")
	)
	@MetaData (
		displayName = "Team Members", required = false, readOnly = false,
		description = "Team members involved in this decision", hidden = false, order = 6,
		dataProviderBean = "CUserService"
	)
	private List<CUser> teamMembers = new ArrayList<>();

	// Approval Workflow
	@OneToMany (mappedBy = "decision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<CDecisionApproval> approvals = new ArrayList<>();

	// Decision Implementation Date
	@Column (name = "implementation_date", nullable = true)
	@MetaData (
		displayName = "Implementation Date", required = false, readOnly = false,
		description = "Date when the decision was or will be implemented", hidden = false,
		order = 7
	)
	private LocalDateTime implementationDate;

	// Decision Review Date
	@Column (name = "review_date", nullable = true)
	@MetaData (
		displayName = "Review Date", required = false, readOnly = false,
		description = "Date when the decision will be reviewed", hidden = false, order = 8
	)
	private LocalDateTime reviewDate;

	/**
	 * Default constructor for JPA.
	 */
	public CDecision() {
		super();
		LOGGER.debug("CDecision() - Creating new decision instance");
	}

	/**
	 * Constructor with name and project.
	 * @param name    the decision name - must not be null or empty
	 * @param project the project this decision belongs to - must not be null
	 */
	public CDecision(final String name, final CProject project) {
		super(name, project);
		LOGGER.debug("CDecision constructor called with name: {} and project: {}", name,
			project != null ? project.getName() : "null");

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("CDecision constructor - Name parameter is null or empty");
		}

		if (project == null) {
			LOGGER.warn("CDecision constructor - Project parameter is null");
		}
	}

	/**
	 * Constructor with name, project, and description.
	 * @param name        the decision name - must not be null or empty
	 * @param project     the project this decision belongs to - must not be null
	 * @param description detailed description of the decision - can be null
	 */
	public CDecision(final String name, final CProject project,
		final String description) {
		super(name, project);
		setDescription(description);
		LOGGER.debug(
			"CDecision constructor called with name: {}, project: {}, description: {}",
			name, project != null ? project.getName() : "null", description);
	}
	// Getters and Setters

	/**
	 * Adds an approval to the decision.
	 * @param approval the approval to add - must not be null
	 */
	public void addApproval(final CDecisionApproval approval) {
		LOGGER.debug("addApproval called with approval: {}", approval);

		if (approval == null) {
			LOGGER.warn("addApproval called with null approval");
			return;
		}

		if (approvals == null) {
			approvals = new ArrayList<>();
		}

		if (!approvals.contains(approval)) {
			approval.setDecision(this);
			approvals.add(approval);
			updateLastModified();
		}
	}

	/**
	 * Adds a team member to the decision team.
	 * @param user the user to add to the team - must not be null
	 */
	public void addTeamMember(final CUser user) {
		LOGGER.debug("addTeamMember called with user: {}",
			user != null ? user.getName() : "null");

		if (user == null) {
			LOGGER.warn("addTeamMember called with null user");
			return;
		}

		if (teamMembers == null) {
			teamMembers = new ArrayList<>();
		}

		if (!teamMembers.contains(user)) {
			teamMembers.add(user);
			updateLastModified();
		}
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

	/**
	 * Gets the total number of approvals.
	 * @return the count of approvals
	 */
	public int getApprovalCount() { return approvals != null ? approvals.size() : 0; }

	public List<CDecisionApproval> getApprovals() {
		return approvals != null ? approvals : new ArrayList<>();
	}

	/**
	 * Gets the number of granted approvals.
	 * @return the count of approved approvals
	 */
	public int getApprovedCount() {

		if (approvals == null) {
			return 0;
		}
		return (int) approvals.stream().filter(CDecisionApproval::isApproved).count();
	}

	public CDecisionStatus getDecisionStatus() { return decisionStatus; }

	public CDecisionType getDecisionType() { return decisionType; }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	public LocalDateTime getImplementationDate() { return implementationDate; }

	public LocalDateTime getReviewDate() { return reviewDate; }

	public List<CUser> getTeamMembers() {
		return teamMembers != null ? teamMembers : new ArrayList<>();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Checks if the decision is fully approved.
	 * @return true if all required approvals are granted
	 */
	public boolean isFullyApproved() {

		if ((approvals == null) || approvals.isEmpty()) {
			return false;
		}
		return approvals.stream().allMatch(CDecisionApproval::isApproved);
	}

	/**
	 * Removes a team member from the decision team.
	 * @param user the user to remove from the team - must not be null
	 */
	public void removeTeamMember(final CUser user) {
		LOGGER.debug("removeTeamMember called with user: {}",
			user != null ? user.getName() : "null");

		if (user == null) {
			LOGGER.warn("removeTeamMember called with null user");
			return;
		}

		if (teamMembers != null) {
			teamMembers.remove(user);
			updateLastModified();
		}
	}
	// Business Logic Methods

	public void setAccountableUser(final CUser accountableUser) {
		this.accountableUser = accountableUser;
		updateLastModified();
	}

	public void setApprovals(final List<CDecisionApproval> approvals) {
		this.approvals = approvals != null ? approvals : new ArrayList<>();
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

	public void setTeamMembers(final List<CUser> teamMembers) {
		this.teamMembers = teamMembers != null ? teamMembers : new ArrayList<>();
		updateLastModified();
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}