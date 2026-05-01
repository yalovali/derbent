package tech.derbent.plm.requirements.requirement.domain;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasParentRelation;
import tech.derbent.api.parentrelation.domain.CParentRelation;
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
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;

/** Project-scoped requirement entity that participates in the generic hierarchy refactor.
 * <p>
 * Unlike agile entities, this class stays on the standard {@link CProjectItem} pattern so the hierarchy layer works for any project item with a typed
 * level.
 * </p>
 */
@Entity
@Table (name = "crequirement")
@AttributeOverride (name = "id", column = @Column (name = "requirement_id"))
public class CRequirement extends CProjectItem<CRequirement>
		implements IHasStatusAndWorkflow<CRequirement>, IHasAttachments, IHasComments, IHasLinks, IHasParentRelation {

	public static final String DEFAULT_COLOR = "#7B5EA7";
	public static final String DEFAULT_ICON = "vaadin:clipboard-text";
	public static final String ENTITY_TITLE_PLURAL = "Requirements";
	public static final String ENTITY_TITLE_SINGULAR = "Requirement";
	public static final String VIEW_NAME = "Requirements View";
	@Column (nullable = true, length = 2000)
	@Size (max = 2000)
	@AMetaData (
			displayName = "Acceptance Criteria", required = false, readOnly = false, defaultValue = "",
			description = "Testable criteria that define when the requirement is satisfied", hidden = false,
			maxLength = 2000
	)
	private String acceptanceCriteria = "";
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "requirement_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false,
			description = "Attachments for this requirement", hidden = false, dataProviderBean = "CAttachmentService",
			createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "requirement_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this requirement",
			hidden = false, dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (nullable = true)
	@AMetaData (
			displayName = "Due Date", required = false, readOnly = false,
			description = "Target completion or validation date", hidden = false
	)
	private LocalDate dueDate;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Requirement Type", required = false, readOnly = false,
			description = "Hierarchy-aware type of the requirement", hidden = false,
			dataProviderBean = "CRequirementTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CRequirementType entityType;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "requirement_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false,
			description = "Related entities linked to this requirement", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "agile_parent_relation_id", nullable = false)
	@NotNull (message = "Parent relation is required for hierarchy")
	@AMetaData (
			displayName = "Parent Relation", required = true, readOnly = true,
			description = "Hierarchy tracking for this requirement", hidden = true
	)
	private CParentRelation parentRelation;
	@Transient
	@AMetaData (
			displayName = "Parent", required = false, readOnly = false,
			description = "Hierarchy parent selector for this requirement", hidden = false,
			createComponentMethod = "createComponentParent", dataProviderBean = "pageservice", captionVisible = false
	)
	private final CProjectItem<?> placeHolder_createComponentParent = null;
	@Transient
	@AMetaData (
			displayName = "Children", required = false, readOnly = false,
			description = "Hierarchy children for this requirement", hidden = false,
			createComponentMethod = "createComponentParentChildren", dataProviderBean = "pageservice",
			captionVisible = false
	)
	private final CProjectItem<?> placeHolder_createComponentParentChildren = null;
	@Column (nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Source", required = false, readOnly = false, defaultValue = "",
			description = "Origin of the requirement such as customer request, policy, or roadmap input",
			hidden = false, maxLength = 500
	)
	private String source = "";
	@Column (nullable = true)
	@AMetaData (
			displayName = "Start Date", required = false, readOnly = false,
			description = "Planned start date for requirement implementation", hidden = false
	)
	private LocalDate startDate = LocalDate.now();

	protected CRequirement() {}

	public CRequirement(final String name, final CProject<?> project) {
		super(CRequirement.class, name, project);
		initializeDefaults();
	}

	@PostLoad
	protected void ensureParentRelationOwner() {
		if (parentRelation != null) {
			parentRelation.setOwnerItem(this);
		}
	}

	public String getAcceptanceCriteria() { return acceptanceCriteria; }

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	@Override
	public Set<CComment> getComments() { return comments; }

	public LocalDate getDueDate() { return dueDate; }

	@Override
	public LocalDate getEndDate() { return dueDate; }

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	@Override
	public Set<CLink> getLinks() { return links; }

	@Override
	public CParentRelation getParentRelation() { return parentRelation; }

	public CProjectItem<?> getPlaceHolder_createComponentParent() { return placeHolder_createComponentParent; }

	public CProjectItem<?> getPlaceHolder_createComponentParentChildren() {
		return placeHolder_createComponentParentChildren;
	}

	public String getSource() { return source; }

	@Override
	public LocalDate getStartDate() { return startDate; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Requirement type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	private final void initializeDefaults() {
		// Default dates keep new requirements visible in generic timeline screens immediately.
		startDate = LocalDate.now();
		dueDate = LocalDate.now().plusDays(14);
		parentRelation = new CParentRelation(this);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setAcceptanceCriteria(final String acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDueDate(final LocalDate dueDate) { this.dueDate = dueDate; }

	@Override
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CRequirementType.class, "Type entity must be an instance of CRequirementType");
		Check.notNull(getProject(), "Project must be set before assigning requirement type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning requirement type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning requirement type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id %s does not match requirement project company id %s"
						.formatted(typeEntity.getCompany().getId(), getProject().getCompany().getId()));
		entityType = (CRequirementType) typeEntity;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	@Override
	public void setParentRelation(final CParentRelation parentRelation) { this.parentRelation = parentRelation; }

	public void setSource(final String source) { this.source = source; }

	public void setStartDate(final LocalDate startDate) { this.startDate = startDate; }
}
