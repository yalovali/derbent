package tech.derbent.plm.agile.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.agileparentrelation.domain.CAgileParentRelation;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.gannt.ganntitem.service.IGanntEntityItem;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.sprints.domain.CSprintItem;

@MappedSuperclass
public abstract class CAgileEntity<EntityClass extends CAgileEntity<EntityClass, TypeClass>, TypeClass extends CTypeEntity<?>>
		extends CProjectItem<EntityClass> implements IHasStatusAndWorkflow<EntityClass>, IGanntEntityItem, ISprintableItem, IHasIcon, IHasAttachments,
		IHasComments, IHasLinks, IHasAgileParentRelation {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileEntity.class);
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Acceptance Criteria", required = false, readOnly = false, defaultValue = "",
			description = "Criteria that must be met for the item to be considered complete", hidden = false, maxLength = 2000
	)
	private String acceptanceCriteria;
	@Column (nullable = true, precision = 12, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual cost must be positive")
	@DecimalMax (value = "999999.99", message = "Actual cost cannot exceed 999999.99")
	@AMetaData (
			displayName = "Actual Cost", required = false, readOnly = false, defaultValue = "0.00", description = "Actual cost spent on this item",
			hidden = false
	)
	private BigDecimal actualCost = BigDecimal.ZERO;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Actual hours must be positive")
	@DecimalMax (value = "9999.99", message = "Actual hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Actual Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Actual time spent on this item in hours", hidden = false
	)
	private BigDecimal actualHours = BigDecimal.ZERO;
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "agile_parent_relation_id", nullable = false)
	@NotNull (message = "Agile parent relation is required for agile hierarchy")
	@AMetaData (
			displayName = "Agile Parent Relation", required = true, readOnly = true, description = "Agile hierarchy tracking for this item",
			hidden = true
	)
	private CAgileParentRelation agileParentRelation;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "agile_item_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this item", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "agile_item_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this item", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "completion_date", nullable = true)
	@AMetaData (displayName = "Completion Date", required = false, readOnly = true, description = "Actual completion date", hidden = false)
	private LocalDate completionDate;
	@AMetaData (
			displayName = "Component Widget", required = false, readOnly = false, description = "Component Widget for item", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComponentWidget"
	)
	private final CComponentWidgetEntity<EntityClass> componentWidget = null;
	@Column (nullable = true)
	@AMetaData (displayName = "Due Date", required = false, readOnly = false, description = "Expected completion date", hidden = false)
	private LocalDate dueDate;
	// Concrete entities define their own type field/metadata
	@Column (nullable = true, precision = 12, scale = 2)
	@DecimalMin (value = "0.0", message = "Estimated cost must be positive")
	@DecimalMax (value = "999999.99", message = "Estimated cost cannot exceed 999999.99")
	@AMetaData (
			displayName = "Estimated Cost", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated cost to complete this item", hidden = false
	)
	private BigDecimal estimatedCost;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Estimated hours must be positive")
	@DecimalMax (value = "9999.99", message = "Estimated hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Estimated Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated time in hours to complete this item", hidden = false
	)
	private BigDecimal estimatedHours;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Hourly rate must be positive")
	@DecimalMax (value = "9999.99", message = "Hourly rate cannot exceed 9999.99")
	@AMetaData (
			displayName = "Hourly Rate", required = false, readOnly = false, defaultValue = "0.00", description = "Hourly rate for cost calculation",
			hidden = false
	)
	private BigDecimal hourlyRate;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "agile_item_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Links to other entities", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Notes", required = false, readOnly = false, defaultValue = "", description = "Additional notes and comments",
			hidden = false, maxLength = 2000
	)
	private String notes;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "cactivitypriority_id", nullable = true)
	@AMetaData (
			displayName = "Priority", required = false, readOnly = false, description = "Priority level", hidden = false,
			dataProviderBean = "CActivityPriorityService", setBackgroundFromColor = true, useIcon = true
	)
	private CActivityPriority priority;
	@Column (nullable = true)
	@Min (value = 0, message = "Progress percentage must be between 0 and 100")
	@Max (value = 100, message = "Progress percentage must be between 0 and 100")
	@AMetaData (
			displayName = "Progress %", required = false, readOnly = false, defaultValue = "0", description = "Completion percentage (0-100)",
			hidden = false
	)
	private Integer progressPercentage = 0;
	@Column (nullable = true, precision = 10, scale = 2)
	@DecimalMin (value = "0.0", message = "Remaining hours must be positive")
	@DecimalMax (value = "9999.99", message = "Remaining hours cannot exceed 9999.99")
	@AMetaData (
			displayName = "Remaining Hours", required = false, readOnly = false, defaultValue = "0.00",
			description = "Estimated remaining time in hours", hidden = false
	)
	private BigDecimal remainingHours;
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Results", required = false, readOnly = false, defaultValue = "", description = "Results and outcomes of the item",
			hidden = false, maxLength = 2000
	)
	private String results;
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "sprintitem_id", nullable = false)
	@NotNull (message = "Sprint item is required for progress tracking")
	@AMetaData (displayName = "Sprint Item", required = true, readOnly = true, description = "Progress tracking for this item", hidden = true)
	private CSprintItem sprintItem;
	@Column (name = "sprint_order", nullable = true)
	@Min (value = 1, message = "Sprint order must be positive")
	@AMetaData (
			displayName = "Sprint Order", required = false, readOnly = false,
			description = "Display order within sprint and backlog views (assigned automatically)", hidden = true
	)
	private Integer sprintOrder;
	@Column (nullable = true)
	@AMetaData (
			displayName = "Start Date", required = false, readOnly = false, description = "Planned or actual start date of the item", hidden = false
	)
	private LocalDate startDate;
	@Column (nullable = true)
	@AMetaData (
			displayName = "Story Points", required = false, readOnly = false, defaultValue = "0",
			description = "Estimated effort or complexity in story points", hidden = false
	)
	private Long storyPoint;

	protected CAgileEntity() {}

	protected CAgileEntity(final Class<EntityClass> clazz, final String name, final tech.derbent.api.projects.domain.CProject<?> project) {
		super(clazz, name, project);
		initializeDefaults();
	}

	@jakarta.persistence.PostLoad
	protected void ensureSprintItemParent() {
		if (sprintItem != null) {
			sprintItem.setParentItem(this);
		}
		if (agileParentRelation != null) {
			agileParentRelation.setOwnerItem(this);
		}
	}

	public String getAcceptanceCriteria() { return acceptanceCriteria; }

	public BigDecimal getActualCost() { return actualCost != null ? actualCost : BigDecimal.ZERO; }

	public BigDecimal getActualHours() { return actualHours != null ? actualHours : BigDecimal.ZERO; }

	@Override
	public CAgileParentRelation getAgileParentRelation() { return agileParentRelation; }

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public LocalDate getCompletionDate() { return completionDate; }

	public CComponentWidgetEntity<EntityClass> getComponentWidget() { return componentWidget; }

	public LocalDate getDueDate() { return dueDate; }

	@Override
	public CTypeEntity<?> getEntityType() { return getTypedEntityType(); }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	public BigDecimal getEstimatedHours() { return estimatedHours; }

	public BigDecimal getHourlyRate() { return hourlyRate; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public String getNotes() { return notes; }

	public CActivityPriority getPriority() { return priority; }

	@Override
	public Integer getProgressPercentage() {
		Check.notNull(sprintItem, "Sprint item must not be null");
		return sprintItem.getProgressPercentage();
	}

	public BigDecimal getRemainingHours() { return remainingHours; }

	public String getResults() { return results; }

	@Override
	public CSprintItem getSprintItem() { return sprintItem; }

	@Override
	public Integer getSprintOrder() { return sprintOrder; }

	@Override
	public LocalDate getStartDate() {
		Check.notNull(sprintItem, "Sprint item must not be null");
		return sprintItem.getStartDate();
	}

	@Override
	public Long getStoryPoint() {
		Check.notNull(sprintItem, "Sprint item must not be null");
		return sprintItem.getStoryPoint();
	}

	public abstract TypeClass getTypedEntityType();

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(getEntityType(), "Entity type cannot be null when retrieving workflow");
		return getEntityType().getWorkflow();
	}

	private final void initializeDefaults() {
		estimatedHours = BigDecimal.ZERO;
		estimatedCost = BigDecimal.ZERO;
		remainingHours = BigDecimal.ZERO;
		hourlyRate = BigDecimal.ZERO;
		dueDate = LocalDate.now().plusDays(7);
		notes = "";
		results = "";
		sprintOrder = Integer.MAX_VALUE;
		storyPoint = 0L;
		completionDate = null;
		sprintItem = new CSprintItem(true);
		sprintItem.setParentItem(this);
		sprintItem.setStartDate(LocalDate.now());
		sprintItem.setStoryPoint(0L);
		agileParentRelation = new CAgileParentRelation(this);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public boolean matchesFilter(final String searchValue, final java.util.Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true;
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		if (fieldNames.remove("entityType") && getEntityType() != null && getEntityType().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		if (fieldNames.remove("priority") && getPriority() != null && getPriority().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
	}

	public void setAcceptanceCriteria(final String acceptanceCriteria) {
		this.acceptanceCriteria = acceptanceCriteria;
		updateLastModified();
	}

	public void setActualCost(final BigDecimal actualCost) {
		this.actualCost = actualCost != null ? actualCost : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setActualHours(final BigDecimal actualHours) {
		this.actualHours = actualHours != null ? actualHours : BigDecimal.ZERO;
		updateLastModified();
	}

	@Override
	public void setAgileParentRelation(final CAgileParentRelation agileParentRelation) { this.agileParentRelation = agileParentRelation; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setColor(final String color) { /* color derived from type */ }

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
		updateLastModified();
	}

	public void setCompletionDate(final LocalDate completionDate) {
		this.completionDate = completionDate;
		updateLastModified();
	}

	public void setDueDate(final LocalDate dueDate) {
		this.dueDate = dueDate;
		updateLastModified();
	}

	@Override
	@SuppressWarnings ("unchecked")
	public void setEntityType(final CTypeEntity<?> entityType) {
		setTypedEntityType((TypeClass) entityType);
	}

	public void setEstimatedCost(final BigDecimal estimatedCost) {
		this.estimatedCost = estimatedCost != null ? estimatedCost : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setEstimatedHours(final BigDecimal estimatedHours) {
		this.estimatedHours = estimatedHours != null ? estimatedHours : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setHourlyRate(final BigDecimal hourlyRate) {
		this.hourlyRate = hourlyRate != null ? hourlyRate : BigDecimal.ZERO;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) {
		this.links = links;
		updateLastModified();
	}

	public void setNotes(final String notes) {
		this.notes = notes;
		updateLastModified();
	}

	public void setPriority(final CActivityPriority priority) {
		this.priority = priority;
		updateLastModified();
	}

	public void setProgressPercentage(final Integer progressPercentage) {
		this.progressPercentage = progressPercentage;
		updateLastModified();
	}

	public void setRemainingHours(final BigDecimal remainingHours) {
		this.remainingHours = remainingHours != null ? remainingHours : BigDecimal.ZERO;
		updateLastModified();
	}

	public void setResults(final String results) {
		this.results = results;
		updateLastModified();
	}

	@Override
	public void setSprintItem(final CSprintItem sprintItem) { this.sprintItem = sprintItem; }

	@Override
	public void setSprintOrder(final Integer sprintOrder) {
		this.sprintOrder = sprintOrder;
		updateLastModified();
	}

	public void setStartDate(final LocalDate startDate) {
		this.startDate = startDate;
		updateLastModified();
	}

	@Override
	public void setStatus(final CProjectItemStatus status) {
		super.setStatus(status);
	}

	@Override
	public void setStoryPoint(final Long storyPoint) {
		this.storyPoint = storyPoint;
		updateLastModified();
	}

	protected abstract void setTypedEntityType(TypeClass entityType);
}
