package tech.derbent.plm.projectincomes.projectincome.domain;

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
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.plm.projectincomes.projectincometype.domain.CProjectIncomeType;

@Entity
@Table (name = "\"cprojectincome\"")
@AttributeOverride (name = "id", column = @Column (name = "projectincome_id"))
public class CProjectIncome extends CProjectItem<CProjectIncome>
		implements IHasStatusAndWorkflow<CProjectIncome>, IFinancialEntity, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#B8860B"; // X11 DarkGoldenrod - incoming money (darker)
	public static final String DEFAULT_ICON = "vaadin:money-deposit";
	public static final String ENTITY_TITLE_PLURAL = "Project Incomes";
	public static final String ENTITY_TITLE_SINGULAR = "Project Income";
	public static final String VIEW_NAME = "Project Income View";
	@Column (nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.0", message = "Amount must be positive")
	@DecimalMax (value = "9999999999.99", message = "Amount cannot exceed 9999999999.99")
	@AMetaData (displayName = "Amount", required = false, readOnly = false, defaultValue = "0.00", description = "Income amount", hidden = false)
	private BigDecimal amount = BigDecimal.ZERO;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "projectincome_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this entity", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "projectincome_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this entity", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "currency_id", nullable = true)
	@AMetaData (
			displayName = "Currency", required = false, readOnly = false, description = "Currency for income amount", hidden = false,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "ProjectIncome Type", required = false, readOnly = false, description = "Type category of the projectincome",
			hidden = false, dataProviderBean = "CProjectIncomeTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProjectIncomeType entityType;
	@Column (name = "income_date", nullable = true)
	@AMetaData (displayName = "Income Date", required = false, readOnly = false, description = "Date when income was received", hidden = false)
	private LocalDate incomeDate;

	/** Default constructor for JPA. */
	public CProjectIncome() {
		super();
		initializeDefaults();
	}

	public CProjectIncome(final String name, final CProject<?> project) {
		super(CProjectIncome.class, name, project);
		initializeDefaults();
	}

	@Override
	public BigDecimal getAmount() { return amount; }

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
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
	public CCurrency getCurrency() { return currency; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public LocalDate getIncomeDate() { return incomeDate; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		amount = BigDecimal.ZERO;
		incomeDate = LocalDate.now();
		attachments = new HashSet<>();
		comments = new HashSet<>();
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
		Check.instanceOf(typeEntity, CProjectIncomeType.class, "Type entity must be an instance of CProjectIncomeType");
		Check.notNull(getProject(), "Project must be set before assigning project income type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning project income type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning project income type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match project income project company id " + getProject().getCompany().getId());
		entityType = (CProjectIncomeType) typeEntity;
		updateLastModified();
	}

	public void setIncomeDate(final LocalDate incomeDate) {
		this.incomeDate = incomeDate;
		updateLastModified();
	}
}
