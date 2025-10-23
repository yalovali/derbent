package tech.derbent.app.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;

@Entity
@Table (name = "corder")
@AttributeOverride (name = "id", column = @Column (name = "order_id"))
public class COrder extends CEntityOfProject<COrder> {

	public static final String DEFAULT_COLOR = "#fd7e14";
	public static final String DEFAULT_ICON = "vaadin:invoice";
	public static final String VIEW_NAME = "Orders View";
	@Column (name = "actual_cost", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.00", message = "Actual cost must be positive")
	@DecimalMax (value = "99999999999.99", message = "Actual cost cannot exceed 99,999,999,999.99")
	@AMetaData (
			displayName = "Actual Cost", required = false, readOnly = false, defaultValue = "0.00", description = "Actual cost of the order",
			hidden = false, order = 42
	)
	private BigDecimal actualCost = BigDecimal.ZERO;
	// Approval Management (One-to-Many relationship)
	@OneToMany (mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@AMetaData (
			displayName = "Approvals", required = false, readOnly = true, description = "Approval records for this order", hidden = false, order = 70
	)
	private List<COrderApproval> approvals = new ArrayList<>();
	// Financial Information
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "currency_id", nullable = false)
	@AMetaData (
			displayName = "Currency", required = true, readOnly = false, description = "Currency for the order cost", hidden = false, order = 40,
			dataProviderBean = "CCurrencyService"
	)
	private CCurrency currency;
	@Column (name = "delivery_address", nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Delivery Address", required = false, readOnly = false, description = "Address where the order should be delivered",
			hidden = false, order = 61, maxLength = 500
	)
	private String deliveryAddress;
	@Column (name = "delivery_date", nullable = true)
	@AMetaData (
			displayName = "Delivery Date", required = false, readOnly = false, description = "Actual or planned delivery date", hidden = false,
			order = 52
	)
	private LocalDate deliveryDate;
	@Column (name = "estimated_cost", nullable = true, precision = 15, scale = 2)
	@DecimalMin (value = "0.00", message = "Estimated cost must be positive")
	@DecimalMax (value = "99999999999.99", message = "Estimated cost cannot exceed 99,999,999,999.99")
	@AMetaData (
			displayName = "Estimated Cost", required = false, readOnly = false, defaultValue = "0.00", description = "Estimated cost of the order",
			hidden = false, order = 41
	)
	private BigDecimal estimatedCost = BigDecimal.ZERO;
	// Date Management
	@Column (name = "order_date", nullable = false)
	@AMetaData (
			displayName = "Order Date", required = true, readOnly = false, description = "Date when the order was created", hidden = false, order = 50
	)
	private LocalDate orderDate;
	// Additional Details
	@Column (name = "order_number", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Order Number", required = false, readOnly = false, description = "External order reference number", hidden = false,
			order = 60, maxLength = 50
	)
	private String orderNumber;
	// Order Type and Classification
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "order_type_id", nullable = false)
	@AMetaData (
			displayName = "Order Type", required = true, readOnly = false,
			description = "Type category of the order (e.g., Purchase Order, Service Order)", hidden = false, order = 2,
			dataProviderBean = "COrderTypeService"
	)
	private COrderType orderType;
	// Provider Information
	@Column (name = "provider_company_name", nullable = false, length = 200)
	@Size (max = 200)
	@AMetaData (
			displayName = "Provider Company", required = true, readOnly = false, description = "Name of the company providing goods or services",
			hidden = false, order = 10, maxLength = 200
	)
	private String providerCompanyName;
	@Column (name = "provider_contact_name", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Provider Contact", required = false, readOnly = false, description = "Contact person at the provider company",
			hidden = false, order = 11, maxLength = 100
	)
	private String providerContactName;
	@Column (name = "provider_email", nullable = true, length = 150)
	@Size (max = 150)
	@AMetaData (
			displayName = "Provider Email", required = false, readOnly = false, description = "Email address of the provider contact", hidden = false,
			order = 12, maxLength = 150
	)
	private String providerEmail;
	// Requestor and Responsibility
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "requestor_id", nullable = false)
	@AMetaData (
			displayName = "Requestor", required = true, readOnly = false, description = "User who requested this order", hidden = false, order = 20,
			dataProviderBean = "CUserService"
	)
	private CUser requestor;
	@Column (name = "required_date", nullable = true)
	@AMetaData (
			displayName = "Required Date", required = false, readOnly = false, description = "Date when the order is required to be completed",
			hidden = false, order = 51
	)
	private LocalDate requiredDate;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "responsible_id", nullable = true)
	@AMetaData (
			displayName = "Responsible", required = false, readOnly = false, description = "User responsible for managing this order", hidden = false,
			order = 21, dataProviderBean = "CUserService"
	)
	private CUser responsible;
	// Status Management
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "order_status_id", nullable = false)
	@AMetaData (
			displayName = "Status", required = true, readOnly = false, description = "Current status of the order", hidden = false, order = 30,
			dataProviderBean = "CProjectItemStatusService"
	)
	private CProjectItemStatus status;

	public COrder() {
		super();
		orderDate = LocalDate.now();
	}

	/** Constructor with name and project.
	 * @param name    the name/description of the order
	 * @param project the project this order belongs to */
	public COrder(final String name, final CProject project) {
		super(COrder.class, name, project);
		orderDate = LocalDate.now();
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

	public CCurrency getCurrency() { return currency; }

	public String getDeliveryAddress() { return deliveryAddress; }

	public LocalDate getDeliveryDate() { return deliveryDate; }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	public LocalDate getOrderDate() { return orderDate; }

	public String getOrderNumber() { return orderNumber; }

	// Getters and setters
	public COrderType getOrderType() { return orderType; }

	public String getProviderCompanyName() { return providerCompanyName; }

	public String getProviderContactName() { return providerContactName; }

	public String getProviderEmail() { return providerEmail; }

	public CUser getRequestor() { return requestor; }

	public LocalDate getRequiredDate() { return requiredDate; }

	public CUser getResponsible() { return responsible; }

	public CProjectItemStatus getStatus() { return status; }

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

	public void setCurrency(final CCurrency currency) {
		this.currency = currency;
		updateLastModified();
	}

	public void setDeliveryAddress(final String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
		updateLastModified();
	}

	public void setDeliveryDate(final LocalDate deliveryDate) {
		this.deliveryDate = deliveryDate;
		updateLastModified();
	}

	public void setEstimatedCost(final BigDecimal estimatedCost) {
		this.estimatedCost = estimatedCost;
		updateLastModified();
	}

	public void setOrderDate(final LocalDate orderDate) {
		this.orderDate = orderDate;
		updateLastModified();
	}

	public void setOrderNumber(final String orderNumber) {
		this.orderNumber = orderNumber;
		updateLastModified();
	}

	public void setOrderType(final COrderType orderType) {
		this.orderType = orderType;
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

	public void setResponsible(final CUser responsible) {
		this.responsible = responsible;
		updateLastModified();
	}

	public void setStatus(final CProjectItemStatus status) {
		this.status = status;
		updateLastModified();
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships
		if (currency != null) {
			currency.getName(); // Trigger currency loading
		}
		if (orderType != null) {
			orderType.getName(); // Trigger order type loading
		}
		if (requestor != null) {
			requestor.getLogin(); // Trigger requestor loading
		}
		if (responsible != null) {
			responsible.getLogin(); // Trigger responsible user loading
		}
		if (status != null) {
			status.getName(); // Trigger status loading
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
		// Note: approvals collection will be initialized if accessed
	}
}
