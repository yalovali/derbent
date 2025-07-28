package tech.derbent.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * COrder - Domain entity representing orders in the company system.
 * Layer: Domain (MVC)
 * 
 * Comprehensive order management entity that tracks all aspects of company orders
 * including type, project association, provider details, financial information,
 * responsible parties, and approval workflow.
 * 
 * This entity extends CEntityOfProject to inherit project association and standard
 * audit fields, with comprehensive order-specific fields using MetaData annotations
 * for automatic form generation.
 * 
 * Key Features:
 * - Project association (via CEntityOfProject)
 * - Provider company and contact management
 * - Financial tracking with currency and cost information
 * - Multi-level approval workflow
 * - Status tracking and responsibility assignment
 * - Full audit trail and descriptive text
 */
@Entity
@Table(name = "corder")
@AttributeOverride(name = "id", column = @Column(name = "order_id"))
public class COrder extends CEntityOfProject {

    private static final Logger LOGGER = LoggerFactory.getLogger(COrder.class);

    // Order Type and Classification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_type_id", nullable = false)
    @MetaData(
        displayName = "Order Type", required = true, readOnly = false,
        description = "Type category of the order (e.g., Purchase Order, Service Order)", 
        hidden = false, order = 2,
        dataProviderBean = "COrderTypeService"
    )
    private COrderType orderType;

    // Provider Information
    @Column(name = "provider_company_name", nullable = false, length = 200)
    @Size(max = 200)
    @MetaData(
        displayName = "Provider Company", required = true, readOnly = false,
        description = "Name of the company providing goods or services", 
        hidden = false, order = 10, maxLength = 200
    )
    private String providerCompanyName;

    @Column(name = "provider_contact_name", nullable = true, length = 100)
    @Size(max = 100)
    @MetaData(
        displayName = "Provider Contact", required = false, readOnly = false,
        description = "Contact person at the provider company", 
        hidden = false, order = 11, maxLength = 100
    )
    private String providerContactName;

    @Column(name = "provider_email", nullable = true, length = 150)
    @Size(max = 150)
    @MetaData(
        displayName = "Provider Email", required = false, readOnly = false,
        description = "Email address of the provider contact", 
        hidden = false, order = 12, maxLength = 150
    )
    private String providerEmail;

    // Requestor and Responsibility
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id", nullable = false)
    @MetaData(
        displayName = "Requestor", required = true, readOnly = false,
        description = "User who requested this order", 
        hidden = false, order = 20,
        dataProviderBean = "CUserService"
    )
    private CUser requestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id", nullable = true)
    @MetaData(
        displayName = "Responsible", required = false, readOnly = false,
        description = "User responsible for managing this order", 
        hidden = false, order = 21,
        dataProviderBean = "CUserService"
    )
    private CUser responsible;

    // Status Management
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_status_id", nullable = false)
    @MetaData(
        displayName = "Status", required = true, readOnly = false,
        description = "Current status of the order", 
        hidden = false, order = 30,
        dataProviderBean = "COrderStatusService"
    )
    private COrderStatus status;

