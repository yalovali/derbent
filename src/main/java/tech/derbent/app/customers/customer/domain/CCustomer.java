package tech.derbent.app.customers.customer.domain;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.customers.customertype.domain.CCustomerType;

@Entity
@Table(name = "ccustomer")
@AttributeOverride(name = "id", column = @Column(name = "customer_id"))
public class CCustomer extends CProjectItem<CCustomer> implements IHasStatusAndWorkflow<CCustomer>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#4169E1"; // RoyalBlue - customers
	public static final String DEFAULT_ICON = "vaadin:briefcase";
	public static final String ENTITY_TITLE_PLURAL = "Customers";
	public static final String ENTITY_TITLE_SINGULAR = "Customer";
	public static final String VIEW_NAME = "Customers View";

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entitytype_id", nullable = false)
	@AMetaData(
			displayName = "Customer Type", required = true, readOnly = false,
			description = "Type category of the customer (e.g., Prospect, Active, Key Account)", hidden = false,
			dataProviderBean = "CCustomerTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CCustomerType entityType;

	@Column(name = "company_name", nullable = false, length = 200)
	@Size(max = 200)
	@AMetaData(
			displayName = "Company Name", required = true, readOnly = false,
			description = "Official name of the customer company", hidden = false, maxLength = 200, order = 1
	)
	private String companyName;

	@Column(name = "industry", nullable = true, length = 100)
	@Size(max = 100)
	@AMetaData(
			displayName = "Industry", required = false, readOnly = false,
			description = "Industry sector (e.g., Technology, Healthcare, Finance)", hidden = false, maxLength = 100, order = 2
	)
	private String industry;

	@Column(name = "company_size", nullable = true, length = 50)
	@Size(max = 50)
	@AMetaData(
			displayName = "Company Size", required = false, readOnly = false,
			description = "Number of employees (e.g., 1-10, 11-50, 51-200, 201-500, 500+)", hidden = false, maxLength = 50, order = 3
	)
	private String companySize;

	@Column(name = "website", nullable = true, length = 200)
	@Size(max = 200)
	@AMetaData(
			displayName = "Website", required = false, readOnly = false,
			description = "Company website URL", hidden = false, maxLength = 200, order = 4
	)
	private String website;

	@Column(name = "annual_revenue", nullable = true, precision = 15, scale = 2)
	@DecimalMin(value = "0.00", message = "Annual revenue must be positive")
	@DecimalMax(value = "99999999999.99", message = "Annual revenue cannot exceed 99,999,999,999.99")
	@AMetaData(
			displayName = "Annual Revenue", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated or actual annual revenue", hidden = false, order = 5
	)
	private BigDecimal annualRevenue = BigDecimal.ZERO;

	@Column(name = "relationship_start_date", nullable = true)
	@AMetaData(
			displayName = "Relationship Start Date", required = false, readOnly = false,
			description = "Date when the customer relationship began", hidden = false, order = 6
	)
	private LocalDate relationshipStartDate;

	@Column(name = "primary_contact_name", nullable = true, length = 100)
	@Size(max = 100)
	@AMetaData(
			displayName = "Primary Contact Name", required = false, readOnly = false,
			description = "Name of the main contact person", hidden = false, maxLength = 100, order = 7
	)
	private String primaryContactName;

	@Column(name = "primary_contact_email", nullable = true, length = 150)
	@Email
	@Size(max = 150)
	@AMetaData(
			displayName = "Primary Contact Email", required = false, readOnly = false,
			description = "Email of the primary contact", hidden = false, maxLength = 150, order = 8
	)
	private String primaryContactEmail;

	@Column(name = "primary_contact_phone", nullable = true, length = 50)
	@Size(max = 50)
	@AMetaData(
			displayName = "Primary Contact Phone", required = false, readOnly = false,
			description = "Phone number of the primary contact", hidden = false, maxLength = 50, order = 9
	)
	private String primaryContactPhone;

	@Column(name = "billing_address", nullable = true, length = 500)
	@Size(max = 500)
	@AMetaData(
			displayName = "Billing Address", required = false, readOnly = false,
			description = "Full billing address", hidden = false, maxLength = 500, order = 10
	)
	private String billingAddress;

	@Column(name = "shipping_address", nullable = true, length = 500)
	@Size(max = 500)
	@AMetaData(
			displayName = "Shipping Address", required = false, readOnly = false,
			description = "Full shipping address", hidden = false, maxLength = 500, order = 11
	)
	private String shippingAddress;

	@Column(name = "customer_notes", nullable = true, length = 2000)
	@Size(max = 2000)
	@AMetaData(
			displayName = "Notes", required = false, readOnly = false,
			description = "Additional notes about the customer", hidden = false, maxLength = 2000, order = 12
	)
	private String customerNotes;

	@Column(name = "lifetime_value", nullable = true, precision = 15, scale = 2)
	@DecimalMin(value = "0.00", message = "Lifetime value must be positive")
	@DecimalMax(value = "99999999999.99", message = "Lifetime value cannot exceed 99,999,999,999.99")
	@AMetaData(
			displayName = "Lifetime Value", required = false, readOnly = false, defaultValue = "0.00",
			description = "Total value of the customer relationship", hidden = false, order = 13
	)
	private BigDecimal lifetimeValue = BigDecimal.ZERO;

	@Column(name = "last_interaction_date", nullable = true)
	@AMetaData(
			displayName = "Last Interaction Date", required = false, readOnly = false,
			description = "Date of the most recent interaction with this customer", hidden = false, order = 14
	)
	private LocalDate lastInteractionDate;

	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	@AMetaData(
			displayName = "Attachments", required = false, readOnly = false,
			description = "Documents and files related to this customer", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();

	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	@AMetaData(
			displayName = "Comments", required = false, readOnly = false,
			description = "Discussion comments about this customer", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CCustomer() {
		super();
		initializeDefaults();
	}

	public CCustomer(final String name, final CProject project) {
		super(CCustomer.class, name, project);
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
	public CTypeEntity<?> getEntityType() {
		return entityType;
	}

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
		Check.instanceOf(typeEntity, CCustomerType.class, "Type entity must be an instance of CCustomerType");
		Check.notNull(getProject(), "Project must be set before assigning customer type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning customer type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning customer type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match customer project company id "
						+ getProject().getCompany().getId());
		entityType = (CCustomerType) typeEntity;
		updateLastModified();
	}

	// Getters and setters
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(final String companyName) {
		this.companyName = companyName;
		updateLastModified();
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(final String industry) {
		this.industry = industry;
		updateLastModified();
	}

	public String getCompanySize() {
		return companySize;
	}

	public void setCompanySize(final String companySize) {
		this.companySize = companySize;
		updateLastModified();
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(final String website) {
		this.website = website;
		updateLastModified();
	}

	public BigDecimal getAnnualRevenue() {
		return annualRevenue;
	}

	public void setAnnualRevenue(final BigDecimal annualRevenue) {
		this.annualRevenue = annualRevenue;
		updateLastModified();
	}

	public LocalDate getRelationshipStartDate() {
		return relationshipStartDate;
	}

	public void setRelationshipStartDate(final LocalDate relationshipStartDate) {
		this.relationshipStartDate = relationshipStartDate;
		updateLastModified();
	}

	public String getPrimaryContactName() {
		return primaryContactName;
	}

	public void setPrimaryContactName(final String primaryContactName) {
		this.primaryContactName = primaryContactName;
		updateLastModified();
	}

	public String getPrimaryContactEmail() {
		return primaryContactEmail;
	}

	public void setPrimaryContactEmail(final String primaryContactEmail) {
		this.primaryContactEmail = primaryContactEmail;
		updateLastModified();
	}

	public String getPrimaryContactPhone() {
		return primaryContactPhone;
	}

	public void setPrimaryContactPhone(final String primaryContactPhone) {
		this.primaryContactPhone = primaryContactPhone;
		updateLastModified();
	}

	public String getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(final String billingAddress) {
		this.billingAddress = billingAddress;
		updateLastModified();
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(final String shippingAddress) {
		this.shippingAddress = shippingAddress;
		updateLastModified();
	}

	public String getCustomerNotes() {
		return customerNotes;
	}

	public void setCustomerNotes(final String customerNotes) {
		this.customerNotes = customerNotes;
		updateLastModified();
	}

	public BigDecimal getLifetimeValue() {
		return lifetimeValue;
	}

	public void setLifetimeValue(final BigDecimal lifetimeValue) {
		this.lifetimeValue = lifetimeValue;
		updateLastModified();
	}

	public LocalDate getLastInteractionDate() {
		return lastInteractionDate;
	}

	public void setLastInteractionDate(final LocalDate lastInteractionDate) {
		this.lastInteractionDate = lastInteractionDate;
		updateLastModified();
	}
}
