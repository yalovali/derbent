package tech.derbent.plm.orders.order.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.orders.approval.domain.COrderApproval;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.plm.orders.type.domain.COrderType;
import tech.derbent.base.users.domain.CUser;

@Entity
@Table (name = "corder")
@AttributeOverride (name = "id", column = @Column (name = "order_id"))
public class COrder extends CProjectItem<COrder> implements IHasStatusAndWorkflow<COrder>, IHasAttachments, IHasComments, IHasLinks {

	public static final String DEFAULT_COLOR = "#D2B48C"; // X11 Tan - purchase orders
	public static final String DEFAULT_ICON = "vaadin:invoice";
	public static final String ENTITY_TITLE_PLURAL = "Orders";
	public static final String ENTITY_TITLE_SINGULAR = "Order";
	public static final String VIEW_NAME = "Orders View";
	@Column (name = "actual_cost", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.00", message = "Actual cost must be positive")
	@DecimalMax (value = "99999999999.99", message = "Actual cost cannot exceed 99,999,999,999.99")
	@AMetaData (
			displayName = "Actual Cost", required = false, readOnly = false, defaultValue = "0.00", description = "Actual cost of the order",
			hidden = false
	)
	private BigDecimal actualCost = BigDecimal.ZERO;
	// Approval Management (One-to-Many relationship)
	@OneToMany (mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AMetaData (displayName = "Approvals", required = false, readOnly = true, description = "Approval records for this order", hidden = false)
	private List<COrderApproval> approvals = new ArrayList<>();
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "order_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Order documents and invoices", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "order_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Discussion comments for this order", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	// Financial Information
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "currency_id", nullable = false)
	@AMetaData (
			displayName = "Currency", required = true, readOnly = false, description = "Currency for the order cost", hidden = false,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;
	@Column (name = "delivery_address", nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Delivery Address", required = false, readOnly = false, description = "Address where the order should be delivered",
			hidden = false, maxLength = 500
	)
	private String deliveryAddress;
	@Column (name = "delivery_date", nullable = true)
	@AMetaData (displayName = "Delivery Date", required = false, readOnly = false, description = "Actual or planned delivery date", hidden = false)
	private LocalDate deliveryDate;
	// Order Type and Classification
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = false)
	@AMetaData (
			displayName = "Order Type", required = true, readOnly = false,
			description = "Type category of the order (e.g., Purchase Order, Service Order)", hidden = false, dataProviderBean = "COrderTypeService"
	)
	private COrderType entityType;
	@Column (name = "estimated_cost", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.00", message = "Estimated cost must be positive")
	@DecimalMax (value = "99999999999.99", message = "Estimated cost cannot exceed 99,999,999,999.99")
	@AMetaData (
			displayName = "Estimated Cost", required = false, readOnly = false, defaultValue = "0.00", description = "Estimated cost of the order",
			hidden = false
	)
	private BigDecimal estimatedCost = BigDecimal.ZERO;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "order_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related entities linked to this corder", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// Date Management
	@Column (name = "order_date", nullable = false)
	@AMetaData (displayName = "Order Date", required = true, readOnly = false, description = "Date when the order was created", hidden = false)
	private LocalDate orderDate;
	// Additional Details
	@Column (name = "order_number", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Order Number", required = false, readOnly = false, description = "External order reference number", hidden = false,
			maxLength = 50
	)
	private String orderNumber;
	// Provider Information
	@Column (name = "provider_company_name", nullable = false, length = 200)
	@Size (max = 200)
	@AMetaData (
			displayName = "Provider Company", required = true, readOnly = false, description = "Name of the company providing goods or services",
			hidden = false, maxLength = 200
	)
	private String providerCompanyName;
	@Column (name = "provider_contact_name", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Provider Contact", required = false, readOnly = false, description = "Contact person at the provider company",
			hidden = false, maxLength = 100
	)
	private String providerContactName;
	@Column (name = "provider_email", nullable = true, length = 150)
	@Size (max = 150)
	@AMetaData (
			displayName = "Provider Email", required = false, readOnly = false, description = "Email address of the provider contact", hidden = false,
			maxLength = 150
	)
	private String providerEmail;
	// Requestor and Responsibility
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "requestor_id", nullable = false)
	@AMetaData (
			displayName = "Requestor", required = true, readOnly = false, description = "User who requested this order", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser requestor;
	@Column (name = "required_date", nullable = true)
	@AMetaData (
			displayName = "Required Date", required = false, readOnly = false, description = "Date when the order is required to be completed",
			hidden = false
	)
	private LocalDate requiredDate;

	public COrder() {
		super();
		initializeDefaults();
	}

	/** Constructor with name and project.
	 * @param name    the name/description of the order
	 * @param project the project this order belongs to */
	public COrder(final String name, final CProject<?> project) {
		super(COrder.class, name, project);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		orderDate = LocalDate.now();
		requiredDate = LocalDate.now().plusDays(7); // Default to one week from now
		actualCost = BigDecimal.ZERO;
		estimatedCost = BigDecimal.ZERO;
		approvals = new ArrayList<>();
		attachments = new HashSet<>();
		comments = new HashSet<>();
		links = new HashSet<>();
	}

	/** Add an approval to this order.
	 * @param approval the approval to add */
	public void addApproval(final COrderApproval approval) {
		Check.notNull(approval, "Approval cannot be null");
		approvals.add(approval);
		approval.setOrder(this);
		updateLastModified();
	}

	public BigDecimal getActualCost() { return actualCost; }

	public List<COrderApproval> getApprovals() { return approvals; }

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

	public CCurrency getCurrency() { return currency; }

	public String getDeliveryAddress() { return deliveryAddress; }

	public LocalDate getDeliveryDate() { return deliveryDate; }

	/** Gets the end date for Gantt chart display. For orders, this is the delivery date.
	 * @return the delivery date */
	@Override
	public LocalDate getEndDate() { return deliveryDate; }

	// Getters and setters
	@Override
	public COrderType getEntityType() { return entityType; }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	/** Gets the icon for Gantt chart display.
	 * @return the order icon identifier */
	@Override
	public String getIconString() { return DEFAULT_ICON; }

	@Override
	public Set<CLink> getLinks() {
		if (links == null) {
			links = new HashSet<>();
		}
		return links;
	}

	public LocalDate getOrderDate() { return orderDate; }

	public String getOrderNumber() { return orderNumber; }

	public String getProviderCompanyName() { return providerCompanyName; }

	public String getProviderContactName() { return providerContactName; }

	public String getProviderEmail() { return providerEmail; }

	public CUser getRequestor() { return requestor; }

	public LocalDate getRequiredDate() { return requiredDate; }

	/** Gets the start date for Gantt chart display. For orders, this is the order date.
	 * @return the order date */
	@Override
	public LocalDate getStartDate() { return orderDate; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	/** Remove an approval from this order.
	 * @param approval the approval to remove */
	public void removeApproval(final COrderApproval approval) {
		Check.notNull(approval, "Approval cannot be null");
		approvals.remove(approval);
		approval.setOrder(null);
		updateLastModified();
	}

	public void setActualCost(final BigDecimal actualCost) {
		this.actualCost = actualCost;
		updateLastModified();
	}

	public void setApprovals(final List<COrderApproval> approvals) {
		this.approvals = approvals;
		updateLastModified();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setCurrency(final CCurrency currency) { this.currency = currency; }

	public void setDeliveryAddress(final String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
		updateLastModified();
	}

	public void setDeliveryDate(final LocalDate deliveryDate) {
		this.deliveryDate = deliveryDate;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, COrderType.class, "Type entity must be an instance of COrderType");
		Check.notNull(getProject(), "Project must be set before assigning order type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning order type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning order type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match order project company id " + getProject().getCompany().getId());
		entityType = (COrderType) typeEntity;
		updateLastModified();
	}

	public void setEstimatedCost(final BigDecimal estimatedCost) {
		this.estimatedCost = estimatedCost;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setOrderDate(final LocalDate orderDate) {
		this.orderDate = orderDate;
		updateLastModified();
	}

	public void setOrderNumber(final String orderNumber) {
		this.orderNumber = orderNumber;
		updateLastModified();
	}

	public void setProviderCompanyName(final String providerCompanyName) {
		this.providerCompanyName = providerCompanyName;
		updateLastModified();
	}

	public void setProviderContactName(final String providerContactName) {
		this.providerContactName = providerContactName;
		updateLastModified();
	}

	public void setProviderEmail(final String providerEmail) {
		this.providerEmail = providerEmail;
		updateLastModified();
	}

	public void setRequestor(final CUser requestor) {
		this.requestor = requestor;
		updateLastModified();
	}

	public void setRequiredDate(final LocalDate requiredDate) {
		this.requiredDate = requiredDate;
		updateLastModified();
	}
}
