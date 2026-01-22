package tech.derbent.plm.budgets.budget.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.budgets.budgettype.domain.CBudgetType;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.orders.currency.domain.CCurrency;

@Entity
@Table (name = "\"cbudget\"")
@AttributeOverride (name = "id", column = @Column (name = "budget_id"))
public class CBudget extends CProjectItem<CBudget> implements IHasStatusAndWorkflow<CBudget>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#8B4513"; // X11 SaddleBrown - financial planning (darker)
	public static final String DEFAULT_ICON = "vaadin:dollar";
	public static final String ENTITY_TITLE_PLURAL = "Budgets";
	public static final String ENTITY_TITLE_SINGULAR = "Budget";
	public static final String VIEW_NAME = "Budget View";
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual cost must be positive")
	@DecimalMax (value = "9999999999.99", message = "Actual cost cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Actual Cost", required = false, readOnly = true, defaultValue = "0.00",
			description = "Actual cost spent (calculated from expenses)", hidden = false
	)
	private BigDecimal actualCost = BigDecimal.ZERO;
	@Column (nullable = true, precision = 8, scale = 2)
	@DecimalMin (value = "0.0", message = "Alert threshold must be positive")
	@DecimalMax (value = "100.0", message = "Alert threshold cannot exceed 100%")
	@AMetaData (
			displayName = "Alert Threshold (%)", required = false, readOnly = false, defaultValue = "80.00",
			description = "Alert when actual exceeds this percentage of budget", hidden = false
	)
	private BigDecimal alertThreshold = new BigDecimal("80.00");
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "budget_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this budget", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Budget amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Budget amount cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Budget Amount", required = false, readOnly = false, defaultValue = "0.00", description = "Total budget amount allocated",
			hidden = false
	)
	private BigDecimal budgetAmount = BigDecimal.ZERO;
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "budget_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this budget", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "currency_id", nullable = true)
	@AMetaData (
			displayName = "Currency", required = false, readOnly = false, description = "Currency for budget amounts", hidden = false,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Earned Value must be positive")
	@DecimalMax (value = "9999999999.99", message = "Earned Value cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Earned Value (EV)", required = false, readOnly = false, defaultValue = "0.00",
			description = "Value of work actually completed - Budgeted Cost of Work Performed (PMBOK EVM)", hidden = false
	)
	private BigDecimal earnedValue = BigDecimal.ZERO;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Budget Type", required = false, readOnly = false, description = "Type category of the budget", hidden = false,
			dataProviderBean = "CBudgetTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CBudgetType entityType;
	// PMI PMBOK - Earned Value Management (EVM) Fields
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Planned Value must be positive")
	@DecimalMax (value = "9999999999.99", message = "Planned Value cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Planned Value (PV)", required = false, readOnly = false, defaultValue = "0.00",
			description = "Budget Baseline - Authorized budget for scheduled work (PMBOK EVM)", hidden = false
	)
	private BigDecimal plannedValue = BigDecimal.ZERO;

	/** Default constructor for JPA. */
	public CBudget() {
		super();
		initializeDefaults();
	}

	public CBudget(final String name, final CProject<?> project) {
		super(CBudget.class, name, project);
		initializeDefaults();
	}

	/** Calculate variance between budget and actual cost.
	 * @return variance amount (positive means under budget, negative means over budget) */
	public BigDecimal calculateVariance() {
		if (budgetAmount == null || actualCost == null) {
			return BigDecimal.ZERO;
		}
		return budgetAmount.subtract(actualCost);
	}

	/** Calculate variance percentage.
	 * @return variance percentage (positive means under budget, negative means over budget) */
	public BigDecimal calculateVariancePercentage() {
		if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		final BigDecimal variance = calculateVariance();
		return variance.divide(budgetAmount, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
	}

	public BigDecimal getActualCost() { return actualCost; }

	public BigDecimal getAlertThreshold() { return alertThreshold; }

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	public BigDecimal getBudgetAmount() { return budgetAmount; }

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	/** Calculate Cost Performance Index (CPI = EV / AC) per PMBOK EVM. CPI > 1.0 means under budget, CPI < 1.0 means over budget.
	 * @return Cost performance index (1.0 = on budget) */
	public BigDecimal getCostPerformanceIndex() {
		if (actualCost == null || actualCost.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ONE;
		}
		if (earnedValue == null) {
			return BigDecimal.ZERO;
		}
		return earnedValue.divide(actualCost, 2, RoundingMode.HALF_UP);
	}

	/** Calculate Cost Variance (CV = EV - AC) per PMBOK EVM. Positive CV means under budget, negative means over budget.
	 * @return Cost variance amount */
	public BigDecimal getCostVariance() {
		if (earnedValue == null || actualCost == null) {
			return BigDecimal.ZERO;
		}
		return earnedValue.subtract(actualCost);
	}

	public CCurrency getCurrency() { return currency; }

	public BigDecimal getEarnedValue() { return earnedValue; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	/** Get EVM performance status as human-readable string.
	 * @return Performance status description */
	@jakarta.persistence.Transient
	public String getEVMPerformanceStatus() {
		final BigDecimal cpi = getCostPerformanceIndex();
		final BigDecimal spi = getSchedulePerformanceIndex();
		final String costStatus =
				cpi.compareTo(BigDecimal.ONE) > 0 ? "Under Budget" : cpi.compareTo(BigDecimal.ONE) < 0 ? "Over Budget" : "On Budget";
		final String scheduleStatus =
				spi.compareTo(BigDecimal.ONE) > 0 ? "Ahead of Schedule" : spi.compareTo(BigDecimal.ONE) < 0 ? "Behind Schedule" : "On Schedule";
		return costStatus + ", " + scheduleStatus;
	}

	public BigDecimal getPlannedValue() { return plannedValue; }

	/** Calculate Schedule Performance Index (SPI = EV / PV) per PMBOK EVM. SPI > 1.0 means ahead of schedule, SPI < 1.0 means behind schedule.
	 * @return Schedule performance index (1.0 = on schedule) */
	public BigDecimal getSchedulePerformanceIndex() {
		if (plannedValue == null || plannedValue.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ONE;
		}
		if (earnedValue == null) {
			return BigDecimal.ZERO;
		}
		return earnedValue.divide(plannedValue, 2, RoundingMode.HALF_UP);
	}

	/** Calculate Schedule Variance (SV = EV - PV) per PMBOK EVM. Positive SV means ahead of schedule, negative means behind schedule.
	 * @return Schedule variance amount */
	public BigDecimal getScheduleVariance() {
		if (earnedValue == null || plannedValue == null) {
			return BigDecimal.ZERO;
		}
		return earnedValue.subtract(plannedValue);
	}

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		actualCost = BigDecimal.ZERO;
		alertThreshold = new BigDecimal("80.00");
		budgetAmount = BigDecimal.ZERO;
		earnedValue = BigDecimal.ZERO;
		plannedValue = BigDecimal.ZERO;
		attachments = new HashSet<>();
		comments = new HashSet<>();
	}

	/** Check if actual cost exceeds alert threshold.
	 * @return true if alert threshold is exceeded */
	public boolean isAlertThresholdExceeded() {
		if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0 || actualCost == null || alertThreshold == null) {
			return false;
		}
		final BigDecimal threshold = budgetAmount.multiply(alertThreshold).divide(new BigDecimal("100"));
		return actualCost.compareTo(threshold) > 0;
	}
	// PMI PMBOK - Earned Value Management (EVM) Methods

	public void setActualCost(final BigDecimal actualCost) {
		this.actualCost = actualCost;
		updateLastModified();
	}

	public void setAlertThreshold(final BigDecimal alertThreshold) {
		this.alertThreshold = alertThreshold;
		updateLastModified();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setBudgetAmount(final BigDecimal budgetAmount) {
		this.budgetAmount = budgetAmount;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setCurrency(final CCurrency currency) {
		this.currency = currency;
		updateLastModified();
	}

	public void setEarnedValue(final BigDecimal earnedValue) {
		this.earnedValue = earnedValue;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CBudgetType.class, "Type entity must be an instance of CBudgetType");
		Check.notNull(getProject(), "Project must be set before assigning budget type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning budget type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning budget type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match budget project company id " + getProject().getCompany().getId());
		entityType = (CBudgetType) typeEntity;
		updateLastModified();
	}

	public void setPlannedValue(final BigDecimal plannedValue) {
		this.plannedValue = plannedValue;
		updateLastModified();
	}
}
