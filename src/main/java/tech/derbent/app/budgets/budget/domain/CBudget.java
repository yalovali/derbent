package tech.derbent.app.budgets.budget.domain;

import java.math.BigDecimal;
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
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.budgets.budgettype.domain.CBudgetType;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.orders.currency.domain.CCurrency;

@Entity
@Table (name = "\"cbudget\"")
@AttributeOverride (name = "id", column = @Column (name = "budget_id"))
public class CBudget extends CProjectItem<CBudget> implements IHasStatusAndWorkflow<CBudget>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#8B4513"; // X11 SaddleBrown - financial planning (darker)
	public static final String DEFAULT_ICON = "vaadin:dollar";
	public static final String ENTITY_TITLE_PLURAL = "Budgets";
	public static final String ENTITY_TITLE_SINGULAR = "Budget";
	public static final String VIEW_NAME = "Budget View";
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Budget Type", required = false, readOnly = false, description = "Type category of the budget", hidden = false,
			dataProviderBean = "CBudgetTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CBudgetType entityType;
	
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Budget amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Budget amount cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Budget Amount", required = false, readOnly = false, defaultValue = "0.00",
			description = "Total budget amount allocated", hidden = false
	)
	private BigDecimal budgetAmount = BigDecimal.ZERO;
	
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
	
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "currency_id", nullable = true)
	@AMetaData (
			displayName = "Currency", required = false, readOnly = false,
			description = "Currency for budget amounts", hidden = false,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "budget_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this budget", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "budget_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this budget", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CBudget() {
		super();
		initializeDefaults();
	}

	public CBudget(final String name, final CProject project) {
		super(CBudget.class, name, project);
		initializeDefaults();
	}

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CBudgetType.class, "Type entity must be an instance of CBudgetType");
		Check.notNull(getProject(), "Project must be set before assigning budget type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning budget type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning budget type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match budget project company id "
						+ getProject().getCompany().getId());
		entityType = (CBudgetType) typeEntity;
		updateLastModified();
	}
	
	public BigDecimal getBudgetAmount() { return budgetAmount; }
	
	public void setBudgetAmount(final BigDecimal budgetAmount) {
		this.budgetAmount = budgetAmount;
		updateLastModified();
	}
	
	public BigDecimal getActualCost() { return actualCost; }
	
	public void setActualCost(final BigDecimal actualCost) {
		this.actualCost = actualCost;
		updateLastModified();
	}
	
	public BigDecimal getAlertThreshold() { return alertThreshold; }
	
	public void setAlertThreshold(final BigDecimal alertThreshold) {
		this.alertThreshold = alertThreshold;
		updateLastModified();
	}
	
	public CCurrency getCurrency() { return currency; }
	
	public void setCurrency(final CCurrency currency) {
		this.currency = currency;
		updateLastModified();
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
		BigDecimal variance = calculateVariance();
		return variance.divide(budgetAmount, 2, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
	}
	
	/** Check if actual cost exceeds alert threshold.
	 * @return true if alert threshold is exceeded */
	public boolean isAlertThresholdExceeded() {
		if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0 || 
			actualCost == null || alertThreshold == null) {
			return false;
		}
		BigDecimal threshold = budgetAmount.multiply(alertThreshold).divide(new BigDecimal("100"));
		return actualCost.compareTo(threshold) > 0;
	}
}