    // Financial Information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    @MetaData(
        displayName = "Currency", required = true, readOnly = false,
        description = "Currency for the order cost", 
        hidden = false, order = 40,
        dataProviderBean = "CCurrencyService"
    )
    private CCurrency currency;

    @Column(name = "estimated_cost", nullable = true, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Estimated cost must be positive")
    @DecimalMax(value = "99999999999.99", message = "Estimated cost cannot exceed 99,999,999,999.99")
    @MetaData(
        displayName = "Estimated Cost", required = false, readOnly = false,
        defaultValue = "0.00",
        description = "Estimated cost of the order", 
        hidden = false, order = 41
    )
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(name = "actual_cost", nullable = true, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Actual cost must be positive")
    @DecimalMax(value = "99999999999.99", message = "Actual cost cannot exceed 99,999,999,999.99")
    @MetaData(
        displayName = "Actual Cost", required = false, readOnly = false,
        defaultValue = "0.00",
        description = "Actual cost of the order", 
        hidden = false, order = 42
    )
    private BigDecimal actualCost = BigDecimal.ZERO;

    // Date Management
    @Column(name = "order_date", nullable = false)
    @MetaData(
        displayName = "Order Date", required = true, readOnly = false,
        description = "Date when the order was created", 
        hidden = false, order = 50
    )
    private LocalDate orderDate;

    @Column(name = "required_date", nullable = true)
    @MetaData(
        displayName = "Required Date", required = false, readOnly = false,
        description = "Date when the order is required to be completed", 
        hidden = false, order = 51
    )
    private LocalDate requiredDate;

    @Column(name = "delivery_date", nullable = true)
    @MetaData(
        displayName = "Delivery Date", required = false, readOnly = false,
        description = "Actual or planned delivery date", 
        hidden = false, order = 52
    )
    private LocalDate deliveryDate;

    // Additional Details
    @Column(name = "order_number", nullable = true, length = 50)
    @Size(max = 50)
    @MetaData(
        displayName = "Order Number", required = false, readOnly = false,
        description = "External order reference number", 
        hidden = false, order = 60, maxLength = 50
    )
    private String orderNumber;

    @Column(name = "delivery_address", nullable = true, length = 500)
    @Size(max = 500)
    @MetaData(
        displayName = "Delivery Address", required = false, readOnly = false,
        description = "Address where the order should be delivered", 
        hidden = false, order = 61, maxLength = 500
    )
    private String deliveryAddress;

    // Approval Management (One-to-Many relationship)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MetaData(
        displayName = "Approvals", required = false, readOnly = true,
        description = "Approval records for this order", 
        hidden = false, order = 70
    )
    private List<COrderApproval> approvals = new ArrayList<>();

    /**
     * Default constructor for JPA.
     */
    public COrder() {
        super();
        this.orderDate = LocalDate.now();
        LOGGER.debug("COrder default constructor called");
    }

    /**
     * Constructor with name and project.
     * 
     * @param name the name/description of the order
     * @param project the project this order belongs to
     */
    public COrder(final String name, final CProject project) {
        super(name, project);
        this.orderDate = LocalDate.now();
        LOGGER.debug("COrder constructor called with name: {} and project: {}", name, project);
    }

    // Getters and setters
    public COrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(final COrderType orderType) {
        this.orderType = orderType;
        updateLastModified();
    }

    public String getProviderCompanyName() {
        return providerCompanyName;
    }

    public void setProviderCompanyName(final String providerCompanyName) {
        this.providerCompanyName = providerCompanyName;
        updateLastModified();
    }

    public String getProviderContactName() {
        return providerContactName;
    }

    public void setProviderContactName(final String providerContactName) {
        this.providerContactName = providerContactName;
        updateLastModified();
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public void setProviderEmail(final String providerEmail) {
        this.providerEmail = providerEmail;
        updateLastModified();
    }

    public CUser getRequestor() {
        return requestor;
    }

    public void setRequestor(final CUser requestor) {
        this.requestor = requestor;
        updateLastModified();
    }

    public CUser getResponsible() {
        return responsible;
    }

    public void setResponsible(final CUser responsible) {
        this.responsible = responsible;
        updateLastModified();
    }

    public COrderStatus getStatus() {
        return status;
    }

    public void setStatus(final COrderStatus status) {
        this.status = status;
        updateLastModified();
    }

    public CCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(final CCurrency currency) {
        this.currency = currency;
        updateLastModified();
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(final BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
        updateLastModified();
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(final BigDecimal actualCost) {
        this.actualCost = actualCost;
        updateLastModified();
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(final LocalDate orderDate) {
        this.orderDate = orderDate;
        updateLastModified();
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(final LocalDate requiredDate) {
        this.requiredDate = requiredDate;
        updateLastModified();
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(final LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
        updateLastModified();
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
        updateLastModified();
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(final String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
        updateLastModified();
    }

    public List<COrderApproval> getApprovals() {
        return approvals;
    }

    public void setApprovals(final List<COrderApproval> approvals) {
        this.approvals = approvals;
        updateLastModified();
    }

    /**
     * Add an approval to this order.
     * 
     * @param approval the approval to add
     */
    public void addApproval(final COrderApproval approval) {
        if (approval != null) {
            approvals.add(approval);
            approval.setOrder(this);
            updateLastModified();
        }
    }

    /**
     * Remove an approval from this order.
     * 
     * @param approval the approval to remove
     */
    public void removeApproval(final COrderApproval approval) {
        if (approval != null) {
            approvals.remove(approval);
            approval.setOrder(null);
            updateLastModified();
        }
    }
}