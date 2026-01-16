package tech.derbent.app.projectexpenses.projectexpense.domain;

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
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IFinancialEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.projectexpenses.projectexpensetype.domain.CProjectExpenseType;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.orders.currency.domain.CCurrency;

@Entity
@Table (name = "\"cprojectexpense\"")
@AttributeOverride (name = "id", column = @Column (name = "projectexpense_id"))
public class CProjectExpense extends CProjectItem<CProjectExpense> implements IHasStatusAndWorkflow<CProjectExpense>, IFinancialEntity, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#A0522D"; // X11 Sienna - outgoing money (darker)
	public static final String DEFAULT_ICON = "vaadin:money-withdraw";
	public static final String ENTITY_TITLE_PLURAL = "Project Expenses";
	public static final String ENTITY_TITLE_SINGULAR = "Project Expense";
	public static final String VIEW_NAME = "Project Expense View";
	
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "ProjectExpense Type", required = false, readOnly = false, description = "Type category of the projectexpense",
			hidden = false,  dataProviderBean = "CProjectExpenseTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectExpenseType entityType;
	
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Amount cannot exceed 9999999999.99")
	@AMetaData (
			displayName = "Amount", required = false, readOnly = false, defaultValue = "0.00",
			description = "Expense amount", hidden = false
	)
	private BigDecimal amount = BigDecimal.ZERO;
	
	@Column (name = "expense_date", nullable = true)
	@AMetaData (
			displayName = "Expense Date", required = false, readOnly = false,
			description = "Date when expense was incurred", hidden = false
	)
	private LocalDate expenseDate;
	
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "currency_id", nullable = true)
	@AMetaData (
			displayName = "Currency", required = false, readOnly = false,
			description = "Currency for expense amount", hidden = false,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;

	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "projectexpense_id")
	@AMetaData(
		displayName = "Attachments",
		required = false,
		readOnly = false,
		description = "File attachments for this entity",
		hidden = false,
		dataProviderBean = "CAttachmentService",
		createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "projectexpense_id")
	@AMetaData(
		displayName = "Comments",
		required = false,
		readOnly = false,
		description = "Comments for this entity",
		hidden = false,
		dataProviderBean = "CCommentService",
		createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CProjectExpense() {
		super();
		initializeDefaults();
	}

	public CProjectExpense(final String name, final CProject project) {
		super(CProjectExpense.class, name, project);
		initializeDefaults();
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
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CProjectExpenseType.class, "Type entity must be an instance of CProjectExpenseType");
		Check.notNull(getProject(), "Project must be set before assigning project expense type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning project expense type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning project expense type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match project expense project company id "
						+ getProject().getCompany().getId());
		entityType = (CProjectExpenseType) typeEntity;
		updateLastModified();
	}
	
	public BigDecimal getAmount() { return amount; }
	
	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
		updateLastModified();
	}
	
	public LocalDate getExpenseDate() { return expenseDate; }
	
	public void setExpenseDate(final LocalDate expenseDate) {
		this.expenseDate = expenseDate;
		updateLastModified();
	}
	
	public CCurrency getCurrency() { return currency; }
	
	public void setCurrency(final CCurrency currency) {
		this.currency = currency;
		updateLastModified();
	}

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments;
	}

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
		updateLastModified();
	}
}
