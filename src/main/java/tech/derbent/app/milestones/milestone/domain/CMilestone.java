package tech.derbent.app.milestones.milestone.domain;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CAgileParentRelation;
import tech.derbent.api.domains.CAgileParentRelationService;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
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
import tech.derbent.app.milestones.milestonetype.domain.CMilestoneType;

@Entity
@Table (name = "\"cmilestone\"")
@AttributeOverride (name = "id", column = @Column (name = "milestone_id"))
public class CMilestone extends CProjectItem<CMilestone>
		implements IHasStatusAndWorkflow<CMilestone>, IHasAttachments, IHasComments, IHasLinks, IHasAgileParentRelation {

	public static final String DEFAULT_COLOR = "#4B4382"; // CDE Titlebar Purple - key achievements
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String ENTITY_TITLE_PLURAL = "Milestones";
	public static final String ENTITY_TITLE_SINGULAR = "Milestone";
	private static final Logger LOGGER = LoggerFactory.getLogger(CMilestone.class);
	public static final String VIEW_NAME = "Milestone View";
	// Agile Parent Relation - REQUIRED: every milestone must have an agile parent relation for agile hierarchy
	@OneToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn (name = "agile_parent_relation_id", nullable = false)
	@NotNull (message = "Agile parent relation is required for agile hierarchy")
	@AMetaData (
			displayName = "Agile Parent Relation", required = true, readOnly = true, description = "Agile hierarchy tracking for this milestone",
			hidden = true
	)
	private CAgileParentRelation agileParentRelation;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "milestone_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this milestone", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "milestone_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this milestone", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Milestone Type", required = false, readOnly = false, description = "Type category of the milestone", hidden = false,
			dataProviderBean = "CMilestoneTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CMilestoneType entityType;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "milestone_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related entities linked to this cmilestone", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();

	/** Default constructor for JPA. */
	public CMilestone() {
		super();
		initializeDefaults();
	}

	public CMilestone(final String name, final CProject project) {
		super(CMilestone.class, name, project);
		initializeDefaults();
	}

	/** Creates a clone of this milestone with the specified options. This implementation follows the recursive cloning pattern: 1. Calls parent's
	 * createClone() to handle inherited fields (CProjectItem) 2. Clones milestone-specific fields based on options 3. Recursively clones collections
	 * (comments, attachments) if requested Cloning behavior: - Basic fields (strings, numbers, enums) are always cloned - Workflow field is cloned
	 * only if options.isCloneWorkflow() - Comments collection is recursively cloned if options.includesComments() - Attachments collection is
	 * recursively cloned if options.includesAttachments()
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the milestone with cloned data
	 * @throws CloneNotSupportedException if cloning fails */
	@Override
	public CMilestone createClone(final CCloneOptions options) throws Exception {
		// Get parent's clone (CProjectItem -> CEntityOfProject -> CEntityNamed -> CEntityDB)
		final CMilestone clone = super.createClone(options);
		// Clone entity type (milestone type)
		clone.entityType = entityType;
		// Clone workflow if requested
		if (options.isCloneWorkflow() && getWorkflow() != null) {
			// Workflow is obtained via entityType.getWorkflow() - already cloned via entityType
		}
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
		LOGGER.debug("Successfully cloned milestone '{}' with options: {}", getName(), options);
		return clone;
	}

	@jakarta.persistence.PostLoad
	protected void ensureAgileParentRelationOwner() {
		if (agileParentRelation != null) {
			agileParentRelation.setOwnerItem(this);
		}
	}

	@Override
	public CAgileParentRelation getAgileParentRelation() { return agileParentRelation; }

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
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public Set<CLink> getLinks() {
		if (links == null) {
			links = new HashSet<>();
		}
		return links;
	}

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		// Ensure agile parent relation is always created for composition pattern
		if (agileParentRelation == null) {
			agileParentRelation = CAgileParentRelationService.createDefaultAgileParentRelation();
		}
		// Set back-reference so agileParentRelation can access owner for display
		if (agileParentRelation != null) {
			agileParentRelation.setOwnerItem(this);
		}
	}

	@Override
	public void setAgileParentRelation(CAgileParentRelation agileParentRelation) { this.agileParentRelation = agileParentRelation; }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CMilestoneType.class, "Type entity must be an instance of CMilestoneType");
		Check.notNull(getProject(), "Project must be set before assigning milestone type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning milestone type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning milestone type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match milestone project company id " + getProject().getCompany().getId());
		entityType = (CMilestoneType) typeEntity;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }
}
