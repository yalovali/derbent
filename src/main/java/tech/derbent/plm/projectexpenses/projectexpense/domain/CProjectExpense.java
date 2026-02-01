package tech.derbent.plm.projectexpenses.projectexpense.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IFinancialEntity;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.plm.projectexpenses.projectexpensetype.domain.CProjectExpenseType;

@Entity
@Table (name = "\"cprojectexpense\"")
@AttributeOverride (name = "id", column = @Column (name = "projectexpense_id"))
public class CProjectExpense extends CProjectItem<CProjectExpense>
		implements IHasStatusAndWorkflow<CProjectExpense>, IFinancialEntity, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#A0522D"; // X11 Sienna - outgoing money (darker)
	public static final String DEFAULT_ICON = "vaadin:money-withdraw";
	public static final String ENTITY_TITLE_PLURAL = "Project Expenses";
	public static final String ENTITY_TITLE_SINGULAR = "Project Expense";
	public static final String VIEW_NAME = "Project Expense View";
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Amount cannot exceed 9999999999.99")
	@AMetaData (displayName = "Amount", required = false, readOnly = false, defaultValue = "0.00", description = "Expense amount", hidden = false)
	private BigDecimal amount = BigDecimal.ZERO;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "projectexpense_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this entity", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "projectexpense_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this entity", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "currency_id", nullable = true)
	@AMetaData (
			displayName = "Currency", required = false, readOnly = false, description = "Currency for expense amount", hidden = false,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "ProjectExpense Type", required = false, readOnly = false, description = "Type category of the projectexpense",
			hidden = false, dataProviderBean = "CProjectExpenseTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectExpenseType entityType;
	@Column (name = "expense_date", nullable = true)
	@AMetaData (displayName = "Expense Date", required = false, readOnly = false, description = "Date when expense was incurred", hidden = false)
	private LocalDate expenseDate;

	/** Default constructor for JPA. */
	protected CProjectExpense() {}

	public CProjectExpense(final String name, final CProject<?> project) {
		super(CProjectExpense.class, name, project);
		initializeDefaults();
	}

	@Override
	public BigDecimal getAmount() { return amount; }

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() { return comments; }

	@Override
	public CCurrency getCurrency() { return currency; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public LocalDate getExpenseDate() { return expenseDate; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	private final void initializeDefaults() {
		expenseDate = LocalDate.now();
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
		updateLastModified();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
		updateLastModified();
	}

	@Override
	public void setCurrency(final CCurrency currency) {
		this.currency = currency;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CProjectExpenseType.class, "Type entity must be an instance of CProjectExpenseType");
		Check.notNull(getProject(), "Project must be set before assigning project expense type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning project expense type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning project expense type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match project expense project company id " + getProject().getCompany().getId());
		entityType = (CProjectExpenseType) typeEntity;
		updateLastModified();
	}

	public void setExpenseDate(final LocalDate expenseDate) {
		this.expenseDate = expenseDate;
		updateLastModified();
	}
}
