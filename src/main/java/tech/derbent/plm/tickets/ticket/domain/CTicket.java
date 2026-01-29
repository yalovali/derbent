package tech.derbent.plm.tickets.ticket.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.components.component.domain.CProjectComponent;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.milestones.milestone.domain.CMilestone;
import tech.derbent.plm.products.product.domain.CProduct;
import tech.derbent.plm.products.productversion.domain.CProductVersion;
import tech.derbent.plm.tickets.servicedepartment.domain.CTicketServiceDepartment;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;

@Entity
@Table (name = "\"cticket\"")
@AttributeOverride (name = "id", column = @Column (name = "ticket_id"))
public class CTicket extends CProjectItem<CTicket> implements IHasStatusAndWorkflow<CTicket>, IHasAttachments, IHasComments, IHasLinks {

	public static final String DEFAULT_COLOR = "#3A5791"; // Darker blue - support items
	public static final String DEFAULT_ICON = "vaadin:ticket";
	public static final String ENTITY_TITLE_PLURAL = "Tickets";
	public static final String ENTITY_TITLE_SINGULAR = "Ticket";
	private static final Logger LOGGER = LoggerFactory.getLogger(CTicket.class);
	public static final String VIEW_NAME = "Ticket View";
	// ============================================================
	// TICKET IDENTITY FIELDS
	// ============================================================
	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable (
			name = "ticket_affected_versions", joinColumns = @JoinColumn (name = "ticket_id"),
			inverseJoinColumns = @JoinColumn (name = "productversion_id")
	)
	@AMetaData (
			displayName = "Affected Versions", required = false, readOnly = false, description = "Product versions affected by this ticket",
			hidden = false, dataProviderBean = "CProductVersionService"
	)
	private Set<CProductVersion> affectedVersions = new HashSet<>();
	// ============================================================
	// REQUEST METADATA FIELDS
	// ============================================================
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ticket_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this ticket", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ticket_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this ticket", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "component_id", nullable = true)
	@AMetaData (
			displayName = "Component", required = false, readOnly = false, description = "Component affected by this ticket", hidden = false,
			dataProviderBean = "CProjectComponentService"
	)
	private CProjectComponent component;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Context Information", required = false, readOnly = false, defaultValue = "",
			description = "Additional context and environment details", hidden = false, maxLength = 2000
	)
	private String contextInformation;
	// ============================================================
	// PRIORITY & URGENCY FIELDS
	// ============================================================
	@Enumerated (EnumType.STRING)
	@Column (name = "ticket_criticality", nullable = true, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Criticality", required = false, readOnly = false, defaultValue = "MEDIUM",
			description = "System impact and business criticality level", hidden = false, useRadioButtons = false
	)
	private ETicketCriticality criticality;
	@Column (name = "due_date", nullable = true)
	@AMetaData (displayName = "Due Date", required = false, readOnly = false, description = "Target date for ticket resolution", hidden = false)
	private LocalDate dueDate;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "duplicate_of_ticket_id", nullable = true)
	@AMetaData (
			displayName = "Duplicate Of", required = false, readOnly = false, description = "Reference to original ticket if this is a duplicate",
			hidden = false, dataProviderBean = "CTicketService"
	)
	private CTicket duplicateOf;
	// ============================================================
	// TIME TRACKING FIELDS
	// ============================================================
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Ticket Type", required = false, readOnly = false, description = "Type category of the ticket", hidden = false,
			dataProviderBean = "CTicketTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CTicketType entityType;
	@Column (nullable = true, length = 255)
	@Size (max = 255)
	@AMetaData (
			displayName = "External Reference", required = false, readOnly = false,
			description = "External ticket ID or reference number from integrated systems", hidden = false, maxLength = 255
	)
	private String externalReference;
	@Column (name = "initial_date", nullable = true)
	@AMetaData (
			displayName = "Initial Date", required = false, readOnly = false, description = "Date when ticket was initially reported", hidden = false
	)
	private LocalDate initialDate;
	@Column (name = "is_regression", nullable = true)
	@AMetaData (
			displayName = "Is Regression", required = false, readOnly = false, defaultValue = "false",
			description = "Flag indicating if this is a regression issue", hidden = false
	)
	private Boolean isRegression;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ticket_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related entities linked to this ticket", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Enumerated (EnumType.STRING)
	@Column (name = "ticket_origin", nullable = true, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Origin", required = false, readOnly = false, defaultValue = "WEB",
			description = "Channel through which ticket was created", hidden = false, useRadioButtons = false
	)
	private ETicketOrigin origin;
	// ============================================================
	// RESOLUTION FIELDS
	// ============================================================
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "planned_activity_id", nullable = true)
	@AMetaData (
			displayName = "Planned Activity", required = false, readOnly = false, description = "Activity planned to resolve this ticket",
			hidden = false, dataProviderBean = "CActivityService"
	)
	private CActivity plannedActivity;
	@Column (name = "planned_date", nullable = true)
	@AMetaData (displayName = "Planned Date", required = false, readOnly = false, description = "Date when work is planned to start", hidden = false)
	private LocalDate plannedDate;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "ticket_priority_id", nullable = true)
	@AMetaData (
			displayName = "Priority", required = false, readOnly = false, description = "Priority level for ticket processing", hidden = false,
			dataProviderBean = "CTicketPriorityService", setBackgroundFromColor = true, useIcon = true
	)
	private CTicketPriority priority;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "product_id", nullable = true)
	@AMetaData (
			displayName = "Product", required = false, readOnly = false, description = "Product affected by this ticket", hidden = false,
			dataProviderBean = "CProductService"
	)
	private CProduct product;
	// ============================================================
	// PLANNING FIELDS
	// ============================================================
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "requestor_id", nullable = true)
	@AMetaData (
			displayName = "Requestor", required = false, readOnly = false, description = "User who reported or requested this ticket", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser requestor;
	@Enumerated (EnumType.STRING)
	@Column (name = "ticket_resolution", nullable = true, length = 30, columnDefinition = "VARCHAR(30)")
	@AMetaData (
			displayName = "Resolution", required = false, readOnly = false, defaultValue = "NONE",
			description = "How the ticket was resolved or closed", hidden = false, useRadioButtons = false
	)
	private ETicketResolution resolution;
	@Column (name = "resolution_date", nullable = true)
	@AMetaData (displayName = "Resolution Date", required = false, readOnly = true, description = "Date when ticket was resolved", hidden = false)
	private LocalDate resolutionDate;
	// ============================================================
	// PRODUCT/COMPONENT FIELDS
	// ============================================================
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Result", required = false, readOnly = false, defaultValue = "", description = "Resolution details and outcome",
			hidden = false, maxLength = 2000
	)
	private String result;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "service_department_id", nullable = true)
	@AMetaData (
			displayName = "Service Department", required = false, readOnly = false,
			description = "Department responsible for this ticket (all responsible users will be notified)", hidden = false,
			dataProviderBean = "CTicketServiceDepartmentService", setBackgroundFromColor = true, useIcon = true
	)
	private CTicketServiceDepartment serviceDepartment;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "target_milestone_id", nullable = true)
	@AMetaData (
			displayName = "Target Milestone", required = false, readOnly = false, description = "Milestone for ticket resolution", hidden = false,
			dataProviderBean = "CMilestoneService"
	)
	private CMilestone targetMilestone;
	// ============================================================
	// TYPE AND WORKFLOW FIELDS
	// ============================================================
	@Enumerated (EnumType.STRING)
	@Column (name = "ticket_urgency", nullable = true, length = 20, columnDefinition = "VARCHAR(20)")
	@AMetaData (
			displayName = "Urgency", required = false, readOnly = false, defaultValue = "MEDIUM",
			description = "How quickly the ticket needs to be addressed", hidden = false, useRadioButtons = false
	)
	private ETicketUrgency urgency;
	// ============================================================
	// RELATIONSHIPS (Attachments, Comments, Links)
	// ============================================================
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Work hours estimated must be positive")
	@DecimalMax (value = "9999.99", message = "Work hours estimated cannot exceed 9999.99")
	@AMetaData (
			displayName = "Work Hours Estimated", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated time in hours to resolve this ticket", hidden = false
	)
	private BigDecimal workHoursEstimated;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Work hours left must be positive")
	@DecimalMax (value = "9999.99", message = "Work hours left cannot exceed 9999.99")
	@AMetaData (
			displayName = "Work Hours Left", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated remaining time in hours", hidden = false
	)
	private BigDecimal workHoursLeft;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Work hours real must be positive")
	@DecimalMax (value = "9999.99", message = "Work hours real cannot exceed 9999.99")
	@AMetaData (
			displayName = "Work Hours Real", required = false, readOnly = false, defaultValue = "0.00",
			description = "Actual time spent resolving this ticket", hidden = false
	)
	private BigDecimal workHoursReal;
	// ============================================================
	// CONSTRUCTORS
	// ============================================================

	protected CTicket() {
	}

	/** Constructor with name and project.
	 * @param name    the name of the ticket
	 * @param project the project this ticket belongs to */
	public CTicket(final String name, final CProject<?> project) {
		super(CTicket.class, name, project);
		initializeDefaults();
	}

	/** Constructor with name, project, and requestor.
	 * @param name      the name of the ticket
	 * @param project   the project this ticket belongs to
	 * @param requestor the user who requested this ticket */
	public CTicket(final String name, final CProject<?> project, final CUser requestor) {
		super(CTicket.class, name, project);
		initializeDefaults();
		setRequestor(requestor);
	}
	// ============================================================
	// BUSINESS LOGIC METHODS
	// ============================================================

	/** Calculate work hours variance (real - estimated).
	 * @return the hours variance, positive if over estimate, negative if under */
	public BigDecimal calculateWorkHoursVariance() {
		if (!(workHoursReal == null || workHoursEstimated == null)) {
			return workHoursReal.subtract(workHoursEstimated);
		}
		LOGGER.debug("calculateWorkHoursVariance() - Missing data, real={}, estimated={}", workHoursReal, workHoursEstimated);
		return BigDecimal.ZERO;
	}

	/** Copies ticket fields to target using copyField pattern. Override to add more fields. Always call super.copyEntityTo() first!
	 * @param target        The target entity
	 * @param serviceTarget The service for the target entity
	 * @param options       Clone options */

	public Set<CProductVersion> getAffectedVersions() { return affectedVersions; }

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }
	// ============================================================
	// GETTERS AND SETTERS
	// ============================================================

	public CProjectComponent getComponent() { return component; }

	public String getContextInformation() { return contextInformation; }

	public ETicketCriticality getCriticality() { return criticality; }

	public LocalDate getDueDate() { return dueDate; }

	public CTicket getDuplicateOf() { return duplicateOf; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	public String getExternalReference() { return externalReference; }

	public LocalDate getInitialDate() { return initialDate; }

	public Boolean getIsRegression() { return isRegression; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public ETicketOrigin getOrigin() { return origin; }

	public CActivity getPlannedActivity() { return plannedActivity; }

	public LocalDate getPlannedDate() { return plannedDate; }

	public CTicketPriority getPriority() { return priority; }

	public CProduct getProduct() { return product; }

	public CUser getRequestor() { return requestor; }

	public ETicketResolution getResolution() { return resolution; }

	public LocalDate getResolutionDate() { return resolutionDate; }

	public String getResult() { return result; }

	public CTicketServiceDepartment getServiceDepartment() { return serviceDepartment; }

	public CMilestone getTargetMilestone() { return targetMilestone; }

	public ETicketUrgency getUrgency() { return urgency; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	public BigDecimal getWorkHoursEstimated() { return workHoursEstimated != null ? workHoursEstimated : BigDecimal.ZERO; }

	public BigDecimal getWorkHoursLeft() { return workHoursLeft != null ? workHoursLeft : BigDecimal.ZERO; }

	public BigDecimal getWorkHoursReal() { return workHoursReal != null ? workHoursReal : BigDecimal.ZERO; }

	private final void initializeDefaults() {
		// Initialize numeric fields to zero
		workHoursEstimated = BigDecimal.ZERO;
		workHoursReal = BigDecimal.ZERO;
		workHoursLeft = BigDecimal.ZERO;
		// Initialize boolean fields
		isRegression = false;
		// Initialize enum defaults
		origin = ETicketOrigin.WEB;
		urgency = ETicketUrgency.MEDIUM;
		criticality = ETicketCriticality.MEDIUM;
		resolution = ETicketResolution.NONE;
		// Initialize date fields
		initialDate = LocalDate.now();
		// Initialize collections
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if the ticket is overdue.
	 * @return true if the due date has passed and the ticket is not resolved */
	public boolean isOverdue() {
		if (dueDate == null || isResolved()) {
			return false;
		}
		final boolean overdue = LocalDate.now().isAfter(dueDate);
		LOGGER.debug("isOverdue() - Ticket id={} overdue={} (dueDate={}, today={})", getId(), overdue, dueDate, LocalDate.now());
		return overdue;
	}

	public boolean isRegression() { return Boolean.TRUE.equals(isRegression); }

	/** Check if the ticket is resolved.
	 * @return true if the ticket has a resolution date or resolution status */
	public boolean isResolved() {
		final boolean hasResolutionDate = resolutionDate != null;
		final boolean hasResolution = resolution != null && resolution != ETicketResolution.NONE;
		final boolean isFinalStatus = status != null && status.getFinalStatus();
		final boolean resolved = hasResolutionDate || hasResolution || isFinalStatus;
		LOGGER.debug("isResolved() - Ticket id={} resolved={} (resolutionDate={}, resolution={}, finalStatus={})", getId(), resolved,
				hasResolutionDate, resolution, isFinalStatus);
		return resolved;
	}

	public void setAffectedVersions(final Set<CProductVersion> affectedVersions) {
		this.affectedVersions = affectedVersions;
		updateLastModified();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setComponent(final CProjectComponent component) {
		this.component = component;
		updateLastModified();
	}

	public void setContextInformation(final String contextInformation) {
		this.contextInformation = contextInformation;
		updateLastModified();
	}

	public void setCriticality(final ETicketCriticality criticality) {
		this.criticality = criticality;
		updateLastModified();
	}

	public void setDueDate(final LocalDate dueDate) {
		this.dueDate = dueDate;
		updateLastModified();
	}

	public void setDuplicateOf(final CTicket duplicateOf) {
		this.duplicateOf = duplicateOf;
		updateLastModified();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CTicketType.class, "Type entity must be an instance of CTicketType");
		Check.notNull(getProject(), "Project must be set before assigning ticket type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning ticket type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning ticket type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match ticket project company id " + getProject().getCompany().getId());
		entityType = (CTicketType) typeEntity;
		updateLastModified();
	}

	public void setExternalReference(final String externalReference) {
		this.externalReference = externalReference;
		updateLastModified();
	}

	public void setInitialDate(final LocalDate initialDate) {
		this.initialDate = initialDate;
		updateLastModified();
	}

	public void setIsRegression(final Boolean isRegression) {
		this.isRegression = isRegression;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setOrigin(final ETicketOrigin origin) {
		this.origin = origin;
		updateLastModified();
	}

	public void setPlannedActivity(final CActivity plannedActivity) {
		this.plannedActivity = plannedActivity;
		updateLastModified();
	}

	public void setPlannedDate(final LocalDate plannedDate) {
		this.plannedDate = plannedDate;
		updateLastModified();
	}

	public void setPriority(final CTicketPriority priority) {
		this.priority = priority;
		updateLastModified();
	}

	public void setProduct(final CProduct product) {
		this.product = product;
		updateLastModified();
	}

	public void setRequestor(final CUser requestor) {
		this.requestor = requestor;
		updateLastModified();
	}

	public void setResolution(final ETicketResolution resolution) {
		this.resolution = resolution;
		// Auto-set resolution date if resolution is not NONE
		if (resolution != null && resolution != ETicketResolution.NONE && resolutionDate == null) {
			resolutionDate = LocalDate.now();
		}
		updateLastModified();
	}

	public void setResolutionDate(final LocalDate resolutionDate) {
		this.resolutionDate = resolutionDate;
		updateLastModified();
	}

	public void setResult(final String result) {
		this.result = result;
		updateLastModified();
	}

	public void setServiceDepartment(final CTicketServiceDepartment serviceDepartment) {
		this.serviceDepartment = serviceDepartment;
		updateLastModified();
	}

	public void setTargetMilestone(final CMilestone targetMilestone) {
		this.targetMilestone = targetMilestone;
		updateLastModified();
	}

	public void setUrgency(final ETicketUrgency urgency) {
		this.urgency = urgency;
		updateLastModified();
	}

	public void setWorkHoursEstimated(final BigDecimal workHoursEstimated) {
		if (workHoursEstimated != null && workHoursEstimated.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setWorkHoursEstimated - Attempting to set negative hours: {} for ticket id={}", workHoursEstimated, getId());
		}
		this.workHoursEstimated = workHoursEstimated != null ? workHoursEstimated : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setWorkHoursLeft(final BigDecimal workHoursLeft) {
		if (workHoursLeft != null && workHoursLeft.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setWorkHoursLeft - Attempting to set negative hours: {} for ticket id={}", workHoursLeft, getId());
		}
		this.workHoursLeft = workHoursLeft != null ? workHoursLeft : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setWorkHoursReal(final BigDecimal workHoursReal) {
		if (workHoursReal != null && workHoursReal.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setWorkHoursReal - Attempting to set negative hours: {} for ticket id={}", workHoursReal, getId());
		}
		this.workHoursReal = workHoursReal != null ? workHoursReal : BigDecimal.ZERO;
		updateLastModified();
	}
}
