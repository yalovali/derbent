package tech.derbent.app.decisions.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
import jakarta.validation.constraints.DecimalMin;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;
import tech.derbent.app.links.domain.CLink;
import tech.derbent.app.links.domain.IHasLinks;

/** CDecision - Domain entity representing project decisions with comprehensive management features. Layer: Domain (MVC) Supports: - Decision type
 * categorization - Cost estimation and tracking - Team collaboration and assignments - Multi-stage approval workflow - Accountable personnel
 * management - Descriptive documentation Follows the established patterns from CActivity for consistency. */
@Entity
@Table (name = "cdecision")
@AttributeOverride (name = "id", column = @Column (name = "decision_id"))
public class CDecision extends CProjectItem<CDecision> implements IHasStatusAndWorkflow<CDecision>, IHasAttachments, IHasComments, IHasLinks {

	public static final String DEFAULT_COLOR = "#91856C"; // OpenWindows Border Dark - authoritative decisions
	public static final String DEFAULT_ICON = "vaadin:gavel";
	public static final String ENTITY_TITLE_PLURAL = "Decisions";
	public static final String ENTITY_TITLE_SINGULAR = "Decision";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDecision.class);
	public static final String VIEW_NAME = "Decisions View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "decision_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Decision supporting documents", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "decision_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Discussion comments for this decision", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JoinColumn (name = "decision_id")
@AMetaData (
displayName = "Links", required = false, readOnly = false, description = "Related entities linked to this cdecision", hidden = false,
dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
)
private Set<CLink> links = new HashSet<>();
	// Decision Type Classification
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (displayName = "Decision Type", required = false, readOnly = false, description = "Category or type of the decision", hidden = false)
	private CDecisionType entityType;
	// Cost Estimation
	@Column (name = "estimated_cost", nullable = true, precision = 19, scale = 2)
	@DecimalMin (value = "0.0", inclusive = true)
	@AMetaData (
			displayName = "Estimated Cost", required = false, readOnly = false, description = "Estimated cost impact of the decision", hidden = false,
			min = 0.0
	)
	private BigDecimal estimatedCost;
	// Decision Implementation Date
	@Column (name = "implementation_date", nullable = true)
	@AMetaData (
			displayName = "Implementation Date", required = false, readOnly = false,
			description = "Date when the decision was or will be implemented", hidden = false
	)
	private LocalDateTime implementationDate;
	// Decision Review Date
	@Column (name = "review_date", nullable = true)
	@AMetaData (
			displayName = "Review Date", required = false, readOnly = false, description = "Date when the decision will be reviewed", hidden = false
	)
	private LocalDateTime reviewDate;

	/** Default constructor for JPA. */
	public CDecision() {
		super();
	}

	public CDecision(final String name, final CProject project) {
		super(CDecision.class, name, project);
	}

	@Override
	protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings ("rawtypes") CAbstractService serviceTarget,
			final CCloneOptions options) {
		super.copyEntityTo(target, serviceTarget, options);
		if (target instanceof final CDecision targetDecision) {
			// Copy basic fields
			copyField(this::getEstimatedCost, targetDecision::setEstimatedCost);
			copyField(this::getEntityType, targetDecision::setEntityType);
			// Conditional: dates
			if (!options.isResetDates()) {
				copyField(this::getImplementationDate, targetDecision::setImplementationDate);
				copyField(this::getReviewDate, targetDecision::setReviewDate);
			}
			LOGGER.debug("Successfully copied decision '{}' with options: {}", getName(), options);
		}
	}

	/** Creates a clone of this decision with the specified options. This implementation demonstrates the recursive cloning pattern: 1. Calls parent's
	 * createClone() to handle inherited fields 2. Clones decision-specific fields based on options 3. Returns the fully cloned decision
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the decision with cloned data
	 * @throws CloneNotSupportedException if cloning fails */
	@Override
	public CDecision createClone(final CCloneOptions options) throws Exception {
		// Get parent's clone (CProjectItem -> CEntityOfProject -> CEntityNamed -> CEntityDB)
		final CDecision clone = super.createClone(options);
		// Clone basic decision fields (always included)
		clone.estimatedCost = estimatedCost;
		// Clone decision type (not a date or assignment)
		clone.entityType = entityType;
		// Clone workflow if requested
		if (options.isCloneWorkflow() && getWorkflow() != null) {
			// Workflow is already handled by entityType, no additional cloning needed
		}
		// Handle date fields based on options
		if (!options.isResetDates()) {
			clone.implementationDate = implementationDate;
			clone.reviewDate = reviewDate;
		}
		// If resetDates is true, leave dates null
		// Clone comments if requested
		if (options.includesComments() && comments != null && !comments.isEmpty()) {
			clone.comments = new HashSet<>();
			for (final CComment comment : comments) {
				try {
					final CComment commentClone = comment.createClone(options);
					clone.comments.add(commentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone comment: {}", e.getMessage());
				}
			}
		}
		// Clone attachments if requested
		if (options.includesAttachments() && attachments != null && !attachments.isEmpty()) {
			clone.attachments = new HashSet<>();
			for (final CAttachment attachment : attachments) {
				try {
					final CAttachment attachmentClone = attachment.createClone(options);
					clone.attachments.add(attachmentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone attachment: {}", e.getMessage());
				}
			}
		}
		LOGGER.debug("Successfully cloned decision '{}' with options: {}", getName(), options);
		return clone;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CDecision)) {
			return false;
		}
		return super.equals(o);
	}

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

	/** Gets the end date for Gantt chart display. For decisions, this is the review date.
	 * @return the review date as LocalDate, or null if not set */
	@Override
	public LocalDate getEndDate() { return reviewDate != null ? reviewDate.toLocalDate() : null; }

	@Override
	public CDecisionType getEntityType() { return entityType; }

	public BigDecimal getEstimatedCost() { return estimatedCost; }

	/** Gets the icon for Gantt chart display.
	 * @return the decision icon identifier */
	@Override
	public String getIconString() { return DEFAULT_ICON; }

	public LocalDateTime getImplementationDate() { return implementationDate; }

	public LocalDateTime getReviewDate() { return reviewDate; }

	/** Gets the start date for Gantt chart display. For decisions, this is the implementation date.
	 * @return the implementation date as LocalDate, or null if not set */
	@Override
	public LocalDate getStartDate() { return implementationDate != null ? implementationDate.toLocalDate() : null; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }
@Override
public Set<CLink> getLinks() {
if (links == null) {
links = new HashSet<>();
}
eturn links;
}

@Override
public void setLinks(final Set<CLink> links) { this.links = links; }

	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CDecisionType.class, "Type entity must be an instance of CDecisionType");
		Check.notNull(getProject(), "Project must be set before assigning decision type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning decision type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning decision type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match decision project company id " + getProject().getCompany().getId());
		entityType = (CDecisionType) typeEntity;
		updateLastModified();
	}

	public void setEstimatedCost(final BigDecimal estimatedCost) {
		if (estimatedCost != null && estimatedCost.compareTo(BigDecimal.ZERO) < 0) {
			LOGGER.warn("setEstimatedCost called with negative value: {}", estimatedCost);
		}
		this.estimatedCost = estimatedCost;
		updateLastModified();
	}

	public void setImplementationDate(final LocalDateTime implementationDate) {
		this.implementationDate = implementationDate;
		updateLastModified();
	}

	public void setReviewDate(final LocalDateTime reviewDate) {
		this.reviewDate = reviewDate;
		updateLastModified();
	}

	@Override
	public String toString() {
		return getName() != null ? getName() : super.toString();
	}
}
